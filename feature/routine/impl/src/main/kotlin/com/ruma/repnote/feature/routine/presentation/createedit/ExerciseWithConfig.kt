package com.ruma.repnote.feature.routine.presentation.createedit

data class ExerciseWithConfig(
    val exerciseId: String,
    val exerciseName: String,
    val order: Int,
    val sets: Int = 3,
    val reps: Int? = 10,
    val restSeconds: Int? = 60,
    val notes: String? = null,
)
