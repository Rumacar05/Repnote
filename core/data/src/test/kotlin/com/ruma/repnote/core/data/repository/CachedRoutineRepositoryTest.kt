package com.ruma.repnote.core.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.database.dao.RoutineDao
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineResult
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
 * Unit tests for CachedRoutineRepository.
 *
 * Note: These tests focus on error handling and business logic.
 * Integration tests with real Firebase instances are recommended for
 * testing complex Flow behaviors and Firebase Task operations.
 */
class CachedRoutineRepositoryTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var routineDao: RoutineDao
    private lateinit var repository: CachedRoutineRepository

    private val userId = "user-123"
    private val routineId = "routine-123"

    @BeforeEach
    fun setup() {
        firestore = mockk(relaxed = true)
        routineDao = mockk(relaxed = true)
        repository = CachedRoutineRepository(firestore, routineDao)
    }

    @Test
    fun `WHEN createRoutine is called with valid routine THEN routine is created and cached`() =
        runTest {
            val routine =
                Routine(
                    id = "",
                    userId = userId,
                    name = "New Routine",
                    description = "A new routine",
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567890L,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val task = mockk<Task<Void>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document() } returns routineDoc
            every { routineDoc.id } returns "generated-id"
            every { routineDoc.set(any()) } returns task
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns null

            coEvery { routineDao.insertRoutine(any()) } returns Unit

            val result = repository.createRoutine(routine)

            result shouldBeInstanceOf RoutineResult.Success::class.java
            (result as RoutineResult.Success).data shouldBeEqualTo "generated-id"
            coVerify { routineDao.insertRoutine(any()) }
        }

    @Test
    fun `WHEN createRoutine is called and Firestore throws exception THEN error is returned`() =
        runTest {
            val routine =
                Routine(
                    id = "",
                    userId = userId,
                    name = "New Routine",
                    description = "A new routine",
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567890L,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val exception =
                mockk<FirebaseFirestoreException> {
                    every { code } returns FirebaseFirestoreException.Code.PERMISSION_DENIED
                }

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document() } returns routineDoc
            every { routineDoc.id } returns "generated-id"
            every { routineDoc.set(any()) } throws exception

            val result = repository.createRoutine(routine)

            result shouldBeInstanceOf RoutineResult.Error::class.java
            (result as RoutineResult.Error).exception shouldBe RoutineException.UnauthorizedAccess
        }

    @Test
    fun `WHEN updateRoutine is called with existing routine THEN routine is updated and cached`() =
        runTest {
            val routine =
                Routine(
                    id = routineId,
                    userId = userId,
                    name = "Updated Routine",
                    description = "Updated description",
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567950L,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val getTask = mockk<Task<DocumentSnapshot>>()
            val setTask = mockk<Task<Void>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document(routineId) } returns routineDoc
            every { routineDoc.get() } returns getTask
            every { getTask.isComplete } returns true
            every { getTask.exception } returns null
            every { getTask.isCanceled } returns false
            every { getTask.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns true
            every { routineDoc.set(any()) } returns setTask
            every { setTask.isComplete } returns true
            every { setTask.exception } returns null
            every { setTask.isCanceled } returns false
            every { setTask.result } returns null

            coEvery { routineDao.insertRoutine(any()) } returns Unit

            val result = repository.updateRoutine(routine)

            result shouldBeInstanceOf RoutineResult.Success::class.java
            (result as RoutineResult.Success).data shouldBe Unit
            coVerify { routineDao.insertRoutine(any()) }
        }

    @Test
    fun `WHEN updateRoutine is called with non-existent routine THEN RoutineNotFound error is returned`() =
        runTest {
            val routine =
                Routine(
                    id = routineId,
                    userId = userId,
                    name = "Updated Routine",
                    description = "Updated description",
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567950L,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val task = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document(routineId) } returns routineDoc
            every { routineDoc.get() } returns task
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns false

            val result = repository.updateRoutine(routine)

            result shouldBeInstanceOf RoutineResult.Error::class.java
            (result as RoutineResult.Error).exception shouldBe RoutineException.RoutineNotFound
        }

    @Test
    fun `WHEN deleteRoutine is called with existing routine THEN routine is deleted from Firestore and cache`() =
        runTest {
            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val getTask = mockk<Task<DocumentSnapshot>>()
            val deleteTask = mockk<Task<Void>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document(routineId) } returns routineDoc
            every { routineDoc.get() } returns getTask
            every { getTask.isComplete } returns true
            every { getTask.exception } returns null
            every { getTask.isCanceled } returns false
            every { getTask.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns true
            every { routineDoc.delete() } returns deleteTask
            every { deleteTask.isComplete } returns true
            every { deleteTask.exception } returns null
            every { deleteTask.isCanceled } returns false
            every { deleteTask.result } returns null

            coEvery { routineDao.deleteRoutine(routineId) } returns Unit

            val result = repository.deleteRoutine(userId, routineId)

            result shouldBeInstanceOf RoutineResult.Success::class.java
            (result as RoutineResult.Success).data shouldBe Unit
            coVerify { routineDao.deleteRoutine(routineId) }
        }

    @Test
    fun `WHEN deleteRoutine is called with non-existent routine THEN RoutineNotFound error is returned`() =
        runTest {
            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val task = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document(routineId) } returns routineDoc
            every { routineDoc.get() } returns task
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns false

            val result = repository.deleteRoutine(userId, routineId)

            result shouldBeInstanceOf RoutineResult.Error::class.java
            (result as RoutineResult.Error).exception shouldBe RoutineException.RoutineNotFound
        }

    @Test
    fun `WHEN deleteRoutine is called and Firestore throws exception THEN error is returned`() =
        runTest {
            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val routinesCollection = mockk<CollectionReference>()
            val routineDoc = mockk<DocumentReference>()
            val exception =
                mockk<FirebaseFirestoreException> {
                    every { code } returns FirebaseFirestoreException.Code.UNAVAILABLE
                }

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("routines") } returns routinesCollection
            every { routinesCollection.document(routineId) } returns routineDoc
            every { routineDoc.get() } throws exception

            val result = repository.deleteRoutine(userId, routineId)

            result shouldBeInstanceOf RoutineResult.Error::class.java
            (result as RoutineResult.Error).exception shouldBe RoutineException.NetworkError
        }
}
