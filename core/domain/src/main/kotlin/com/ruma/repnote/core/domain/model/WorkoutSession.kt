package com.ruma.repnote.core.domain.model

/**
 * Represents a workout session, which can be based on a routine or ad-hoc.
 *
 * @property id Unique identifier for the session
 * @property userId ID of the user who owns this session
 * @property routineId ID of the routine this session is based on (null for ad-hoc workouts)
 * @property routineName Name of the routine (snapshot at session creation for historical accuracy)
 * @property status Current status of the workout session
 * @property exercises List of exercises in this workout session with their configurations and completed sets
 * @property startTime Timestamp (milliseconds) when the workout was started
 * @property endTime Timestamp (milliseconds) when the workout was completed or abandoned (null if in progress)
 * @property totalDurationSeconds Total duration of the workout in seconds (null if in progress)
 * @property notes Optional notes about the overall workout session
 * @property createdAt Timestamp (milliseconds) when this session was created
 * @property updatedAt Timestamp (milliseconds) when this session was last updated
 */
data class WorkoutSession(
    val id: String,
    val userId: String,
    val routineId: String?,
    val routineName: String,
    val status: WorkoutStatus,
    val exercises: List<WorkoutExercise>,
    val startTime: Long,
    val endTime: Long?,
    val totalDurationSeconds: Long?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * Status of a workout session.
 */
enum class WorkoutStatus {
    /** Session is currently in progress */
    IN_PROGRESS,

    /** Session has been successfully completed */
    COMPLETED,

    /** Session was started but not completed (user abandoned it) */
    ABANDONED,
}
