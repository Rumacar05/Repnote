package com.ruma.repnote.feature.routine.presentation.createedit

import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineLoaderTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: TestScope
    private lateinit var routineRepository: RoutineRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var exerciseListManager: ExerciseListManager
    private lateinit var uiState: MutableStateFlow<CreateEditRoutineUiState>
    private lateinit var loader: RoutineLoader

    private val mockUser =
        AuthUser(
            uid = "test-user-id",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
        )

    private val mockExercises =
        listOf(
            Exercise(
                id = "exercise-1",
                name = "Bench Press",
                description = "Chest exercise",
                imageUrl = null,
                primaryMuscleGroup = MuscleGroup.CHEST,
                secondaryMuscleGroups = emptyList(),
                isGlobal = true,
                createdBy = null,
            ),
            Exercise(
                id = "exercise-2",
                name = "Squat",
                description = "Leg exercise",
                imageUrl = null,
                primaryMuscleGroup = MuscleGroup.QUADS,
                secondaryMuscleGroups = listOf(MuscleGroup.GLUTES),
                isGlobal = true,
                createdBy = null,
            ),
        )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        testScope = TestScope(testDispatcher)
        routineRepository = mockk()
        exerciseRepository = mockk()
        getCurrentUserUseCase = mockk()
        uiState = MutableStateFlow(CreateEditRoutineUiState())
        exerciseListManager = ExerciseListManager(uiState)

        every { getCurrentUserUseCase() } returns flowOf(mockUser)

        loader =
            RoutineLoader(
                routineRepository,
                exerciseRepository,
                getCurrentUserUseCase,
                exerciseListManager,
                uiState,
                testScope,
            )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN loadAvailableExercises succeeds THEN exercises are loaded`() =
        testScope.runTest {
            coEvery {
                exerciseRepository.getAllExercisesForUser("test-user-id", any())
            } returns ExerciseResult.Success(mockExercises)

            loader.loadAvailableExercises()
            advanceUntilIdle()

            val finalState = uiState.value
            finalState.availableExerciseLoading shouldBe false
            finalState.availableExercises.size shouldBeEqualTo 2
            finalState.availableExercises shouldContain mockExercises[0]
            finalState.errorMessage shouldBe null
        }

    @Test
    fun `WHEN loadAvailableExercises fails with NetworkError THEN error message is shown`() =
        testScope.runTest {
            coEvery {
                exerciseRepository.getAllExercisesForUser("test-user-id", any())
            } returns ExerciseResult.Error(ExerciseException.NetworkError)

            uiState.test {
                skipItems(1)
                loader.loadAvailableExercises()
                advanceUntilIdle()

                skipItems(1) // Skip loading state
                val finalState = awaitItem()
                finalState.availableExerciseLoading shouldBe false
                finalState.errorMessage shouldBeEqualTo
                    "No network connection. Please check your internet and try again."
            }
        }

    @Test
    fun `WHEN loadAvailableExercises fails with StorageError THEN error message is shown`() =
        testScope.runTest {
            coEvery {
                exerciseRepository.getAllExercisesForUser("test-user-id", any())
            } returns ExerciseResult.Error(ExerciseException.StorageError)

            uiState.test {
                skipItems(1)
                loader.loadAvailableExercises()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Storage error. Please try again later."
            }
        }

    @Test
    fun `WHEN loadAvailableExercises fails with UnauthorizedAccess THEN error message is shown`() =
        testScope.runTest {
            coEvery {
                exerciseRepository.getAllExercisesForUser("test-user-id", any())
            } returns ExerciseResult.Error(ExerciseException.UnauthorizedAccess)

            uiState.test {
                skipItems(1)
                loader.loadAvailableExercises()
                advanceUntilIdle()

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo
                    "Permission denied. Please check your account permissions."
            }
        }

    @Test
    fun `WHEN loadRoutine succeeds THEN routine data is loaded`() =
        testScope.runTest {
            val routine =
                Routine(
                    id = "routine-1",
                    userId = "test-user-id",
                    name = "Test Routine",
                    description = "Test Description",
                    exercises =
                        listOf(
                            RoutineExercise(
                                exerciseId = "exercise-1",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = "Test notes",
                            ),
                        ),
                    createdAt = 0,
                    updatedAt = 0,
                )

            coEvery {
                routineRepository.getRoutineById("test-user-id", "routine-1")
            } returns flowOf(RoutineResult.Success(routine))

            coEvery {
                exerciseRepository.getExerciseById("exercise-1", "test-user-id", any())
            } returns ExerciseResult.Success(mockExercises[0])

            uiState.test {
                skipItems(1)
                loader.loadRoutine("routine-1")
                advanceUntilIdle()

                skipItems(1) // Skip loading state
                val finalState = awaitItem()
                finalState.isLoading shouldBe false
                finalState.name shouldBeEqualTo "Test Routine"
                finalState.description shouldBeEqualTo "Test Description"
                finalState.exercisesWithConfig.size shouldBeEqualTo 1
                finalState.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-1"
                finalState.exercisesWithConfig[0].sets shouldBeEqualTo 3
                finalState.exercisesWithConfig[0].reps shouldBeEqualTo 10
            }
        }

    @Test
    fun `WHEN loadRoutine fails THEN error message is shown`() =
        testScope.runTest {
            coEvery {
                routineRepository.getRoutineById("test-user-id", "routine-1")
            } returns flowOf(RoutineResult.Error(RoutineException.Unknown("Failed to load")))

            uiState.test {
                skipItems(1)
                loader.loadRoutine("routine-1")
                advanceUntilIdle()

                skipItems(1) // Skip loading state
                val finalState = awaitItem()
                finalState.isLoading shouldBe false
                finalState.errorMessage shouldBeEqualTo "Failed to load routine"
            }
        }

    @Test
    fun `WHEN loadRoutine succeeds but exercise loading fails THEN exercise is skipped`() =
        testScope.runTest {
            val routine =
                Routine(
                    id = "routine-1",
                    userId = "test-user-id",
                    name = "Test Routine",
                    description = "Test Description",
                    exercises =
                        listOf(
                            RoutineExercise(
                                exerciseId = "exercise-1",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                            RoutineExercise(
                                exerciseId = "exercise-2",
                                order = 1,
                                sets = 4,
                                reps = 8,
                                restSeconds = 90,
                                notes = null,
                            ),
                        ),
                    createdAt = 0,
                    updatedAt = 0,
                )

            coEvery {
                routineRepository.getRoutineById("test-user-id", "routine-1")
            } returns flowOf(RoutineResult.Success(routine))

            coEvery {
                exerciseRepository.getExerciseById("exercise-1", "test-user-id", any())
            } returns ExerciseResult.Success(mockExercises[0])

            coEvery {
                exerciseRepository.getExerciseById("exercise-2", "test-user-id", any())
            } returns ExerciseResult.Error(ExerciseException.ExerciseNotFound)

            uiState.test {
                skipItems(1)
                loader.loadRoutine("routine-1")
                advanceUntilIdle()

                skipItems(1) // Skip loading state
                val finalState = awaitItem()
                finalState.exercisesWithConfig.size shouldBeEqualTo 1
                finalState.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-1"
            }
        }
}
