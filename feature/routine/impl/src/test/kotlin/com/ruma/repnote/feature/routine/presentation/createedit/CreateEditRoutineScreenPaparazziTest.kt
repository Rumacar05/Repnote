package com.ruma.repnote.feature.routine.presentation.createedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import com.ruma.repnote.feature.routine.presentation.components.ExerciseConfigCard
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for CreateEditRoutineScreen using Paparazzi.
 *
 * Run tests with: ./gradlew :feature:routine:impl:testDebug
 * Record screenshots with: ./gradlew :feature:routine:impl:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :feature:routine:impl:verifyPaparazziDebug
 */
class CreateEditRoutineScreenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    @Test
    fun createEditRoutineScreen_initial_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                CreateEditRoutinePreview(
                    name = "",
                    description = "",
                    exercises = emptyList(),
                    isNameValid = true,
                    errorMessage = null,
                )
            }
        }
    }

    @Test
    fun createEditRoutineScreen_with_filled_fields() {
        paparazzi.snapshot {
            RepnoteTheme {
                CreateEditRoutinePreview(
                    name = "Upper Body Strength",
                    description = "Focus on building strength in chest, back, and shoulders",
                    exercises = emptyList(),
                    isNameValid = true,
                    errorMessage = null,
                )
            }
        }
    }

    @Test
    fun createEditRoutineScreen_with_exercises() {
        val exercises =
            listOf(
                ExerciseWithConfig(
                    exerciseId = "exercise-1",
                    exerciseName = "Bench Press",
                    order = 0,
                    sets = 4,
                    reps = 8,
                    restSeconds = 90,
                    notes = "Heavy weight",
                ),
                ExerciseWithConfig(
                    exerciseId = "exercise-2",
                    exerciseName = "Bent Over Row",
                    order = 1,
                    sets = 3,
                    reps = 10,
                    restSeconds = 60,
                    notes = null,
                ),
                ExerciseWithConfig(
                    exerciseId = "exercise-3",
                    exerciseName = "Overhead Press",
                    order = 2,
                    sets = 3,
                    reps = 12,
                    restSeconds = 45,
                    notes = null,
                ),
            )

        paparazzi.snapshot {
            RepnoteTheme {
                CreateEditRoutinePreview(
                    name = "Upper Body Strength",
                    description = "Focus on chest, back, and shoulders",
                    exercises = exercises,
                    isNameValid = true,
                    errorMessage = null,
                )
            }
        }
    }

    @Test
    fun createEditRoutineScreen_validation_error() {
        paparazzi.snapshot {
            RepnoteTheme {
                CreateEditRoutinePreview(
                    name = "",
                    description = "",
                    exercises = emptyList(),
                    isNameValid = false,
                    errorMessage = "Routine name cannot be empty",
                )
            }
        }
    }

    @Test
    fun createEditRoutineScreen_dark_theme() {
        val exercises =
            listOf(
                ExerciseWithConfig(
                    exerciseId = "exercise-1",
                    exerciseName = "Bench Press",
                    order = 0,
                    sets = 4,
                    reps = 8,
                    restSeconds = 90,
                    notes = null,
                ),
            )

        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                CreateEditRoutinePreview(
                    name = "Upper Body Strength",
                    description = "Test description",
                    exercises = exercises,
                    isNameValid = true,
                    errorMessage = null,
                )
            }
        }
    }

    @Composable
    private fun CreateEditRoutinePreview(
        name: String,
        description: String,
        exercises: List<ExerciseWithConfig>,
        isNameValid: Boolean,
        errorMessage: String?,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {},
                label = { Text("Routine Name") },
                isError = !isNameValid,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = description,
                onValueChange = {},
                label = { Text("Description (Optional)") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                minLines = 3,
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            exercises.forEachIndexed { index, exercise ->
                ExerciseConfigCard(
                    exerciseName = exercise.exerciseName,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    restSeconds = exercise.restSeconds,
                    notes = exercise.notes ?: "",
                    onSetsChange = {},
                    onRepsChange = {},
                    onRestSecondsChange = {},
                    onNotesChange = {},
                    onRemove = {},
                    isDragging = false,
                    dragHandleModifier = Modifier,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }
    }
}
