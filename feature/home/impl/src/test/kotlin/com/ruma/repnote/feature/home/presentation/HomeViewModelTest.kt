package com.ruma.repnote.feature.home.presentation

import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.domain.repository.WorkoutRepository
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getCurrentUserUseCase = mockk()
        workoutRepository = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `WHEN ViewModel is initialized with user THEN user data is loaded`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.userEmail shouldBeEqualTo "test@example.com"
                state.displayName shouldBeEqualTo "Test User"
                state.userId shouldBeEqualTo "test-uid"
            }
        }

    @Test
    fun `WHEN ViewModel is initialized with no user THEN Guest is shown`() =
        runTest {
            every { getCurrentUserUseCase() } returns flowOf(null)

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.userEmail shouldBeEqualTo "Guest"
                state.displayName shouldBe null
                state.userId shouldBe null
            }
        }

    @Test
    fun `WHEN recent workouts are loaded successfully THEN workouts are shown in state`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
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
                    WorkoutSession(
                        id = "workout-2",
                        userId = "test-uid",
                        routineId = "routine-2",
                        routineName = "Another Routine",
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
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(mockWorkouts))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.recentWorkouts.size shouldBeEqualTo 2
                state.isLoadingWorkouts shouldBe false
            }
        }

    @Test
    fun `WHEN recent workouts loading fails THEN empty list is shown`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Error(WorkoutException.Unknown("Failed to load workouts")))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.recentWorkouts shouldBeEqualTo emptyList()
                state.isLoadingWorkouts shouldBe false
            }
        }

    @Test
    fun `WHEN there is an active session THEN hasActiveWorkout is true`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            val activeSession =
                WorkoutSession(
                    id = "active-session",
                    userId = "test-uid",
                    routineId = "routine-1",
                    routineName = "Active Routine",
                    status = WorkoutStatus.IN_PROGRESS,
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    exercises = emptyList(),
                    totalDurationSeconds = null,
                    notes = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(activeSession))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.hasActiveWorkout shouldBe true
                state.activeWorkoutSessionId shouldBeEqualTo "active-session"
            }
        }

    @Test
    fun `WHEN there is no active session THEN hasActiveWorkout is false`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.hasActiveWorkout shouldBe false
                state.activeWorkoutSessionId shouldBe null
            }
        }

    @Test
    fun `WHEN active session check fails THEN hasActiveWorkout is false`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Error(WorkoutException.Unknown("Failed to check active session")))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.hasActiveWorkout shouldBe false
                state.activeWorkoutSessionId shouldBe null
            }
        }

    @Test
    fun `WHEN onResumeWorkoutClick is called with active session THEN NavigateToActiveWorkout is emitted`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            val activeSession =
                WorkoutSession(
                    id = "active-session",
                    userId = "test-uid",
                    routineId = "routine-1",
                    routineName = "Active Routine",
                    status = WorkoutStatus.IN_PROGRESS,
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    exercises = emptyList(),
                    totalDurationSeconds = null,
                    notes = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(activeSession))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.navigationEvents.test {
                viewModel.onResumeWorkoutClick()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo HomeNavigationEvent.NavigateToActiveWorkout("active-session")
            }
        }

    @Test
    fun `WHEN onResumeWorkoutClick is called without active session THEN no event is emitted`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.navigationEvents.test {
                viewModel.onResumeWorkoutClick()
                advanceUntilIdle()

                expectNoEvents()
            }
        }

    @Test
    fun `WHEN user logs in THEN syncFromFirestore is called`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns WorkoutResult.Success(Unit)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(emptyList()))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            coVerify { workoutRepository.syncFromFirestore("test-uid") }
        }

    @Test
    fun `WHEN syncFromFirestore fails THEN workouts are still loaded from local`() =
        runTest {
            val mockUser =
                AuthUser(
                    uid = "test-uid",
                    email = "test@example.com",
                    displayName = "Test User",
                    photoUrl = null,
                )
            val localWorkouts =
                listOf(
                    WorkoutSession(
                        id = "local-workout",
                        userId = "test-uid",
                        routineId = "routine-1",
                        routineName = "Local Routine",
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
            every { getCurrentUserUseCase() } returns flowOf(mockUser)
            coEvery { workoutRepository.syncFromFirestore("test-uid") } returns
                WorkoutResult.Error(WorkoutException.NetworkError)
            every { workoutRepository.getUserWorkoutHistory("test-uid", 10) } returns
                flowOf(WorkoutResult.Success(localWorkouts))
            every { workoutRepository.getActiveSession("test-uid") } returns
                flowOf(WorkoutResult.Success(null))

            viewModel = HomeViewModel(getCurrentUserUseCase, workoutRepository)
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.recentWorkouts.size shouldBeEqualTo 1
                state.recentWorkouts[0].id shouldBeEqualTo "local-workout"
            }
        }
}
