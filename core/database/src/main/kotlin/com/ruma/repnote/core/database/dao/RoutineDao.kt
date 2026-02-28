package com.ruma.repnote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruma.repnote.core.database.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Routine operations.
 */
@Dao
interface RoutineDao {
    /**
     * Get all routines for a user as a Flow (real-time updates).
     */
    @Query("SELECT * FROM routines WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getUserRoutines(userId: String): Flow<List<RoutineEntity>>

    /**
     * Get routine by ID.
     */
    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    suspend fun getRoutineById(routineId: String): RoutineEntity?

    /**
     * Get routine by ID as Flow (real-time updates).
     */
    @Query("SELECT * FROM routines WHERE id = :routineId LIMIT 1")
    fun getRoutineByIdFlow(routineId: String): Flow<RoutineEntity?>

    /**
     * Insert or replace routine (upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    /**
     * Insert or replace multiple routines (batch upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<RoutineEntity>)

    /**
     * Delete routine by ID.
     */
    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: String)

    /**
     * Delete all routines for a user.
     */
    @Query("DELETE FROM routines WHERE userId = :userId")
    suspend fun deleteUserRoutines(userId: String)

    /**
     * Delete all cached routines.
     */
    @Query("DELETE FROM routines")
    suspend fun deleteAllRoutines()

    /**
     * Delete old cached routines (older than specified timestamp).
     */
    @Query("DELETE FROM routines WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)

    /**
     * Get count of routines for a user.
     */
    @Query("SELECT COUNT(*) FROM routines WHERE userId = :userId")
    suspend fun getRoutineCount(userId: String): Int
}
