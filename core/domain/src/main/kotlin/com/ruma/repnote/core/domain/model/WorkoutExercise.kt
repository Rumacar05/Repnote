package com.ruma.repnote.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an exercise within a workout session.
 *
 * @property exerciseId ID of the exercise definition
 * @property exerciseName Name of the exercise (snapshot at session creation for historical accuracy)
 * @property order Position of this exercise in the workout (0-indexed)
 * @property targetSets Target number of sets to complete
 * @property targetReps Target number of reps per set (null for time-based or open-ended exercises)
 * @property targetRestSeconds Target rest time between sets in seconds (null if no rest period specified)
 * @property completedSets List of sets that have been completed for this exercise
 * @property notes Optional notes about this exercise in the context of this workout
 */
@Serializable
data class WorkoutExercise(
    val exerciseId: String,
    val exerciseName: String,
    val order: Int,
    val targetSets: Int,
    val targetReps: Int?,
    val targetRestSeconds: Int?,
    val completedSets: List<CompletedSet>,
    val notes: String?,
)

/**
 * Represents a completed set within a workout exercise.
 *
 * @property setNumber Set number (1-indexed for display purposes)
 * @property reps Number of repetitions actually completed in this set
 * @property weight Weight used for this set in kilograms (null if bodyweight exercise or not tracked)
 * @property completedAt Timestamp (milliseconds) when this set was marked as complete
 * @property restTimerSeconds Actual rest time taken before starting the next set (null if not tracked)
 * @property notes Optional notes about this specific set
 */
@Serializable
data class CompletedSet(
    val setNumber: Int,
    val reps: Int,
    val weight: Double?,
    val completedAt: Long,
    val restTimerSeconds: Int?,
    val notes: String?,
)
