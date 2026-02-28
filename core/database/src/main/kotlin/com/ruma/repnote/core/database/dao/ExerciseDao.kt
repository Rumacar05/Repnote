package com.ruma.repnote.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ruma.repnote.core.database.entity.ExerciseEntity
import com.ruma.repnote.core.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Exercise operations.
 */
@Suppress("TooManyFunctions")
@Dao
interface ExerciseDao {
    /**
     * Get all global exercises for a specific language.
     */
    @Query("SELECT * FROM exercises WHERE isGlobal = 1 AND language = :language ORDER BY name ASC")
    fun getGlobalExercises(language: String): Flow<List<ExerciseEntity>>

    /**
     * Get all custom exercises for a user in a specific language.
     */
    @Query(
        "SELECT * FROM exercises WHERE isGlobal = 0 AND createdBy = :userId AND language = :language ORDER BY name ASC",
    )
    fun getUserCustomExercises(
        userId: String,
        language: String,
    ): Flow<List<ExerciseEntity>>

    /**
     * Get all exercises for a user (global + custom) with pagination.
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE (isGlobal = 1 OR createdBy = :userId) AND language = :language
        ORDER BY name ASC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun getAllExercisesForUserPaginated(
        userId: String,
        language: String,
        limit: Int,
        offset: Int,
    ): List<ExerciseEntity>

    /**
     * Get all exercises for a user (global + custom).
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE (isGlobal = 1 OR createdBy = :userId) AND language = :language
        ORDER BY name ASC
        """,
    )
    suspend fun getAllExercisesForUser(
        userId: String,
        language: String,
    ): List<ExerciseEntity>

    /**
     * Get exercise by ID and language.
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId AND language = :language LIMIT 1")
    suspend fun getExerciseById(
        exerciseId: String,
        language: String,
    ): ExerciseEntity?

    /**
     * Get exercises by muscle group.
     */
    @Query(
        """
        SELECT * FROM exercises
        WHERE (isGlobal = 1 OR createdBy = :userId)
        AND language = :language
        AND (primaryMuscleGroup = :muscleGroup OR secondaryMuscleGroups LIKE '%' || :muscleGroup || '%')
        ORDER BY name ASC
        """,
    )
    suspend fun getExercisesByMuscleGroup(
        userId: String,
        muscleGroup: MuscleGroup,
        language: String,
    ): List<ExerciseEntity>

    /**
     * Get exercises by IDs (batch fetch).
     */
    @Query("SELECT * FROM exercises WHERE id IN (:exerciseIds) AND language = :language")
    suspend fun getExercisesByIds(
        exerciseIds: List<String>,
        language: String,
    ): List<ExerciseEntity>

    /**
     * Insert or replace exercise (upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity)

    /**
     * Insert or replace multiple exercises (batch upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    /**
     * Delete exercise by ID.
     */
    @Query("DELETE FROM exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: String)

    /**
     * Delete all cached exercises (for cache invalidation).
     */
    @Query("DELETE FROM exercises")
    suspend fun deleteAllExercises()

    /**
     * Delete old cached exercises (older than specified timestamp).
     */
    @Query("DELETE FROM exercises WHERE cachedAt < :timestamp")
    suspend fun deleteOldCache(timestamp: Long)

    /**
     * Get count of cached exercises.
     */
    @Query("SELECT COUNT(*) FROM exercises WHERE language = :language")
    suspend fun getExerciseCount(language: String): Int
}
