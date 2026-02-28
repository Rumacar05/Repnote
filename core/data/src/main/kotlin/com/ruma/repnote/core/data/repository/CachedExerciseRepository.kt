package com.ruma.repnote.core.data.repository

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.mapper.toExercise
import com.ruma.repnote.core.data.mapper.toExerciseDocument
import com.ruma.repnote.core.data.mapper.toExerciseException
import com.ruma.repnote.core.data.model.ExerciseDocument
import com.ruma.repnote.core.data.model.ExerciseTranslationDocument
import com.ruma.repnote.core.database.dao.ExerciseDao
import com.ruma.repnote.core.database.entity.toEntity
import com.ruma.repnote.core.database.entity.toExercise
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.ExerciseResult
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.domain.repository.ExerciseRepository
import com.ruma.repnote.core.domain.service.ImageStorageService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

private const val TAG = "CachedExerciseRepository"
private const val EXERCISES_COLLECTION = "exercises"
private const val CUSTOM_EXERCISES_COLLECTION = "customExercises"
private const val USERS_COLLECTION = "users"
private const val TRANSLATIONS_COLLECTION = "translations"

/**
 * Exercise repository with local caching for improved performance.
 *
 * Strategy:
 * 1. Try to load from local cache first (instant)
 * 2. Fetch from Firestore in background
 * 3. Update cache with fresh data
 * 4. Use batch fetching to solve N+1 query problem
 */
