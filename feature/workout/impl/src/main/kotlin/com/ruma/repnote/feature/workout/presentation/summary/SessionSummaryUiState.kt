package com.ruma.repnote.feature.workout.presentation.summary

import com.ruma.repnote.core.domain.model.WorkoutSession

/**
 * UI state for the session summary screen.
 */
data class SessionSummaryUiState(
    val isLoading: Boolean = true,
    val session: WorkoutSession? = null,
    val previousSession: WorkoutSession? = null,
    val statistics: WorkoutStatistics? = null,
    val comparison: WorkoutComparison? = null,
    val errorMessage: String? = null,
)

/**
 * Statistics for a completed workout session.
 */
data class WorkoutStatistics(
    val totalDurationSeconds: Long,
    val totalVolume: Double,
    val exerciseStats: List<ExerciseStatistics>,
)

/**
 * Statistics for an individual exercise within a workout.
 */
data class ExerciseStatistics(
    val exerciseId: String,
    val exerciseName: String,
    val sets: Int,
    val totalReps: Int,
    val totalVolume: Double,
    val maxWeight: Double?,
    val durationSeconds: Long,
    val completedSets: List<SetInfo> = emptyList(),
)

/**
 * Information about a single completed set.
 */
data class SetInfo(
    val setNumber: Int,
    val reps: Int,
    val weight: Double?,
)

/**
 * Comparison data between current session and previous session.
 */
data class WorkoutComparison(
    val volumeDifference: Double,
    val volumePercentChange: Double?,
    val durationDifference: Long,
    val exerciseComparisons: List<ExerciseComparison>,
)

/**
 * Comparison data for an individual exercise.
 */
data class ExerciseComparison(
    val exerciseId: String,
    val exerciseName: String,
    val volumeDifference: Double,
    val maxWeightDifference: Double?,
)

/**
 * Navigation events for the session summary screen.
 */
sealed interface SessionSummaryNavigationEvent {
    data object NavigateToHome : SessionSummaryNavigationEvent
}
