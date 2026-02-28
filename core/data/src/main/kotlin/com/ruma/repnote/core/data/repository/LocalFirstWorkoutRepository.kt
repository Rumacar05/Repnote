package com.ruma.repnote.core.data.repository

import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.ruma.repnote.core.data.mapper.toWorkoutSession
import com.ruma.repnote.core.data.model.WorkoutSessionDocument
import com.ruma.repnote.core.data.sync.WorkoutSyncWorker
import com.ruma.repnote.core.database.dao.WorkoutSessionDao
import com.ruma.repnote.core.database.entity.WorkoutSessionEntity
import com.ruma.repnote.core.database.entity.toEntity
import com.ruma.repnote.core.database.entity.toWorkoutSession
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Local-first implementation of WorkoutRepository.
 * Saves to Room immediately for instant UI updates, then syncs to Firestore in background.
 */
@Suppress("TooManyFunctions")
class LocalFirstWorkoutRepository(
    private val firestore: FirebaseFirestore,
    private val exerciseRepository: ExerciseRepository,
    private val workoutSessionDao: WorkoutSessionDao,
    private val workManager: WorkManager,
) : WorkoutRepository {
    /**
     * Observes active session from local Room database for instant updates.
     * Local-first approach: UI always reads from Room, background sync keeps Firestore updated.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun getActiveSession(userId: String): Flow<WorkoutResult<WorkoutSession?>> =
        workoutSessionDao
            .observeActiveSession(userId)
            .map { sessionEntity ->
                val session = sessionEntity?.toWorkoutSession()
                WorkoutResult.Success(session) as WorkoutResult<WorkoutSession?>
            }.catch { e ->
                // Rethrow cancellation exceptions to maintain Flow cancellation semantics
                if (e is CancellationException) {
                    throw e
                }
                Log.e(TAG, "Error observing active session from Room", e)
                emit(WorkoutResult.Error(WorkoutException.Unknown(e.message)))
            }

    @Suppress("TooGenericExceptionCaught", "LongMethod")
    override suspend fun startWorkoutFromRoutine(
        userId: String,
        routineId: String,
        routine: Routine,
    ): WorkoutResult<String> {
        return try {
            val activeSession = workoutSessionDao.getActiveSession(userId)

            if (activeSession != null) {
                Log.w(TAG, "Cannot start workout: active session already exists")
                return WorkoutResult.Error(WorkoutException.SessionAlreadyActive)
            }

            // Generate session ID
            val sessionId =
                UUID
                    .randomUUID()
                    .toString()
            val timestamp = System.currentTimeMillis()

            // Load all exercises in parallel using async/await
            val exerciseMap =
                coroutineScope {
                    routine.exercises
                        .map { routineExercise ->
                            async {
                                val result =
                                    exerciseRepository.getExerciseById(
                                        exerciseId = routineExercise.exerciseId,
                                        userId = userId,
                                    )

                                when (result) {
                                    is ExerciseResult.Success -> {
                                        routineExercise.exerciseId to result.data.name
                                    }

                                    is ExerciseResult.Error -> {
                                        Log.w(
                                            TAG,
                                            "Failed to load exercise: ${result.exception}",
                                        )
                                        null
                                    }
                                }
                            }
                        }.awaitAll()
                        .filterNotNull()
                        .toMap()
                }

            // Convert routine exercises to workout exercises (snapshot names)
            val workoutExercises =
                routine.exercises.map { routineExercise ->
                    val exerciseName =
                        exerciseMap[routineExercise.exerciseId] ?: "Unknown Exercise"
                    WorkoutExercise(
                        exerciseId = routineExercise.exerciseId,
                        exerciseName = exerciseName,
                        order = routineExercise.order,
                        targetSets = routineExercise.sets,
                        targetReps = routineExercise.reps,
                        targetRestSeconds = routineExercise.restSeconds,
                        completedSets = emptyList(),
                        notes = routineExercise.notes,
                    )
                }

            val session =
                WorkoutSession(
                    id = sessionId,
                    userId = userId,
                    routineId = routineId,
                    routineName = routine.name,
                    status = WorkoutStatus.IN_PROGRESS,
                    exercises = workoutExercises,
                    startTime = timestamp,
                    endTime = null,
                    totalDurationSeconds = null,
                    notes = null,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                )

            workoutSessionDao.insert(session.toEntity(syncStatus = SyncStatus.PENDING))

            // Schedule background sync to Firestore
            scheduleSyncWork()

            Log.d(TAG, "Workout session started locally: $sessionId, sync scheduled")
            WorkoutResult.Success(sessionId)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting workout from routine", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }
    }

    /**
     * Starts an ad-hoc workout using local-first pattern.
     * Creates session in Room immediately, then schedules background sync to Firestore.
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun startAdHocWorkout(
        userId: String,
        workoutName: String,
    ): WorkoutResult<String> {
        return try {
            // Check for existing active session in Room (FAST - ~5ms)
            val activeSession = workoutSessionDao.getActiveSession(userId)

            if (activeSession != null) {
                Log.w(TAG, "Cannot start workout: active session already exists")
                return WorkoutResult.Error(WorkoutException.SessionAlreadyActive)
            }

            // Generate session ID
            val sessionId =
                UUID
                    .randomUUID()
                    .toString()
            val timestamp = System.currentTimeMillis()

            val session =
                WorkoutSession(
                    id = sessionId,
                    userId = userId,
                    routineId = null, // Ad-hoc workout
                    routineName = workoutName,
                    status = WorkoutStatus.IN_PROGRESS,
                    exercises = emptyList(), // Start with no exercises
                    startTime = timestamp,
                    endTime = null,
                    totalDurationSeconds = null,
                    notes = null,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                )

            // Save to Room first (FAST - ~10ms)
            workoutSessionDao.insert(session.toEntity(syncStatus = SyncStatus.PENDING))

            // Schedule background sync to Firestore
            scheduleSyncWork()

            Log.d(TAG, "Ad-hoc workout session started locally: $sessionId, sync scheduled")
            WorkoutResult.Success(sessionId)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ad-hoc workout", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }
    }

    /**
     * Updates session using local-first pattern.
     * Saves to Room immediately (~10ms) then schedules background sync to Firestore.
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun updateSession(session: WorkoutSession): WorkoutResult<Unit> =
        try {
            val startTime = System.currentTimeMillis()
            val updatedSession =
                session.copy(
                    updatedAt = System.currentTimeMillis(),
                )

            val exerciseCount = updatedSession.exercises.size
            val totalSets = updatedSession.exercises.sumOf { it.completedSets.size }

            Log.d(
                TAG,
                "[LOCAL-FIRST] updateSession - Exercises: $exerciseCount, Total sets: $totalSets",
            )

            // Save to Room first (FAST - ~10ms)
            val beforeRoom = System.currentTimeMillis()
            workoutSessionDao.upsert(updatedSession.toEntity(syncStatus = SyncStatus.PENDING))
            val roomTime = System.currentTimeMillis() - beforeRoom

            // Schedule background sync to Firestore
            scheduleSyncWork()

            val totalTime = System.currentTimeMillis() - startTime
            Log.d(
                TAG,
                "[LOCAL-FIRST] updateSession - Room write: ${roomTime}ms, Total: ${totalTime}ms",
            )
            Log.d(TAG, "Workout session saved locally: ${session.id}, sync scheduled")
            WorkoutResult.Success(Unit)
        } catch (e: CancellationException) {
            // Expected when debouncing cancels previous save jobs
            Log.d(TAG, "Update session cancelled (debouncing)")
            throw e // Re-throw to propagate cancellation
        } catch (e: Exception) {
            Log.e(TAG, "Error updating workout session locally", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }

    /**
     * Completes session using local-first pattern.
     * Updates Room immediately for instant navigation, then syncs to Firestore in background.
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun completeSession(
        userId: String,
        sessionId: String,
    ): WorkoutResult<Unit> {
        return try {
            val startTime = System.currentTimeMillis()

            // Read from Room (FAST - ~5ms)
            val sessionEntity = workoutSessionDao.getById(sessionId)

            if (sessionEntity == null) {
                Log.w(TAG, "Session not found in local database: $sessionId")
                return WorkoutResult.Error(WorkoutException.SessionNotFound)
            }

            val endTime = System.currentTimeMillis()
            val durationSeconds =
                (endTime - sessionEntity.startTime) / MILLISECONDS_TO_SECONDS

            // Update status in Room (FAST - ~10ms)
            workoutSessionDao.updateStatus(
                sessionId = sessionId,
                status = WorkoutStatus.COMPLETED,
                endTime = endTime,
                durationSeconds = durationSeconds,
                updatedAt = endTime,
            )

            // Schedule background sync
            scheduleSyncWork()

            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "[LOCAL-FIRST] Session completed locally in ${totalTime}ms: $sessionId")
            WorkoutResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error completing workout session locally", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }
    }

    /**
     * Abandons session using local-first pattern.
     * Updates Room immediately for instant navigation, then syncs to Firestore in background.
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun abandonSession(
        userId: String,
        sessionId: String,
    ): WorkoutResult<Unit> {
        return try {
            val startTime = System.currentTimeMillis()

            // Verify session exists in Room
            val sessionEntity = workoutSessionDao.getById(sessionId)

            if (sessionEntity == null) {
                Log.w(TAG, "Session not found in local database: $sessionId")
                return WorkoutResult.Error(WorkoutException.SessionNotFound)
            }

            val endTime = System.currentTimeMillis()

            // Update status in Room (FAST - ~10ms)
            workoutSessionDao.updateStatus(
                sessionId = sessionId,
                status = WorkoutStatus.ABANDONED,
                endTime = endTime,
                durationSeconds = null,
                updatedAt = endTime,
            )

            // Schedule background sync
            scheduleSyncWork()

            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "[LOCAL-FIRST] Session abandoned locally in ${totalTime}ms: $sessionId")
            WorkoutResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error abandoning workout session locally", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }
    }

    /**
     * Reads session by ID from Room database for instant display.
     * Used by Summary screen - shows completed session immediately without waiting for Firestore.
     * Background sync happens via startFirestoreSync() called at app startup.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun getSessionById(
        userId: String,
        sessionId: String,
    ): Flow<WorkoutResult<WorkoutSession>> =
        workoutSessionDao
            .observeById(sessionId)
            .map<WorkoutSessionEntity?, WorkoutResult<WorkoutSession>> { sessionEntity ->
                if (sessionEntity != null) {
                    WorkoutResult.Success(sessionEntity.toWorkoutSession())
                } else {
                    WorkoutResult.Error(WorkoutException.SessionNotFound)
                }
            }.catch { e ->
                // Rethrow cancellation exceptions to maintain Flow cancellation semantics
                if (e is CancellationException) {
                    throw e
                }
                Log.e(TAG, "Error observing session by ID from Room", e)
                emit(WorkoutResult.Error(WorkoutException.Unknown(e.message)))
            }

    /**
     * Observes workout history from Room database for instant and reactive display.
     * Automatically updates when new workouts are completed.
     * Background sync happens via WorkoutSyncWorker.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun getUserWorkoutHistory(
        userId: String,
        limit: Int,
    ): Flow<WorkoutResult<List<WorkoutSession>>> =
        workoutSessionDao
            .observeCompletedSessions(userId)
            .map<List<WorkoutSessionEntity>, WorkoutResult<List<WorkoutSession>>> { sessions ->
                val workoutSessions =
                    sessions
                        .take(limit)
                        .map { it.toWorkoutSession() }
                WorkoutResult.Success(workoutSessions)
            }.catch { e ->
                // Rethrow cancellation exceptions to maintain Flow cancellation semantics
                if (e is CancellationException) {
                    throw e
                }
                Log.e(TAG, "Error observing workout history from Room", e)
                emit(WorkoutResult.Error(WorkoutException.Unknown(e.message)))
            }

    /**
     * Observes workout history for a specific routine from local Room database.
     * Local-first approach: reads from Room for instant display and consistency.
     * Used by Summary screen for comparing with previous sessions.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun getRoutineWorkoutHistory(
        userId: String,
        routineId: String,
    ): Flow<WorkoutResult<List<WorkoutSession>>> =
        workoutSessionDao
            .observeSessionsByRoutine(userId, routineId)
            .map<List<WorkoutSessionEntity>, WorkoutResult<List<WorkoutSession>>> { sessions ->
                val workoutSessions = sessions.map { it.toWorkoutSession() }
                WorkoutResult.Success(workoutSessions)
            }.catch { e ->
                // Rethrow cancellation exceptions to maintain Flow cancellation semantics
                if (e is CancellationException) {
                    throw e
                }
                Log.e(TAG, "Error observing routine workout history from Room", e)
                emit(WorkoutResult.Error(WorkoutException.Unknown(e.message)))
            }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getUnsyncedSessionCount(userId: String): Int =
        try {
            workoutSessionDao.getUnsyncedSessionCount(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting unsynced session count", e)
            0
        }

    /**
     * Syncs workout sessions from Firestore to local Room database.
     * Downloads recent sessions and merges them with local data using updatedAt timestamp.
     * Also cleans up old cached sessions after sync.
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun syncFromFirestore(userId: String): WorkoutResult<Unit> =
        try {
            // Only sync recent sessions (last N days) to avoid downloading entire history
            val cutoffTimestamp =
                System.currentTimeMillis() - (SYNC_LOOKBACK_DAYS * MILLIS_PER_DAY)

            val snapshot =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(WORKOUT_SESSIONS_COLLECTION)
                    .whereGreaterThan("startTime", cutoffTimestamp)
                    .get()
                    .await()

            processSyncDocuments(snapshot.documents)

            // Clean up old cached sessions after successful sync
            cleanupOldSessions(userId)

            Log.d(TAG, "Synced ${snapshot.documents.size} sessions from Firestore")
            WorkoutResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firestore", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }

    private suspend fun processSyncDocuments(
        documents: List<com.google.firebase.firestore.DocumentSnapshot>,
    ) {
        documents.forEach { doc -> processDocument(doc) }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun processDocument(doc: com.google.firebase.firestore.DocumentSnapshot) {
        try {
            val sessionDoc = doc.toObject(WorkoutSessionDocument::class.java)
            sessionDoc?.let { mergeRemoteSession(it.toWorkoutSession()) }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sync document ${doc.id}", e)
        }
    }

    private suspend fun mergeRemoteSession(session: WorkoutSession) {
        val localSession = workoutSessionDao.getById(session.id)

        when {
            localSession == null -> {
                workoutSessionDao.insert(session.toEntity(syncStatus = SyncStatus.SYNCED))
            }

            localSession.syncStatus == SyncStatus.SYNCED &&
                session.updatedAt > localSession.updatedAt -> {
                workoutSessionDao.upsert(session.toEntity(syncStatus = SyncStatus.SYNCED))
            }
        }
    }

    /**
     * Schedules background sync work to upload pending sessions to Firestore.
     * Uses unique work name to avoid duplicates.
     */
    @Suppress("TooGenericExceptionCaught")
    private fun scheduleSyncWork() {
        try {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val syncWorkRequest =
                OneTimeWorkRequestBuilder<WorkoutSyncWorker>()
                    .setConstraints(constraints)
                    .build()

            workManager.enqueueUniqueWork(
                WorkoutSyncWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest,
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule sync work", e)
        }
    }

    /**
     * Cleans up old workout sessions from local cache.
     * Only removes sessions that are already synced to Firestore.
     * Keeps recent sessions for quick access, older ones can be loaded from Firestore.
     */
    @Suppress("TooGenericExceptionCaught")
    override suspend fun cleanupOldSessions(userId: String): WorkoutResult<Int> =
        try {
            val cutoffTimestamp =
                System.currentTimeMillis() - (CACHE_RETENTION_DAYS * MILLIS_PER_DAY)

            val countBefore = workoutSessionDao.getCompletedSessionCount(userId)

            workoutSessionDao.deleteOldSyncedSessions(
                userId = userId,
                keepCount = MAX_CACHED_SESSIONS,
                cutoffTimestamp = cutoffTimestamp,
            )

            val countAfter = workoutSessionDao.getCompletedSessionCount(userId)
            val deletedCount = countBefore - countAfter

            if (deletedCount > 0) {
                Log.d(TAG, "Cleaned up $deletedCount old synced sessions from cache")
            }

            WorkoutResult.Success(deletedCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old sessions", e)
            WorkoutResult.Error(WorkoutException.Unknown(e.message))
        }

    companion object {
        private const val TAG = "LocalFirstWorkoutRepo"
        private const val USERS_COLLECTION = "users"
        private const val WORKOUT_SESSIONS_COLLECTION = "workoutSessions"
        private const val MILLISECONDS_TO_SECONDS = 1000L

        // Cache configuration
        private const val MAX_CACHED_SESSIONS = 20
        private const val CACHE_RETENTION_DAYS = 60
        private const val SYNC_LOOKBACK_DAYS = 90
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
}
