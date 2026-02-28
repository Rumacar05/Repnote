package com.ruma.repnote.core.data.model

/**
 * Firestore document representing a workout routine.
 */
data class RoutineDocument(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String? = null,
    val exercises: List<RoutineExerciseDocument> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
