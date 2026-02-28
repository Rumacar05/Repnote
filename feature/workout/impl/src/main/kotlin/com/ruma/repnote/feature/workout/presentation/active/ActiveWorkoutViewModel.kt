package com.ruma.repnote.feature.workout.presentation.active

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.analytics.domain.model.AnalyticsEvent
import com.ruma.repnote.core.analytics.domain.service.AnalyticsService
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.CompletedSet
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel for the active workout screen.
 * Manages workout session state, set completion, rest timers, and elapsed time tracking.
 */
@Suppress("TooManyFunctions")
class ActiveWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val analyticsService: AnalyticsService,
    private val enableTimeTracking: Boolean = true,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ActiveWorkoutNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _setInputState = MutableStateFlow(SetInputState())
    val setInputState: StateFlow<SetInputState> = _setInputState.asStateFlow()

    // Timers
    private var elapsedTimeJob: Job? = null
    private var restTimerJob: Job? = null
    private var saveSessionJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var isNavigatingAway = false
    private var isProcessingSet = false

    init {
        loadActiveSession()
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()
            when (val result = workoutRepository.getActiveSession(user.uid).first()) {
                is WorkoutResult.Success -> {
                    val session = result.data
                    if (session != null) {
                        val currentExercise = session.exercises.firstOrNull()
                        val currentSetNumber = (currentExercise?.completedSets?.size ?: 0) + 1
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                session = session,
                                currentSetNumber = currentSetNumber,
                            )
                        }
                        sessionStartTime = session.startTime
                        if (enableTimeTracking) {
                            startElapsedTimeTracking()
                        }
                    } else if (!isNavigatingAway) {
                        // No active session, navigate back only if not already navigating
                        isNavigatingAway = true
                        _navigationEvent.emit(ActiveWorkoutNavigationEvent.NavigateBack)
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

    private fun startElapsedTimeTracking() {
        elapsedTimeJob?.cancel()
        elapsedTimeJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(ONE_SECOND_MILLIS)
                    if (sessionStartTime > 0) {
                        val elapsed =
                            (System.currentTimeMillis() - sessionStartTime) / ONE_SECOND_MILLIS
                        _uiState.update { it.copy(totalElapsedSeconds = elapsed) }
                    }
                }
            }
    }

    /**
     * Debounces session saves to Firestore to avoid concurrent writes.
     * Waits 1 second after the last change before saving.
     */
    private fun debouncedSaveSession(session: com.ruma.repnote.core.domain.model.WorkoutSession) {
        // Cancel any pending save
        saveSessionJob?.cancel()

        // Schedule new save after delay
        saveSessionJob =
            viewModelScope.launch {
                delay(SAVE_DEBOUNCE_MILLIS)
                workoutRepository.updateSession(session)
            }
    }

    /**
     * Saves the session immediately without debouncing.
     * Used when completing or abandoning workouts.
     */
    private suspend fun saveSessionImmediately() {
        saveSessionJob?.cancel()
        val session = _uiState.value.session ?: return
        workoutRepository.updateSession(session)
    }

    fun onSetComplete() {
        // Prevent duplicate calls from recompositions
        if (!isProcessingSet) {
            isProcessingSet = true

            val currentState = _uiState.value
            val inputState = _setInputState.value
            val session = currentState.session
            val reps = inputState.reps.toIntOrNull()
            val exerciseIndex = currentState.currentExerciseIndex

            val exercise = session?.exercises?.getOrNull(exerciseIndex)

            if (session != null && reps != null && exercise != null) {
                val weight = inputState.weight.toDoubleOrNull()

                val newSet =
                    CompletedSet(
                        setNumber = currentState.currentSetNumber,
                        reps = reps,
                        weight = weight,
                        completedAt = System.currentTimeMillis(),
                        restTimerSeconds = null, // Will be set when rest completes
                        notes = inputState.notes.takeIf { it.isNotBlank() },
                    )

                val updatedExercise =
                    exercise.copy(
                        completedSets = exercise.completedSets + newSet,
                    )

                val updatedExercises =
                    session.exercises.toMutableList().apply {
                        set(exerciseIndex, updatedExercise)
                    }

                val updatedSession =
                    session.copy(
                        exercises = updatedExercises,
                        updatedAt = System.currentTimeMillis(),
                    )

                // Update UI state immediately (optimistic update)
                _uiState.update { it.copy(session = updatedSession) }

                // Debounce Firestore writes to avoid concurrent writes
                debouncedSaveSession(updatedSession)

                // Clear input
                _setInputState.update { SetInputState() }

                // Advance to next set or move to next exercise
                if (currentState.currentSetNumber < exercise.targetSets) {
                    _uiState.update {
                        it.copy(currentSetNumber = currentState.currentSetNumber + 1)
                    }
                    startRestTimer(exercise.targetRestSeconds)
                } else {
                    // Move to next exercise
                    moveToNextExercise()
                }
            }

            // Reset processing flag
            isProcessingSet = false
        }
    }

    private fun startRestTimer(seconds: Int?) {
        if (seconds == null || seconds <= 0) return

        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isRestTimerActive = true,
                restTimerSecondsRemaining = seconds,
            )
        }

        restTimerJob =
            viewModelScope.launch {
                for (remaining in seconds downTo 0) {
                    _uiState.update { it.copy(restTimerSecondsRemaining = remaining) }
                    delay(ONE_SECOND_MILLIS)
                }
                _uiState.update { it.copy(isRestTimerActive = false) }
            }
    }

    fun onSkipRestTimer() {
        restTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isRestTimerActive = false,
                restTimerSecondsRemaining = 0,
            )
        }
    }

    private fun moveToNextExercise() {
        val currentState = _uiState.value
        val session = currentState.session ?: return

        val nextIndex = currentState.currentExerciseIndex + 1
        if (nextIndex < session.exercises.size) {
            val nextExercise = session.exercises[nextIndex]
            val nextSetNumber = (nextExercise.completedSets.size) + 1
            _uiState.update {
                it.copy(
                    currentExerciseIndex = nextIndex,
                    currentSetNumber = nextSetNumber,
                )
            }
        } else {
            // All exercises complete, show completion dialog
            _uiState.update { it.copy(showCompleteDialog = true) }
        }
    }

    fun onNextExercise() {
        moveToNextExercise()
    }

    fun onPreviousExercise() {
        val currentState = _uiState.value
        val session = currentState.session
        if (currentState.currentExerciseIndex > 0 && session != null) {
            val prevIndex = currentState.currentExerciseIndex - 1
            val prevExercise = session.exercises[prevIndex]
            val prevSetNumber = (prevExercise.completedSets.size) + 1
            _uiState.update {
                it.copy(
                    currentExerciseIndex = prevIndex,
                    currentSetNumber = prevSetNumber,
                )
            }
        }
    }

    fun onRepsChange(reps: String) {
        _setInputState.update { it.copy(reps = reps) }
    }

    fun onWeightChange(weight: String) {
        _setInputState.update { it.copy(weight = weight) }
    }

    fun onNotesChange(notes: String) {
        _setInputState.update { it.copy(notes = notes) }
    }

    fun onCompleteWorkout() {
        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()
            val session =
                _uiState.value.session ?: run {
                    Log.e(TAG, "No session found in UI state")
                    return@launch
                }

            _uiState.update { it.copy(showCompleteDialog = false) }

            // Save any pending changes immediately before completing
            saveSessionImmediately()

            isNavigatingAway = true
            when (val result = workoutRepository.completeSession(user.uid, session.id)) {
                is WorkoutResult.Success -> {
                    val totalSets = session.exercises.sumOf { it.completedSets.size }
                    analyticsService.logEvent(
                        AnalyticsEvent.WorkoutCompleted(
                            sessionId = session.id,
                            durationSeconds = _uiState.value.totalElapsedSeconds,
                            exerciseCount = session.exercises.size,
                            totalSets = totalSets,
                        ),
                    )
                    _navigationEvent.emit(
                        ActiveWorkoutNavigationEvent.NavigateToSessionSummary(session.id),
                    )
                }

                is WorkoutResult.Error -> {
                    Log.e(TAG, "Failed to complete workout", result.exception as? Throwable)
                    isNavigatingAway = false
                    _uiState.update {
                        it.copy(errorMessage = "Failed to complete workout")
                    }
                }
            }
        }
    }

    fun onAbandonWorkout() {
        _uiState.update { it.copy(showAbandonDialog = true) }
    }

    fun onAbandonConfirm() {
        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()
            val session = _uiState.value.session ?: return@launch

            _uiState.update { it.copy(showAbandonDialog = false) }

            // Save any pending changes immediately before abandoning
            saveSessionImmediately()

            isNavigatingAway = true
            val completedExercises = session.exercises.count { it.completedSets.isNotEmpty() }
            analyticsService.logEvent(
                AnalyticsEvent.WorkoutAbandoned(
                    sessionId = session.id,
                    durationSeconds = _uiState.value.totalElapsedSeconds,
                    completedExercises = completedExercises,
                ),
            )
            workoutRepository.abandonSession(user.uid, session.id)
            _navigationEvent.emit(ActiveWorkoutNavigationEvent.NavigateBack)
        }
    }

    fun onDismissCompleteDialog() {
        _uiState.update { it.copy(showCompleteDialog = false) }
    }

    fun onDismissAbandonDialog() {
        _uiState.update { it.copy(showAbandonDialog = false) }
    }

    override fun onCleared() {
        super.onCleared()
        elapsedTimeJob?.cancel()
        restTimerJob?.cancel()
        saveSessionJob?.cancel()
    }

    companion object {
        private const val TAG = "ActiveWorkoutViewModel"
        private const val ONE_SECOND_MILLIS = 1000L
        private const val SAVE_DEBOUNCE_MILLIS = 1000L
    }
}
