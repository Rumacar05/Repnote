package com.ruma.repnote.core.domain.model

sealed class ExerciseException : Exception() {
    data object ExerciseNotFound : ExerciseException()
    data object UnauthorizedAccess : ExerciseException()
    data object InvalidExerciseData : ExerciseException()
    data object NetworkError : ExerciseException()
    data object StorageError : ExerciseException()
    data class Unknown(
        override val message: String?,
    ) : ExerciseException()
}
