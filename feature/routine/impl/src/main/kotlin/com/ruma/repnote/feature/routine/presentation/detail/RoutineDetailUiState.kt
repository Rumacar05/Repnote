package com.ruma.repnote.feature.routine.presentation.detail

import com.ruma.repnote.core.domain.model.Routine

data class RoutineDetailUiState(
    val isLoading: Boolean = true,
    val routine: Routine? = null,
    val exercisesWithDetails: List<ExerciseDetail> = emptyList(),
    val errorMessage: String? = null,
    val showDeleteDialog: Boolean = false,
)

data class ExerciseDetail(
    val exerciseId: String,
    val name: String,
    val imageUrl: String?,
    val sets: Int,
    val reps: Int?,
    val restSeconds: Int?,
    val notes: String?,
)

sealed interface RoutineDetailNavigationEvent {
    data class NavigateToEdit(
        val routineId: String,
    ) : RoutineDetailNavigationEvent
    data object NavigateBack : RoutineDetailNavigationEvent
    data object NavigateToActiveWorkout : RoutineDetailNavigationEvent
}
