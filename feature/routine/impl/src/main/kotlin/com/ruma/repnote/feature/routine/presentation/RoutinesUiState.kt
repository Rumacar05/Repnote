package com.ruma.repnote.feature.routine.presentation

import com.ruma.repnote.core.domain.model.Routine

/**
 * UI state for My Routines screen.
 */
data class RoutinesUiState(
    val routines: List<Routine> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hasActiveWorkout: Boolean = false,
    val activeWorkoutSessionId: String? = null,
)

sealed interface RoutinesNavigationEvent {
    data object NavigateToActiveWorkout : RoutinesNavigationEvent

    data class NavigateToResumeWorkout(
        val sessionId: String,
    ) : RoutinesNavigationEvent
}
