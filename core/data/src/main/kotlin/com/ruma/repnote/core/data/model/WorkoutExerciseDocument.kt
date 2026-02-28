package com.ruma.repnote.core.data.model

/**
 * Firestore document representing an exercise within a workout session.
 * All fields have default values for Firestore deserialization.
 */
data class WorkoutExerciseDocument(
    val exerciseId: String = "",
    val exerciseName: String = "",
    val order: Int = 0,
    val targetSets: Int = 0,
    val targetReps: Int? = null,
    val targetRestSeconds: Int? = null,
    val completedSets: List<CompletedSetDocument> = emptyList(),
    val notes: String? = null,
)

/**
 * Firestore document representing a completed set within an exercise.
 * All fields have default values for Firestore deserialization.
 */
data class CompletedSetDocument(
    val setNumber: Int = 0,
    val reps: Int = 0,
    val weight: Double? = null,
    val completedAt: Long = 0L,
    val restTimerSeconds: Int? = null,
    val notes: String? = null,
)
