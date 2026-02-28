package com.ruma.repnote.feature.routine.presentation.createedit

import app.cash.turbine.test
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineSaveHandlerTest {
    private lateinit var routineRepository: RoutineRepository
    private lateinit var uiState: MutableStateFlow<CreateEditRoutineUiState>
    private lateinit var navigationEvent: MutableSharedFlow<CreateEditRoutineNavigationEvent>
    private lateinit var saveHandler: RoutineSaveHandler

    @BeforeEach
    fun setup() {
        routineRepository = mockk()
        uiState = MutableStateFlow(CreateEditRoutineUiState())
        navigationEvent = MutableSharedFlow()
        saveHandler = RoutineSaveHandler(routineRepository, uiState, navigationEvent)
    }

    @Test
    fun `WHEN save is called for new routine THEN createRoutine is called`() =
        runTest {
            val routineId = "new-routine-id"
            coEvery { routineRepository.createRoutine(any()) } returns RoutineResult.Success(routineId)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "Test Routine",
                    description = "Test Description",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = "Test notes",
                            ),
                        ),
                )

            navigationEvent.test {
                saveHandler.save(state, "test-user-id")

                val event = awaitItem()
                event shouldBeEqualTo CreateEditRoutineNavigationEvent.NavigateToDetail(routineId)
            }

            coVerify { routineRepository.createRoutine(any()) }
        }

    @Test
    fun `WHEN save is called for new routine THEN saving state is updated`() =
        runTest {
            val routineId = "new-routine-id"
            coEvery { routineRepository.createRoutine(any()) } returns RoutineResult.Success(routineId)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "Test Routine",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            uiState.test {
                skipItems(1)
                saveHandler.save(state, "test-user-id")

                val savingState = awaitItem()
                savingState.isSaving shouldBe true
                savingState.errorMessage shouldBe null
            }
        }

    @Test
    fun `WHEN save is called for existing routine THEN updateRoutine is called`() =
        runTest {
            val routineId = "existing-routine-id"
            coEvery { routineRepository.updateRoutine(any()) } returns RoutineResult.Success(Unit)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = true,
                    routineId = routineId,
                    name = "Updated Routine",
                    description = "Updated Description",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 4,
                                reps = 8,
                                restSeconds = 90,
                                notes = null,
                            ),
                        ),
                )

            navigationEvent.test {
                saveHandler.save(state, "test-user-id")

                val event = awaitItem()
                event shouldBeEqualTo CreateEditRoutineNavigationEvent.NavigateToDetail(routineId)
            }

            coVerify { routineRepository.updateRoutine(any()) }
        }

    @Test
    fun `WHEN save builds routine THEN routine data is correct`() =
        runTest {
            val routineSlot = slot<com.ruma.repnote.core.domain.model.Routine>()
            coEvery { routineRepository.createRoutine(capture(routineSlot)) } returns RoutineResult.Success("routine-id")

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "  Test Routine  ",
                    description = "  Test Description  ",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = "Test notes",
                            ),
                        ),
                )

            saveHandler.save(state, "test-user-id")

            val capturedRoutine = routineSlot.captured
            capturedRoutine.userId shouldBeEqualTo "test-user-id"
            capturedRoutine.name shouldBeEqualTo "Test Routine"
            capturedRoutine.description shouldBeEqualTo "Test Description"
            capturedRoutine.exercises.size shouldBeEqualTo 1
            capturedRoutine.exercises[0].exerciseId shouldBeEqualTo "exercise-1"
            capturedRoutine.exercises[0].sets shouldBeEqualTo 3
            capturedRoutine.exercises[0].reps shouldBeEqualTo 10
        }

    @Test
    fun `WHEN save is called with blank description THEN description is null`() =
        runTest {
            val routineSlot = slot<com.ruma.repnote.core.domain.model.Routine>()
            coEvery { routineRepository.createRoutine(capture(routineSlot)) } returns RoutineResult.Success("routine-id")

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "Test Routine",
                    description = "   ",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            saveHandler.save(state, "test-user-id")

            val capturedRoutine = routineSlot.captured
            capturedRoutine.description shouldBe null
        }

    @Test
    fun `WHEN createRoutine fails with NetworkError THEN error message is shown`() =
        runTest {
            coEvery {
                routineRepository.createRoutine(any())
            } returns RoutineResult.Error(RoutineException.NetworkError)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "Test Routine",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            uiState.test {
                skipItems(1)
                saveHandler.save(state, "test-user-id")

                skipItems(1) // Skip saving state
                val finalState = awaitItem()
                finalState.isSaving shouldBe false
                finalState.errorMessage shouldBeEqualTo "Network error. Please check your connection."
            }
        }

    @Test
    fun `WHEN createRoutine fails with UnauthorizedAccess THEN error message is shown`() =
        runTest {
            coEvery {
                routineRepository.createRoutine(any())
            } returns RoutineResult.Error(RoutineException.UnauthorizedAccess)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "Test Routine",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            uiState.test {
                skipItems(1)
                saveHandler.save(state, "test-user-id")

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "You don't have permission to create routines."
            }
        }

    @Test
    fun `WHEN createRoutine fails with InvalidRoutineData THEN error message is shown`() =
        runTest {
            coEvery {
                routineRepository.createRoutine(any())
            } returns RoutineResult.Error(RoutineException.InvalidRoutineData)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = false,
                    name = "Test Routine",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            uiState.test {
                skipItems(1)
                saveHandler.save(state, "test-user-id")

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Invalid routine data. Please check your inputs."
            }
        }

    @Test
    fun `WHEN updateRoutine fails with RoutineNotFound THEN error message is shown`() =
        runTest {
            coEvery {
                routineRepository.updateRoutine(any())
            } returns RoutineResult.Error(RoutineException.RoutineNotFound)

            val state =
                CreateEditRoutineUiState(
                    isEditMode = true,
                    routineId = "routine-1",
                    name = "Test Routine",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            uiState.test {
                skipItems(1)
                saveHandler.save(state, "test-user-id")

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Routine not found."
            }
        }

    @Test
    fun `WHEN updateRoutine fails with Unknown error THEN error message is shown`() =
        runTest {
            coEvery {
                routineRepository.updateRoutine(any())
            } returns RoutineResult.Error(RoutineException.Unknown("Custom error message"))

            val state =
                CreateEditRoutineUiState(
                    isEditMode = true,
                    routineId = "routine-1",
                    name = "Test Routine",
                    exercisesWithConfig =
                        listOf(
                            ExerciseWithConfig(
                                exerciseId = "exercise-1",
                                exerciseName = "Bench Press",
                                order = 0,
                                sets = 3,
                                reps = 10,
                                restSeconds = 60,
                                notes = null,
                            ),
                        ),
                )

            uiState.test {
                skipItems(1)
                saveHandler.save(state, "test-user-id")

                skipItems(1)
                val finalState = awaitItem()
                finalState.errorMessage shouldBeEqualTo "Custom error message"
            }
        }
}
