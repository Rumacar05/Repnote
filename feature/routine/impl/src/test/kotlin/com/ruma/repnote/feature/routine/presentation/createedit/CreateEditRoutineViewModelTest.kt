package com.ruma.repnote.feature.routine.presentation.createedit

import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
class CreateEditRoutineViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var routineRepository: RoutineRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var viewModel: CreateEditRoutineViewModel

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
            Exercise(
                id = "exercise-3",
                name = "Deadlift",
                description = "Back exercise",
                imageUrl = null,
                primaryMuscleGroup = MuscleGroup.BACK,
                secondaryMuscleGroups = emptyList(),
                isGlobal = true,
                createdBy = null,
            ),
        )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        routineRepository = mockk()
        exerciseRepository = mockk()
        getCurrentUserUseCase = mockk()

        every { getCurrentUserUseCase() } returns flowOf(mockUser)
        coEvery { exerciseRepository.getAllExercisesForUser(any(), any()) } returns ExerciseResult.Success(mockExercises)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = CreateEditRoutineViewModel(routineRepository, exerciseRepository, getCurrentUserUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `WHEN ViewModel is initialized THEN initial state is correct`() =
        runTest {
            createViewModel()

            val state = viewModel.uiState.value
            state.isLoading shouldBe false
            state.isEditMode shouldBe false
            state.routineId shouldBe null
            state.name shouldBeEqualTo ""
            state.description shouldBeEqualTo ""
            state.exercisesWithConfig shouldBeEqualTo emptyList()
            state.isNameValid shouldBe true
            state.errorMessage shouldBe null
            state.showExerciseSelector shouldBe false
            state.isSaving shouldBe false
        }

    @Test
    fun `WHEN ViewModel is initialized THEN available exercises are loaded`() =
        runTest {
            createViewModel()

            val state = viewModel.uiState.value
            state.availableExercises.size shouldBeEqualTo 3
            state.availableExercises shouldContain mockExercises[0]
        }

    @Test
    fun `WHEN onNameChange is called THEN name is updated in state`() =
        runTest {
            createViewModel()

            viewModel.onNameChange("My Routine")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.name shouldBeEqualTo "My Routine"
            state.isNameValid shouldBe true
        }

    @Test
    fun `WHEN onNameChange is called with blank name THEN validation fails`() =
        runTest {
            createViewModel()

            viewModel.onNameChange("")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.name shouldBeEqualTo ""
            state.isNameValid shouldBe false
        }

    @Test
    fun `WHEN onDescriptionChange is called THEN description is updated in state`() =
        runTest {
            createViewModel()

            viewModel.onDescriptionChange("My routine description")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.description shouldBeEqualTo "My routine description"
        }

    @Test
    fun `WHEN onAddExerciseClick is called THEN exercise selector is shown`() =
        runTest {
            createViewModel()

            viewModel.onAddExerciseClick()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.showExerciseSelector shouldBe true
        }

    @Test
    fun `WHEN onDismissExerciseSelector is called THEN exercise selector is hidden`() =
        runTest {
            createViewModel()

            viewModel.onAddExerciseClick()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onDismissExerciseSelector()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.showExerciseSelector shouldBe false
        }

    @Test
    fun `WHEN onExerciseSearchQueryChange is called THEN search query is updated`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSearchQueryChange("Bench")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.exerciseSearchQuery shouldBeEqualTo "Bench"
        }

    @Test
    fun `WHEN onExerciseSelected is called THEN exercise is added to list`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.exercisesWithConfig.size shouldBeEqualTo 1
            state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-1"
            state.exercisesWithConfig[0].exerciseName shouldBeEqualTo "Bench Press"
            state.exercisesWithConfig[0].sets shouldBeEqualTo 3
            state.exercisesWithConfig[0].reps shouldBeEqualTo 10
            state.showExerciseSelector shouldBe false
        }

    @Test
    fun `WHEN multiple exercises are selected THEN they are added in order`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onExerciseSelected("exercise-2", "Squat")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.exercisesWithConfig.size shouldBeEqualTo 2
            state.exercisesWithConfig[0].order shouldBeEqualTo 0
            state.exercisesWithConfig[1].order shouldBeEqualTo 1
        }

    @Test
    fun `WHEN onRemoveExercise is called THEN exercise is removed from list`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onExerciseSelected("exercise-2", "Squat")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onRemoveExercise(0)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.exercisesWithConfig.size shouldBeEqualTo 1
            state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-2"
        }

    @Test
    fun `WHEN onExerciseConfigChange is called THEN exercise config is updated`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onExerciseConfigChange(0, 4, 12, 90, "Test notes")
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.exercisesWithConfig[0].sets shouldBeEqualTo 4
            state.exercisesWithConfig[0].reps shouldBeEqualTo 12
            state.exercisesWithConfig[0].restSeconds shouldBeEqualTo 90
            state.exercisesWithConfig[0].notes shouldBeEqualTo "Test notes"
        }

    @Test
    fun `WHEN onReorderExercises is called THEN exercises are reordered`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onExerciseSelected("exercise-2", "Squat")
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onExerciseSelected("exercise-3", "Deadlift")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onReorderExercises(0, 2)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-2"
            state.exercisesWithConfig[1].exerciseId shouldBeEqualTo "exercise-3"
            state.exercisesWithConfig[2].exerciseId shouldBeEqualTo "exercise-1"
        }

    @Test
    fun `WHEN onSaveClick is called with valid data THEN routine is saved`() =
        runTest {
            val routineId = "new-routine-id"
            coEvery { routineRepository.createRoutine(any()) } returns RoutineResult.Success(routineId)

            createViewModel()

            viewModel.onNameChange("Test Routine")
            viewModel.onDescriptionChange("Test Description")
            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onSaveClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo CreateEditRoutineNavigationEvent.NavigateToDetail(routineId)
            }

            coVerify { routineRepository.createRoutine(any()) }
        }

    @Test
    fun `WHEN onSaveClick is called with blank name THEN validation error is shown`() =
        runTest {
            createViewModel()

            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onSaveClick()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.isNameValid shouldBe false
            state.errorMessage?.contains("name", ignoreCase = true) shouldBe true

            coVerify(exactly = 0) { routineRepository.createRoutine(any()) }
        }

    @Test
    fun `WHEN onSaveClick is called with no exercises THEN validation error is shown`() =
        runTest {
            createViewModel()

            viewModel.onNameChange("Test Routine")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onSaveClick()
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.errorMessage?.contains("exercise", ignoreCase = true) shouldBe true

            coVerify(exactly = 0) { routineRepository.createRoutine(any()) }
        }

    @Test
    fun `WHEN onCancelClick is called THEN NavigateBack is emitted`() =
        runTest {
            createViewModel()

            viewModel.navigationEvent.test {
                viewModel.onCancelClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo CreateEditRoutineNavigationEvent.NavigateBack
            }
        }

    @Test
    fun `WHEN loadRoutineForEdit is called THEN routine is loaded and state is updated`() =
        runTest {
            val routineId = "routine-1"
            val routine =
                Routine(
                    id = routineId,
                    userId = "test-user-id",
                    name = "Existing Routine",
                    description = "Existing Description",
                    exercises =
                        listOf(
                            RoutineExercise(
                                exerciseId = "exercise-1",
                                order = 0,
                                sets = 4,
                                reps = 8,
                                restSeconds = 120,
                                notes = "Heavy",
                            ),
                        ),
                    createdAt = 0,
                    updatedAt = 0,
                )

            every { routineRepository.getRoutineById("test-user-id", routineId) } returns flowOf(RoutineResult.Success(routine))
            coEvery { exerciseRepository.getExerciseById(any(), any(), any()) } returns ExerciseResult.Success(mockExercises[0])

            createViewModel()

            viewModel.loadRoutineForEdit(routineId)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            state.isEditMode shouldBe true
            state.routineId shouldBeEqualTo routineId
            state.name shouldBeEqualTo "Existing Routine"
            state.description shouldBeEqualTo "Existing Description"
            state.exercisesWithConfig.size shouldBeEqualTo 1
            state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-1"
            state.exercisesWithConfig[0].sets shouldBeEqualTo 4
            state.exercisesWithConfig[0].reps shouldBeEqualTo 8
        }

    @Test
    fun `WHEN loadRoutineForEdit is called in edit mode THEN routine is saved with update`() =
        runTest {
            val routineId = "routine-1"
            val routine =
                Routine(
                    id = routineId,
                    userId = "test-user-id",
                    name = "Existing Routine",
                    description = "Existing Description",
                    exercises = emptyList(),
                    createdAt = 0,
                    updatedAt = 0,
                )

            every { routineRepository.getRoutineById("test-user-id", routineId) } returns flowOf(RoutineResult.Success(routine))
            coEvery { exerciseRepository.getExerciseById(any(), any(), any()) } returns ExerciseResult.Success(mockExercises[0])
            coEvery { routineRepository.updateRoutine(any()) } returns RoutineResult.Success(Unit)

            createViewModel()

            viewModel.loadRoutineForEdit(routineId)
            testDispatcher.scheduler.advanceUntilIdle()
            viewModel.onNameChange("Updated Routine")
            viewModel.onExerciseSelected("exercise-1", "Bench Press")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onSaveClick()
                testDispatcher.scheduler.advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo CreateEditRoutineNavigationEvent.NavigateToDetail(routineId)
            }

            coVerify { routineRepository.updateRoutine(any()) }
        }
}
