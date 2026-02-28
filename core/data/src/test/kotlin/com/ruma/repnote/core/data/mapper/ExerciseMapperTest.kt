package com.ruma.repnote.core.data.mapper

import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.model.ExerciseDocument
import com.ruma.repnote.core.data.model.ExerciseTranslationDocument
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.MuscleGroup
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test

class ExerciseMapperTest {
    @Test
    fun `GIVEN exercise document with translation WHEN toExercise is called THEN exercise is returned with all fields`() {
        val document =
            ExerciseDocument(
                id = "ex-123",
                imageUrl = "https://example.com/image.jpg",
                primaryMuscleGroup = "CHEST",
                secondaryMuscleGroups = listOf("SHOULDERS", "TRICEPS"),
                isGlobal = true,
                createdBy = "user-123",
            )

        val translation =
            ExerciseTranslationDocument(
                name = "Bench Press",
                description = "Chest exercise",
            )

        val result = document.toExercise(translation)

        result.id shouldBeEqualTo "ex-123"
        result.name shouldBeEqualTo "Bench Press"
        result.description shouldBeEqualTo "Chest exercise"
        result.imageUrl shouldBeEqualTo "https://example.com/image.jpg"
        result.primaryMuscleGroup shouldBe MuscleGroup.CHEST
        result.secondaryMuscleGroups shouldContain MuscleGroup.SHOULDERS
        result.secondaryMuscleGroups shouldContain MuscleGroup.TRICEPS
        result.isGlobal shouldBe true
        result.createdBy shouldBeEqualTo "user-123"
    }

    @Test
    fun `GIVEN exercise document with null imageUrl WHEN toExercise is called THEN exercise is returned with null imageUrl`() {
        val document =
            ExerciseDocument(
                id = "ex-123",
                imageUrl = null,
                primaryMuscleGroup = "QUADS",
                secondaryMuscleGroups = emptyList(),
                isGlobal = false,
                createdBy = null,
            )

        val translation =
            ExerciseTranslationDocument(
                name = "Squat",
                description = "Leg exercise",
            )

        val result = document.toExercise(translation)

        result.imageUrl shouldBe null
        result.createdBy shouldBe null
    }

    @Test
    fun `GIVEN exercise document with empty secondary muscles WHEN toExercise is called THEN exercise is returned with empty list`() {
        val document =
            ExerciseDocument(
                id = "ex-123",
                imageUrl = null,
                primaryMuscleGroup = "BICEPS",
                secondaryMuscleGroups = emptyList(),
                isGlobal = true,
                createdBy = "user-123",
            )

        val translation =
            ExerciseTranslationDocument(
                name = "Bicep Curl",
                description = "Arm exercise",
            )

        val result = document.toExercise(translation)

        result.secondaryMuscleGroups.size shouldBeEqualTo 0
    }

    @Test
    fun `GIVEN exercise WHEN toExerciseDocument is called THEN document is returned with correct fields`() {
        val exercise =
            Exercise(
                id = "ex-456",
                name = "Deadlift",
                description = "Back exercise",
                imageUrl = "https://example.com/deadlift.jpg",
                primaryMuscleGroup = MuscleGroup.BACK,
                secondaryMuscleGroups = listOf(MuscleGroup.GLUTES, MuscleGroup.HAMSTRINGS),
                isGlobal = true,
                createdBy = "user-456",
            )

        val result = exercise.toExerciseDocument()

        result.id shouldBeEqualTo "ex-456"
        result.imageUrl shouldBeEqualTo "https://example.com/deadlift.jpg"
        result.primaryMuscleGroup shouldBeEqualTo "BACK"
        result.secondaryMuscleGroups shouldContain "GLUTES"
        result.secondaryMuscleGroups shouldContain "HAMSTRINGS"
        result.isGlobal shouldBe true
        result.createdBy shouldBeEqualTo "user-456"
    }

    @Test
    fun `GIVEN exercise with null fields WHEN toExerciseDocument is called THEN document is returned with null fields`() {
        val exercise =
            Exercise(
                id = "ex-789",
                name = "Pull-up",
                description = "Back exercise",
                imageUrl = null,
                primaryMuscleGroup = MuscleGroup.BACK,
                secondaryMuscleGroups = emptyList(),
                isGlobal = false,
                createdBy = null,
            )

        val result = exercise.toExerciseDocument()

        result.imageUrl shouldBe null
        result.createdBy shouldBe null
        result.secondaryMuscleGroups.size shouldBeEqualTo 0
    }

    @Test
    fun `GIVEN firestore exception with NOT_FOUND code WHEN toExerciseException is called THEN ExerciseNotFound is returned`() {
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.NOT_FOUND
            }

        val result = exception.toExerciseException()

        result shouldBe ExerciseException.ExerciseNotFound
    }

    @Test
    fun `GIVEN firestore exception with PERMISSION_DENIED code WHEN toExerciseException is called THEN UnauthorizedAccess is returned`() {
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.PERMISSION_DENIED
            }

        val result = exception.toExerciseException()

        result shouldBe ExerciseException.UnauthorizedAccess
    }

    @Test
    fun `GIVEN firestore exception with UNAVAILABLE code WHEN toExerciseException is called THEN NetworkError is returned`() {
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.UNAVAILABLE
            }

        val result = exception.toExerciseException()

        result shouldBe ExerciseException.NetworkError
    }

    @Test
    fun `GIVEN firestore exception with unknown code WHEN toExerciseException is called THEN Unknown is returned with message`() {
        val errorMessage = "Unknown error occurred"
        val exception =
            mockk<FirebaseFirestoreException> {
                every { code } returns FirebaseFirestoreException.Code.ABORTED
                every { message } returns errorMessage
            }

        val result = exception.toExerciseException()

        result shouldBeEqualTo ExerciseException.Unknown(errorMessage)
    }
}
