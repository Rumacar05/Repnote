package com.ruma.repnote.core.database.converter

import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.model.RoutineExercise
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

class TypeConvertersTest {
    private val converters = TypeConverters()

    @Test
    fun `GIVEN list of muscle groups WHEN fromMuscleGroupList is called THEN comma-separated string is returned`() {
        val muscleGroups = listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)

        val result = converters.fromMuscleGroupList(muscleGroups)

        result shouldBeEqualTo "CHEST,TRICEPS,SHOULDERS"
    }

    @Test
    fun `GIVEN empty list of muscle groups WHEN fromMuscleGroupList is called THEN empty string is returned`() {
        val muscleGroups = emptyList<MuscleGroup>()

        val result = converters.fromMuscleGroupList(muscleGroups)

        result shouldBeEqualTo ""
    }

    @Test
    fun `GIVEN null muscle groups WHEN fromMuscleGroupList is called THEN null is returned`() {
        val result = converters.fromMuscleGroupList(null)

        result.shouldBeNull()
    }

    @Test
    fun `GIVEN comma-separated string WHEN toMuscleGroupList is called THEN list of muscle groups is returned`() {
        val muscleGroupString = "CHEST,TRICEPS,SHOULDERS"

        val result = converters.toMuscleGroupList(muscleGroupString)

        result shouldBeEqualTo listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS, MuscleGroup.SHOULDERS)
    }

    @Test
    fun `GIVEN empty string WHEN toMuscleGroupList is called THEN empty list is returned`() {
        val result = converters.toMuscleGroupList("")

        result shouldBeEqualTo emptyList()
    }

    @Test
    fun `GIVEN null string WHEN toMuscleGroupList is called THEN null is returned`() {
        val result = converters.toMuscleGroupList(null)

        result.shouldBeNull()
    }

    @Test
    fun `GIVEN string with invalid muscle group WHEN toMuscleGroupList is called THEN invalid entries are ignored`() {
        val muscleGroupString = "CHEST,INVALID,TRICEPS"

        val result = converters.toMuscleGroupList(muscleGroupString)

        result shouldBeEqualTo listOf(MuscleGroup.CHEST, MuscleGroup.TRICEPS)
    }

    @Test
    fun `GIVEN list of routine exercises WHEN fromRoutineExerciseList is called THEN semicolon-separated string is returned`() {
        val exercises =
            listOf(
                RoutineExercise(
                    exerciseId = "ex1",
                    order = 0,
                    sets = 3,
                    reps = 10,
                    restSeconds = 60,
                    notes = "Warmup set",
                ),
                RoutineExercise(
                    exerciseId = "ex2",
                    order = 1,
                    sets = 4,
                    reps = 12,
                    restSeconds = 90,
                    notes = null,
                ),
            )

        val result = converters.fromRoutineExerciseList(exercises)

        result shouldBeEqualTo "ex1,0,3,10,60,Warmup set;ex2,1,4,12,90,"
    }

    @Test
    fun `GIVEN routine exercises with null reps and restSeconds WHEN fromRoutineExerciseList is called THEN empty values are used`() {
        val exercises =
            listOf(
                RoutineExercise(
                    exerciseId = "ex1",
                    order = 0,
                    sets = 3,
                    reps = null,
                    restSeconds = null,
                    notes = null,
                ),
            )

        val result = converters.fromRoutineExerciseList(exercises)

        result shouldBeEqualTo "ex1,0,3,,,"
    }

    @Test
    fun `GIVEN empty list WHEN fromRoutineExerciseList is called THEN empty string is returned`() {
        val result = converters.fromRoutineExerciseList(emptyList())

        result shouldBeEqualTo ""
    }

    @Test
    fun `GIVEN null routine exercises WHEN fromRoutineExerciseList is called THEN null is returned`() {
        val result = converters.fromRoutineExerciseList(null)

        result.shouldBeNull()
    }

    @Test
    fun `GIVEN semicolon-separated string WHEN toRoutineExerciseList is called THEN list of routine exercises is returned`() {
        val exerciseString = "ex1,0,3,10,60,Warmup set;ex2,1,4,12,90,"

        val result = converters.toRoutineExerciseList(exerciseString)

        result shouldBeEqualTo
            listOf(
                RoutineExercise(
                    exerciseId = "ex1",
                    order = 0,
                    sets = 3,
                    reps = 10,
                    restSeconds = 60,
                    notes = "Warmup set",
                ),
                RoutineExercise(
                    exerciseId = "ex2",
                    order = 1,
                    sets = 4,
                    reps = 12,
                    restSeconds = 90,
                    notes = null,
                ),
            )
    }

    @Test
    fun `GIVEN string with empty reps and restSeconds WHEN toRoutineExerciseList is called THEN null values are returned`() {
        val exerciseString = "ex1,0,3,,,"

        val result = converters.toRoutineExerciseList(exerciseString)

        result?.first()?.reps.shouldBeNull()
        result?.first()?.restSeconds.shouldBeNull()
        result?.first()?.notes.shouldBeNull()
    }

    @Test
    fun `GIVEN empty string WHEN toRoutineExerciseList is called THEN empty list is returned`() {
        val result = converters.toRoutineExerciseList("")

        result shouldBeEqualTo emptyList()
    }

    @Test
    fun `GIVEN null string WHEN toRoutineExerciseList is called THEN empty list is returned`() {
        val result = converters.toRoutineExerciseList(null)

        result shouldBeEqualTo emptyList()
    }

    @Test
    fun `GIVEN string with malformed exercise data WHEN toRoutineExerciseList is called THEN malformed entries are ignored`() {
        val exerciseString = "ex1,0,3,10,60,Note;invalid;ex2,1,4,12,90,"

        val result = converters.toRoutineExerciseList(exerciseString)

        result?.size shouldBe 2
        result?.first()?.exerciseId shouldBeEqualTo "ex1"
        result?.last()?.exerciseId shouldBeEqualTo "ex2"
    }

    @Test
    fun `GIVEN string with insufficient parts WHEN toRoutineExerciseList is called THEN entry is ignored`() {
        val exerciseString = "ex1,0;ex2,1,4,12,90,"

        val result = converters.toRoutineExerciseList(exerciseString)

        result?.size shouldBe 1
        result?.first()?.exerciseId shouldBeEqualTo "ex2"
    }
}
