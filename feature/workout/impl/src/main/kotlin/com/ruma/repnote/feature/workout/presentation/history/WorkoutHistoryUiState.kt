package com.ruma.repnote.feature.workout.presentation.history

import com.ruma.repnote.core.domain.model.WorkoutSession

data class WorkoutHistoryUiState(
    val workouts: List<WorkoutSession> = emptyList(),
    val isLoading: Boolean = true,
)
