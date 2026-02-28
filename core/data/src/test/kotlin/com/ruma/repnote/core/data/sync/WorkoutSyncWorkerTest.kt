package com.ruma.repnote.core.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.ruma.repnote.core.database.dao.WorkoutSessionDao
import com.ruma.repnote.core.database.entity.WorkoutSessionEntity
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

/**
 * Unit tests for WorkoutSyncWorker.
 * Tests the background synchronization of workout sessions from Room to Firestore.
 */
class WorkoutSyncWorkerTest : KoinTest {
    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var workoutSessionDao: WorkoutSessionDao
    private lateinit var firestore: FirebaseFirestore
    private lateinit var worker: WorkoutSyncWorker

    private val userId = "user-123"
    private val sessionId = "session-123"

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        workoutSessionDao = mockk(relaxed = true)
        firestore = mockk(relaxed = true)

        // Default run attempt count
        every { workerParams.runAttemptCount } returns 0

        // Start Koin with test module
        startKoin {
            modules(
                module {
                    single { workoutSessionDao }
                    single { firestore }
                },
            )
        }

        worker =
            WorkoutSyncWorker(
                context = context,
                params = workerParams,
            )
    }

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    private fun createTestSessionEntity(
        syncStatus: SyncStatus = SyncStatus.PENDING,
        retryCount: Int = 0,
    ): WorkoutSessionEntity =
        WorkoutSessionEntity(
            id = sessionId,
            userId = userId,
            routineId = "routine-123",
            routineName = "Push Day",
            status = WorkoutStatus.COMPLETED,
            exercises = emptyList(),
            startTime = 1234567890L,
            endTime = 1234567900L,
            totalDurationSeconds = 10L,
            notes = null,
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
            syncStatus = syncStatus,
            lastSyncAttempt = null,
            syncError = null,
            retryCount = retryCount,
        )

    @Test
    fun `WHEN no sessions need syncing THEN returns Success`() =
        runTest {
            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns emptyList()

            val result = worker.doWork()

            result shouldBeEqualTo ListenableWorker.Result.success()
            coVerify(exactly = 0) { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) }
        }

    @Test
    fun `WHEN all sessions sync successfully THEN returns Success`() =
        runTest {
            val session1 = createTestSessionEntity()
            val session2 = createTestSessionEntity().copy(id = "session-456")

            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns listOf(session1, session2)
            coEvery { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) } returns Unit
            coEvery { workoutSessionDao.markAsSynced(any(), any()) } returns Unit

            // Mock Firestore to succeed using completed Task
            every {
                firestore
                    .collection(any())
                    .document(any())
                    .collection(any())
                    .document(any())
                    .set(any())
            } returns
                Tasks.forResult(null)

            val result = worker.doWork()

            result shouldBeEqualTo ListenableWorker.Result.success()

            // Verify all sessions were marked as synced
            coVerify { workoutSessionDao.markAsSynced(session1.id, any()) }
            coVerify { workoutSessionDao.markAsSynced(session2.id, any()) }
        }

    @Test
    fun `WHEN session sync fails THEN returns Retry`() =
        runTest {
            val session = createTestSessionEntity()

            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns listOf(session)
            coEvery { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) } returns Unit

            // Mock Firestore to fail with exception Task
            val exception = Exception("Network error")
            every {
                firestore
                    .collection(any())
                    .document(any())
                    .collection(any())
                    .document(any())
                    .set(any())
            } returns
                Tasks.forException(exception)

            val result = worker.doWork()

            result shouldBeEqualTo ListenableWorker.Result.retry()

            // Verify session was marked as ERROR
            coVerify {
                workoutSessionDao.updateSyncStatus(
                    sessionId = session.id,
                    syncStatus = SyncStatus.ERROR,
                    timestamp = any(),
                    error = "Network error",
                    retryCount = session.retryCount + 1,
                )
            }
        }

    @Test
    fun `WHEN sync starts THEN session status is updated to SYNCING`() =
        runTest {
            val session = createTestSessionEntity()

            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns listOf(session)
            coEvery { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) } returns Unit
            coEvery { workoutSessionDao.markAsSynced(any(), any()) } returns Unit

            // Mock Firestore to succeed
            every {
                firestore
                    .collection(any())
                    .document(any())
                    .collection(any())
                    .document(any())
                    .set(any())
            } returns
                Tasks.forResult(null)

            worker.doWork()

            // Verify status was updated to SYNCING before upload
            coVerify {
                workoutSessionDao.updateSyncStatus(
                    sessionId = session.id,
                    syncStatus = SyncStatus.SYNCING,
                    timestamp = any(),
                    error = null,
                    retryCount = session.retryCount,
                )
            }
        }

    @Test
    fun `WHEN sync succeeds THEN session is marked as SYNCED`() =
        runTest {
            val session = createTestSessionEntity()

            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns listOf(session)
            coEvery { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) } returns Unit
            coEvery { workoutSessionDao.markAsSynced(any(), any()) } returns Unit

            // Mock Firestore to succeed
            every {
                firestore
                    .collection(any())
                    .document(any())
                    .collection(any())
                    .document(any())
                    .set(any())
            } returns
                Tasks.forResult(null)

            worker.doWork()

            // Verify session was marked as synced
            coVerify { workoutSessionDao.markAsSynced(session.id, any()) }
        }

    @Test
    fun `WHEN sync encounters exception THEN retryCount is incremented`() =
        runTest {
            val session = createTestSessionEntity(retryCount = 2)

            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns listOf(session)
            coEvery { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) } returns Unit

            // Mock Firestore to fail with exception
            val exception = Exception("Firestore unavailable")
            every {
                firestore
                    .collection(any())
                    .document(any())
                    .collection(any())
                    .document(any())
                    .set(any())
            } returns
                Tasks.forException(exception)

            val result = worker.doWork()

            result shouldBeEqualTo ListenableWorker.Result.retry()

            // Verify retry count was incremented
            coVerify {
                workoutSessionDao.updateSyncStatus(
                    sessionId = session.id,
                    syncStatus = SyncStatus.ERROR,
                    timestamp = any(),
                    error = "Firestore unavailable",
                    retryCount = 3, // Incremented from 2
                )
            }
        }

    @Test
    fun `WHEN mixed success and failure THEN returns Retry`() =
        runTest {
            val session1 = createTestSessionEntity()
            val session2 = createTestSessionEntity().copy(id = "session-456")

            coEvery { workoutSessionDao.getSessionsNeedingSync() } returns listOf(session1, session2)
            coEvery { workoutSessionDao.updateSyncStatus(any(), any(), any(), any(), any()) } returns Unit
            coEvery { workoutSessionDao.markAsSynced(any(), any()) } returns Unit

            // Mock Firestore - first call succeeds, second fails
            var callCount = 0
            every {
                firestore
                    .collection(any())
                    .document(any())
                    .collection(any())
                    .document(any())
                    .set(any())
            } answers {
                callCount++
                if (callCount == 1) {
                    Tasks.forResult(null) // First session succeeds
                } else {
                    Tasks.forException(Exception("Network timeout")) // Second session fails
                }
            }

            val result = worker.doWork()

            result shouldBeEqualTo ListenableWorker.Result.retry()

            // Verify first session was synced
            coVerify { workoutSessionDao.markAsSynced(session1.id, any()) }

            // Verify second session was marked as ERROR
            coVerify {
                workoutSessionDao.updateSyncStatus(
                    sessionId = session2.id,
                    syncStatus = SyncStatus.ERROR,
                    timestamp = any(),
                    error = "Network timeout",
                    retryCount = session2.retryCount + 1,
                )
            }
        }
}
