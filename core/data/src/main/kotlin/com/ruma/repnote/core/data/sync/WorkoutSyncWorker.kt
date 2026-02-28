package com.ruma.repnote.core.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.ruma.repnote.core.database.dao.WorkoutSessionDao
import com.ruma.repnote.core.database.entity.WorkoutSessionEntity
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutExercise
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * WorkManager worker that syncs workout sessions from local Room database to Firestore.
 * Handles retry logic and error tracking.
 */
class WorkoutSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),
    KoinComponent {
    private val workoutSessionDao: WorkoutSessionDao by inject()
    private val firestore: FirebaseFirestore by inject()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun doWork(): Result {
        Log.d(TAG, "WorkoutSyncWorker started (attempt: $runAttemptCount)")

        return try {
            val sessionsToSync = workoutSessionDao.getSessionsNeedingSync()

            if (sessionsToSync.isEmpty()) {
                return Result.success()
            }

            Log.d(TAG, "Syncing ${sessionsToSync.size} sessions")

            var successCount = 0
            var failureCount = 0

            for (session in sessionsToSync) {
                val synced = syncSession(session)
                if (synced) {
                    successCount++
                } else {
                    failureCount++
                }
            }

            Log.d(TAG, "Sync complete: $successCount synced, $failureCount failed")

            // Return retry if there were any failures
            if (failureCount > 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker error", e)
            Result.retry()
        }
    }

    @Suppress("TooGenericExceptionCaught", "MagicNumber")
    private suspend fun syncSession(session: WorkoutSessionEntity): Boolean =
        try {
            workoutSessionDao.updateSyncStatus(
                sessionId = session.id,
                syncStatus = SyncStatus.SYNCING,
                timestamp = System.currentTimeMillis(),
                error = null,
                retryCount = session.retryCount,
            )

            val sessionDoc = createFirestoreDocument(session)

            firestore
                .collection(USERS_COLLECTION)
                .document(session.userId)
                .collection(WORKOUT_SESSIONS_COLLECTION)
                .document(session.id)
                .set(sessionDoc)
                .await()

            workoutSessionDao.markAsSynced(session.id)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync session ${session.id.take(8)}", e)
            workoutSessionDao.updateSyncStatus(
                sessionId = session.id,
                syncStatus = SyncStatus.ERROR,
                timestamp = System.currentTimeMillis(),
                error = e.message ?: "Unknown error",
                retryCount = session.retryCount + 1,
            )
            false
        }

    private fun createFirestoreDocument(session: WorkoutSessionEntity): Map<String, Any?> =
        mapOf(
            "id" to session.id,
            "userId" to session.userId,
            "routineId" to session.routineId,
            "routineName" to session.routineName,
            "status" to session.status.name,
            "startTime" to session.startTime,
            "endTime" to session.endTime,
            "totalDurationSeconds" to session.totalDurationSeconds,
            "exercises" to session.exercises.map { it.toFirestoreMap() },
            "notes" to session.notes,
            "createdAt" to session.createdAt,
            "updatedAt" to session.updatedAt,
        )

    private fun WorkoutExercise.toFirestoreMap(): Map<String, Any?> =
        mapOf(
            "exerciseId" to exerciseId,
            "exerciseName" to exerciseName,
            "order" to order,
            "targetSets" to targetSets,
            "targetReps" to targetReps,
            "targetRestSeconds" to targetRestSeconds,
            "completedSets" to
                completedSets.map { set ->
                    mapOf(
                        "setNumber" to set.setNumber,
                        "reps" to set.reps,
                        "weight" to set.weight,
                        "completedAt" to set.completedAt,
                        "restTimerSeconds" to set.restTimerSeconds,
                        "notes" to set.notes,
                    )
                },
            "notes" to notes,
        )

    companion object {
        private const val TAG = "WorkoutSyncWorker"
        private const val USERS_COLLECTION = "users"
        private const val WORKOUT_SESSIONS_COLLECTION = "workoutSessions"

        const val WORK_NAME = "workout_session_sync"
    }
}
