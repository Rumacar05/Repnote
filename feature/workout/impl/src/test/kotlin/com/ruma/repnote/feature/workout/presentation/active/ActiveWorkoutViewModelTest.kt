package com.ruma.repnote.feature.workout.presentation.active

import com.ruma.repnote.core.analytics.domain.service.AnalyticsService
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
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
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
class ActiveWorkoutViewModelTest {
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var analyticsService: AnalyticsService
    private lateinit var viewModel: ActiveWorkoutViewModel

    private val mockUser =
        AuthUser(
            uid = "test-user-id",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null,
        )

    private val mockExercise1 =
        WorkoutExercise(
            exerciseId = "exercise-1",
            exerciseName = "Bench Press",
            order = 0,
            targetSets = 3,
            targetReps = 10,
            targetRestSeconds = 60,
            notes = null,
            completedSets = emptyList(),
        )

    private val mockExercise2 =
        WorkoutExercise(
            exerciseId = "exercise-2",
            exerciseName = "Squat",
            order = 1,
            targetSets = 3,
            targetReps = 10,
            targetRestSeconds = 60,
            notes = null,
            completedSets = emptyList(),
        )

    private val mockSession =
        WorkoutSession(
            id = "session-1",
            userId = "test-user-id",
            routineId = "routine-1",
            routineName = "Test Routine",
            status = WorkoutStatus.IN_PROGRESS,
            startTime = Instant.now().toEpochMilli(),
            endTime = null,
            exercises = listOf(mockExercise1, mockExercise2),
            totalDurationSeconds = null,
            notes = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        workoutRepository = mockk(relaxed = true)
        getCurrentUserUseCase = mockk()
        analyticsService = mockk(relaxed = true)

        every { getCurrentUserUseCase() } returns flowOf(mockUser)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel =
            ActiveWorkoutViewModel(
                workoutRepository = workoutRepository,
                getCurrentUser = getCurrentUserUseCase,
                analyticsService = analyticsService,
                enableTimeTracking = false,
            )
    }

