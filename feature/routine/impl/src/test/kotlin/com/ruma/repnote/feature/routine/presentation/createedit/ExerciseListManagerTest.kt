package com.ruma.repnote.feature.routine.presentation.createedit

import app.cash.turbine.test
import com.ruma.repnote.core.domain.model.Exercise
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseListManagerTest {
    private lateinit var uiState: MutableStateFlow<CreateEditRoutineUiState>
    private lateinit var manager: ExerciseListManager

    @BeforeEach
    fun setup() {
        uiState = MutableStateFlow(CreateEditRoutineUiState())
        manager = ExerciseListManager(uiState)
    }

    @Test
    fun `WHEN showExerciseSelector is called THEN selector is shown`() =
        runTest {
            uiState.test {
                skipItems(1) // Skip initial state
                manager.showExerciseSelector()

                val state = awaitItem()
                state.showExerciseSelector shouldBe true
            }
        }

    @Test
    fun `WHEN updateSearchQuery is called THEN query is updated`() =
        runTest {
            uiState.test {
                skipItems(1)
                manager.updateSearchQuery("Bench Press")

                val state = awaitItem()
                state.exerciseSearchQuery shouldBeEqualTo "Bench Press"
            }
        }

    @Test
    fun `WHEN selectExercise is called THEN exercise is added to list`() =
        runTest {
            uiState.test {
                skipItems(1)
                manager.selectExercise("exercise-1", "Bench Press")

                val state = awaitItem()
                state.exercisesWithConfig.size shouldBeEqualTo 1
                state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-1"
                state.exercisesWithConfig[0].exerciseName shouldBeEqualTo "Bench Press"
                state.exercisesWithConfig[0].order shouldBeEqualTo 0
                state.exercisesWithConfig[0].sets shouldBeEqualTo 3
                state.exercisesWithConfig[0].reps shouldBeEqualTo 10
                state.exercisesWithConfig[0].restSeconds shouldBeEqualTo 60
                state.exercisesWithConfig[0].notes shouldBe null
                state.showExerciseSelector shouldBe false
                state.exerciseSearchQuery shouldBeEqualTo ""
            }
        }

    @Test
    fun `WHEN selectExercise is called with duplicate exercise THEN exercise is not added`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")

            uiState.test {
                skipItems(1)
                manager.selectExercise("exercise-1", "Bench Press")

                expectNoEvents()
            }
        }

    @Test
    fun `WHEN multiple exercises are selected THEN they are added with correct order`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")
            manager.selectExercise("exercise-2", "Squat")
            manager.selectExercise("exercise-3", "Deadlift")

            uiState.test {
                val state = awaitItem()
                state.exercisesWithConfig.size shouldBeEqualTo 3
                state.exercisesWithConfig[0].order shouldBeEqualTo 0
                state.exercisesWithConfig[1].order shouldBeEqualTo 1
                state.exercisesWithConfig[2].order shouldBeEqualTo 2
            }
        }

    @Test
    fun `WHEN dismissSelector is called THEN selector is hidden and query is cleared`() =
        runTest {
            manager.showExerciseSelector()
            manager.updateSearchQuery("Bench")

            uiState.test {
                skipItems(1)
                manager.dismissSelector()

                val state = awaitItem()
                state.showExerciseSelector shouldBe false
                state.exerciseSearchQuery shouldBeEqualTo ""
            }
        }

    @Test
    fun `WHEN removeExercise is called THEN exercise is removed and remaining are reordered`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")
            manager.selectExercise("exercise-2", "Squat")
            manager.selectExercise("exercise-3", "Deadlift")

            uiState.test {
                skipItems(1)
                manager.removeExercise(1) // Remove Squat

                val state = awaitItem()
                state.exercisesWithConfig.size shouldBeEqualTo 2
                state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-1"
                state.exercisesWithConfig[0].order shouldBeEqualTo 0
                state.exercisesWithConfig[1].exerciseId shouldBeEqualTo "exercise-3"
                state.exercisesWithConfig[1].order shouldBeEqualTo 1
            }
        }

    @Test
    fun `WHEN reorderExercises is called THEN exercises are reordered correctly`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")
            manager.selectExercise("exercise-2", "Squat")
            manager.selectExercise("exercise-3", "Deadlift")

            uiState.test {
                skipItems(1)
                manager.reorderExercises(0, 2) // Move Bench Press to end

                val state = awaitItem()
                state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-2"
                state.exercisesWithConfig[0].order shouldBeEqualTo 0
                state.exercisesWithConfig[1].exerciseId shouldBeEqualTo "exercise-3"
                state.exercisesWithConfig[1].order shouldBeEqualTo 1
                state.exercisesWithConfig[2].exerciseId shouldBeEqualTo "exercise-1"
                state.exercisesWithConfig[2].order shouldBeEqualTo 2
            }
        }

    @Test
    fun `WHEN reorderExercises moves item down THEN order is updated correctly`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")
            manager.selectExercise("exercise-2", "Squat")
            manager.selectExercise("exercise-3", "Deadlift")

            uiState.test {
                skipItems(1)
                manager.reorderExercises(2, 0) // Move Deadlift to start

                val state = awaitItem()
                state.exercisesWithConfig[0].exerciseId shouldBeEqualTo "exercise-3"
                state.exercisesWithConfig[1].exerciseId shouldBeEqualTo "exercise-1"
                state.exercisesWithConfig[2].exerciseId shouldBeEqualTo "exercise-2"
            }
        }

    @Test
    fun `WHEN updateExerciseConfig is called THEN config is updated`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")

            uiState.test {
                skipItems(1)
                manager.updateExerciseConfig(0, 5, 12, 90, "Heavy weight")

                val state = awaitItem()
                state.exercisesWithConfig[0].sets shouldBeEqualTo 5
                state.exercisesWithConfig[0].reps shouldBeEqualTo 12
                state.exercisesWithConfig[0].restSeconds shouldBeEqualTo 90
                state.exercisesWithConfig[0].notes shouldBeEqualTo "Heavy weight"
            }
        }

    @Test
    fun `WHEN updateExerciseConfig is called with blank notes THEN notes are set to null`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")

            manager.updateExerciseConfig(0, 3, 10, 60, "   ")

            val state = uiState.value
            state.exercisesWithConfig[0].notes shouldBe null
        }

    @Test
    fun `WHEN updateExerciseConfig is called with invalid index THEN state is not updated`() =
        runTest {
            manager.selectExercise("exercise-1", "Bench Press")

            uiState.test {
                skipItems(1)
                manager.updateExerciseConfig(5, 5, 12, 90, "Notes")

                expectNoEvents()
            }
        }

    @Test
    fun `WHEN updateAvailableExercises is called THEN available exercises are updated`() =
        runTest {
            val exercises =
                listOf(
                    Exercise(
                        id = "exercise-1",
                        name = "Bench Press",
                        description = "Chest exercise",
                        imageUrl = null,
                        primaryMuscleGroup = com.ruma.repnote.core.domain.model.MuscleGroup.CHEST,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                    ),
                    Exercise(
                        id = "exercise-2",
                        name = "Squat",
                        description = "Leg exercise",
                        imageUrl = null,
                        primaryMuscleGroup = com.ruma.repnote.core.domain.model.MuscleGroup.QUADS,
                        secondaryMuscleGroups = emptyList(),
                        isGlobal = true,
                        createdBy = null,
                    ),
                )

            uiState.test {
                skipItems(1)
                manager.updateAvailableExercises(exercises)

                val state = awaitItem()
                state.availableExercises.size shouldBeEqualTo 2
                state.availableExercises shouldContain exercises[0]
                state.availableExercises shouldContain exercises[1]
            }
        }
}
