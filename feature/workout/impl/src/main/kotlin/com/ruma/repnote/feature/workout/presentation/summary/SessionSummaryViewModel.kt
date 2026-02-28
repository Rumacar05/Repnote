package com.ruma.repnote.feature.workout.presentation.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the session summary screen.
 * Loads workout session data, calculates statistics, and compares with previous sessions.
 */
class SessionSummaryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var sessionId: String? = savedStateHandle["sessionId"]
    private var isInitialized = false

    private val _uiState = MutableStateFlow(SessionSummaryUiState())
    val uiState: StateFlow<SessionSummaryUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SessionSummaryNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun initialize(sessionId: String) {
        if (!isInitialized) {
            this.sessionId = sessionId
            isInitialized = true
            loadSessionData()
        }
    }

    private fun loadSessionData() {
        val currentSessionId = sessionId
        if (currentSessionId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Session ID not found",
                )
            }
            return
        }

        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()

            workoutRepository.getSessionById(user.uid, currentSessionId).collect { result ->
                when (result) {
                    is WorkoutResult.Success -> {
                        val session = result.data
                        _uiState.update { it.copy(session = session, isLoading = false) }

                        // Calculate statistics for current session
                        val stats = calculateStatistics(session)
                        _uiState.update { it.copy(statistics = stats) }

                        // Load previous session for comparison if routine-based
                        val routineId = session.routineId
                        if (routineId != null) {
                            loadPreviousSession(user.uid, routineId, session.id)
                        }
                    }

                    is WorkoutResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load workout session",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadPreviousSession(
        userId: String,
        routineId: String,
        currentSessionId: String,
    ) {
        viewModelScope.launch {
            workoutRepository.getRoutineWorkoutHistory(userId, routineId).collect { result ->
                when (result) {
                    is WorkoutResult.Success -> {
                        // Get the most recent completed session excluding current one
                        val previousSession =
                            result.data
                                .filter {
                                    it.id != currentSessionId &&
                                        it.status == WorkoutStatus.COMPLETED
                                }.maxByOrNull { it.startTime }

                        if (previousSession != null) {
                            _uiState.update { it.copy(previousSession = previousSession) }

                            // Calculate comparison
                            val currentStats = _uiState.value.statistics
                            if (currentStats != null) {
                                val comparison =
                                    calculateComparison(currentStats, previousSession)
                                _uiState.update { it.copy(comparison = comparison) }
                            }
                        }
                    }

                    is WorkoutResult.Error -> {
                        // Comparison is optional, don't show error
                    }
                }
            }
        }
    }

    private fun calculateStatistics(session: WorkoutSession): WorkoutStatistics {
        val exerciseStats =
            session.exercises.map { exercise ->
                calculateExerciseStatistics(exercise)
            }

        val totalVolume = exerciseStats.sumOf { it.totalVolume }

        return WorkoutStatistics(
            totalDurationSeconds = session.totalDurationSeconds ?: 0L,
            totalVolume = totalVolume,
            exerciseStats = exerciseStats,
        )
    }

    private fun calculateExerciseStatistics(exercise: WorkoutExercise): ExerciseStatistics {
        val completedSets = exercise.completedSets

        // Total volume = sum of (weight × reps) for all sets
        val totalVolume =
            completedSets.sumOf { set ->
                (set.weight ?: 0.0) * set.reps
            }

        // Total reps across all sets
        val totalReps = completedSets.sumOf { it.reps }

        // Max weight lifted in any single set
        val maxWeight = completedSets.mapNotNull { it.weight }.maxOrNull()

        // Exercise duration from first to last set timestamp
        val durationSeconds =
            if (completedSets.isNotEmpty()) {
                val firstSet = completedSets.minByOrNull { it.completedAt }
                val lastSet = completedSets.maxByOrNull { it.completedAt }
                if (firstSet != null && lastSet != null) {
                    (lastSet.completedAt - firstSet.completedAt) / MILLIS_PER_SECOND
                } else {
                    0L
                }
            } else {
                0L
            }

        // Map individual sets for display
        val setsInfo =
            completedSets.map { set ->
                SetInfo(
                    setNumber = set.setNumber,
                    reps = set.reps,
                    weight = set.weight,
                )
            }

        return ExerciseStatistics(
            exerciseId = exercise.exerciseId,
            exerciseName = exercise.exerciseName,
            sets = completedSets.size,
            totalReps = totalReps,
            totalVolume = totalVolume,
            maxWeight = maxWeight,
            durationSeconds = durationSeconds,
            completedSets = setsInfo,
        )
    }

    private fun calculateComparison(
        currentStats: WorkoutStatistics,
        previousSession: WorkoutSession,
    ): WorkoutComparison {
        val previousStats = calculateStatistics(previousSession)

        // Volume comparison
        val volumeDiff = currentStats.totalVolume - previousStats.totalVolume
        val volumePercentChange =
            if (previousStats.totalVolume > 0) {
                (volumeDiff / previousStats.totalVolume) * PERCENT_MULTIPLIER
            } else {
                null
            }

        // Duration comparison (positive means current took longer)
        val durationDiff =
            currentStats.totalDurationSeconds - previousStats.totalDurationSeconds

        // Per-exercise comparisons
        val exerciseComparisons =
            currentStats.exerciseStats.mapNotNull { currentExercise ->
                val previousExercise =
                    previousStats.exerciseStats
                        .find { it.exerciseId == currentExercise.exerciseId }

                if (previousExercise != null) {
                    ExerciseComparison(
                        exerciseId = currentExercise.exerciseId,
                        exerciseName = currentExercise.exerciseName,
                        volumeDifference =
                            currentExercise.totalVolume - previousExercise.totalVolume,
                        maxWeightDifference =
                            if (currentExercise.maxWeight != null &&
                                previousExercise.maxWeight != null
                            ) {
                                currentExercise.maxWeight - previousExercise.maxWeight
                            } else {
                                null
                            },
                    )
                } else {
                    null
                }
            }

        return WorkoutComparison(
            volumeDifference = volumeDiff,
            volumePercentChange = volumePercentChange,
            durationDifference = durationDiff,
            exerciseComparisons = exerciseComparisons,
        )
    }

    fun onNavigateToHome() {
        viewModelScope.launch {
            _navigationEvent.emit(SessionSummaryNavigationEvent.NavigateToHome)
        }
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000L
        const val PERCENT_MULTIPLIER = 100.0
    }
}
