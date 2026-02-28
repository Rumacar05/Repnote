package com.ruma.repnote.core.domain.model

/**
 * Represents an exercise within a routine.
 *
 * @property exerciseId The ID of the exercise
 * @property order The position of this exercise in the routine (0-based)
 * @property sets The number of sets to perform
 * @property reps The target number of repetitions per set (null for time-based exercises)
 * @property restSeconds The rest time in seconds between sets
 * @property notes Optional notes for this exercise
 */
data class RoutineExercise(
    val exerciseId: String,
    val order: Int,
    val sets: Int,
    val reps: Int? = null,
    val restSeconds: Int? = null,
    val notes: String? = null,
)
