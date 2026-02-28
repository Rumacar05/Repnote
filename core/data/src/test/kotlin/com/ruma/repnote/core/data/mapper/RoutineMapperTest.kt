package com.ruma.repnote.core.data.mapper

import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.model.RoutineDocument
import com.ruma.repnote.core.data.model.RoutineExerciseDocument
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineException
import com.ruma.repnote.core.domain.model.RoutineExercise
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class RoutineMapperTest {
    @Test
    fun `GIVEN routine document with exercises WHEN toRoutine is called THEN routine is returned with all fields`() {
        val document =
            RoutineDocument(
                id = "routine-123",
                userId = "user-123",
                name = "Full Body Workout",
                description = "A comprehensive full body routine",
                exercises =
                    listOf(
                        RoutineExerciseDocument(
                            exerciseId = "ex1",
                            order = 0,
                            sets = 3,
                            reps = 10,
                            restSeconds = 60,
                            notes = "Warmup set",
                        ),
                        RoutineExerciseDocument(
                            exerciseId = "ex2",
                            order = 1,
                            sets = 4,
                            reps = 12,
                            restSeconds = 90,
                            notes = null,
                        ),
                    ),
                createdAt = 1234567890L,
                updatedAt = 1234567900L,
            )

        val result = document.toRoutine()

        result.id shouldBeEqualTo "routine-123"
        result.userId shouldBeEqualTo "user-123"
        result.name shouldBeEqualTo "Full Body Workout"
        result.description shouldBeEqualTo "A comprehensive full body routine"
        result.exercises.size shouldBe 2
        result.exercises[0].exerciseId shouldBeEqualTo "ex1"
        result.exercises[0].order shouldBe 0
        result.exercises[0].sets shouldBe 3
        result.exercises[0].reps shouldBe 10
        result.exercises[0].restSeconds shouldBe 60
        result.exercises[0].notes shouldBeEqualTo "Warmup set"
        result.exercises[1].exerciseId shouldBeEqualTo "ex2"
        result.exercises[1].notes shouldBe null
        result.createdAt shouldBeEqualTo 1234567890L
        result.updatedAt shouldBeEqualTo 1234567900L
    }

    @Test
    fun `GIVEN routine document with null description WHEN toRoutine is called THEN routine is returned with null description`() {
        val document =
            RoutineDocument(
                id = "routine-123",
                userId = "user-123",
                name = "Full Body Workout",
                description = null,
                exercises = emptyList(),
                createdAt = 1234567890L,
                updatedAt = 1234567900L,
            )

        val result = document.toRoutine()

        result.description shouldBe null
    }

    @Test
    fun `GIVEN routine document with empty exercises WHEN toRoutine is called THEN routine is returned with empty list`() {
        val document =
            RoutineDocument(
                id = "routine-123",
                userId = "user-123",
                name = "Full Body Workout",
                description = "A routine",
                exercises = emptyList(),
                createdAt = 1234567890L,
                updatedAt = 1234567900L,
            )

        val result = document.toRoutine()

        result.exercises.size shouldBe 0
    }

    @Test
    fun `GIVEN routine exercise document WHEN toRoutineExercise is called THEN routine exercise is returned with all fields`() {
        val document =
            RoutineExerciseDocument(
                exerciseId = "ex1",
                order = 0,
                sets = 3,
                reps = 10,
                restSeconds = 60,
                notes = "Warmup set",
            )

        val result = document.toRoutineExercise()

        result.exerciseId shouldBeEqualTo "ex1"
        result.order shouldBe 0
        result.sets shouldBe 3
        result.reps shouldBe 10
        result.restSeconds shouldBe 60
        result.notes shouldBeEqualTo "Warmup set"
    }

    @Test
    fun `GIVEN routine exercise doc with null optionals WHEN toRoutineExercise THEN null fields are kept`() {
        val document =
            RoutineExerciseDocument(
                exerciseId = "ex1",
                order = 0,
                sets = 3,
                reps = null,
                restSeconds = null,
                notes = null,
            )

        val result = document.toRoutineExercise()

        result.reps shouldBe null
        result.restSeconds shouldBe null
        result.notes shouldBe null
    }

    @Test
    fun `GIVEN routine WHEN toRoutineDocument is called THEN document is returned with correct fields`() {
        val routine =
            Routine(
                id = "routine-456",
                userId = "user-456",
                name = "Upper Body Workout",
                description = "Focus on chest and back",
                exercises =
                    listOf(
                        RoutineExercise(
                            exerciseId = "ex3",
                            order = 0,
                            sets = 4,
                            reps = 8,
                            restSeconds = 120,
                            notes = "Heavy weight",
                        ),
                    ),
                createdAt = 1234567890L,
                updatedAt = 1234567900L,
            )

        val result = routine.toRoutineDocument()

        result.id shouldBeEqualTo "routine-456"
        result.userId shouldBeEqualTo "user-456"
        result.name shouldBeEqualTo "Upper Body Workout"
        result.description shouldBeEqualTo "Focus on chest and back"
        result.exercises.size shouldBe 1
        result.exercises[0].exerciseId shouldBeEqualTo "ex3"
        result.exercises[0].order shouldBe 0
        result.exercises[0].sets shouldBe 4
        result.exercises[0].reps shouldBe 8
        result.exercises[0].restSeconds shouldBe 120
        result.exercises[0].notes shouldBeEqualTo "Heavy weight"
        result.createdAt shouldBeEqualTo 1234567890L
        result.updatedAt shouldBeEqualTo 1234567900L
    }

    @Test
    fun `GIVEN routine with null fields WHEN toRoutineDocument is called THEN document is returned with null fields`() {
        val routine =
            Routine(
                id = "routine-789",
                userId = "user-789",
                name = "Leg Day",
                description = null,
                exercises = emptyList(),
                createdAt = 1234567890L,
                updatedAt = 1234567900L,
            )

        val result = routine.toRoutineDocument()

        result.description shouldBe null
        result.exercises.size shouldBe 0
    }

    @Test
    fun `GIVEN routine exercise WHEN toRoutineExerciseDocument is called THEN document is returned with correct fields`() {
        val routineExercise =
            RoutineExercise(
                exerciseId = "ex4",
                order = 2,
                sets = 5,
                reps = 15,
                restSeconds = 45,
                notes = "Light weight, high reps",
            )

        val result = routineExercise.toRoutineExerciseDocument()

        result.exerciseId shouldBeEqualTo "ex4"
        result.order shouldBe 2
        result.sets shouldBe 5
        result.reps shouldBe 15
        result.restSeconds shouldBe 45
        result.notes shouldBeEqualTo "Light weight, high reps"
    }

    @Test
    fun `GIVEN routine exercise with null fields WHEN toRoutineExerciseDocument is called THEN document is returned with null fields`() {
        val routineExercise =
            RoutineExercise(
                exerciseId = "ex5",
                order = 0,
                sets = 3,
                reps = null,
                restSeconds = null,
                notes = null,
            )

        val result = routineExercise.toRoutineExerciseDocument()

        result.reps shouldBe null
        result.restSeconds shouldBe null
        result.notes shouldBe null
    }

    @Test
    fun `GIVEN firestore exception with NOT_FOUND code WHEN toRoutineException is called THEN RoutineNotFound is returned`() {
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.NOT_FOUND
            }

        val result = exception.toRoutineException()

        result shouldBe RoutineException.RoutineNotFound
    }

    @Test
    fun `GIVEN firestore exception with PERMISSION_DENIED code WHEN toRoutineException is called THEN UnauthorizedAccess is returned`() {
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.PERMISSION_DENIED
            }

        val result = exception.toRoutineException()

        result shouldBe RoutineException.UnauthorizedAccess
    }

    @Test
    fun `GIVEN firestore exception with UNAVAILABLE code WHEN toRoutineException is called THEN NetworkError is returned`() {
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.UNAVAILABLE
            }

        val result = exception.toRoutineException()

        result shouldBe RoutineException.NetworkError
    }

    @Test
    fun `GIVEN firestore exception with unknown code WHEN toRoutineException is called THEN Unknown is returned with message`() {
        val errorMessage = "Unknown error occurred"
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.ABORTED
                every { message } returns errorMessage
            }

        val result = exception.toRoutineException()

        result shouldBeEqualTo RoutineException.Unknown(errorMessage)
    }
}
