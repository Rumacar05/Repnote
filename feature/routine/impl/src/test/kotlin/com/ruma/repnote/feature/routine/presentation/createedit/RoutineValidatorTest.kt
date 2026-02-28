package com.ruma.repnote.feature.routine.presentation.createedit

import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RoutineValidatorTest {
    private lateinit var validator: RoutineValidator

    @BeforeEach
    fun setup() {
        validator = RoutineValidator()
    }

    @Test
    fun `WHEN name is empty THEN validation fails with error message`() {
        val result = validator.validate("", listOf(createValidExercise()))

        result shouldBeEqualTo "Routine name cannot be empty"
    }

    @Test
    fun `WHEN name is blank THEN validation fails with error message`() {
        val result = validator.validate("   ", listOf(createValidExercise()))

        result shouldBeEqualTo "Routine name cannot be empty"
    }

    @Test
    fun `WHEN exercises list is empty THEN validation fails with error message`() {
        val result = validator.validate("Valid Name", emptyList())

        result shouldBeEqualTo "Add at least one exercise"
    }

    @Test
    fun `WHEN exercise has zero sets THEN validation fails with error message`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 0,
                reps = 10,
                restSeconds = 60,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBeEqualTo "Sets must be greater than 0"
    }

    @Test
    fun `WHEN exercise has negative sets THEN validation fails with error message`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = -1,
                reps = 10,
                restSeconds = 60,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBeEqualTo "Sets must be greater than 0"
    }

    @Test
    fun `WHEN exercise has zero reps THEN validation fails with error message`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = 0,
                restSeconds = 60,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBeEqualTo "Reps must be greater than 0"
    }

    @Test
    fun `WHEN exercise has negative reps THEN validation fails with error message`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = -5,
                restSeconds = 60,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBeEqualTo "Reps must be greater than 0"
    }

    @Test
    fun `WHEN exercise has null reps THEN validation passes`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = null,
                restSeconds = 60,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBe null
    }

    @Test
    fun `WHEN exercise has negative rest seconds THEN validation fails with error message`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = 10,
                restSeconds = -10,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBeEqualTo "Rest time cannot be negative"
    }

    @Test
    fun `WHEN exercise has zero rest seconds THEN validation passes`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = 10,
                restSeconds = 0,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBe null
    }

    @Test
    fun `WHEN exercise has null rest seconds THEN validation passes`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = 10,
                restSeconds = null,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise))

        result shouldBe null
    }

    @Test
    fun `WHEN all fields are valid THEN validation passes`() {
        val exercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 3,
                reps = 10,
                restSeconds = 60,
                notes = "Test notes",
            )

        val result = validator.validate("Valid Routine Name", listOf(exercise))

        result shouldBe null
    }

    @Test
    fun `WHEN multiple exercises and first is invalid THEN returns error for first exercise`() {
        val invalidExercise =
            ExerciseWithConfig(
                exerciseId = "exercise-1",
                exerciseName = "Bench Press",
                order = 0,
                sets = 0,
                reps = 10,
                restSeconds = 60,
                notes = null,
            )
        val validExercise = createValidExercise()

        val result = validator.validate("Valid Name", listOf(invalidExercise, validExercise))

        result shouldBeEqualTo "Sets must be greater than 0"
    }

    @Test
    fun `WHEN multiple exercises and second is invalid THEN returns error for second exercise`() {
        val validExercise = createValidExercise()
        val invalidExercise =
            ExerciseWithConfig(
                exerciseId = "exercise-2",
                exerciseName = "Squat",
                order = 1,
                sets = 3,
                reps = -1,
                restSeconds = 60,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(validExercise, invalidExercise))

        result shouldBeEqualTo "Reps must be greater than 0"
    }

    @Test
    fun `WHEN multiple valid exercises THEN validation passes`() {
        val exercise1 = createValidExercise()
        val exercise2 =
            ExerciseWithConfig(
                exerciseId = "exercise-2",
                exerciseName = "Squat",
                order = 1,
                sets = 4,
                reps = 8,
                restSeconds = 90,
                notes = null,
            )

        val result = validator.validate("Valid Name", listOf(exercise1, exercise2))

        result shouldBe null
    }

    private fun createValidExercise() =
        ExerciseWithConfig(
            exerciseId = "exercise-1",
            exerciseName = "Bench Press",
            order = 0,
            sets = 3,
            reps = 10,
            restSeconds = 60,
            notes = null,
        )
}
