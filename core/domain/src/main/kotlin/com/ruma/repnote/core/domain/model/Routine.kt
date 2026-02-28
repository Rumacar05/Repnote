package com.ruma.repnote.core.domain.model

/**
 * Represents a workout routine created by a user.
 *
 * @property id The unique identifier for the routine
 * @property userId The ID of the user who created this routine
 * @property name The name of the routine
 * @property description Optional description of the routine
 * @property exercises List of exercises in this routine
 * @property createdAt Timestamp when the routine was created (milliseconds since epoch)
 * @property updatedAt Timestamp when the routine was last updated (milliseconds since epoch)
 */
data class Routine(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val exercises: List<RoutineExercise> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
)
