package com.ruma.repnote.core.data.repository

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.ruma.repnote.core.database.dao.WorkoutSessionDao
import com.ruma.repnote.core.database.entity.WorkoutSessionEntity
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.core.domain.model.SyncStatus
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for LocalFirstWorkoutRepository.
 *
 * These tests mock Firebase Firestore and ExerciseRepository to test business logic.
 */
class LocalFirstWorkoutRepositoryTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var workoutSessionDao: WorkoutSessionDao
    private lateinit var workManager: WorkManager
    private lateinit var repository: LocalFirstWorkoutRepository

    private val userId = "user-123"
    private val routineId = "routine-123"
    private val sessionId = "session-123"

    @BeforeEach
    fun setup() {
        firestore = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        workoutSessionDao = mockk(relaxed = true)
        workManager = mockk(relaxed = true)

        // Mock WorkManager to avoid background work in tests
        every {
            workManager.enqueueUniqueWork(
                any<String>(),
                any<ExistingWorkPolicy>(),
                any<OneTimeWorkRequest>(),
            )
        } returns mockk<Operation>(relaxed = true)

        repository =
            LocalFirstWorkoutRepository(
                firestore,
                exerciseRepository,
                workoutSessionDao,
                workManager,
            )
    }

    private fun createTestExercise(): Exercise =
        Exercise(
            id = "ex1",
            name = "Bench Press",
            description = "Chest exercise",
            imageUrl = null,
            primaryMuscleGroup = MuscleGroup.CHEST,
            secondaryMuscleGroups = emptyList(),
            isGlobal = true,
            createdBy = null,
        )

    private fun createTestRoutine(): Routine =
        Routine(
            id = routineId,
            userId = userId,
            name = "Push Day",
            description = "Upper body push",
            exercises =
                listOf(
                    RoutineExercise(
                        exerciseId = "ex1",
                        order = 0,
                        sets = 3,
                        reps = 10,
                        restSeconds = 60,
                        notes = null,
                    ),
                ),
            createdAt = 1234567890L,
            updatedAt = 1234567900L,
        )

    private fun createTestSessionEntity(status: WorkoutStatus = WorkoutStatus.IN_PROGRESS): WorkoutSessionEntity =
        WorkoutSessionEntity(
            id = sessionId,
            userId = userId,
            routineId = routineId,
            routineName = "Push Day",
            status = status,
            exercises = emptyList(),
            startTime = 1234567890L,
            endTime = null,
            totalDurationSeconds = null,
            notes = null,
            createdAt = 1234567890L,
            updatedAt = 1234567890L,
            syncStatus = SyncStatus.PENDING,
            lastSyncAttempt = null,
            syncError = null,
            retryCount = 0,
        )

    @Test
    fun `WHEN startWorkoutFromRoutine is called with valid routine THEN session is created`() =
        runTest {
            val exercise = createTestExercise()
            val routine = createTestRoutine()

            // Mock DAO to return no active session
            coEvery { workoutSessionDao.getActiveSession(userId) } returns null
            // Mock insert to complete successfully
            coEvery { workoutSessionDao.insert(any()) } returns Unit

            coEvery {
                exerciseRepository.getExerciseById("ex1", userId)
            } returns ExerciseResult.Success(exercise)

            val result = repository.startWorkoutFromRoutine(userId, routineId, routine)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            // Verify a session ID was generated (UUID format)
            val returnedSessionId = (result as WorkoutResult.Success).data
            returnedSessionId.length shouldBeEqualTo 36 // UUID length with hyphens

            coVerify { exerciseRepository.getExerciseById("ex1", userId) }
            // Verify session was inserted into Room
            coVerify { workoutSessionDao.insert(any()) }
        }

    @Test
    fun `WHEN startWorkoutFromRoutine with active session THEN SessionAlreadyActive error returned`() =
        runTest {
            val routine =
                Routine(
                    id = routineId,
                    userId = userId,
                    name = "Push Day",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            // Mock DAO to return an active session
            val activeSession = createTestSessionEntity()
            coEvery { workoutSessionDao.getActiveSession(userId) } returns activeSession

            val result = repository.startWorkoutFromRoutine(userId, routineId, routine)

            result shouldBeInstanceOf WorkoutResult.Error::class.java
            (result as WorkoutResult.Error).exception shouldBe WorkoutException.SessionAlreadyActive
        }

    @Test
    fun `WHEN startAdHocWorkout is called THEN session is created without routine`() =
        runTest {
            val workoutName = "Quick Workout"

            // Mock DAO to return no active session
            coEvery { workoutSessionDao.getActiveSession(userId) } returns null
            // Mock insert to complete successfully
            coEvery { workoutSessionDao.insert(any()) } returns Unit

            val result = repository.startAdHocWorkout(userId, workoutName)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            // Verify a session ID was generated (UUID format)
            val returnedSessionId = (result as WorkoutResult.Success).data
            returnedSessionId.length shouldBeEqualTo 36 // UUID length with hyphens

            // Verify session was inserted into Room
            coVerify { workoutSessionDao.insert(any()) }
            // Verify WorkManager was called to schedule sync
            coVerify {
                workManager.enqueueUniqueWork(
                    any<String>(),
                    any<ExistingWorkPolicy>(),
                    any<OneTimeWorkRequest>(),
                )
            }
        }

    @Test
    fun `WHEN startAdHocWorkout with active session THEN SessionAlreadyActive error returned`() =
        runTest {
            val workoutName = "Quick Workout"

            // Mock DAO to return an active session
            val activeSession = createTestSessionEntity()
            coEvery { workoutSessionDao.getActiveSession(userId) } returns activeSession

            val result = repository.startAdHocWorkout(userId, workoutName)

            result shouldBeInstanceOf WorkoutResult.Error::class.java
            (result as WorkoutResult.Error).exception shouldBe WorkoutException.SessionAlreadyActive
        }

    @Test
    fun `WHEN updateSession is called THEN session is updated in Firestore`() =
        runTest {
            val session =
                WorkoutSession(
                    id = sessionId,
                    userId = userId,
                    routineId = routineId,
                    routineName = "Push Day",
                    status = WorkoutStatus.IN_PROGRESS,
                    exercises = emptyList(),
                    startTime = 1234567890L,
                    endTime = null,
                    totalDurationSeconds = null,
                    notes = null,
                    createdAt = 1234567890L,
                    updatedAt = 1234567890L,
                )

            // Mock DAO upsert
            coEvery { workoutSessionDao.upsert(any()) } returns Unit

            val result = repository.updateSession(session)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            (result as WorkoutResult.Success).data shouldBe Unit

            // Verify upsert was called
            coVerify { workoutSessionDao.upsert(any()) }
        }

    @Test
    fun `WHEN completeSession is called THEN session status is updated to COMPLETED`() =
        runTest {
            val sessionEntity = createTestSessionEntity()

            // Mock DAO to return session
            coEvery { workoutSessionDao.getById(sessionId) } returns sessionEntity
            // Mock updateStatus
            coEvery { workoutSessionDao.updateStatus(any(), any(), any(), any(), any()) } returns Unit

            val result = repository.completeSession(userId, sessionId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            (result as WorkoutResult.Success).data shouldBe Unit

            // Verify updateStatus was called
            coVerify {
                workoutSessionDao.updateStatus(
                    sessionId = sessionId,
                    status = WorkoutStatus.COMPLETED,
                    endTime = any(),
                    durationSeconds = any(),
                    updatedAt = any(),
                )
            }
        }

    @Test
    fun `WHEN completeSession with non-existent session THEN SessionNotFound error returned`() =
        runTest {
            // Mock DAO to return null (session not found)
            coEvery { workoutSessionDao.getById(sessionId) } returns null

            val result = repository.completeSession(userId, sessionId)

            result shouldBeInstanceOf WorkoutResult.Error::class.java
            (result as WorkoutResult.Error).exception shouldBe WorkoutException.SessionNotFound
        }

    @Test
    fun `WHEN abandonSession is called THEN session status is updated to ABANDONED`() =
        runTest {
            val sessionEntity = createTestSessionEntity()

            // Mock DAO to return session
            coEvery { workoutSessionDao.getById(sessionId) } returns sessionEntity
            // Mock updateStatus
            coEvery { workoutSessionDao.updateStatus(any(), any(), any(), any(), any()) } returns Unit

            val result = repository.abandonSession(userId, sessionId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            (result as WorkoutResult.Success).data shouldBe Unit

            // Verify updateStatus was called with ABANDONED
            coVerify {
                workoutSessionDao.updateStatus(
                    sessionId = sessionId,
                    status = WorkoutStatus.ABANDONED,
                    endTime = any(),
                    durationSeconds = any(),
                    updatedAt = any(),
                )
            }
        }

    @Test
    fun `WHEN abandonSession with non-existent session THEN SessionNotFound error returned`() =
        runTest {
            // Mock DAO to return null (session not found)
            coEvery { workoutSessionDao.getById(sessionId) } returns null

            val result = repository.abandonSession(userId, sessionId)

            result shouldBeInstanceOf WorkoutResult.Error::class.java
            (result as WorkoutResult.Error).exception shouldBe WorkoutException.SessionNotFound
        }

    @Test
    fun `WHEN getUnsyncedSessionCount is called THEN returns count from DAO`() =
        runTest {
            val expectedCount = 3

            coEvery { workoutSessionDao.getUnsyncedSessionCount(userId) } returns expectedCount

            val result = repository.getUnsyncedSessionCount(userId)

            result shouldBeEqualTo expectedCount
            coVerify { workoutSessionDao.getUnsyncedSessionCount(userId) }
        }

    @Test
    fun `WHEN getUnsyncedSessionCount throws exception THEN returns 0`() =
        runTest {
            coEvery { workoutSessionDao.getUnsyncedSessionCount(userId) } throws Exception("Database error")

            val result = repository.getUnsyncedSessionCount(userId)

            result shouldBeEqualTo 0
        }

    @Test
    fun `WHEN startWorkoutFromRoutine is called THEN WorkManager is scheduled for sync`() =
        runTest {
            val exercise = createTestExercise()
            val routine = createTestRoutine()

            coEvery { workoutSessionDao.getActiveSession(userId) } returns null
            coEvery { workoutSessionDao.insert(any()) } returns Unit
            coEvery {
                exerciseRepository.getExerciseById("ex1", userId)
            } returns ExerciseResult.Success(exercise)

            val result = repository.startWorkoutFromRoutine(userId, routineId, routine)

            result shouldBeInstanceOf WorkoutResult.Success::class.java

            // Verify WorkManager was called to schedule sync
            coVerify {
                workManager.enqueueUniqueWork(
                    any<String>(),
                    any<ExistingWorkPolicy>(),
                    any<OneTimeWorkRequest>(),
                )
            }
        }

    @Test
    fun `WHEN updateSession is called THEN WorkManager is scheduled for sync`() =
        runTest {
            val session =
                WorkoutSession(
                    id = sessionId,
                    userId = userId,
                    routineId = routineId,
                    routineName = "Push Day",
                    status = WorkoutStatus.IN_PROGRESS,
                    exercises = emptyList(),
                    startTime = 1234567890L,
                    endTime = null,
                    totalDurationSeconds = null,
                    notes = null,
                    createdAt = 1234567890L,
                    updatedAt = 1234567890L,
                )

            coEvery { workoutSessionDao.upsert(any()) } returns Unit

            val result = repository.updateSession(session)

            result shouldBeInstanceOf WorkoutResult.Success::class.java

            // Verify WorkManager was called to schedule sync
            coVerify {
                workManager.enqueueUniqueWork(
                    any<String>(),
                    any<ExistingWorkPolicy>(),
                    any<OneTimeWorkRequest>(),
                )
            }
        }

    @Test
    fun `WHEN completeSession is called THEN WorkManager is scheduled for sync`() =
        runTest {
            val sessionEntity = createTestSessionEntity()

            coEvery { workoutSessionDao.getById(sessionId) } returns sessionEntity
            coEvery { workoutSessionDao.updateStatus(any(), any(), any(), any(), any()) } returns Unit

            val result = repository.completeSession(userId, sessionId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java

            // Verify WorkManager was called to schedule sync
            coVerify {
                workManager.enqueueUniqueWork(
                    any<String>(),
                    any<ExistingWorkPolicy>(),
                    any<OneTimeWorkRequest>(),
                )
            }
        }

    @Test
    fun `WHEN abandonSession is called THEN WorkManager is scheduled for sync`() =
        runTest {
            val sessionEntity = createTestSessionEntity()

            coEvery { workoutSessionDao.getById(sessionId) } returns sessionEntity
            coEvery { workoutSessionDao.updateStatus(any(), any(), any(), any(), any()) } returns Unit

            val result = repository.abandonSession(userId, sessionId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java

            // Verify WorkManager was called to schedule sync
            coVerify {
                workManager.enqueueUniqueWork(
                    any<String>(),
                    any<ExistingWorkPolicy>(),
                    any<OneTimeWorkRequest>(),
                )
            }
        }

    // syncFromFirestore tests

    private fun mockFirestoreQuery(documents: List<DocumentSnapshot>) {
        val querySnapshot = mockk<QuerySnapshot>()
        every { querySnapshot.documents } returns documents

        val collectionRef = mockk<CollectionReference>()
        val documentRef = mockk<DocumentReference>()
        val query = mockk<Query>()

        every { firestore.collection("users") } returns collectionRef
        every { collectionRef.document(userId) } returns documentRef
        every { documentRef.collection("workoutSessions") } returns collectionRef
        every { collectionRef.whereGreaterThan(any<String>(), any()) } returns query
        every { query.get() } returns Tasks.forResult(querySnapshot)
    }

    private fun createMockDocumentSnapshot(
        id: String,
        sessionData: Map<String, Any?>,
    ): DocumentSnapshot {
        val doc = mockk<DocumentSnapshot>()
        every { doc.id } returns id
        every { doc.toObject(any<Class<*>>()) } returns null

        // For WorkoutSessionDocument conversion
        every { doc.toObject(com.ruma.repnote.core.data.model.WorkoutSessionDocument::class.java) } returns
            com.ruma.repnote.core.data.model.WorkoutSessionDocument(
                id = sessionData["id"] as String,
                userId = sessionData["userId"] as String,
                routineId = sessionData["routineId"] as? String,
                routineName = sessionData["routineName"] as String,
                status = sessionData["status"] as String,
                exercises = emptyList(),
                startTime = sessionData["startTime"] as Long,
                endTime = sessionData["endTime"] as? Long,
                totalDurationSeconds = sessionData["totalDurationSeconds"] as? Long,
                notes = sessionData["notes"] as? String,
                createdAt = sessionData["createdAt"] as Long,
                updatedAt = sessionData["updatedAt"] as Long,
            )
        return doc
    }

    @Test
    fun `WHEN syncFromFirestore with empty Firestore THEN returns Success`() =
        runTest {
            mockFirestoreQuery(emptyList())

            val result = repository.syncFromFirestore(userId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
        }

    @Test
    fun `WHEN syncFromFirestore with new session THEN inserts into Room`() =
        runTest {
            val sessionData =
                mapOf(
                    "id" to "remote-session-1",
                    "userId" to userId,
                    "routineId" to routineId,
                    "routineName" to "Push Day",
                    "status" to "COMPLETED",
                    "startTime" to 1234567890L,
                    "endTime" to 1234568890L,
                    "totalDurationSeconds" to 1000L,
                    "notes" to null,
                    "createdAt" to 1234567890L,
                    "updatedAt" to 1234567890L,
                )

            val doc = createMockDocumentSnapshot("remote-session-1", sessionData)
            mockFirestoreQuery(listOf(doc))

            // No local session exists
            coEvery { workoutSessionDao.getById("remote-session-1") } returns null
            coEvery { workoutSessionDao.insert(any()) } returns Unit

            val result = repository.syncFromFirestore(userId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            coVerify { workoutSessionDao.insert(any()) }
        }

    @Test
    fun `WHEN syncFromFirestore with newer remote session THEN updates Room`() =
        runTest {
            val remoteUpdatedAt = 2000000000L
            val localUpdatedAt = 1000000000L

            val sessionData =
                mapOf(
                    "id" to sessionId,
                    "userId" to userId,
                    "routineId" to routineId,
                    "routineName" to "Push Day",
                    "status" to "COMPLETED",
                    "startTime" to 1234567890L,
                    "endTime" to 1234568890L,
                    "totalDurationSeconds" to 1000L,
                    "notes" to null,
                    "createdAt" to 1234567890L,
                    "updatedAt" to remoteUpdatedAt,
                )

            val doc = createMockDocumentSnapshot(sessionId, sessionData)
            mockFirestoreQuery(listOf(doc))

            // Local session exists but is older and SYNCED
            val localSession =
                createTestSessionEntity().copy(
                    updatedAt = localUpdatedAt,
                    syncStatus = SyncStatus.SYNCED,
                )
            coEvery { workoutSessionDao.getById(sessionId) } returns localSession
            coEvery { workoutSessionDao.upsert(any()) } returns Unit

            val result = repository.syncFromFirestore(userId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            coVerify { workoutSessionDao.upsert(any()) }
        }

    @Test
    fun `WHEN syncFromFirestore with local pending changes THEN skips update`() =
        runTest {
            val sessionData =
                mapOf(
                    "id" to sessionId,
                    "userId" to userId,
                    "routineId" to routineId,
                    "routineName" to "Push Day",
                    "status" to "COMPLETED",
                    "startTime" to 1234567890L,
                    "endTime" to 1234568890L,
                    "totalDurationSeconds" to 1000L,
                    "notes" to null,
                    "createdAt" to 1234567890L,
                    "updatedAt" to 2000000000L,
                )

            val doc = createMockDocumentSnapshot(sessionId, sessionData)
            mockFirestoreQuery(listOf(doc))

            // Local session has PENDING sync status (should not be overwritten)
            val localSession =
                createTestSessionEntity().copy(
                    syncStatus = SyncStatus.PENDING,
                )
            coEvery { workoutSessionDao.getById(sessionId) } returns localSession

            val result = repository.syncFromFirestore(userId)

            result shouldBeInstanceOf WorkoutResult.Success::class.java
            // Should NOT call insert or upsert
            coVerify(exactly = 0) { workoutSessionDao.insert(any()) }
            coVerify(exactly = 0) { workoutSessionDao.upsert(any()) }
        }

    @Test
    fun `WHEN syncFromFirestore fails THEN returns Error`() =
        runTest {
            val collectionRef = mockk<CollectionReference>()
            val documentRef = mockk<DocumentReference>()

            every { firestore.collection("users") } returns collectionRef
            every { collectionRef.document(userId) } returns documentRef
            every { documentRef.collection("workoutSessions") } returns collectionRef
            every { collectionRef.get() } returns
                Tasks.forException(
                    FirebaseFirestoreException(
                        "Network error",
                        FirebaseFirestoreException.Code.UNAVAILABLE,
                    ),
                )

            val result = repository.syncFromFirestore(userId)

            result shouldBeInstanceOf WorkoutResult.Error::class.java
        }
}
