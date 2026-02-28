package com.ruma.repnote.feature.workout.presentation.history

import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.WorkoutRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutHistoryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var viewModel: WorkoutHistoryViewModel

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
        workoutRepository = mockk()
        getCurrentUserUseCase = mockk()
        every { getCurrentUserUseCase() } returns flowOf(mockUser)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN ViewModel is initialized THEN workout history is loaded`() =
        runTest {
            val mockWorkouts =
                listOf(
                    WorkoutSession(
                        id = "workout-1",
                        userId = "test-uid",
                        routineId = "routine-1",
                        routineName = "Test Routine",
                        status = WorkoutStatus.COMPLETED,
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        exercises = emptyList(),
                        totalDurationSeconds = 3600,
                        notes = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            every { workoutRepository.getUserWorkoutHistory("test-uid") } returns
                flowOf(WorkoutResult.Success(mockWorkouts))

            viewModel = WorkoutHistoryViewModel(workoutRepository, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.workouts.size shouldBeEqualTo 1
                state.isLoading shouldBe false
            }
        }

    @Test
    fun `WHEN loading workout history fails THEN empty list is shown`() =
        runTest {
            every { workoutRepository.getUserWorkoutHistory("test-uid") } returns
                flowOf(WorkoutResult.Error(WorkoutException.Unknown("Failed to load workouts")))

            viewModel = WorkoutHistoryViewModel(workoutRepository, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.workouts shouldBeEqualTo emptyList()
                state.isLoading shouldBe false
            }
        }

    @Test
    fun `WHEN workout history contains only COMPLETED and IN_PROGRESS workouts THEN they are shown`() =
        runTest {
            val mockWorkouts =
                listOf(
                    WorkoutSession(
                        id = "workout-1",
                        userId = "test-uid",
                        routineId = "routine-1",
                        routineName = "Completed Routine",
                        status = WorkoutStatus.COMPLETED,
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis(),
                        exercises = emptyList(),
                        totalDurationSeconds = 3600,
                        notes = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                    WorkoutSession(
                        id = "workout-2",
                        userId = "test-uid",
                        routineId = "routine-2",
                        routineName = "In Progress Routine",
                        status = WorkoutStatus.IN_PROGRESS,
                        startTime = System.currentTimeMillis(),
                        endTime = null,
                        exercises = emptyList(),
                        totalDurationSeconds = null,
                        notes = null,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            every { workoutRepository.getUserWorkoutHistory("test-uid") } returns
                flowOf(WorkoutResult.Success(mockWorkouts))

            viewModel = WorkoutHistoryViewModel(workoutRepository, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.workouts.size shouldBeEqualTo 2
                state.isLoading shouldBe false
            }
        }

    @Test
    fun `WHEN user is null THEN workout history is not loaded`() =
        runTest {
            every { getCurrentUserUseCase() } returns flowOf(null)

            viewModel = WorkoutHistoryViewModel(workoutRepository, getCurrentUserUseCase)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.workouts shouldBeEqualTo emptyList()
                state.isLoading shouldBe true
            }
        }
}
