package com.ruma.repnote.feature.routine.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.repository.RoutineRepository
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

class RoutinesViewModel(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val getCurrentUser: GetCurrentUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RoutinesNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        loadRoutines()
        checkForActiveSession()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()

            routineRepository.getUserRoutines(user.uid).collect { result ->
                when (result) {
                    is RoutineResult.Success -> {
                        _uiState.update {
                            it.copy(
                                routines = result.data,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }

                    is RoutineResult.Error -> {
                        Log.e(TAG, "Error loading routines: ${result.exception}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error loading routines",
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkForActiveSession() {
        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()

            workoutRepository.getActiveSession(user.uid).collect { result ->
                when (result) {
                    is WorkoutResult.Success -> {
                        val session = result.data
                        _uiState.update { currentState ->
                            currentState.copy(
                                hasActiveWorkout = session != null,
                                activeWorkoutSessionId = session?.id,
                            )
                        }
                    }

                    is WorkoutResult.Error -> {
                        // Log the error but don't show it to the user
                        _uiState.update { currentState ->
                            currentState.copy(
                                hasActiveWorkout = false,
                                activeWorkoutSessionId = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadRoutines()
    }

    fun onStartWorkoutClick(routine: Routine) {
        viewModelScope.launch {
            val user = getCurrentUser().filterNotNull().first()

            when (
                val result =
                    workoutRepository.startWorkoutFromRoutine(
                        user.uid,
                        routine.id,
                        routine,
                    )
            ) {
                is WorkoutResult.Success -> {
                    _navigationEvent.emit(RoutinesNavigationEvent.NavigateToActiveWorkout)
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

    fun onResumeWorkoutClick() {
        val sessionId = _uiState.value.activeWorkoutSessionId
        if (sessionId != null) {
            viewModelScope.launch {
                _navigationEvent.emit(
                    RoutinesNavigationEvent.NavigateToResumeWorkout(sessionId),
                )
            }
        }
    }

    companion object Companion {
        private const val TAG = "RoutinesViewModel"
    }
}
