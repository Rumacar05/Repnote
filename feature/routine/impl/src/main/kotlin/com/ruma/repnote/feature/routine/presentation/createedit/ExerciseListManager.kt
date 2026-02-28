package com.ruma.repnote.feature.routine.presentation.createedit

import com.ruma.repnote.core.domain.model.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class ExerciseListManager(
    private val uiState: MutableStateFlow<CreateEditRoutineUiState>,
) {
    fun showExerciseSelector() {
        uiState.update { it.copy(showExerciseSelector = true) }
    }

    fun updateSearchQuery(query: String) {
        uiState.update { it.copy(exerciseSearchQuery = query) }
    }

    fun selectExercise(
        exerciseId: String,
        exerciseName: String,
    ) {
        val currentExercises = uiState.value.exercisesWithConfig

        if (currentExercises.any { it.exerciseId == exerciseId }) {
            return
        }

        val newExercise =
            ExerciseWithConfig(
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                order = currentExercises.size,
                sets = 3,
                reps = 10,
                restSeconds = 60,
                notes = null,
            )

        uiState.update {
            it.copy(
                exercisesWithConfig = currentExercises + newExercise,
                showExerciseSelector = false,
                exerciseSearchQuery = "",
            )
        }
    }

    fun dismissSelector() {
        uiState.update {
            it.copy(
                showExerciseSelector = false,
                exerciseSearchQuery = "",
            )
        }
    }

    fun removeExercise(index: Int) {
        val currentExercises = uiState.value.exercisesWithConfig.toMutableList()
        currentExercises.removeAt(index)

        val reorderedExercises =
            currentExercises.mapIndexed { newIndex, exercise ->
                exercise.copy(order = newIndex)
            }

        uiState.update { it.copy(exercisesWithConfig = reorderedExercises) }
    }

    fun reorderExercises(
        fromIndex: Int,
        toIndex: Int,
    ) {
        val currentExercises = uiState.value.exercisesWithConfig.toMutableList()
        val exercise = currentExercises.removeAt(fromIndex)
        currentExercises.add(toIndex, exercise)

        val reorderedExercises =
            currentExercises.mapIndexed { index, ex ->
                ex.copy(order = index)
            }

        uiState.update { it.copy(exercisesWithConfig = reorderedExercises) }
    }

    fun updateExerciseConfig(
        index: Int,
        sets: Int,
        reps: Int?,
        restSeconds: Int?,
        notes: String,
    ) {
        val currentExercises = uiState.value.exercisesWithConfig.toMutableList()
        if (index in currentExercises.indices) {
            currentExercises[index] =
                currentExercises[index].copy(
                    sets = sets,
                    reps = reps,
                    restSeconds = restSeconds,
                    notes = notes.ifBlank { null },
                )
            uiState.update { it.copy(exercisesWithConfig = currentExercises) }
        }
    }

    fun updateAvailableExercises(exercises: List<Exercise>) {
        uiState.update { it.copy(availableExercises = exercises) }
    }
}
