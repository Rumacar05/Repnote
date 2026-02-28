package com.ruma.repnote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus

/**
 * Room entity for caching workout sessions locally.
 * Provides local-first storage with background sync to Firestore.
 *
 * @property id Session ID (primary key)
 * @property userId ID of the user who owns this session
 * @property routineId ID of the routine this session is based on (null for ad-hoc)
 * @property routineName Name of the routine
 * @property status Current status (IN_PROGRESS, COMPLETED, ABANDONED)
 * @property exercises List of exercises with completed sets (stored as JSON)
 * @property startTime Timestamp when workout started
 * @property endTime Timestamp when workout ended (null if in progress)
 * @property totalDurationSeconds Total duration in seconds (null if in progress)
 * @property notes Optional notes about the session
 * @property createdAt Timestamp when session was created
 * @property updatedAt Timestamp when session was last updated locally
 * @property syncStatus Current sync status with Firestore
 * @property lastSyncAttempt Timestamp of last sync attempt (null if never attempted)
 * @property syncError Error message from last failed sync attempt (null if no error)
 * @property retryCount Number of failed sync attempts
 */
@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey
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
    val syncStatus: SyncStatus,
    val lastSyncAttempt: Long?,
    val syncError: String?,
    val retryCount: Int = 0,
)

/**
 * Converts WorkoutSessionEntity to domain WorkoutSession model
 */
fun WorkoutSessionEntity.toWorkoutSession(): WorkoutSession =
    WorkoutSession(
        id = id,
        userId = userId,
        routineId = routineId,
        routineName = routineName,
        status = status,
        exercises = exercises,
        startTime = startTime,
        endTime = endTime,
        totalDurationSeconds = totalDurationSeconds,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/**
 * Converts domain WorkoutSession to WorkoutSessionEntity
 * Initial sync status is PENDING, will be synced in background
 */
fun WorkoutSession.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING,
    lastSyncAttempt: Long? = null,
    syncError: String? = null,
    retryCount: Int = 0,
): WorkoutSessionEntity =
    WorkoutSessionEntity(
        id = id,
        userId = userId,
        routineId = routineId,
        routineName = routineName,
        status = status,
        exercises = exercises,
        startTime = startTime,
        endTime = endTime,
        totalDurationSeconds = totalDurationSeconds,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        lastSyncAttempt = lastSyncAttempt,
        syncError = syncError,
        retryCount = retryCount,
    )
