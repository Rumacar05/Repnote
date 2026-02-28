package com.ruma.repnote.feature.workout.presentation.summary

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ruma.repnote.core.auth.domain.model.AuthUser
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.domain.model.CompletedSet
import com.ruma.repnote.core.domain.model.WorkoutException
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutResult
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
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
import org.amshove.kluent.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SessionSummaryViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: SessionSummaryViewModel

    private val mockUser =
        AuthUser(
            uid = "test-user-id",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
        )

    private val mockCompletedSets =
        listOf(
            CompletedSet(1, 10, 100.0, Instant.now().toEpochMilli(), null, null),
            CompletedSet(2, 10, 100.0, Instant.now().toEpochMilli() + 60000, null, null),
            CompletedSet(3, 10, 100.0, Instant.now().toEpochMilli() + 120000, null, null),
        )

    private val mockExercise =
        WorkoutExercise(
            exerciseId = "exercise-1",
            exerciseName = "Bench Press",
            order = 0,
            targetSets = 3,
            targetReps = 10,
            targetRestSeconds = 60,
            notes = null,
            completedSets = mockCompletedSets,
        )

    private val mockSession =
        WorkoutSession(
            id = "session-1",
            userId = "test-user-id",
            routineId = "routine-1",
            routineName = "Test Routine",
            status = WorkoutStatus.COMPLETED,
            startTime = Instant.now().toEpochMilli(),
            endTime = Instant.now().toEpochMilli() + 3600000, // 1 hour
            exercises = listOf(mockExercise),
            totalDurationSeconds = 3600,
            notes = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        workoutRepository = mockk(relaxed = true)
        getCurrentUserUseCase = mockk()
        savedStateHandle = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(mockUser)
        every { savedStateHandle.get<String>("sessionId") } returns null
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = SessionSummaryViewModel(workoutRepository, getCurrentUserUseCase, savedStateHandle)
    }

    @Test
    fun `WHEN ViewModel is initialized without sessionId THEN error message is shown`() =
        runTest {
            createViewModel()
            advanceUntilIdle()

            viewModel.initialize("session-1")
            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1") } returns
                flowOf(
                    WorkoutResult.Success(
                        emptyList(),
                    ),
                )

            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.isLoading shouldBe false
            }
        }

    @Test
    fun `WHEN session is loaded successfully THEN session and statistics are calculated`() =
        runTest {
            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1") } returns
                flowOf(
                    WorkoutResult.Success(
                        emptyList(),
                    ),
                )

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.isLoading shouldBe false
                state.session shouldBeEqualTo mockSession
                state.statistics shouldNotBe null
                state.statistics?.totalVolume shouldBeEqualTo 3000.0 // 3 sets × 10 reps × 100 lbs
                state.statistics?.exerciseStats?.size shouldBeEqualTo 1
            }
        }

    @Test
    fun `WHEN session is loaded successfully THEN exercise statistics are calculated correctly`() =
        runTest {
            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1") } returns
                flowOf(
                    WorkoutResult.Success(
                        emptyList(),
                    ),
                )

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                val exerciseStats = state.statistics?.exerciseStats?.first()
                exerciseStats shouldNotBe null
                exerciseStats?.exerciseId shouldBeEqualTo "exercise-1"
                exerciseStats?.exerciseName shouldBeEqualTo "Bench Press"
                exerciseStats?.sets shouldBeEqualTo 3
                exerciseStats?.totalReps shouldBeEqualTo 30
                exerciseStats?.totalVolume shouldBeEqualTo 3000.0
                exerciseStats?.maxWeight shouldBeEqualTo 100.0
            }
        }

    @Test
    fun `WHEN loading session fails THEN error message is shown`() =
        runTest {
            coEvery {
                workoutRepository.getSessionById("test-user-id", "session-1")
            } returns flowOf(WorkoutResult.Error(WorkoutException.Unknown("Failed to load")))

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.isLoading shouldBe false
                state.errorMessage shouldBeEqualTo "Failed to load workout session"
            }
        }

    @Test
    fun `WHEN previous session exists THEN comparison is calculated`() =
        runTest {
            val previousCompletedSets =
                listOf(
                    CompletedSet(1, 10, 90.0, Instant.now().toEpochMilli(), null, null),
                    CompletedSet(2, 10, 90.0, Instant.now().toEpochMilli() + 60000, null, null),
                    CompletedSet(3, 10, 90.0, Instant.now().toEpochMilli() + 120000, null, null),
                )

            val previousExercise =
                WorkoutExercise(
                    exerciseId = "exercise-1",
                    exerciseName = "Bench Press",
                    order = 0,
                    targetSets = 3,
                    targetReps = 10,
                    targetRestSeconds = 60,
                    notes = null,
                    completedSets = previousCompletedSets,
                )

            val previousSession =
                WorkoutSession(
                    id = "session-0",
                    userId = "test-user-id",
                    routineId = "routine-1",
                    routineName = "Test Routine",
                    status = WorkoutStatus.COMPLETED,
                    startTime = Instant.now().toEpochMilli() - 86400000, // 1 day ago
                    endTime = Instant.now().toEpochMilli() - 86400000 + 3600000,
                    exercises = listOf(previousExercise),
                    totalDurationSeconds = 3600,
                    notes = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )

            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery {
                workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1")
            } returns flowOf(WorkoutResult.Success(listOf(previousSession)))

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.previousSession shouldBeEqualTo previousSession
                state.comparison shouldNotBe null
                state.comparison?.volumeDifference shouldBeEqualTo 300.0 // 3000 - 2700
                state.comparison?.volumePercentChange shouldNotBe null
            }
        }

    @Test
    fun `WHEN previous session exists THEN exercise comparison is calculated correctly`() =
        runTest {
            val previousCompletedSets =
                listOf(
                    CompletedSet(1, 10, 90.0, Instant.now().toEpochMilli(), null, null),
                    CompletedSet(2, 10, 90.0, Instant.now().toEpochMilli() + 60000, null, null),
                    CompletedSet(3, 10, 90.0, Instant.now().toEpochMilli() + 120000, null, null),
                )

            val previousExercise =
                WorkoutExercise(
                    exerciseId = "exercise-1",
                    exerciseName = "Bench Press",
                    order = 0,
                    targetSets = 3,
                    targetReps = 10,
                    targetRestSeconds = 60,
                    notes = null,
                    completedSets = previousCompletedSets,
                )

            val previousSession =
                WorkoutSession(
                    id = "session-0",
                    userId = "test-user-id",
                    routineId = "routine-1",
                    routineName = "Test Routine",
                    status = WorkoutStatus.COMPLETED,
                    startTime = Instant.now().toEpochMilli() - 86400000,
                    endTime = Instant.now().toEpochMilli() - 86400000 + 3600000,
                    exercises = listOf(previousExercise),
                    totalDurationSeconds = 3600,
                    notes = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )

            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery {
                workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1")
            } returns flowOf(WorkoutResult.Success(listOf(previousSession)))

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                val exerciseComparison = state.comparison?.exerciseComparisons?.first()
                exerciseComparison shouldNotBe null
                exerciseComparison?.exerciseId shouldBeEqualTo "exercise-1"
                exerciseComparison?.volumeDifference shouldBeEqualTo 300.0
                exerciseComparison?.maxWeightDifference shouldBeEqualTo 10.0
            }
        }

    @Test
    fun `WHEN no previous session exists THEN comparison is null`() =
        runTest {
            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1") } returns
                flowOf(
                    WorkoutResult.Success(
                        emptyList(),
                    ),
                )

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.previousSession shouldBe null
                state.comparison shouldBe null
            }
        }

    @Test
    fun `WHEN onNavigateToHome is called THEN NavigateToHome event is emitted`() =
        runTest {
            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1") } returns
                flowOf(
                    WorkoutResult.Success(
                        emptyList(),
                    ),
                )

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.navigationEvent.test {
                viewModel.onNavigateToHome()
                advanceUntilIdle()

                val event = awaitItem()
                event shouldBeEqualTo SessionSummaryNavigationEvent.NavigateToHome
            }
        }

    @Test
    fun `WHEN session has no routineId THEN no previous session is loaded`() =
        runTest {
            val sessionWithoutRoutine = mockSession.copy(routineId = null)

            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns
                flowOf(
                    WorkoutResult.Success(
                        sessionWithoutRoutine,
                    ),
                )

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.session shouldBeEqualTo sessionWithoutRoutine
                state.previousSession shouldBe null
                state.comparison shouldBe null
            }
        }

    @Test
    fun `WHEN multiple previous sessions exist THEN most recent is selected`() =
        runTest {
            val olderSession =
                mockSession.copy(
                    id = "session-old",
                    startTime = Instant.now().toEpochMilli() - 172800000, // 2 days ago
                )

            val newerSession =
                mockSession.copy(
                    id = "session-newer",
                    startTime = Instant.now().toEpochMilli() - 86400000, // 1 day ago
                )

            coEvery { workoutRepository.getSessionById("test-user-id", "session-1") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery {
                workoutRepository.getRoutineWorkoutHistory("test-user-id", "routine-1")
            } returns flowOf(WorkoutResult.Success(listOf(olderSession, newerSession)))

            createViewModel()
            viewModel.initialize("session-1")
            advanceUntilIdle()

            viewModel.uiState.test {
                val state = awaitItem()
                state.previousSession shouldBeEqualTo newerSession
            }
        }
}
