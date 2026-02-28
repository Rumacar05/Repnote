package com.ruma.repnote.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ruma.repnote.core.database.RepnoteDatabase
import com.ruma.repnote.core.database.entity.RoutineEntity
import com.ruma.repnote.core.domain.model.RoutineExercise
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
 * Instrumentation tests for RoutineDao.
 * These tests run on an Android device or emulator and use a real Room database.
 */
@RunWith(AndroidJUnit4::class)
class RoutineDaoTest {
    private lateinit var database: RepnoteDatabase
    private lateinit var routineDao: RoutineDao

    private val userId = "test-user-123"
    private val routineId = "routine-123"

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, RepnoteDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        routineDao = database.routineDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertRoutine_andRetrieveById_returnsCorrectRoutine() =
        runTest {
            val routine =
                RoutineEntity(
                    id = routineId,
                    userId = userId,
                    name = "Full Body Workout",
                    description = "Complete workout routine",
                    exercises =
                        listOf(
                            RoutineExercise(
                                exerciseId = "ex1",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = "Warmup set",
                            ),
                        ),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val retrieved = routineDao.getRoutineById(routineId)

            assertNotNull(retrieved)
            retrieved!!.let {
                assertEquals(routineId, it.id)
                assertEquals(userId, it.userId)
                assertEquals("Full Body Workout", it.name)
                assertEquals("Complete workout routine", it.description)
                assertEquals(1, it.exercises.size)
                assertEquals("ex1", it.exercises[0].exerciseId)
            }
        }

    @Test
    fun insertMultipleRoutines_andRetrieveByUser_returnsAllUserRoutines() =
        runTest {
            val routine1 =
                RoutineEntity(
                    id = "routine-1",
                    userId = userId,
                    name = "Routine 1",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            val routine2 =
                RoutineEntity(
                    id = "routine-2",
                    userId = userId,
                    name = "Routine 2",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567891L,
                    updatedAt = 1234567901L,
                )

            val routine3 =
                RoutineEntity(
                    id = "routine-3",
                    userId = "different-user",
                    name = "Routine 3",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567892L,
                    updatedAt = 1234567902L,
                )

            routineDao.insertRoutines(listOf(routine1, routine2, routine3))
            val userRoutines = routineDao.getUserRoutines(userId).first()

            assertEquals(2, userRoutines.size)
            assertTrue(userRoutines.any { it.id == "routine-1" })
            assertTrue(userRoutines.any { it.id == "routine-2" })
            assertTrue(userRoutines.none { it.id == "routine-3" })
        }

    @Test
    fun getUserRoutines_returnsRoutinesOrderedByUpdatedAtDesc() =
        runTest {
            val routine1 =
                RoutineEntity(
                    id = "routine-1",
                    userId = userId,
                    name = "Oldest",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1000L,
                    updatedAt = 1000L,
                )

            val routine2 =
                RoutineEntity(
                    id = "routine-2",
                    userId = userId,
                    name = "Newest",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 3000L,
                    updatedAt = 3000L,
                )

            val routine3 =
                RoutineEntity(
                    id = "routine-3",
                    userId = userId,
                    name = "Middle",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 2000L,
                    updatedAt = 2000L,
                )

            routineDao.insertRoutines(listOf(routine1, routine2, routine3))
            val userRoutines = routineDao.getUserRoutines(userId).first()

            assertEquals(3, userRoutines.size)
            assertEquals("Newest", userRoutines[0].name)
            assertEquals("Middle", userRoutines[1].name)
            assertEquals("Oldest", userRoutines[2].name)
        }

    @Test
    fun getRoutineByIdFlow_emitsUpdatedValue_whenRoutineIsUpdated() =
        runTest {
            val routine =
                RoutineEntity(
                    id = routineId,
                    userId = userId,
                    name = "Original Name",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val initial = routineDao.getRoutineByIdFlow(routineId).first()
            assertEquals("Original Name", initial?.name)

            val updated = routine.copy(name = "Updated Name")
            routineDao.insertRoutine(updated)

            val afterUpdate = routineDao.getRoutineByIdFlow(routineId).first()
            assertEquals("Updated Name", afterUpdate?.name)
        }

    @Test
    fun deleteRoutine_removesRoutineFromDatabase() =
        runTest {
            val routine =
                RoutineEntity(
                    id = routineId,
                    userId = userId,
                    name = "To Delete",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(routine)
            val beforeDelete = routineDao.getRoutineById(routineId)
            assertNotNull(beforeDelete)

            routineDao.deleteRoutine(routineId)
            val afterDelete = routineDao.getRoutineById(routineId)
            assertNull(afterDelete)
        }

    @Test
    fun deleteUserRoutines_removesOnlyUserRoutines() =
        runTest {
            val userRoutine1 =
                RoutineEntity(
                    id = "routine-1",
                    userId = userId,
                    name = "User Routine 1",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            val userRoutine2 =
                RoutineEntity(
                    id = "routine-2",
                    userId = userId,
                    name = "User Routine 2",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567891L,
                    updatedAt = 1234567901L,
                )

            val otherUserRoutine =
                RoutineEntity(
                    id = "routine-3",
                    userId = "other-user",
                    name = "Other User Routine",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567892L,
                    updatedAt = 1234567902L,
                )

            routineDao.insertRoutines(listOf(userRoutine1, userRoutine2, otherUserRoutine))
            routineDao.deleteUserRoutines(userId)

            val userRoutines = routineDao.getUserRoutines(userId).first()
            val otherRoutines = routineDao.getUserRoutines("other-user").first()

            assertEquals(0, userRoutines.size)
            assertEquals(1, otherRoutines.size)
        }

    @Test
    fun deleteAllRoutines_removesAllRoutinesFromDatabase() =
        runTest {
            val routines =
                listOf(
                    RoutineEntity(
                        id = "routine-1",
                        userId = userId,
                        name = "Routine 1",
                        description = null,
                        exercises = emptyList(),
                        createdAt = 1234567890L,
                        updatedAt = 1234567900L,
                    ),
                    RoutineEntity(
                        id = "routine-2",
                        userId = "other-user",
                        name = "Routine 2",
                        description = null,
                        exercises = emptyList(),
                        createdAt = 1234567891L,
                        updatedAt = 1234567901L,
                    ),
                )

            routineDao.insertRoutines(routines)
            routineDao.deleteAllRoutines()

            val count = routineDao.getRoutineCount(userId)
            assertEquals(0, count)
        }

    @Test
    fun deleteOldCache_removesOnlyOldRoutines() =
        runTest {
            val currentTime = System.currentTimeMillis()
            val oldTime = currentTime - 100000

            val oldRoutine =
                RoutineEntity(
                    id = "old-routine",
                    userId = userId,
                    name = "Old Routine",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                    cachedAt = oldTime,
                )

            val newRoutine =
                RoutineEntity(
                    id = "new-routine",
                    userId = userId,
                    name = "New Routine",
                    description = null,
                    exercises = emptyList(),
                    createdAt = 1234567891L,
                    updatedAt = 1234567901L,
                    cachedAt = currentTime,
                )

            routineDao.insertRoutines(listOf(oldRoutine, newRoutine))
            routineDao.deleteOldCache(currentTime - 50000)

            val remaining = routineDao.getUserRoutines(userId).first()
            assertEquals(1, remaining.size)
            assertEquals("New Routine", remaining[0].name)
        }

    @Test
    fun getRoutineCount_returnsCorrectCount() =
        runTest {
            val routines =
                listOf(
                    RoutineEntity(
                        id = "routine-1",
                        userId = userId,
                        name = "Routine 1",
                        description = null,
                        exercises = emptyList(),
                        createdAt = 1234567890L,
                        updatedAt = 1234567900L,
                    ),
                    RoutineEntity(
                        id = "routine-2",
                        userId = userId,
                        name = "Routine 2",
                        description = null,
                        exercises = emptyList(),
                        createdAt = 1234567891L,
                        updatedAt = 1234567901L,
                    ),
                    RoutineEntity(
                        id = "routine-3",
                        userId = "other-user",
                        name = "Routine 3",
                        description = null,
                        exercises = emptyList(),
                        createdAt = 1234567892L,
                        updatedAt = 1234567902L,
                    ),
                )

            routineDao.insertRoutines(routines)
            val count = routineDao.getRoutineCount(userId)

            assertEquals(2, count)
        }

    @Test
    fun upsert_replacesExistingRoutine() =
        runTest {
            val original =
                RoutineEntity(
                    id = routineId,
                    userId = userId,
                    name = "Original",
                    description = "Original description",
                    exercises = emptyList(),
                    createdAt = 1234567890L,
                    updatedAt = 1234567900L,
                )

            routineDao.insertRoutine(original)

            val updated =
                RoutineEntity(
                    id = routineId,
                    userId = userId,
                    name = "Updated",
                    description = "Updated description",
                    exercises =
                        listOf(
                            RoutineExercise(
                                exerciseId = "ex1",
                                order = 0,
                                sets = 5,
                                reps = 12,
                                restSeconds = 90,
                                notes = null,
                            ),
                        ),
                    createdAt = 1234567890L,
                    updatedAt = 1234567950L,
                )

            routineDao.insertRoutine(updated)

            val result = routineDao.getRoutineById(routineId)
            assertNotNull(result)
            result!!.let {
                assertEquals("Updated", it.name)
                assertEquals("Updated description", it.description)
                assertEquals(1, it.exercises.size)
                assertEquals(5, it.exercises[0].sets)
            }
        }
}
