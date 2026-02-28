package com.ruma.repnote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ruma.repnote.core.database.entity.WorkoutSessionEntity
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for workout sessions.
 * Provides local-first storage with sync tracking.
 */
@Suppress("TooManyFunctions")
@Dao
interface WorkoutSessionDao {
    /**
     * Insert a new workout session.
     * Replaces if session with same ID exists.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSessionEntity)

    /**
     * Update an existing workout session.
     */
    @Update
    suspend fun update(session: WorkoutSessionEntity)

    /**
     * Insert or update a workout session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: WorkoutSessionEntity)

    /**
     * Get a workout session by ID.
     */
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getById(sessionId: String): WorkoutSessionEntity?

    /**
     * Observe a workout session by ID.
     */
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun observeById(sessionId: String): Flow<WorkoutSessionEntity?>

    /**
     * Get the active workout session for a user.
     * Returns the most recent IN_PROGRESS session.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId AND status = 'IN_PROGRESS'
        ORDER BY startTime DESC
        LIMIT 1
        """,
    )
    suspend fun getActiveSession(userId: String): WorkoutSessionEntity?

    /**
     * Observe the active workout session for a user.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId AND status = 'IN_PROGRESS'
        ORDER BY startTime DESC
        LIMIT 1
        """,
    )
    fun observeActiveSession(userId: String): Flow<WorkoutSessionEntity?>

    /**
     * Get all workout sessions for a user.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId
        ORDER BY startTime DESC
        """,
    )
    suspend fun getAllByUser(userId: String): List<WorkoutSessionEntity>

    /**
     * Get all completed workout sessions for a user.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId AND status = 'COMPLETED'
        ORDER BY startTime DESC
        """,
    )
    suspend fun getCompletedSessions(userId: String): List<WorkoutSessionEntity>

    /**
     * Observe all completed workout sessions for a user.
     * Returns a Flow that emits whenever sessions are added/updated/deleted.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId AND status = 'COMPLETED'
        ORDER BY startTime DESC
        """,
    )
    fun observeCompletedSessions(userId: String): Flow<List<WorkoutSessionEntity>>

    /**
     * Observe all workout sessions for a specific routine.
     * Used for comparing with previous sessions in summary.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId AND routineId = :routineId
        ORDER BY startTime DESC
        """,
    )
    fun observeSessionsByRoutine(
        userId: String,
        routineId: String,
    ): Flow<List<WorkoutSessionEntity>>

    /**
     * Get all sessions that need to be synced to Firestore.
     * Returns sessions with PENDING or ERROR status.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE syncStatus IN ('PENDING', 'ERROR')
        ORDER BY updatedAt ASC
        """,
    )
    suspend fun getSessionsNeedingSync(): List<WorkoutSessionEntity>

    /**
     * Update sync status for a session.
     */
    @Query(
        """
        UPDATE workout_sessions
        SET syncStatus = :syncStatus,
            lastSyncAttempt = :timestamp,
            syncError = :error,
            retryCount = :retryCount
        WHERE id = :sessionId
        """,
    )
    suspend fun updateSyncStatus(
        sessionId: String,
        syncStatus: SyncStatus,
        timestamp: Long,
        error: String?,
        retryCount: Int,
    )

    /**
     * Mark a session as synced successfully.
     */
    @Query(
        """
        UPDATE workout_sessions
        SET syncStatus = 'SYNCED',
            lastSyncAttempt = :timestamp,
            syncError = NULL,
            retryCount = 0
        WHERE id = :sessionId
        """,
    )
    suspend fun markAsSynced(
        sessionId: String,
        timestamp: Long = System.currentTimeMillis(),
    )

    /**
     * Update session status (IN_PROGRESS, COMPLETED, ABANDONED).
     * Also marks session as needing sync.
     */
    @Query(
        """
        UPDATE workout_sessions
        SET status = :status,
            endTime = :endTime,
            totalDurationSeconds = :durationSeconds,
            updatedAt = :updatedAt,
            syncStatus = 'PENDING'
        WHERE id = :sessionId
        """,
    )
    suspend fun updateStatus(
        sessionId: String,
        status: WorkoutStatus,
        endTime: Long?,
        durationSeconds: Long?,
        updatedAt: Long,
    )

    /**
     * Delete a workout session.
     */
    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun delete(sessionId: String)

    /**
     * Delete all workout sessions for a user.
     * Use with caution!
     */
    @Query("DELETE FROM workout_sessions WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    /**
     * Get count of sessions needing sync.
     */
    @Query(
        """
        SELECT COUNT(*) FROM workout_sessions
        WHERE syncStatus IN ('PENDING', 'ERROR')
        """,
    )
    suspend fun getUnsyncedCount(): Int

    /**
     * Get count of sessions needing sync for a specific user.
     */
    @Query(
        """
        SELECT COUNT(*) FROM workout_sessions
        WHERE userId = :userId AND syncStatus IN ('PENDING', 'ERROR')
        """,
    )
    suspend fun getUnsyncedSessionCount(userId: String): Int

    /**
     * Observe count of sessions needing sync.
     */
    @Query(
        """
        SELECT COUNT(*) FROM workout_sessions
        WHERE syncStatus IN ('PENDING', 'ERROR')
        """,
    )
    fun observeUnsyncedCount(): Flow<Int>

    // ==================== Cache Management ====================

    /**
     * Get total count of completed sessions for a user.
     */
    @Query(
        """
        SELECT COUNT(*) FROM workout_sessions
        WHERE userId = :userId AND status = 'COMPLETED'
        """,
    )
    suspend fun getCompletedSessionCount(userId: String): Int

    /**
     * Delete old synced sessions, keeping only the most recent ones.
     * Only deletes sessions that are already synced (not pending or error).
     * Keeps sessions newer than the cutoff date regardless of count.
     *
     * @param userId User ID to clean up
     * @param keepCount Number of recent sessions to keep
     * @param cutoffTimestamp Sessions older than this AND exceeding keepCount will be deleted
     */
    @Query(
        """
        DELETE FROM workout_sessions
        WHERE userId = :userId
            AND status = 'COMPLETED'
            AND syncStatus = 'SYNCED'
            AND startTime < :cutoffTimestamp
            AND id NOT IN (
                SELECT id FROM workout_sessions
                WHERE userId = :userId AND status = 'COMPLETED'
                ORDER BY startTime DESC
                LIMIT :keepCount
            )
        """,
    )
    suspend fun deleteOldSyncedSessions(
        userId: String,
        keepCount: Int,
        cutoffTimestamp: Long,
    )

    /**
     * Get the oldest completed and synced sessions for a user.
     * Used to check what can be safely deleted.
     */
    @Query(
        """
        SELECT * FROM workout_sessions
        WHERE userId = :userId
            AND status = 'COMPLETED'
            AND syncStatus = 'SYNCED'
        ORDER BY startTime ASC
        LIMIT :limit
        """,
    )
    suspend fun getOldestSyncedSessions(
        userId: String,
        limit: Int,
    ): List<WorkoutSessionEntity>
}
