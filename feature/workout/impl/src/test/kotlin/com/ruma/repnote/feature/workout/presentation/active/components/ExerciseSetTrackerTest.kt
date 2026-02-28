package com.ruma.repnote.feature.workout.presentation.active.components

import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.domain.model.CompletedSet
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.feature.workout.presentation.active.SetInputState
import org.junit.Rule
import org.junit.Test

/**
 * Paparazzi snapshot tests for ExerciseSetTracker component.
 */
class ExerciseSetTrackerTest {
    @get:Rule
    val paparazzi = Paparazzi()

    private val testExercise =
        WorkoutExercise(
            exerciseId = "exercise-1",
            exerciseName = "Bench Press",
            order = 0,
            targetSets = 3,
            targetReps = 10,
            targetRestSeconds = 90,
            completedSets =
                listOf(
                    CompletedSet(
                        setNumber = 1,
                        reps = 10,
                        weight = 100.0,
                        completedAt = 1000L,
                        restTimerSeconds = 90,
                        notes = "",
                    ),
                ),
            notes = "",
        )

    private val testInputState =
        SetInputState(
            reps = "10",
            weight = "100.0",
            notes = "",
        )

    @Test
    fun `ExerciseSetTracker displays with completed sets`() {
        paparazzi.snapshot {
            ExerciseSetTracker(
                exercise = testExercise,
                currentSetNumber = 2,
                exerciseNumber = 1,
                totalExercises = 3,
                inputState = testInputState,
                onRepsChange = {},
                onWeightChange = {},
                onNotesChange = {},
                onCompleteSet = {},
            )
        }
    }

    @Test
    fun `ExerciseSetTracker displays with no completed sets`() {
        paparazzi.snapshot {
            ExerciseSetTracker(
                exercise = testExercise.copy(completedSets = emptyList()),
                currentSetNumber = 1,
                exerciseNumber = 1,
                totalExercises = 3,
                inputState = testInputState,
                onRepsChange = {},
                onWeightChange = {},
                onNotesChange = {},
                onCompleteSet = {},
            )
        }
    }

    @Test
    fun `ExerciseSetTracker displays as first exercise`() {
        paparazzi.snapshot {
            ExerciseSetTracker(
                exercise = testExercise,
                currentSetNumber = 2,
                exerciseNumber = 1,
                totalExercises = 3,
                inputState = testInputState,
                onRepsChange = {},
                onWeightChange = {},
                onNotesChange = {},
                onCompleteSet = {},
            )
        }
    }

    @Test
    fun `ExerciseSetTracker displays with all sets completed`() {
        paparazzi.snapshot {
            ExerciseSetTracker(
                exercise =
                    testExercise.copy(
                        completedSets =
                            listOf(
                                CompletedSet(1, 10, 100.0, 1000L, 90, ""),
                                CompletedSet(2, 10, 100.0, 2000L, 90, ""),
                                CompletedSet(3, 10, 100.0, 3000L, 90, ""),
                            ),
                    ),
                currentSetNumber = 4,
                exerciseNumber = 2,
                totalExercises = 3,
                inputState = testInputState,
                onRepsChange = {},
                onWeightChange = {},
                onNotesChange = {},
                onCompleteSet = {},
            )
        }
    }
}
