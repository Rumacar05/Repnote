package com.ruma.repnote.feature.workout.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutHistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutHistoryUiState())
    val uiState: StateFlow<WorkoutHistoryUiState> = _uiState.asStateFlow()

    init {
        loadWorkoutHistory()
    }

    private fun loadWorkoutHistory() {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase().first()
            val userId = currentUser?.uid ?: return@launch

            workoutRepository.getUserWorkoutHistory(userId).collect { result ->
                when (result) {
                    is WorkoutResult.Success -> {
                        val filteredWorkouts =
                            result.data.filter { workout ->
                                workout.status == WorkoutStatus.COMPLETED ||
                                    workout.status == WorkoutStatus.IN_PROGRESS
                            }
                        _uiState.update { currentState ->
                            currentState.copy(
                                workouts = filteredWorkouts,
                                isLoading = false,
                            )
                        }
                    }

                    is WorkoutResult.Error -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                workouts = emptyList(),
                                isLoading = false,
                            )
                        }
                    }
                }
            }
        }
    }
}
