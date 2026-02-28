package com.ruma.repnote.core.domain.repository

import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing workout sessions.
 */
@Suppress("TooManyFunctions")
interface WorkoutRepository {
    // Active session management

    /**
     * Get the active workout session for a user (if any).
     * Only one active session is allowed per user at a time.
     *
     * @param userId The ID of the user
     * @return Flow emitting the result with the active session, or null if no active session
     */
    fun getActiveSession(userId: String): Flow<WorkoutResult<WorkoutSession?>>

    /**
     * Start a new workout session based on a routine.
     * Will fail if user already has an active session.
     *
     * @param userId The ID of the user
     * @param routineId The ID of the routine to base the workout on
     * @param routine The routine data (used to snapshot exercise info)
     * @return Result with the created session ID
     */
    suspend fun startWorkoutFromRoutine(
        userId: String,
        routineId: String,
        routine: Routine,
    ): WorkoutResult<String>

    /**
     * Start a new ad-hoc workout session without a routine.
     * Will fail if user already has an active session.
     *
     * @param userId The ID of the user
     * @param workoutName Name for the ad-hoc workout
     * @return Result with the created session ID
     */
    suspend fun startAdHocWorkout(
        userId: String,
        workoutName: String,
    ): WorkoutResult<String>

    /**
     * Update an existing workout session.
     * Used to add completed sets, update notes, etc.
     *
     * @param session The updated session data
     * @return Result of the operation
     */
    suspend fun updateSession(session: WorkoutSession): WorkoutResult<Unit>

    /**
     * Mark a workout session as completed.
     * Sets endTime, calculates totalDuration, and updates status to COMPLETED.
     *
     * @param userId The ID of the user (for authorization)
     * @param sessionId The ID of the session to complete
     * @return Result of the operation
     */
    suspend fun completeSession(
        userId: String,
        sessionId: String,
    ): WorkoutResult<Unit>

    /**
     * Mark a workout session as abandoned.
     * Sets endTime and updates status to ABANDONED.
     *
     * @param userId The ID of the user (for authorization)
     * @param sessionId The ID of the session to abandon
     * @return Result of the operation
     */
    suspend fun abandonSession(
        userId: String,
        sessionId: String,
    ): WorkoutResult<Unit>

    // History and queries

    /**
     * Get a specific workout session by ID.
     *
     * @param userId The ID of the user (for authorization)
     * @param sessionId The ID of the session
     * @return Flow emitting the result of the operation
     */
    fun getSessionById(
        userId: String,
        sessionId: String,
    ): Flow<WorkoutResult<WorkoutSession>>

    /**
     * Get workout history for a user.
     * Returns sessions ordered by startTime descending (most recent first).
     *
     * @param userId The ID of the user
     * @param limit Maximum number of sessions to return (default 50)
     * @return Flow emitting the result with list of sessions
     */
    fun getUserWorkoutHistory(
        userId: String,
        limit: Int = 50,
    ): Flow<WorkoutResult<List<WorkoutSession>>>

    /**
     * Get workout history for a specific routine.
     * Returns all sessions based on the specified routine.
     *
     * @param userId The ID of the user
     * @param routineId The ID of the routine
     * @return Flow emitting the result with list of sessions
     */
    fun getRoutineWorkoutHistory(
        userId: String,
        routineId: String,
    ): Flow<WorkoutResult<List<WorkoutSession>>>

    /**
     * Get count of sessions that need to be synced to Firestore.
     * Useful for debugging sync issues and showing sync status to user.
     *
     * @param userId The ID of the user
     * @return Count of unsynced sessions
     */
    suspend fun getUnsyncedSessionCount(userId: String): Int

    /**
     * Sync workout sessions from Firestore to local Room database.
     * Downloads all sessions from Firestore and merges them with local data.
     * Uses updatedAt timestamp to determine which version to keep.
     *
     * @param userId The ID of the user
     * @return Result of the sync operation
     */
    suspend fun syncFromFirestore(userId: String): WorkoutResult<Unit>

    // Cache management

    /**
     * Clean up old workout sessions from local cache.
     * Only removes sessions that are already synced to Firestore.
     * Older sessions can be loaded from Firestore when needed.
     *
     * @param userId The ID of the user
     * @return Result with count of deleted sessions
     */
    suspend fun cleanupOldSessions(userId: String): WorkoutResult<Int>
}
