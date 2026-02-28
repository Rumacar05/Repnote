package com.ruma.repnote.feature.routine.presentation

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineExercise
import com.ruma.repnote.feature.routine.presentation.components.RoutinesContent
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for RoutinesScreen using Paparazzi.
 *
 * Run tests with: ./gradlew :feature:routine:impl:testDebug
 * Record screenshots with: ./gradlew :feature:routine:impl:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :feature:routine:impl:verifyPaparazziDebug
 */
class RoutinesScreenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    private val mockRoutines =
        listOf(
            Routine(
                id = "routine-1",
                userId = "user-1",
                name = "Upper Body Strength",
                description = "Focus on chest, back, and arms",
                exercises =
                    listOf(
                        RoutineExercise(
                            exerciseId = "exercise-1",
                            order = 0,
                            sets = 4,
                            reps = 8,
                            restSeconds = 90,
                            notes = null,
                        ),
                        RoutineExercise(
                            exerciseId = "exercise-2",
                            order = 1,
                            sets = 3,
                            reps = 10,
                            restSeconds = 60,
                            notes = null,
                        ),
                    ),
                createdAt = 0,
                updatedAt = 0,
            ),
            Routine(
                id = "routine-2",
                userId = "user-1",
                name = "Leg Day",
                description = "Squats and deadlifts",
                exercises =
                    listOf(
                        RoutineExercise(
                            exerciseId = "exercise-3",
                            order = 0,
                            sets = 5,
                            reps = 5,
                            restSeconds = 120,
                            notes = null,
                        ),
                    ),
                createdAt = 0,
                updatedAt = 0,
            ),
            Routine(
                id = "routine-3",
                userId = "user-1",
                name = "Full Body Workout",
                description = null,
                exercises =
                    listOf(
                        RoutineExercise(
                            exerciseId = "exercise-4",
                            order = 0,
                            sets = 3,
                            reps = 12,
                            restSeconds = 60,
                            notes = null,
                        ),
                    ),
                createdAt = 0,
                updatedAt = 0,
            ),
        )

    @Test
    fun routinesScreen_loading_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutinesContent(
                    routines = emptyList(),
                    isLoading = true,
                    errorMessage = null,
                    hasActiveWorkout = false,
                    onRoutineClick = {},
                    onStartWorkout = {},
                    onResumeWorkout = {},
                )
            }
        }
    }

    @Test
    fun routinesScreen_with_routines() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutinesContent(
                    routines = mockRoutines,
                    isLoading = false,
                    errorMessage = null,
                    hasActiveWorkout = false,
                    onRoutineClick = {},
                    onStartWorkout = {},
                    onResumeWorkout = {},
                )
            }
        }
    }

    @Test
    fun routinesScreen_empty_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutinesContent(
                    routines = emptyList(),
                    isLoading = false,
                    errorMessage = null,
                    hasActiveWorkout = false,
                    onRoutineClick = {},
                    onStartWorkout = {},
                    onResumeWorkout = {},
                )
            }
        }
    }

    @Test
    fun routinesScreen_with_active_workout() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutinesContent(
                    routines = mockRoutines,
                    isLoading = false,
                    errorMessage = null,
                    hasActiveWorkout = true,
                    onRoutineClick = {},
                    onStartWorkout = {},
                    onResumeWorkout = {},
                )
            }
        }
    }

    @Test
    fun routinesScreen_error_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RoutinesContent(
                    routines = emptyList(),
                    isLoading = false,
                    errorMessage = "Failed to load routines. Please try again.",
                    hasActiveWorkout = false,
                    onRoutineClick = {},
                    onStartWorkout = {},
                    onResumeWorkout = {},
                )
            }
        }
    }

    @Test
    fun routinesScreen_dark_theme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                RoutinesContent(
                    routines = mockRoutines,
                    isLoading = false,
                    errorMessage = null,
                    hasActiveWorkout = false,
                    onRoutineClick = {},
                    onStartWorkout = {},
                    onResumeWorkout = {},
                )
            }
        }
    }
}
