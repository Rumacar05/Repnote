package com.ruma.repnote.core.data.mapper

import com.ruma.repnote.core.data.model.CompletedSetDocument
import com.ruma.repnote.core.data.model.WorkoutExerciseDocument
import com.ruma.repnote.core.data.model.WorkoutSessionDocument
import com.ruma.repnote.core.domain.model.CompletedSet
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Unit tests for WorkoutMapper.
 */
class WorkoutMapperTest {
    // Test data
    private val testCompletedSet =
        CompletedSet(
            setNumber = 1,
            reps = 10,
            weight = 100.0,
            completedAt = 1000L,
            restTimerSeconds = 60,
            notes = "Test set",
        )

    private val testCompletedSetDocument =
        CompletedSetDocument(
            setNumber = 1,
            reps = 10,
            weight = 100.0,
            completedAt = 1000L,
            restTimerSeconds = 60,
            notes = "Test set",
        )

    private val testWorkoutExercise =
        WorkoutExercise(
            exerciseId = "exercise-1",
            exerciseName = "Bench Press",
            order = 0,
            targetSets = 3,
            targetReps = 10,
            targetRestSeconds = 90,
            completedSets = listOf(testCompletedSet),
            notes = "Test exercise",
        )

    private val testWorkoutExerciseDocument =
        WorkoutExerciseDocument(
            exerciseId = "exercise-1",
            exerciseName = "Bench Press",
            order = 0,
            targetSets = 3,
            targetReps = 10,
            targetRestSeconds = 90,
            completedSets = listOf(testCompletedSetDocument),
            notes = "Test exercise",
        )

    private val testWorkoutSession =
        WorkoutSession(
            id = "session-1",
            userId = "user-1",
            routineId = "routine-1",
            routineName = "Test Routine",
            status = WorkoutStatus.IN_PROGRESS,
            exercises = listOf(testWorkoutExercise),
            startTime = 5000L,
            endTime = null,
            totalDurationSeconds = null,
            notes = "Test session",
            createdAt = 5000L,
            updatedAt = 5000L,
        )

    private val testWorkoutSessionDocument =
        WorkoutSessionDocument(
            id = "session-1",
            userId = "user-1",
            routineId = "routine-1",
            routineName = "Test Routine",
            status = "IN_PROGRESS",
            exercises = listOf(testWorkoutExerciseDocument),
            startTime = 5000L,
            endTime = null,
            totalDurationSeconds = null,
            notes = "Test session",
            createdAt = 5000L,
            updatedAt = 5000L,
        )

    // CompletedSet mapping tests

    @Test
    fun `toCompletedSet maps CompletedSetDocument to CompletedSet correctly`() {
        val result = testCompletedSetDocument.toCompletedSet()

        result shouldBe testCompletedSet
    }

    @Test
    fun `toCompletedSetDocument maps CompletedSet to CompletedSetDocument correctly`() {
        val result = testCompletedSet.toCompletedSetDocument()

        result shouldBe testCompletedSetDocument
    }

    @Test
    fun `CompletedSet round-trip mapping preserves data`() {
        val result =
            testCompletedSet
                .toCompletedSetDocument()
                .toCompletedSet()

        result shouldBe testCompletedSet
    }

    // WorkoutExercise mapping tests

    @Test
    fun `toWorkoutExercise maps WorkoutExerciseDocument to WorkoutExercise correctly`() {
        val result = testWorkoutExerciseDocument.toWorkoutExercise()

        result shouldBe testWorkoutExercise
    }

    @Test
    fun `toWorkoutExerciseDocument maps WorkoutExercise to WorkoutExerciseDocument correctly`() {
        val result = testWorkoutExercise.toWorkoutExerciseDocument()

        result shouldBe testWorkoutExerciseDocument
    }

    @Test
    fun `WorkoutExercise round-trip mapping preserves data`() {
        val result =
            testWorkoutExercise
                .toWorkoutExerciseDocument()
                .toWorkoutExercise()

        result shouldBe testWorkoutExercise
    }

    // WorkoutSession mapping tests

    @Test
    fun `toWorkoutSession maps WorkoutSessionDocument to WorkoutSession correctly`() {
        val result = testWorkoutSessionDocument.toWorkoutSession()

        result shouldBe testWorkoutSession
    }

    @Test
    fun `toWorkoutSessionDocument maps WorkoutSession to WorkoutSessionDocument correctly`() {
        val result = testWorkoutSession.toWorkoutSessionDocument()

        result shouldBe testWorkoutSessionDocument
    }

    @Test
    fun `WorkoutSession round-trip mapping preserves data`() {
        val result =
            testWorkoutSession
                .toWorkoutSessionDocument()
                .toWorkoutSession()

        result shouldBe testWorkoutSession
    }

    // Status mapping tests

    @Test
    fun `toWorkoutSession maps IN_PROGRESS status correctly`() {
        val doc = testWorkoutSessionDocument.copy(status = "IN_PROGRESS")
        val result = doc.toWorkoutSession()

        result.status shouldBe WorkoutStatus.IN_PROGRESS
    }

    @Test
    fun `toWorkoutSession maps COMPLETED status correctly`() {
        val doc = testWorkoutSessionDocument.copy(status = "COMPLETED")
        val result = doc.toWorkoutSession()

        result.status shouldBe WorkoutStatus.COMPLETED
    }

    @Test
    fun `toWorkoutSession maps ABANDONED status correctly`() {
        val doc = testWorkoutSessionDocument.copy(status = "ABANDONED")
        val result = doc.toWorkoutSession()

        result.status shouldBe WorkoutStatus.ABANDONED
    }

    @Test
    fun `toWorkoutSession maps unknown status to IN_PROGRESS as fallback`() {
        val doc = testWorkoutSessionDocument.copy(status = "UNKNOWN_STATUS")
        val result = doc.toWorkoutSession()

        result.status shouldBe WorkoutStatus.IN_PROGRESS
    }

    @Test
    fun `toWorkoutSessionDocument maps IN_PROGRESS status correctly`() {
        val session = testWorkoutSession.copy(status = WorkoutStatus.IN_PROGRESS)
        val result = session.toWorkoutSessionDocument()

        result.status shouldBe "IN_PROGRESS"
    }

    @Test
    fun `toWorkoutSessionDocument maps COMPLETED status correctly`() {
        val session = testWorkoutSession.copy(status = WorkoutStatus.COMPLETED)
        val result = session.toWorkoutSessionDocument()

        result.status shouldBe "COMPLETED"
    }

    @Test
    fun `toWorkoutSessionDocument maps ABANDONED status correctly`() {
        val session = testWorkoutSession.copy(status = WorkoutStatus.ABANDONED)
        val result = session.toWorkoutSessionDocument()

        result.status shouldBe "ABANDONED"
    }
}
