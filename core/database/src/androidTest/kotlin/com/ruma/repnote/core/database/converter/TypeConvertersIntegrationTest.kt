package com.ruma.repnote.core.database.converter

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ruma.repnote.core.database.RepnoteDatabase
import com.ruma.repnote.core.database.dao.ExerciseDao
import com.ruma.repnote.core.database.dao.RoutineDao
import com.ruma.repnote.core.database.entity.ExerciseEntity
import com.ruma.repnote.core.database.entity.RoutineEntity
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.model.RoutineExercise
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration tests for TypeConverters.
 * These tests verify that type conversions work correctly when persisting
 * and retrieving data from the database.
 */
@RunWith(AndroidJUnit4::class)
class TypeConvertersIntegrationTest {
    private lateinit var database: RepnoteDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var routineDao: RoutineDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, RepnoteDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        exerciseDao = database.exerciseDao()
        routineDao = database.routineDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun muscleGroupList_persistsAndRetrievesCorrectly() =
        runTest {
            val muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
            val exercise =
                ExerciseEntity(
                    id = "ex-123",
                    name = "Bench Press",
                    description = "Chest exercise",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.CHEST,
                    secondaryMuscleGroups = muscleGroups,
                    isGlobal = true,
                    createdBy = null,
                    language = "en",
                )

            exerciseDao.insertExercise(exercise)
            val retrieved = exerciseDao.getExerciseById("ex-123", "en")

            assertNotNull(retrieved)
            assertEquals(3, retrieved!!.secondaryMuscleGroups.size)
            assertTrue(retrieved.secondaryMuscleGroups.contains(MuscleGroup.CHEST))
            assertTrue(retrieved.secondaryMuscleGroups.contains(MuscleGroup.TRICEPS))
            assertTrue(retrieved.secondaryMuscleGroups.contains(MuscleGroup.SHOULDERS))
        }

    @Test
    fun emptyMuscleGroupList_persistsAndRetrievesCorrectly() =
        runTest {
            val exercise =
                ExerciseEntity(
                    id = "ex-456",
                    name = "Squat",
                    description = "Leg exercise",
                    imageUrl = null,
                    primaryMuscleGroup = MuscleGroup.QUADS,
                    secondaryMuscleGroups = emptyList(),
                    isGlobal = true,
                    createdBy = null,
                    language = "en",
                )

            exerciseDao.insertExercise(exercise)
            val retrieved = exerciseDao.getExerciseById("ex-456", "en")

            assertNotNull(retrieved)
            assertTrue(retrieved!!.secondaryMuscleGroups.isEmpty())
        }

