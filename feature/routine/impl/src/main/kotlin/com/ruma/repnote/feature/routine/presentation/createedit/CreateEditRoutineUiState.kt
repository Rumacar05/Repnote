package com.ruma.repnote.feature.routine.presentation.createedit

import com.ruma.repnote.core.domain.model.Exercise

data class CreateEditRoutineUiState(
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val routineId: String? = null,
    // Form fields
    val name: String = "",
    val description: String = "",
    // Exercise list with configuration
    val exercisesWithConfig: List<ExerciseWithConfig> = emptyList(),
    // Validation
    val isNameValid: Boolean = true,
    val errorMessage: String? = null,
    // UI state
    val showExerciseSelector: Boolean = false,
    val availableExercises: List<Exercise> = emptyList(),
    val availableExerciseLoading: Boolean = false,
    val exerciseSearchQuery: String = "",
    // Save state
    val isSaving: Boolean = false,
)

sealed interface CreateEditRoutineNavigationEvent {
    data class NavigateToDetail(
        val routineId: String,
    ) : CreateEditRoutineNavigationEvent
    data object NavigateBack : CreateEditRoutineNavigationEvent
}
