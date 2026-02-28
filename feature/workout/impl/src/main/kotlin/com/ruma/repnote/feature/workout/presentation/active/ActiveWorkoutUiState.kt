package com.ruma.repnote.feature.workout.presentation.active

import com.ruma.repnote.core.domain.model.WorkoutSession

/**
 * UI state for the active workout screen.
 */
data class ActiveWorkoutUiState(
    val isLoading: Boolean = true,
    val session: WorkoutSession? = null,
    val currentExerciseIndex: Int = 0,
    val currentSetNumber: Int = 1,
    val isRestTimerActive: Boolean = false,
    val restTimerSecondsRemaining: Int = 0,
    val totalElapsedSeconds: Long = 0,
    val showCompleteDialog: Boolean = false,
    val showAbandonDialog: Boolean = false,
    val showAddExerciseDialog: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * State for set input fields.
 */
data class SetInputState(
    val reps: String = "",
    val weight: String = "",
    val notes: String = "",
)

/**
 * Navigation events for the active workout screen.
 */
sealed interface ActiveWorkoutNavigationEvent {
    data object NavigateBack : ActiveWorkoutNavigationEvent
    data class NavigateToSessionSummary(
        val sessionId: String,
    ) : ActiveWorkoutNavigationEvent
}
