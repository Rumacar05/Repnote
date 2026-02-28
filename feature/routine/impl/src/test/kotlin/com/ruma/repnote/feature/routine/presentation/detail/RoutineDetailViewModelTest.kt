package com.ruma.repnote.feature.routine.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineResult
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.repository.RoutineRepository
import com.ruma.repnote.core.domain.repository.WorkoutRepository
import io.mockk.coEvery
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineDetailViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var routineRepository: RoutineRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: RoutineDetailViewModel

    private val mockUser =
        AuthUser(
            uid = "test-uid",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
        )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        routineRepository = mockk()
        exerciseRepository = mockk()
        workoutRepository = mockk()
        getCurrentUserUseCase = mockk()
        savedStateHandle = SavedStateHandle()
        every { getCurrentUserUseCase() } returns flowOf(mockUser)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN onDeleteClick is called THEN showDeleteDialog is true`() =
        runTest {
            viewModel =
                RoutineDetailViewModel(
                    routineRepository,
                    exerciseRepository,
                    workoutRepository,
                    getCurrentUserUseCase,
                    savedStateHandle,
                )

            viewModel.onDeleteClick()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.showDeleteDialog shouldBe true
            }
        }

    @Test
    fun `WHEN onDeleteCancel is called THEN showDeleteDialog is false`() =
        runTest {
            viewModel =
                RoutineDetailViewModel(
                    routineRepository,
                    exerciseRepository,
                    workoutRepository,
                    getCurrentUserUseCase,
                    savedStateHandle,
                )

            viewModel.onDeleteClick()
            advanceUntilIdle()

            viewModel.onDeleteCancel()
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.showDeleteDialog shouldBe false
            }
        }

    @Test
    fun `WHEN onDeleteConfirm is called and delete succeeds THEN NavigateBack is emitted`() =
        runTest {
            val routineId = "routine-1"
            savedStateHandle["routineId"] = routineId
            coEvery { routineRepository.deleteRoutine("test-uid", routineId) } returns
                RoutineResult.Success(Unit)

            viewModel =
                RoutineDetailViewModel(
                    routineRepository,
                    exerciseRepository,
                    workoutRepository,
                    getCurrentUserUseCase,
                    savedStateHandle,
                )

            viewModel.navigationEvent.test {
                viewModel.onDeleteConfirm()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo RoutineDetailNavigationEvent.NavigateBack
            }
        }

    @Test
    fun `WHEN onEditClick is called THEN NavigateToEdit is emitted`() =
        runTest {
            val routineId = "routine-1"
            savedStateHandle["routineId"] = routineId

            viewModel =
                RoutineDetailViewModel(
                    routineRepository,
                    exerciseRepository,
                    workoutRepository,
                    getCurrentUserUseCase,
                    savedStateHandle,
                )

            viewModel.navigationEvent.test {
                viewModel.onEditClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo RoutineDetailNavigationEvent.NavigateToEdit(routineId)
            }
        }

    @Test
    fun `WHEN routineId is null THEN error message is shown`() =
        runTest {
            // Mock the repository to simulate routine loading with empty string
            // This will force the ViewModel to show "Routine not found"
            every { routineRepository.getRoutineById(any(), any()) } returns
                flowOf(RoutineResult.Error(RoutineException.RoutineNotFound))

            viewModel =
                RoutineDetailViewModel(
                    routineRepository,
                    exerciseRepository,
                    workoutRepository,
                    getCurrentUserUseCase,
                    savedStateHandle,
                )

            // Initialize with empty string, which will try to load and fail
            viewModel.initialize("")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            state.errorMessage shouldBeEqualTo "Routine not found"
            state.isLoading shouldBe false
        }

    @Test
    fun `WHEN initialize is called with valid routineId THEN routine is loaded`() =
        runTest {
            val routineId = "routine-1"
            val mockRoutine =
                Routine(
                    id = routineId,
                    userId = "test-uid",
                    name = "Test Routine",
                    description = null,
                    exercises = emptyList(),
                    createdAt = Instant.now().toEpochMilli(),
                    updatedAt = Instant.now().toEpochMilli(),
                )
            every { routineRepository.getRoutineById("test-uid", routineId) } returns
                flowOf(RoutineResult.Success(mockRoutine))

            viewModel =
                RoutineDetailViewModel(
                    routineRepository,
                    exerciseRepository,
                    workoutRepository,
                    getCurrentUserUseCase,
                    savedStateHandle,
                )

            viewModel.initialize(routineId)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.routine shouldBeEqualTo mockRoutine
                state.isLoading shouldBe false
            }
        }
}