@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class CachedExerciseRepository(
    private val firestore: FirebaseFirestore,
    private val imageStorageService: ImageStorageService,
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {
    /**
     * Batch fetch translations for multiple exercises with fallback to English.
     * Solves the N+1 query problem by fetching all translations in parallel.
     *
     * If translation in requested language is not found, falls back to English.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun batchFetchTranslations(
        documents: List<DocumentSnapshot>,
        language: String,
    ): Map<String, ExerciseTranslationDocument> =
        coroutineScope {
            documents
                .map { doc ->
                    async {
                        try {
                            // Try requested language first
                            val translationDoc =
                                doc.reference
                                    .collection(TRANSLATIONS_COLLECTION)
                                    .document(language)
                                    .get()
                                    .await()

                            var translation =
                                translationDoc.toObject(
                                    ExerciseTranslationDocument::class.java,
                                )

                            // If translation is null or has empty name, try English fallback
                            if (translation == null || translation.name.isBlank()) {
                                if (language != "en") {
                                    Log.d(
                                        TAG,
                                        "Translation for ${doc.id} not found in $language, trying English",
                                    )
                                    val fallbackDoc =
                                        doc.reference
                                            .collection(TRANSLATIONS_COLLECTION)
                                            .document("en")
                                            .get()
                                            .await()

                                    translation =
                                        fallbackDoc.toObject(
                                            ExerciseTranslationDocument::class.java,
                                        )
                                }
                            }

                            if (translation != null && translation.name.isNotBlank()) {
                                doc.id to translation
                            } else {
                                Log.w(TAG, "No valid translation found for ${doc.id}")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching translation for ${doc.id}", e)
                            null
                        }
                    }
                }.awaitAll()
                .filterNotNull()
                .toMap()
        }

    override fun getGlobalExercises(language: String): Flow<ExerciseResult<List<Exercise>>> =
        flow {
            // Step 1: Emit cached data immediately
            val cachedExercises =
                exerciseDao.getGlobalExercises(language).map { entities ->
                    entities.map { it.toExercise() }
                }

            val cached = cachedExercises.firstOrNull() ?: emptyList()
            if (cached.isNotEmpty()) {
                emit(ExerciseResult.Success(cached))
            }

            // Step 2: Fetch from Firestore in background
            try {
                val snapshot =
                    firestore
                        .collection(EXERCISES_COLLECTION)
                        .whereEqualTo("global", true)
                        .get()
                        .await()

                // Batch fetch translations (solves N+1 problem)
                val translations = batchFetchTranslations(snapshot.documents, language)

                val exercises =
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            val exerciseDoc = doc.toObject(ExerciseDocument::class.java)
                            val translation = translations[doc.id]

                            if (exerciseDoc != null && translation != null) {
                                exerciseDoc.toExercise(translation)
                            } else {
                                Log.w(TAG, "Missing exercise or translation for ${doc.id}")
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing exercise ${doc.id}", e)
                            null
                        }
                    }

                // Step 3: Update cache
                if (exercises.isNotEmpty()) {
                    exerciseDao.insertExercises(exercises.map { it.toEntity(language) })
                }

                emit(ExerciseResult.Success(exercises))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching global exercises", e)
                // If we have cached data, we already emitted it
                // If not, emit error
                val cachedCount = exerciseDao.getExerciseCount(language)
                if (cachedCount == 0) {
                    emit(
                        ExerciseResult.Error(
                            when (e) {
                                is FirebaseFirestoreException -> e.toExerciseException()
                                else -> ExerciseException.Unknown(e.message)
                            },
                        ),
                    )
                }
            }
        }.catch { e ->
            Log.e(TAG, "Flow error", e)
            emit(ExerciseResult.Error(ExerciseException.Unknown(e.message)))
        }

    override fun getUserCustomExercises(
        userId: String,
        language: String,
    ): Flow<ExerciseResult<List<Exercise>>> =
        flow {
            // Emit cached data first
            val cachedExercises =
                exerciseDao.getUserCustomExercises(userId, language).map { entities ->
                    entities.map { it.toExercise() }
                }

            val cached = cachedExercises.firstOrNull() ?: emptyList()
            if (cached.isNotEmpty()) {
                emit(ExerciseResult.Success(cached))
            }

            // Fetch from Firestore
            try {
                val snapshot =
                    firestore
                        .collection(USERS_COLLECTION)
                        .document(userId)
                        .collection(CUSTOM_EXERCISES_COLLECTION)
                        .get()
                        .await()

                val translations = batchFetchTranslations(snapshot.documents, language)

                val exercises =
                    snapshot.documents.mapNotNull { doc ->
                        try {
                            val exerciseDoc = doc.toObject(ExerciseDocument::class.java)
                            val translation = translations[doc.id]

                            if (exerciseDoc != null && translation != null) {
                                exerciseDoc.toExercise(translation)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing custom exercise ${doc.id}", e)
                            null
                        }
                    }

                if (exercises.isNotEmpty()) {
                    exerciseDao.insertExercises(exercises.map { it.toEntity(language) })
                }

                emit(ExerciseResult.Success(exercises))
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching custom exercises", e)
                val cachedCount = exerciseDao.getExerciseCount(language)
                if (cachedCount == 0) {
                    emit(
                        ExerciseResult.Error(
                            when (e) {
                                is FirebaseFirestoreException -> e.toExerciseException()
                                else -> ExerciseException.Unknown(e.message)
                            },
                        ),
                    )
                }
            }
        }.catch { e ->
            emit(ExerciseResult.Error(ExerciseException.Unknown(e.message)))
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getAllExercisesForUser(
        userId: String,
        language: String,
    ): ExerciseResult<List<Exercise>> {
        // Try cache first
        val cached =
            exerciseDao
                .getAllExercisesForUser(
                    userId,
                    language,
                ).map { it.toExercise() }

        return try {
            // Fetch global exercises
            val globalSnapshot =
                firestore
                    .collection(EXERCISES_COLLECTION)
                    .whereEqualTo("global", true)
                    .get()
                    .await()

            // Fetch custom exercises
            val customSnapshot =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(CUSTOM_EXERCISES_COLLECTION)
                    .get()
                    .await()

            // Batch fetch translations for both
            val allDocs = globalSnapshot.documents + customSnapshot.documents
            val translations = batchFetchTranslations(allDocs, language)

            val exercises =
                allDocs.mapNotNull { doc ->
                    try {
                        val exerciseDoc = doc.toObject(ExerciseDocument::class.java)
                        val translation = translations[doc.id]

                        if (exerciseDoc != null && translation != null) {
                            exerciseDoc.toExercise(translation)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing exercise ${doc.id}", e)
                        null
                    }
                }

            // Update cache
            if (exercises.isNotEmpty()) {
                exerciseDao.insertExercises(exercises.map { it.toEntity(language) })
            }

            ExerciseResult.Success(exercises)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "Firestore error, falling back to cache", e)
            // Fallback to cache
            if (cached.isNotEmpty()) {
                ExerciseResult.Success(cached)
            } else {
                ExerciseResult.Error(e.toExerciseException())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading exercises, falling back to cache", e)
            if (cached.isNotEmpty()) {
                ExerciseResult.Success(cached)
            } else {
                ExerciseResult.Error(ExerciseException.Unknown(e.message))
            }
        }
    }

    override suspend fun getExercisesByMuscleGroup(
        userId: String,
        muscleGroup: MuscleGroup,
        language: String,
    ): ExerciseResult<List<Exercise>> {
        // Try cache first
        val cached =
            exerciseDao.getExercisesByMuscleGroup(userId, muscleGroup, language).map {
                it.toExercise()
            }

        if (cached.isNotEmpty()) {
            return ExerciseResult.Success(cached)
        }

        // Fallback to getAllExercisesForUser and filter
        return when (val result = getAllExercisesForUser(userId, language)) {
            is ExerciseResult.Success -> {
                val filtered =
                    result.data.filter { exercise ->
                        exercise.primaryMuscleGroup == muscleGroup ||
                            exercise.secondaryMuscleGroups.contains(muscleGroup)
                    }
                ExerciseResult.Success(filtered)
            }

            is ExerciseResult.Error -> {
                result
            }
        }
    }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    override suspend fun getExerciseById(
        exerciseId: String,
        userId: String,
        language: String,
    ): ExerciseResult<Exercise> {
        // Try cache first
        val cached = exerciseDao.getExerciseById(exerciseId, language)
        if (cached != null) {
            return ExerciseResult.Success(cached.toExercise())
        }

        return try {
            // Try global exercises
            val globalDoc =
                firestore
                    .collection(EXERCISES_COLLECTION)
                    .document(exerciseId)
                    .get()
                    .await()

            if (globalDoc.exists()) {
                val exerciseDoc = globalDoc.toObject(ExerciseDocument::class.java)
                val translation = fetchTranslation(globalDoc, language)

                return if (exerciseDoc != null && translation != null) {
                    val exercise = exerciseDoc.toExercise(translation)
                    // Cache it
                    exerciseDao.insertExercise(exercise.toEntity(language))
                    ExerciseResult.Success(exercise)
                } else {
                    ExerciseResult.Error(ExerciseException.InvalidExerciseData)
                }
            }

            // Try custom exercises
            val customDoc =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(CUSTOM_EXERCISES_COLLECTION)
                    .document(exerciseId)
                    .get()
                    .await()

            if (customDoc.exists()) {
                val exerciseDoc = customDoc.toObject(ExerciseDocument::class.java)
                val translation = fetchTranslation(customDoc, language)

                return if (exerciseDoc != null && translation != null) {
                    val exercise = exerciseDoc.toExercise(translation)
                    exerciseDao.insertExercise(exercise.toEntity(language))
                    ExerciseResult.Success(exercise)
                } else {
                    ExerciseResult.Error(ExerciseException.InvalidExerciseData)
                }
            }

            ExerciseResult.Error(ExerciseException.ExerciseNotFound)
        } catch (e: FirebaseFirestoreException) {
            ExerciseResult.Error(e.toExerciseException())
        } catch (e: Exception) {
            Log.e(TAG, "Error getting exercise by ID: $exerciseId", e)
            ExerciseResult.Error(ExerciseException.Unknown(e.message))
        }
    }

    /**
     * Fetches translation for a single exercise with fallback to English.
     *
     * If translation in requested language is not found, falls back to English.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchTranslation(
        exerciseDocRef: DocumentSnapshot,
        language: String,
    ): ExerciseTranslationDocument? =
        try {
            // Try requested language first
            val translationDoc =
                exerciseDocRef.reference
                    .collection(TRANSLATIONS_COLLECTION)
                    .document(language)
                    .get()
                    .await()

            var translation = translationDoc.toObject(ExerciseTranslationDocument::class.java)

            // If translation is null or has empty name, try English fallback
            if (translation == null || translation.name.isBlank()) {
                if (language != "en") {
                    Log.d(
                        TAG,
                        "Translation for ${exerciseDocRef.id} not found in $language, trying English",
                    )
                    val fallbackDoc =
                        exerciseDocRef.reference
                            .collection(TRANSLATIONS_COLLECTION)
                            .document("en")
                            .get()
                            .await()

                    translation = fallbackDoc.toObject(ExerciseTranslationDocument::class.java)
                }
            }

            if (translation != null && translation.name.isNotBlank()) {
                translation
            } else {
                Log.w(TAG, "No valid translation found for ${exerciseDocRef.id}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching translation for ${exerciseDocRef.id}", e)
            null
        }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun createCustomExercise(
        userId: String,
        exercise: Exercise,
    ): ExerciseResult<Exercise> =
        try {
            val exerciseDoc =
                exercise
                    .copy(isGlobal = false, createdBy = userId)
                    .toExerciseDocument()

            val exerciseRef =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(CUSTOM_EXERCISES_COLLECTION)
                    .document(exercise.id)

            exerciseRef.set(exerciseDoc).await()

            val translationEN =
                ExerciseTranslationDocument(
                    name = exercise.name,
                    description = exercise.description,
                )

            exerciseRef
                .collection(TRANSLATIONS_COLLECTION)
                .document("en")
                .set(translationEN)
                .await()

            exerciseRef
                .collection(TRANSLATIONS_COLLECTION)
                .document("es")
                .set(translationEN)
                .await()

            val createdExercise = exerciseDoc.toExercise(translationEN)

            // Cache it
            exerciseDao.insertExercise(createdExercise.toEntity("en"))
            exerciseDao.insertExercise(createdExercise.toEntity("es"))

            ExerciseResult.Success(createdExercise)
        } catch (e: FirebaseFirestoreException) {
            ExerciseResult.Error(e.toExerciseException())
        } catch (e: Exception) {
            Log.e(TAG, "Error creating custom exercise", e)
            ExerciseResult.Error(ExerciseException.Unknown(e.message))
        }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    override suspend fun updateCustomExercise(
        userId: String,
        exercise: Exercise,
    ): ExerciseResult<Exercise> {
        return try {
            val exerciseRef =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(CUSTOM_EXERCISES_COLLECTION)
                    .document(exercise.id)

            val existingDoc = exerciseRef.get().await()

            if (!existingDoc.exists()) {
                return ExerciseResult.Error(ExerciseException.ExerciseNotFound)
            }

            val existing = existingDoc.toObject(ExerciseDocument::class.java)
            if (existing?.createdBy != userId) {
                return ExerciseResult.Error(ExerciseException.UnauthorizedAccess)
            }

            val exerciseDoc = exercise.toExerciseDocument()
            exerciseRef.set(exerciseDoc).await()

            val translation =
                ExerciseTranslationDocument(
                    name = exercise.name,
                    description = exercise.description,
                )

            exerciseRef
                .collection(
                    TRANSLATIONS_COLLECTION,
                ).document("en")
                .set(translation)
                .await()
            exerciseRef
                .collection(
                    TRANSLATIONS_COLLECTION,
                ).document("es")
                .set(translation)
                .await()

            val updatedExercise = exerciseDoc.toExercise(translation)

            // Update cache
            exerciseDao.insertExercise(updatedExercise.toEntity("en"))
            exerciseDao.insertExercise(updatedExercise.toEntity("es"))

            ExerciseResult.Success(updatedExercise)
        } catch (e: FirebaseFirestoreException) {
            ExerciseResult.Error(e.toExerciseException())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating custom exercise", e)
            ExerciseResult.Error(ExerciseException.Unknown(e.message))
        }
    }

    @Suppress("TooGenericExceptionCaught", "ReturnCount")
    override suspend fun deleteCustomExercise(
        userId: String,
        exerciseId: String,
    ): ExerciseResult<Unit> {
        return try {
            val exerciseRef =
                firestore
                    .collection(USERS_COLLECTION)
                    .document(userId)
                    .collection(CUSTOM_EXERCISES_COLLECTION)
                    .document(exerciseId)

            val existingDoc = exerciseRef.get().await()

            if (!existingDoc.exists()) {
                return ExerciseResult.Error(ExerciseException.ExerciseNotFound)
            }

            val existing = existingDoc.toObject(ExerciseDocument::class.java)
            if (existing?.createdBy != userId) {
                return ExerciseResult.Error(ExerciseException.UnauthorizedAccess)
            }

            // Delete translations
            val translationsSnapshot =
                exerciseRef
                    .collection(TRANSLATIONS_COLLECTION)
                    .get()
                    .await()

            translationsSnapshot.documents.forEach { translationDoc ->
                translationDoc.reference.delete().await()
            }

            exerciseRef.delete().await()

            existing.imageUrl?.let { url ->
                imageStorageService.deleteImage(url)
            }

            // Delete from cache
            exerciseDao.deleteExercise(exerciseId)

            ExerciseResult.Success(Unit)
        } catch (e: FirebaseFirestoreException) {
            ExerciseResult.Error(e.toExerciseException())
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting custom exercise", e)
            ExerciseResult.Error(ExerciseException.Unknown(e.message))
        }
    }

    override suspend fun uploadExerciseImage(
        exerciseId: String,
        imageBytes: ByteArray,
    ): ExerciseResult<String> = imageStorageService.uploadImage(exerciseId, imageBytes)
}
