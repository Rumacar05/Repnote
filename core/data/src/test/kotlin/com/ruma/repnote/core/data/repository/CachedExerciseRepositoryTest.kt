package com.ruma.repnote.core.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.ruma.repnote.core.data.model.ExerciseDocument
import com.ruma.repnote.core.database.dao.ExerciseDao
import com.ruma.repnote.core.database.entity.ExerciseEntity
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.service.ImageStorageService
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
 * Unit tests for CachedExerciseRepository.
 *
 * Note: These tests focus on error handling and business logic.
 * Integration tests with real Firebase instances are recommended for
 * testing complex Flow behaviors and Firebase Task operations.
 */
class CachedExerciseRepositoryTest {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var imageStorageService: ImageStorageService
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var repository: CachedExerciseRepository

    private val userId = "user-123"
    private val exerciseId = "exercise-123"
    private val language = "en"

    @BeforeEach
    fun setup() {
        firestore = mockk(relaxed = true)
        imageStorageService = mockk(relaxed = true)
        exerciseDao = mockk(relaxed = true)
        repository = CachedExerciseRepository(firestore, imageStorageService, exerciseDao)
    }

    @Test
    fun `WHEN getExerciseById is called with cached exercise THEN cached data is returned`() =
        runTest {
            val cachedEntity =
                ExerciseEntity(
                    id = exerciseId,
                    name = "Squat",
                    description = "Leg exercise",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.QUADS,
                    secondaryMuscleGroups = listOf(MuscleGroup.GLUTES),
                    isGlobal = true,
                    createdBy = null,
                    language = language,
                )

            coEvery { exerciseDao.getExerciseById(exerciseId, language) } returns cachedEntity

            val result = repository.getExerciseById(exerciseId, userId, language)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data.name shouldBeEqualTo "Squat"
        }

    @Test
    fun `WHEN getExerciseById is called with non-existent exercise THEN ExerciseNotFound is returned`() =
        runTest {
            coEvery { exerciseDao.getExerciseById(exerciseId, language) } returns null

            val exercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val globalTask = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("exercises") } returns exercisesCollection
            every { exercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.get() } returns globalTask
            every { globalTask.isComplete } returns true
            every { globalTask.exception } returns null
            every { globalTask.isCanceled } returns false
            every { globalTask.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns false

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val customExerciseDoc = mockk<DocumentReference>()
            val customDocumentSnapshot = mockk<DocumentSnapshot>()
            val customTask = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns customExerciseDoc
            every { customExerciseDoc.get() } returns customTask
            every { customTask.isComplete } returns true
            every { customTask.exception } returns null
            every { customTask.isCanceled } returns false
            every { customTask.result } returns customDocumentSnapshot
            every { customDocumentSnapshot.exists() } returns false

            val result = repository.getExerciseById(exerciseId, userId, language)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBe ExerciseException.ExerciseNotFound
        }

    @Test
    fun `WHEN createCustomExercise is called THEN exercise is created in Firestore and cached`() =
        runTest {
            val exercise =
                Exercise(
                    id = exerciseId,
                    name = "Custom Exercise",
                    description = "My custom exercise",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val translationsCollection = mockk<CollectionReference>()
            val translationDocEN = mockk<DocumentReference>()
            val translationDocES = mockk<DocumentReference>()
            val setTask = mockk<Task<Void>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.set(any()) } returns setTask
            every { setTask.isComplete } returns true
            every { setTask.exception } returns null
            every { setTask.isCanceled } returns false
            every { setTask.result } returns null
            every { exerciseDoc.collection("translations") } returns translationsCollection
            every { translationsCollection.document("en") } returns translationDocEN
            every { translationsCollection.document("es") } returns translationDocES
            every { translationDocEN.set(any()) } returns setTask
            every { translationDocES.set(any()) } returns setTask

            coEvery { exerciseDao.insertExercise(any()) } returns Unit

            val result = repository.createCustomExercise(userId, exercise)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data.name shouldBeEqualTo "Custom Exercise"
            coVerify(exactly = 2) { exerciseDao.insertExercise(any()) }
        }

    @Test
    fun `WHEN createCustomExercise is called and Firestore throws exception THEN error is returned`() =
        runTest {
            val exercise =
                Exercise(
                    id = exerciseId,
                    name = "Custom Exercise",
                    description = "My custom exercise",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val exception =
                mockk<FirebaseFirestoreException> {
                    every { code } returns FirebaseFirestoreException.Code.PERMISSION_DENIED
                }

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.set(any()) } throws exception

            val result = repository.createCustomExercise(userId, exercise)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBe ExerciseException.UnauthorizedAccess
        }

