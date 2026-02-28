package com.ruma.repnote.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<HomeNavigationEvent>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        userEmail = user?.email ?: "Guest",
                        displayName = user?.displayName,
                        userId = user?.uid,
                    )
                }

                if (user != null) {
                    syncFromFirestore(user.uid)
                    checkForActiveSession(user.uid)
                    loadRecentWorkouts(user.uid)
                }
            }
        }
    }

    private fun syncFromFirestore(userId: String) {
        viewModelScope.launch {
            workoutRepository.syncFromFirestore(userId)
        }
    }

    private fun loadRecentWorkouts(userId: String) {
        viewModelScope.launch {
            workoutRepository.getUserWorkoutHistory(userId, limit = 10).collect { result ->
                when (result) {
                    is WorkoutResult.Success -> {
                        val filteredWorkouts =
                            result.data.filter { workout ->
                                workout.status == WorkoutStatus.COMPLETED ||
                                    workout.status == WorkoutStatus.IN_PROGRESS
                            }

                        _uiState.update { currentState ->
                            currentState.copy(
                                recentWorkouts = filteredWorkouts,
                                isLoadingWorkouts = false,
                            )
                        }
                    }

                    is WorkoutResult.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                recentWorkouts = emptyList(),
                                isLoadingWorkouts = false,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkForActiveSession(userId: String) {
        viewModelScope.launch {
            workoutRepository.getActiveSession(userId).collect { result ->
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

    fun onResumeWorkoutClick() {
        val sessionId = _uiState.value.activeWorkoutSessionId
        if (sessionId != null) {
            viewModelScope.launch {
                _navigationEvents.emit(HomeNavigationEvent.NavigateToActiveWorkout(sessionId))
            }
        }
    }
}

data class HomeUiState(
    val userEmail: String = "",
    val displayName: String? = null,
    val userId: String? = null,
    val hasActiveWorkout: Boolean = false,
    val activeWorkoutSessionId: String? = null,
    val recentWorkouts: List<WorkoutSession> = emptyList(),
    val isLoadingWorkouts: Boolean = true,
)

sealed interface HomeNavigationEvent {
    data class NavigateToActiveWorkout(
        val sessionId: String,
    ) : HomeNavigationEvent
}
