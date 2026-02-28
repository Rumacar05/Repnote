package com.ruma.repnote.core.domain.model

/**
 * Sealed class representing exceptions related to routine operations.
 */
sealed class RoutineException {
    /**
     * Routine not found in the database.
     */
    data object RoutineNotFound : RoutineException()

    /**
     * User is not authorized to access this routine.
     */
    data object UnauthorizedAccess : RoutineException()

    /**
     * Invalid routine data received.
     */
    data object InvalidRoutineData : RoutineException()

    /**
     * Network error occurred during the operation.
     */
    data object NetworkError : RoutineException()

    /**
     * Unknown error with optional message.
     */
    data class Unknown(
        val message: String?,
    ) : RoutineException()
}
