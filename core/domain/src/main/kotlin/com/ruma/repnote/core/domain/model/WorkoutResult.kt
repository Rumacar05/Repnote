package com.ruma.repnote.core.domain.model

/**
 * Sealed class representing the result of a workout operation.
 */
sealed class WorkoutResult<out T> {
    /**
     * Successful result with data.
     */
    data class Success<T>(
        val data: T,
    ) : WorkoutResult<T>()

    /**
     * Error result with exception.
     */
    data class Error(
        val exception: WorkoutException,
    ) : WorkoutResult<Nothing>()
}
