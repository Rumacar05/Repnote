package com.ruma.repnote.feature.routine.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import com.ruma.repnote.core.domain.util.getNormalizedLanguage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoutineDetailViewModel(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RoutineDetailNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private var routineId: String? = savedStateHandle["routineId"]
    private var isInitialized = false

    fun initialize(routineId: String) {
        if (!isInitialized) {
            this.routineId = routineId
            isInitialized = true
            loadRoutineWithExercises()
        }
    }

    private fun loadRoutineWithExercises() {
        val currentRoutineId = routineId
        if (currentRoutineId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Routine not found",
                )
            }
            return
        }

        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()
            routineRepository.getRoutineById(user.uid, currentRoutineId).collect { result ->
                when (result) {
                    is RoutineResult.Success -> {
                        val routine = result.data
                        _uiState.update { it.copy(routine = routine) }

                        // Load exercise details for each exercise in the routine
                        loadExerciseDetails(user.uid)
                    }

                    is RoutineResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    when (val exception = result.exception) {
                                        is RoutineException.RoutineNotFound -> {
                                            "Routine not found"
                                        }

                                        is RoutineException.UnauthorizedAccess -> {
                                            "Unauthorized access"
                                        }

                                        is RoutineException.InvalidRoutineData -> {
                                            "Invalid routine data"
                                        }

                                        is RoutineException.NetworkError -> {
                                            "Network error"
                                        }

                                        is RoutineException.Unknown -> {
                                            exception.message
                                                ?: "Unknown error"
                                        }
                                    },
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadExerciseDetails(userId: String) {
        val language = getNormalizedLanguage()
        val routine = _uiState.value.routine ?: return

        val exerciseDetails =
            routine.exercises.mapNotNull { routineExercise ->
                exerciseRepository
                    .getExerciseById(routineExercise.exerciseId, userId, language)
                    .let { result ->
                        when (result) {
                            is ExerciseResult.Success -> {
                                ExerciseDetail(
                                    exerciseId = routineExercise.exerciseId,
                                    name = result.data.name,
                                    imageUrl = result.data.imageUrl,
                                    sets = routineExercise.sets,
                                    reps = routineExercise.reps,
                                    restSeconds = routineExercise.restSeconds,
                                    notes = routineExercise.notes,
                                )
                            }

                            is ExerciseResult.Error -> {
                                null
                            }
                        }
                    }
            }

        _uiState.update {
            it.copy(
                isLoading = false,
                exercisesWithDetails = exerciseDetails,
            )
        }
    }

    fun onEditClick() {
        val currentRoutineId = routineId ?: return
        viewModelScope.launch {
            _navigationEvent.emit(RoutineDetailNavigationEvent.NavigateToEdit(currentRoutineId))
        }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteCancel() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteConfirm() {
        val currentRoutineId = routineId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false, isLoading = true) }

            val user = getCurrentUser().filterNotNull().first()
            when (val result = routineRepository.deleteRoutine(user.uid, currentRoutineId)) {
                is RoutineResult.Success -> {
                    _navigationEvent.emit(RoutineDetailNavigationEvent.NavigateBack)
                }

                is RoutineResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                when (val exception = result.exception) {
                                    is RoutineException.RoutineNotFound -> {
                                        "Routine not found"
                                    }

                                    is RoutineException.UnauthorizedAccess -> {
                                        "Unauthorized to delete"
                                    }

                                    is RoutineException.InvalidRoutineData -> {
                                        "Invalid routine data"
                                    }

                                    is RoutineException.NetworkError -> {
                                        "Network error"
                                    }

                                    is RoutineException.Unknown -> {
                                        exception.message
                                            ?: "Failed to delete routine"
                                    }
                                },
                        )
                    }
                }
            }
        }
    }

    fun onStartWorkoutClick() {
        val currentRoutineId = routineId
        val routine = _uiState.value.routine

        if (currentRoutineId == null || routine == null) {
            return
        }

        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()

            when (
                val result =
                    workoutRepository.startWorkoutFromRoutine(
                        user.uid,
                        currentRoutineId,
                        routine,
                    )
            ) {
                is WorkoutResult.Success -> {
                    _navigationEvent.emit(RoutineDetailNavigationEvent.NavigateToActiveWorkout)
                }

                is WorkoutResult.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage =
                                when (val exception = result.exception) {
                                    is WorkoutException.SessionAlreadyActive -> {
                                        "You already have an active workout session"
                                    }

                                    is WorkoutException.SessionNotFound -> {
                                        "Session not found"
                                    }

                                    is WorkoutException.UnauthorizedAccess -> {
                                        "Unauthorized access"
                                    }

                                    is WorkoutException.NetworkError -> {
                                        "Network error"
                                    }

                                    is WorkoutException.Unknown -> {
                                        exception.message
                                            ?: "Failed to start workout"
                                    }
                                },
                        )
                    }
                }
            }
        }
    }
}