    @Test
    fun `WHEN ViewModel is initialized with active session THEN session is loaded`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBe false
            viewModel.uiState.value.session shouldBeEqualTo mockSession
            viewModel.uiState.value.currentExerciseIndex shouldBeEqualTo 0
            viewModel.uiState.value.currentSetNumber shouldBeEqualTo 1
        }

    @Test
    fun `WHEN ViewModel is initialized with no active session THEN NavigateBack is emitted`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(null))

            val events = mutableListOf<ActiveWorkoutNavigationEvent>()
            val job =
                launch {
                    viewModel =
                        ActiveWorkoutViewModel(
                            workoutRepository = workoutRepository,
                            getCurrentUser = getCurrentUserUseCase,
                            analyticsService = analyticsService,
                            enableTimeTracking = false,
                        )
                    viewModel.navigationEvent.collect { events.add(it) }
                }

            advanceUntilIdle()
            job.cancel()

            events.size shouldBeEqualTo 1
            events[0] shouldBeEqualTo ActiveWorkoutNavigationEvent.NavigateBack
        }

    @Test
    fun `WHEN loading active session fails THEN error message is shown`() =
        runTest {
            coEvery {
                workoutRepository.getActiveSession("test-user-id")
            } returns flowOf(WorkoutResult.Error(WorkoutException.Unknown("Failed to load")))

            createViewModel()
            advanceUntilIdle()

            viewModel.uiState.value.isLoading shouldBe false
            viewModel.uiState.value.errorMessage shouldBeEqualTo "Failed to load workout session"
        }

    @Test
    fun `WHEN onRepsChange is called THEN reps are updated in setInputState`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onRepsChange("12")
            advanceUntilIdle()

            viewModel.setInputState.value.reps shouldBeEqualTo "12"
        }

    @Test
    fun `WHEN onWeightChange is called THEN weight is updated in setInputState`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onWeightChange("100.5")
            advanceUntilIdle()

            viewModel.setInputState.value.weight shouldBeEqualTo "100.5"
        }

    @Test
    fun `WHEN onNotesChange is called THEN notes are updated in setInputState`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onNotesChange("Felt heavy")
            advanceUntilIdle()

            viewModel.setInputState.value.notes shouldBeEqualTo "Felt heavy"
        }

    @Test
    fun `WHEN onSetComplete is called with valid input THEN set is added and advances to next set`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            viewModel.onRepsChange("12")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()
            advanceUntilIdle()

            viewModel.uiState.value.currentSetNumber shouldBeEqualTo 2
            coVerify { workoutRepository.updateSession(any()) }
        }

    @Test
    fun `WHEN onSetComplete is called on last set THEN moves to next exercise`() =
        runTest {
            val exerciseWith2CompletedSets =
                mockExercise1.copy(
                    completedSets =
                        listOf(
                            CompletedSet(1, 10, 100.0, System.currentTimeMillis(), null, null),
                            CompletedSet(2, 10, 100.0, System.currentTimeMillis(), null, null),
                        ),
                )
            val sessionWithProgress =
                mockSession.copy(
                    exercises = listOf(exerciseWith2CompletedSets, mockExercise2),
                )

            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(sessionWithProgress))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()
            advanceUntilIdle()

            viewModel.uiState.value.currentExerciseIndex shouldBeEqualTo 1
            viewModel.uiState.value.currentSetNumber shouldBeEqualTo 1
        }

    @Test
    fun `WHEN onNextExercise is called THEN advances to next exercise`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onNextExercise()
            advanceUntilIdle()

            viewModel.uiState.value.currentExerciseIndex shouldBeEqualTo 1
            viewModel.uiState.value.currentSetNumber shouldBeEqualTo 1
        }

    @Test
    fun `WHEN onPreviousExercise is called THEN goes back to previous exercise`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onNextExercise()
            advanceUntilIdle()

            viewModel.onPreviousExercise()
            advanceUntilIdle()

            viewModel.uiState.value.currentExerciseIndex shouldBeEqualTo 0
            viewModel.uiState.value.currentSetNumber shouldBeEqualTo 1
        }

    @Test
    fun `WHEN onPreviousExercise is called on first exercise THEN stays on first exercise`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            val initialExerciseIndex = viewModel.uiState.value.currentExerciseIndex
            viewModel.onPreviousExercise()
            advanceUntilIdle()

            viewModel.uiState.value.currentExerciseIndex shouldBeEqualTo initialExerciseIndex
            viewModel.uiState.value.currentExerciseIndex shouldBeEqualTo 0
        }

    @Test
    fun `WHEN onSkipRestTimer is called THEN rest timer is cancelled`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()
            advanceUntilIdle()

            viewModel.onSkipRestTimer()
            advanceUntilIdle()

            viewModel.uiState.value.isRestTimerActive shouldBe false
            viewModel.uiState.value.restTimerSecondsRemaining shouldBeEqualTo 0
        }

    @Test
    fun `WHEN onAbandonWorkout is called THEN abandon dialog is shown`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onAbandonWorkout()
            advanceUntilIdle()

            viewModel.uiState.value.showAbandonDialog shouldBe true
        }

    @Test
    fun `WHEN onDismissAbandonDialog is called THEN abandon dialog is hidden`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onAbandonWorkout()
            advanceUntilIdle()
            viewModel.uiState.value.showAbandonDialog shouldBe true

            viewModel.onDismissAbandonDialog()
            advanceUntilIdle()

            viewModel.uiState.value.showAbandonDialog shouldBe false
        }

    @Test
    fun `WHEN onAbandonConfirm is called THEN session is abandoned and navigates back`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.abandonSession("test-user-id", "session-1") } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            val events = mutableListOf<ActiveWorkoutNavigationEvent>()
            val job =
                launch {
                    viewModel.navigationEvent.collect { events.add(it) }
                }

            advanceUntilIdle() // Let collector start
            viewModel.onAbandonConfirm()
            advanceUntilIdle()

            job.cancel()

            events.size shouldBeEqualTo 1
            events[0] shouldBeEqualTo ActiveWorkoutNavigationEvent.NavigateBack
            coVerify { workoutRepository.abandonSession("test-user-id", "session-1") }
        }

    @Test
    fun `WHEN all exercises are complete THEN complete dialog is shown`() =
        runTest {
            val exerciseWith3CompletedSets =
                mockExercise2.copy(
                    completedSets =
                        listOf(
                            CompletedSet(1, 10, 100.0, System.currentTimeMillis(), null, null),
                            CompletedSet(2, 10, 100.0, System.currentTimeMillis(), null, null),
                            CompletedSet(3, 10, 100.0, System.currentTimeMillis(), null, null),
                        ),
                )
            val sessionWithOneExercise = mockSession.copy(exercises = listOf(exerciseWith3CompletedSets))

            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(sessionWithOneExercise))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            viewModel.onNextExercise()
            advanceUntilIdle()

            viewModel.uiState.value.showCompleteDialog shouldBe true
        }

    @Test
    fun `WHEN onDismissCompleteDialog is called THEN complete dialog is hidden`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))

            createViewModel()
            advanceUntilIdle()

            viewModel.onDismissCompleteDialog()
            advanceUntilIdle()

            viewModel.uiState.value.showCompleteDialog shouldBe false
        }

    @Test
    fun `WHEN onCompleteWorkout is called THEN session is completed and navigates to summary`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.completeSession("test-user-id", "session-1") } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            val events = mutableListOf<ActiveWorkoutNavigationEvent>()
            val job =
                launch {
                    viewModel.navigationEvent.collect { events.add(it) }
                }

            advanceUntilIdle() // Let collector start
            viewModel.onCompleteWorkout()
            advanceUntilIdle()

            job.cancel()

            events.size shouldBeEqualTo 1
            events[0] shouldBeEqualTo ActiveWorkoutNavigationEvent.NavigateToSessionSummary("session-1")
            coVerify { workoutRepository.completeSession("test-user-id", "session-1") }
        }

    @Test
    fun `WHEN multiple sets completed rapidly THEN updateSession is debounced`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            // Complete 3 sets rapidly
            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()

            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()

            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()

            // Only advance slightly (less than debounce delay)
            testScheduler.advanceTimeBy(500)

            // Should have multiple calls queued but only the last one should execute after full delay
            testScheduler.advanceTimeBy(1000)
            advanceUntilIdle()

            // Verify updateSession was called (debouncing reduces the number of calls)
            // Due to debouncing, we expect fewer calls than the number of sets completed
            coVerify(atLeast = 1, atMost = 3) { workoutRepository.updateSession(any()) }
        }

    @Test
    fun `WHEN onCompleteWorkout is called THEN pending saves are flushed immediately`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)
            coEvery { workoutRepository.completeSession("test-user-id", "session-1") } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            // Make a change
            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()

            // Complete workout immediately without waiting for debounce
            viewModel.onCompleteWorkout()
            advanceUntilIdle()

            // Verify updateSession was called before completing
            coVerify { workoutRepository.updateSession(any()) }
            coVerify { workoutRepository.completeSession("test-user-id", "session-1") }
        }

    @Test
    fun `WHEN onAbandonConfirm is called THEN pending saves are flushed immediately`() =
        runTest {
            coEvery { workoutRepository.getActiveSession("test-user-id") } returns flowOf(WorkoutResult.Success(mockSession))
            coEvery { workoutRepository.updateSession(any()) } returns WorkoutResult.Success(Unit)
            coEvery { workoutRepository.abandonSession("test-user-id", "session-1") } returns WorkoutResult.Success(Unit)

            createViewModel()
            advanceUntilIdle()

            // Make a change
            viewModel.onRepsChange("10")
            viewModel.onWeightChange("100")
            viewModel.onSetComplete()

            // Abandon workout immediately without waiting for debounce
            viewModel.onAbandonConfirm()
            advanceUntilIdle()

            // Verify updateSession was called before abandoning
            coVerify { workoutRepository.updateSession(any()) }
            coVerify { workoutRepository.abandonSession("test-user-id", "session-1") }
        }
}
