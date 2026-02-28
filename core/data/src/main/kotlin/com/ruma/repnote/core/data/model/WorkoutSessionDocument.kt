package com.ruma.repnote.core.data.model

/**
 * Firestore document representing a workout session.
 * All fields have default values for Firestore deserialization.
 */
data class WorkoutSessionDocument(
    val id: String = "",
    val userId: String = "",
    val routineId: String? = null,
    val routineName: String = "",
    val status: String = "IN_PROGRESS",
    val exercises: List<WorkoutExerciseDocument> = emptyList(),
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val totalDurationSeconds: Long? = null,
    val notes: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