    @Test
    fun routineExerciseList_persistsAndRetrievesCorrectly() =
        runTest {
            val exercises =
                listOf(
                    RoutineExercise(
                        exerciseId = "ex1",
                        order = 0,
                        sets = 3,
                        reps = 10,
                        restSeconds = 60,
                        notes = "Warmup set",
                    ),
                    RoutineExercise(
                        exerciseId = "ex2",
                        order = 1,
                        sets = 4,
                        reps = 12,
                        restSeconds = 90,
                        notes = null,
                    ),
                    RoutineExercise(
                        exerciseId = "ex3",
                        order = 2,
                        sets = 5,
                        reps = null,
                        restSeconds = null,
                        notes = "AMRAP",
                    ),
                )

            val routine =
                RoutineEntity(
                    id = "routine-123",
                    userId = "user-123",
                    name = "Full Body",
                    description = "Complete workout",
                    exercises = exercises,
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val retrieved = routineDao.getRoutineById("routine-123")

            assertNotNull(retrieved)
            assertEquals(3, retrieved!!.exercises.size)

            val ex1 = retrieved.exercises[0]
            assertEquals("ex1", ex1.exerciseId)
            assertEquals(0, ex1.order)
            assertEquals(3, ex1.sets)
            assertEquals(10, ex1.reps)
            assertEquals(60, ex1.restSeconds)
            assertEquals("Warmup set", ex1.notes)

            val ex2 = retrieved.exercises[1]
            assertEquals("ex2", ex2.exerciseId)
            assertEquals(1, ex2.order)
            assertEquals(4, ex2.sets)
            assertEquals(12, ex2.reps)
            assertEquals(90, ex2.restSeconds)
            assertEquals(null, ex2.notes)

            val ex3 = retrieved.exercises[2]
            assertEquals("ex3", ex3.exerciseId)
            assertEquals(2, ex3.order)
            assertEquals(5, ex3.sets)
            assertEquals(null, ex3.reps)
            assertEquals(null, ex3.restSeconds)
            assertEquals("AMRAP", ex3.notes)
        }

    @Test
    fun emptyRoutineExerciseList_persistsAndRetrievesCorrectly() =
        runTest {
            val routine =
                RoutineEntity(
                    id = "routine-456",
                    userId = "user-123",
                    name = "Empty Routine",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val retrieved = routineDao.getRoutineById("routine-456")

            assertNotNull(retrieved)
            assertTrue(retrieved!!.exercises.isEmpty())
        }

    @Test
    fun routineExerciseWithComplexNotes_persistsAndRetrievesCorrectly() =
        runTest {
            val exercises =
                listOf(
                    RoutineExercise(
                        exerciseId = "ex1",
                        order = 0,
                        sets = 3,
                        reps = 10,
                        restSeconds = 60,
                        notes = "Use tempo 3-1-1-0, focus on form",
                    ),
                    RoutineExercise(
                        exerciseId = "ex2",
                        order = 1,
                        sets = 4,
                        reps = 8,
                        restSeconds = 120,
                        notes = "Heavy weight - spotter recommended",
                    ),
                )

            val routine =
                RoutineEntity(
                    id = "routine-789",
                    userId = "user-123",
                    name = "Advanced Routine",
                    description = "For experienced lifters",
                    exercises = exercises,
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val retrieved = routineDao.getRoutineById("routine-789")

            assertNotNull(retrieved)
            assertEquals(2, retrieved!!.exercises.size)
            assertEquals("Use tempo 3-1-1-0, focus on form", retrieved.exercises[0].notes)
            assertEquals("Heavy weight - spotter recommended", retrieved.exercises[1].notes)
        }

    @Test
    fun multipleMuscleGroups_withDifferentExercises_persistCorrectly() =
        runTest {
            val exercises =
                listOf(
                    ExerciseEntity(
                        id = "ex-1",
                        name = "Bench Press",
                        description = "Chest compound",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.CHEST,
                        secondaryMuscleGroups = listOf(MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS),
                        isGlobal = true,
                        createdBy = null,
                        language = "en",
                    ),
                    ExerciseEntity(
                        id = "ex-2",
                        name = "Pull-up",
                        description = "Back compound",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.BACK,
                        secondaryMuscleGroups = listOf(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
                        isGlobal = true,
                        createdBy = null,
                        language = "en",
                    ),
                    ExerciseEntity(
                        id = "ex-3",
                        name = "Squat",
                        description = "Leg compound",
                        imageUrl = null,
                        primaryMuscleGroup = MuscleGroup.QUADS,
                        secondaryMuscleGroups =
                            listOf(
                                MuscleGroup.GLUTES,
                                MuscleGroup.HAMSTRINGS,
                                MuscleGroup.CALVES,
                            ),
                        isGlobal = true,
                        createdBy = null,
                        language = "en",
                    ),
                )

            exerciseDao.insertExercises(exercises)

            val ex1 = exerciseDao.getExerciseById("ex-1", "en")
            assertNotNull(ex1)
            assertEquals(2, ex1!!.secondaryMuscleGroups.size)

            val ex2 = exerciseDao.getExerciseById("ex-2", "en")
            assertNotNull(ex2)
            assertEquals(2, ex2!!.secondaryMuscleGroups.size)

            val ex3 = exerciseDao.getExerciseById("ex-3", "en")
            assertNotNull(ex3)
            assertEquals(3, ex3!!.secondaryMuscleGroups.size)
        }

    @Test
    fun routineExerciseList_withVaryingFieldCombinations_persistsCorrectly() =
        runTest {
            val exercises =
                listOf(
                    RoutineExercise(
                        exerciseId = "ex1",
                        order = 0,
                        sets = 3,
                        reps = 10,
                        restSeconds = 60,
                        notes = "With all fields",
                    ),
                    RoutineExercise(
                        exerciseId = "ex2",
                        order = 1,
                        sets = 4,
                        reps = null,
                        restSeconds = 90,
                        notes = null,
                    ),
                    RoutineExercise(
                        exerciseId = "ex3",
                        order = 2,
                        sets = 5,
                        reps = 15,
                        restSeconds = null,
                        notes = "Only restSeconds is null",
                    ),
                    RoutineExercise(
                        exerciseId = "ex4",
                        order = 3,
                        sets = 2,
                        reps = null,
                        restSeconds = null,
                        notes = null,
                    ),
                )

            val routine =
                RoutineEntity(
                    id = "routine-complex",
                    userId = "user-123",
                    name = "Complex Routine",
                    description = "Testing various field combinations",
                    exercises = exercises,
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val retrieved = routineDao.getRoutineById("routine-complex")

            assertNotNull(retrieved)
            assertEquals(4, retrieved!!.exercises.size)

            assertEquals("With all fields", retrieved.exercises[0].notes)
            assertEquals(null, retrieved.exercises[1].reps)
            assertEquals(null, retrieved.exercises[2].restSeconds)
            assertEquals(null, retrieved.exercises[3].reps)
            assertEquals(null, retrieved.exercises[3].restSeconds)
            assertEquals(null, retrieved.exercises[3].notes)
        }
}
