package com.ruma.repnote.core.domain.model

sealed class ExerciseResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ExerciseResult<T>()

    data class Error(
        val exception: ExerciseException,
    ) : ExerciseResult<Nothing>()
}
