package com.ruma.repnote.core.domain.repository

import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.MuscleGroup
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    /**
     * Gets all global exercises with translations in the specified language.
     *
     * @param language Language code (e.g., "en", "es"). Defaults to "en".
     * @return Flow of exercise results with translated names/descriptions
     */
    fun getGlobalExercises(language: String = "en"): Flow<ExerciseResult<List<Exercise>>>

    /**
     * Gets user's custom exercises with translations in the specified language.
     *
     * @param userId The user's ID
     * @param language Language code (e.g., "en", "es"). Defaults to "en".
     * @return Flow of exercise results with translated names/descriptions
     */
    fun getUserCustomExercises(
        userId: String,
        language: String = "en",
    ): Flow<ExerciseResult<List<Exercise>>>

    /**
     * Gets all exercises for a user (global + custom) with translations.
     *
     * This performs a one-time fetch of all exercises. If you need real-time updates,
     * consider using getGlobalExercises() and getUserCustomExercises() separately.
     *
     * @param userId The user's ID
     * @param language Language code (e.g., "en", "es"). Defaults to "en".
     * @return Exercise result with translated names/descriptions
     */
    suspend fun getAllExercisesForUser(
        userId: String,
        language: String = "en",
    ): ExerciseResult<List<Exercise>>

    /**
     * Gets exercises filtered by muscle group with translations.
     *
     * @param userId The user's ID
     * @param muscleGroup The muscle group to filter by
     * @param language Language code (e.g., "en", "es"). Defaults to "en".
     * @return Exercise result with translated names/descriptions
     */
    suspend fun getExercisesByMuscleGroup(
        userId: String,
        muscleGroup: MuscleGroup,
        language: String = "en",
    ): ExerciseResult<List<Exercise>>

    /**
     * Gets a single exercise by ID with translation.
     *
     * @param exerciseId The exercise ID
     * @param userId The user's ID
     * @param language Language code (e.g., "en", "es"). Defaults to "en".
     * @return Exercise result with translated name/description
     */
    suspend fun getExerciseById(
        exerciseId: String,
        userId: String,
        language: String = "en",
    ): ExerciseResult<Exercise>

    /**
     * Creates a custom exercise for a user.
     *
     * Note: This will create both the base exercise document and translations
     * for supported languages (EN/ES).
     *
     * @param userId The user's ID
     * @param exercise The exercise to create
     * @return Result with created exercise
     */
    suspend fun createCustomExercise(
        userId: String,
        exercise: Exercise,
    ): ExerciseResult<Exercise>

    /**
     * Updates a custom exercise.
     *
     * Note: This will update both the base exercise document and the translation
     * for the current language.
     *
     * @param userId The user's ID
     * @param exercise The exercise to update
     * @return Result with updated exercise
     */
    suspend fun updateCustomExercise(
        userId: String,
        exercise: Exercise,
    ): ExerciseResult<Exercise>

    /**
     * Deletes a custom exercise and all its translations.
     *
     * @param userId The user's ID
     * @param exerciseId The exercise ID to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteCustomExercise(
        userId: String,
        exerciseId: String,
    ): ExerciseResult<Unit>

    /**
     * Uploads an exercise image to cloud storage.
     *
     * @param exerciseId The exercise ID
     * @param imageBytes The image data
     * @return Result with the uploaded image URL
     */
    suspend fun uploadExerciseImage(
        exerciseId: String,
        imageBytes: ByteArray,
    ): ExerciseResult<String>
}
