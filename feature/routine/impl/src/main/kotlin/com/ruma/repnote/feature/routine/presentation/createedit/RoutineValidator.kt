package com.ruma.repnote.feature.routine.presentation.createedit

internal class RoutineValidator {
    fun validate(
        name: String,
        exercises: List<ExerciseWithConfig>,
    ): String? =
        when {
            name.trim().isBlank() -> "Routine name cannot be empty"
            exercises.isEmpty() -> "Add at least one exercise"
            else -> validateExerciseConfigs(exercises)
        }

    private fun validateExerciseConfigs(exercises: List<ExerciseWithConfig>): String? =
        exercises.firstNotNullOfOrNull { exercise ->
            when {
                exercise.sets <= 0 -> "Sets must be greater than 0"
                exercise.reps != null && exercise.reps <= 0 -> "Reps must be greater than 0"
                exercise.restSeconds != null && exercise.restSeconds < 0 ->
                    "Rest time cannot be negative"
                else -> null
            }
        }
}
