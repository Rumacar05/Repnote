package com.ruma.repnote.core.domain.model

/**
 * Sealed class representing the result of a routine operation.
 */
sealed class RoutineResult<out T> {
    /**
     * Successful result with data.
     */
    data class Success<T>(
        val data: T,
    ) : RoutineResult<T>()

    /**
     * Error result with exception.
     */
    data class Error(
        val exception: RoutineException,
    ) : RoutineResult<Nothing>()
}