    @Test
    fun `WHEN updateCustomExercise is called with existing exercise THEN exercise is updated and cached`() =
        runTest {
            val exercise =
                Exercise(
                    id = exerciseId,
                    name = "Updated Exercise",
                    description = "Updated description",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val translationsCollection = mockk<CollectionReference>()
            val translationDocEN = mockk<DocumentReference>()
            val translationDocES = mockk<DocumentReference>()
            val getTask = mockk<Task<DocumentSnapshot>>()
            val setTask = mockk<Task<Void>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.get() } returns getTask
            every { getTask.isComplete } returns true
            every { getTask.exception } returns null
            every { getTask.isCanceled } returns false
            every { getTask.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns true
            every { documentSnapshot.toObject(ExerciseDocument::class.java) } returns
                ExerciseDocument(
                    id = exerciseId,
                    imageUrl = null,
                    primaryMuscleGroup = "CHEST",
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )
            every { exerciseDoc.set(any()) } returns setTask
            every { setTask.isComplete } returns true
            every { setTask.exception } returns null
            every { setTask.isCanceled } returns false
            every { setTask.result } returns null
            every { exerciseDoc.collection("translations") } returns translationsCollection
            every { translationsCollection.document("en") } returns translationDocEN
            every { translationsCollection.document("es") } returns translationDocES
            every { translationDocEN.set(any()) } returns setTask
            every { translationDocES.set(any()) } returns setTask

            coEvery { exerciseDao.insertExercise(any()) } returns Unit

            val result = repository.updateCustomExercise(userId, exercise)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data.name shouldBeEqualTo "Updated Exercise"
            coVerify(exactly = 2) { exerciseDao.insertExercise(any()) }
        }

    @Test
    fun `WHEN updateCustomExercise is called with non-existent exercise THEN ExerciseNotFound is returned`() =
        runTest {
            val exercise =
                Exercise(
                    id = exerciseId,
                    name = "Updated Exercise",
                    description = "Updated description",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val task = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.get() } returns task
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns false

            val result = repository.updateCustomExercise(userId, exercise)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBe ExerciseException.ExerciseNotFound
        }

    @Test
    fun `WHEN updateCustomExercise is called with unauthorized user THEN UnauthorizedAccess is returned`() =
        runTest {
            val exercise =
                Exercise(
                    id = exerciseId,
                    name = "Updated Exercise",
                    description = "Updated description",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )

            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val task = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.get() } returns task
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns true
            every { documentSnapshot.toObject(ExerciseDocument::class.java) } returns
                ExerciseDocument(
                    id = exerciseId,
                    imageUrl = null,
                    primaryMuscleGroup = "CHEST",
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = "different-user",
                )

            val result = repository.updateCustomExercise(userId, exercise)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBe ExerciseException.UnauthorizedAccess
        }

    @Test
    fun `WHEN deleteCustomExercise is called with existing exercise THEN exercise is deleted from Firestore and cache`() =
        runTest {
            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val translationsCollection = mockk<CollectionReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val translationsSnapshot = mockk<QuerySnapshot>()
            val getTask = mockk<Task<DocumentSnapshot>>()
            val translationsTask = mockk<Task<QuerySnapshot>>()
            val deleteTask = mockk<Task<Void>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.get() } returns getTask
            every { getTask.isComplete } returns true
            every { getTask.exception } returns null
            every { getTask.isCanceled } returns false
            every { getTask.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns true
            every { documentSnapshot.toObject(ExerciseDocument::class.java) } returns
                ExerciseDocument(
                    id = exerciseId,
                    imageUrl = null,
                    primaryMuscleGroup = "CHEST",
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                )
            every { exerciseDoc.collection("translations") } returns translationsCollection
            every { translationsCollection.get() } returns translationsTask
            every { translationsTask.isComplete } returns true
            every { translationsTask.exception } returns null
            every { translationsTask.isCanceled } returns false
            every { translationsTask.result } returns translationsSnapshot
            every { translationsSnapshot.documents } returns emptyList()
            every { exerciseDoc.delete() } returns deleteTask
            every { deleteTask.isComplete } returns true
            every { deleteTask.exception } returns null
            every { deleteTask.isCanceled } returns false
            every { deleteTask.result } returns null

            coEvery { exerciseDao.deleteExercise(exerciseId) } returns Unit

            val result = repository.deleteCustomExercise(userId, exerciseId)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data shouldBe Unit
            coVerify { exerciseDao.deleteExercise(exerciseId) }
        }

    @Test
    fun `WHEN deleteCustomExercise is called with non-existent exercise THEN ExerciseNotFound is returned`() =
        runTest {
            val usersCollection = mockk<CollectionReference>()
            val userDoc = mockk<DocumentReference>()
            val customExercisesCollection = mockk<CollectionReference>()
            val exerciseDoc = mockk<DocumentReference>()
            val documentSnapshot = mockk<DocumentSnapshot>()
            val task = mockk<Task<DocumentSnapshot>>()

            every { firestore.collection("users") } returns usersCollection
            every { usersCollection.document(userId) } returns userDoc
            every { userDoc.collection("customExercises") } returns customExercisesCollection
            every { customExercisesCollection.document(exerciseId) } returns exerciseDoc
            every { exerciseDoc.get() } returns task
            every { task.isComplete } returns true
            every { task.exception } returns null
            every { task.isCanceled } returns false
            every { task.result } returns documentSnapshot
            every { documentSnapshot.exists() } returns false

            val result = repository.deleteCustomExercise(userId, exerciseId)

            result shouldBeInstanceOf ExerciseResult.Error::class.java
            (result as ExerciseResult.Error).exception shouldBe ExerciseException.ExerciseNotFound
        }

    @Test
    fun `WHEN uploadExerciseImage is called THEN imageStorageService is invoked`() =
        runTest {
            val imageBytes = ByteArray(100)
            val imageUrl = "https://example.com/image.jpg"

            coEvery {
                imageStorageService.uploadImage(
                    exerciseId,
                    imageBytes,
                )
            } returns ExerciseResult.Success(imageUrl)

            val result = repository.uploadExerciseImage(exerciseId, imageBytes)

            result shouldBeInstanceOf ExerciseResult.Success::class.java
            (result as ExerciseResult.Success).data shouldBeEqualTo imageUrl
            coVerify { imageStorageService.uploadImage(exerciseId, imageBytes) }
        }
}
