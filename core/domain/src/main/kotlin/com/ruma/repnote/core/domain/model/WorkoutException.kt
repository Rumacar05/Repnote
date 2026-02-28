package com.ruma.repnote.core.domain.model

/**
 * Sealed class representing exceptions related to workout operations.
 */
sealed class WorkoutException {
    /**
     * Workout session not found in the database.
     */
    data object SessionNotFound : WorkoutException()

    /**
     * User already has an active workout session in progress.
     * Only one active session is allowed per user at a time.
     */
    data object SessionAlreadyActive : WorkoutException()

    /**
     * User is not authorized to access this workout session.
     */
    data object UnauthorizedAccess : WorkoutException()

    /**
     * Network error occurred during the operation.
     */
    data object NetworkError : WorkoutException()

    /**
     * Unknown error with optional message.
     */
    data class Unknown(
        val message: String?,
    ) : WorkoutException()
}
