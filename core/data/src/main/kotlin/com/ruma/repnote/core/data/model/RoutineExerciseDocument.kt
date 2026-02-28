package com.ruma.repnote.core.data.model

/**
 * Firestore document representing an exercise within a routine.
 */
data class RoutineExerciseDocument(
    val exerciseId: String = "",
    val order: Int = 0,
    val sets: Int = 0,
    val reps: Int? = null,
    val restSeconds: Int? = null,
    val notes: String? = null,
)
