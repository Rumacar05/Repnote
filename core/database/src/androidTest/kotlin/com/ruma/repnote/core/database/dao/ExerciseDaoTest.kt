package com.ruma.repnote.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ruma.repnote.core.database.RepnoteDatabase
import com.ruma.repnote.core.database.entity.ExerciseEntity
import com.ruma.repnote.core.domain.model.MuscleGroup
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumentation tests for ExerciseDao.
 * These tests run on an Android device or emulator and use a real Room database.
 */
@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {
    private lateinit var database: RepnoteDatabase
    private lateinit var exerciseDao: ExerciseDao

    private val userId = "test-user-123"
    private val language = "en"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, RepnoteDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        exerciseDao = database.exerciseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertExercise_andRetrieveById_returnsCorrectExercise() =
        runTest {
            val exercise =
                ExerciseEntity(
                    id = "ex-123",
                    name = "Bench Press",
                    description = "Chest exercise",
                    imageUrl = "https://example.com/image.jpg",
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                    isGlobal = true,
                    createdBy = null,
                    language = language,
                )

            exerciseDao.insertExercise(exercise)
            val retrieved = exerciseDao.getExerciseById("ex-123", language)

            assertNotNull(retrieved)
            retrieved!!.let {
                assertEquals("ex-123", it.id)
                assertEquals("Bench Press", it.name)
                assertEquals("Chest exercise", it.description)
                assertEquals(MuscleGroup.CHEST, it.primaryMuscleGroup)
                assertEquals(2, it.secondaryMuscleGroups.size)
                assertTrue(it.isGlobal)
            }
        }

    @Test
    fun getGlobalExercises_returnsOnlyGlobalExercises_orderedByName() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Squat",
                        description = "Leg exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.QUADS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "Bench Press",
                        description = "Chest exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "Custom Exercise",
                        description = "My exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = userId,
                        language = language,
                    ),
                )

            exerciseDao.insertExercises(exercises)
            val globalExercises = exerciseDao.getGlobalExercises(language).first()

            assertEquals(2, globalExercises.size)
            assertEquals("Bench Press", globalExercises[0].name)
            assertEquals("Squat", globalExercises[1].name)
        }

    @Test
    fun getUserCustomExercises_returnsOnlyUserCustomExercises() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Global Exercise",
                        description = "Global",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "User Custom 1",
                        description = "My exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = userId,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "User Custom 2",
                        description = "Another exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BICEPS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = userId,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-4",
                        name = "Other User Custom",
                        description = "Other user's exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.TRICEPS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = "other-user",
                        language = language,
                    ),
                )

            exerciseDao.insertExercises(exercises)
            val customExercises = exerciseDao.getUserCustomExercises(userId, language).first()

            assertEquals(2, customExercises.size)
            assertTrue(customExercises.all { !it.isGlobal && it.createdBy == userId })
        }

    @Test
    fun getAllExercisesForUser_returnsGlobalAndUserCustomExercises() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Global Exercise",
                        description = "Global",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "User Custom",
                        description = "My exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = userId,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "Other User Custom",
                        description = "Other's exercise",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.TRICEPS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = "other-user",
                        language = language,
                    ),
                )

            exerciseDao.insertExercises(exercises)
            val allExercises = exerciseDao.getAllExercisesForUser(userId, language)

            assertEquals(2, allExercises.size)
            assertTrue(allExercises.any { it.id == "ex-1" })
            assertTrue(allExercises.any { it.id == "ex-2" })
        }

    @Test
    fun getAllExercisesForUserPaginated_returnsPaginatedResults() =
        runTest {
            val exercises =
                (1..10).map { i ->
                    ExerciseEntity(
                        id = "ex-$i",
                        name = "Exercise $i",
                        description = "Description $i",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    )
                }

            exerciseDao.insertExercises(exercises)

            val page1 =
                exerciseDao.getAllExercisesForUserPaginated(userId, language, limit = 3, offset = 0)
            val page2 =
                exerciseDao.getAllExercisesForUserPaginated(userId, language, limit = 3, offset = 3)

            assertEquals(3, page1.size)
            assertEquals(3, page2.size)
            assertEquals("Exercise 1", page1[0].name)
            assertEquals("Exercise 4", page2[0].name)
        }

    @Test
    fun getExercisesByMuscleGroup_returnsExercisesWithPrimaryOrSecondaryMatch() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Bench Press",
                        description = "Primary chest",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = listOf(MuscleGroup.TRICEPS),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "Dips",
                        description = "Primary triceps, secondary chest",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.TRICEPS,
                        secondaryMuscleGroups = listOf(MuscleGroup.CHEST),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "Squat",
                        description = "Legs only",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.QUADS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                )

            exerciseDao.insertExercises(exercises)
            val chestExercises =
                exerciseDao.getExercisesByMuscleGroup(userId, MuscleGroup.CHEST, language)

            assertEquals(2, chestExercises.size)
            assertTrue(chestExercises.any { it.id == "ex-1" })
            assertTrue(chestExercises.any { it.id == "ex-2" })
        }

    @Test
    fun getExercisesByIds_returnsOnlyRequestedExercises() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Exercise 1",
                        description = "Description 1",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "Exercise 2",
                        description = "Description 2",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "Exercise 3",
                        description = "Description 3",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BICEPS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                )

            exerciseDao.insertExercises(exercises)
            val selected = exerciseDao.getExercisesByIds(listOf("ex-1", "ex-3"), language)

            assertEquals(2, selected.size)
            assertTrue(selected.any { it.id == "ex-1" })
            assertTrue(selected.any { it.id == "ex-3" })
        }

    @Test
    fun deleteExercise_removesExerciseFromDatabase() =
        runTest {
            val exercise =
                ExerciseEntity(
                    id = "ex-123",
                    name = "To Delete",
                    description = "Will be deleted",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = false,
                    createdBy = userId,
                    language = language,
                )

            exerciseDao.insertExercise(exercise)
            val beforeDelete = exerciseDao.getExerciseById("ex-123", language)
            assertNotNull(beforeDelete)

            exerciseDao.deleteExercise("ex-123")
            val afterDelete = exerciseDao.getExerciseById("ex-123", language)
            assertNull(afterDelete)
        }

    @Test
    fun deleteAllExercises_removesAllExercises() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Exercise 1",
                        description = "Description 1",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "Exercise 2",
                        description = "Description 2",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = false,
                        createdBy = userId,
                        language = language,
                    ),
                )

            exerciseDao.insertExercises(exercises)
            exerciseDao.deleteAllExercises()

            val count = exerciseDao.getExerciseCount(language)
            assertEquals(0, count)
        }

    @Test
    fun deleteOldCache_removesOnlyOldExercises() =
        runTest {
            val currentTime = System.currentTimeMillis()
            val oldTime = currentTime - 100000

            val oldExercise =
                ExerciseEntity(
                    id = "old-ex",
                    name = "Old Exercise",
                    description = "Old",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = true,
                    createdBy = null,
                    language = language,
                    cachedAt = oldTime,
                )

            val newExercise =
                ExerciseEntity(
                    id = "new-ex",
                    name = "New Exercise",
                    description = "New",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.BACK,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = true,
                    createdBy = null,
                    language = language,
                    cachedAt = currentTime,
                )

            exerciseDao.insertExercises(listOf(oldExercise, newExercise))
            exerciseDao.deleteOldCache(currentTime - 50000)

            val remaining = exerciseDao.getGlobalExercises(language).first()
            assertEquals(1, remaining.size)
            assertEquals("New Exercise", remaining[0].name)
        }

    @Test
    fun getExerciseCount_returnsCorrectCount() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Exercise 1",
                        description = "Description 1",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "Exercise 2",
                        description = "Description 2",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = language,
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "Exercise 3",
                        description = "Description 3",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BICEPS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                        language = "es",
                    ),
                )

            exerciseDao.insertExercises(exercises)
            val count = exerciseDao.getExerciseCount(language)

            assertEquals(2, count)
        }

    @Test
    fun upsert_replacesExistingExercise() =
        runTest {
            val original =
                ExerciseEntity(
                    id = "ex-123",
                    name = "Original Name",
                    description = "Original description",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = true,
                    createdBy = null,
                    language = language,
                )

            exerciseDao.insertExercise(original)

            val updated =
                ExerciseEntity(
                    id = "ex-123",
                    name = "Updated Name",
                    description = "Updated description",
                    imageUrl = "https://new-image.com",
                    primaryMuscleGroup = MuscleGroup.BACK,
                    secondaryMuscleGroups = listOf(MuscleGroup.BICEPS),
                    isGlobal = true,
                    createdBy = null,
                    language = language,
                )

            exerciseDao.insertExercise(updated)

            val result = exerciseDao.getExerciseById("ex-123", language)
            assertNotNull(result)
            result!!.let {
                assertEquals("Updated Name", it.name)
                assertEquals("Updated description", it.description)
                assertEquals("https://new-image.com", it.imageUrl)
                assertEquals(MuscleGroup.BACK, it.primaryMuscleGroup)
                assertEquals(1, it.secondaryMuscleGroups.size)
            }
        }
}
