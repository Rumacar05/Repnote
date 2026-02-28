package com.ruma.repnote.feature.routine.presentation.detail

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.feature.routine.presentation.detail.components.RoutineDetailContent
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for RoutineDetailScreen using Paparazzi.
 *
 * Run tests with: ./gradlew :feature:routine:impl:testDebug
 * Record screenshots with: ./gradlew :feature:routine:impl:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :feature:routine:impl:verifyPaparazziDebug
 */
class RoutineDetailScreenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    private val mockRoutine =
        Routine(
            id = "routine-1",
            userId = "user-1",
            name = "Upper Body Strength",
            description = "Focus on building strength in chest, back, shoulders, and arms",
            exercises =
                listOf(
                    RoutineExercise(
                        exerciseId = "exercise-1",
                        order = 0,
                        sets = 4,
                        reps = 8,
                        restSeconds = 90,
                        notes = "Heavy weight, focus on form",
                    ),
                    RoutineExercise(
                        exerciseId = "exercise-2",
                        order = 1,
                        sets = 3,
                        reps = 10,
                        restSeconds = 60,
                        notes = null,
                    ),
                    RoutineExercise(
                        exerciseId = "exercise-3",
                        order = 2,
                        sets = 3,
                        reps = 12,
                        restSeconds = 45,
                        notes = null,
                    ),
                ),
            createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
            updatedAt = System.currentTimeMillis(),
        )

    private val mockExercises =
        listOf(
            ExerciseDetail(
                exerciseId = "exercise-1",
                name = "Bench Press",
                imageUrl = null,
                sets = 4,
                reps = 8,
                restSeconds = 90,
                notes = "Heavy weight, focus on form",
            ),
            ExerciseDetail(
                exerciseId = "exercise-2",
                name = "Bent Over Row",
                imageUrl = null,
                sets = 3,
                reps = 10,
                restSeconds = 60,
                notes = null,
            ),
            ExerciseDetail(
                exerciseId = "exercise-3",
                name = "Overhead Press",
                imageUrl = null,
                sets = 3,
                reps = 12,
                restSeconds = 45,
                notes = null,
            ),
        )

    @Test
    fun routineDetailScreen_loading_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutineDetailContent(
                    routine = null,
                    exercises = emptyList(),
                    isLoading = true,
                    errorMessage = null,
                )
            }
        }
    }

    @Test
    fun routineDetailScreen_with_routine() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutineDetailContent(
                    routine = mockRoutine,
                    exercises = mockExercises,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    @Test
    fun routineDetailScreen_with_routine_no_description() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutineDetailContent(
                    routine = mockRoutine.copy(description = null),
                    exercises = mockExercises,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    @Test
    fun routineDetailScreen_error_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutineDetailContent(
                    routine = null,
                    exercises = emptyList(),
                    isLoading = false,
                    errorMessage = "Failed to load routine details. Please try again.",
                )
            }
        }
    }

    @Test
    fun routineDetailScreen_dark_theme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                RoutineDetailContent(
                    routine = mockRoutine,
                    exercises = mockExercises,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }
}
