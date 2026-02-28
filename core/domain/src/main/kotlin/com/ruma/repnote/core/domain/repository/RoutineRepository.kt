package com.ruma.repnote.core.domain.repository

import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.domain.model.RoutineResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing workout routines.
 */
interface RoutineRepository {
    /**
     * Get all routines for a specific user.
     *
     * @param userId The ID of the user
     * @return Flow emitting the result of the operation
     */
    fun getUserRoutines(userId: String): Flow<RoutineResult<List<Routine>>>

    /**
     * Get a specific routine by ID.
     *
     * @param userId The ID of the user (for authorization)
     * @param routineId The ID of the routine
     * @return Flow emitting the result of the operation
     */
    fun getRoutineById(
        userId: String,
        routineId: String,
    ): Flow<RoutineResult<Routine>>

    /**
     * Create a new routine.
     *
     * @param routine The routine to create (id will be generated)
     * @return Result of the operation with the created routine ID
     */
    suspend fun createRoutine(routine: Routine): RoutineResult<String>

    /**
     * Update an existing routine.
     *
     * @param routine The routine to update
     * @return Result of the operation
     */
    suspend fun updateRoutine(routine: Routine): RoutineResult<Unit>

    /**
     * Delete a routine.
     *
     * @param userId The ID of the user (for authorization)
     * @param routineId The ID of the routine to delete
     * @return Result of the operation
     */
    suspend fun deleteRoutine(
        userId: String,
        routineId: String,
    ): RoutineResult<Unit>
}
