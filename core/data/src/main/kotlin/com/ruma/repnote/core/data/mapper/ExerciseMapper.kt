package com.ruma.repnote.core.data.mapper

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.ruma.repnote.core.data.model.ExerciseDocument
import com.ruma.repnote.core.data.model.ExerciseTranslationDocument
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.domain.model.ExerciseException
import com.ruma.repnote.core.domain.model.MuscleGroup

private const val TAG = "ExerciseMapper"

/**
 * Maps ExerciseDocument + ExerciseTranslationDocument to Exercise domain model.
 *
 * @param translation The translation document containing localized name and description
 * @return Exercise domain model with combined data
 */
@Suppress("SwallowedException")
internal fun ExerciseDocument.toExercise(translation: ExerciseTranslationDocument): Exercise =
    Exercise(
        id = id,
        name = translation.name,
        description = translation.description,
        imageUrl = imageUrl,
        primaryMuscleGroup = MuscleGroup.valueOf(primaryMuscleGroup),
        secondaryMuscleGroups =
            secondaryMuscleGroups.mapNotNull {
                try {
                    MuscleGroup.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "Invalid muscle group: $it")
                    null
                }
            },
        isGlobal = isGlobal,
        createdBy = createdBy,
    )

/**
 * Maps Exercise domain model to ExerciseDocument for Firestore.
 *
 * Note: This only maps the base exercise data. Name and description
 * must be written separately to the translations subcollection.
 *
 * @return ExerciseDocument without name/description (stored in translations)
 */
internal fun Exercise.toExerciseDocument(): ExerciseDocument =
    ExerciseDocument(
        id = id,
        imageUrl = imageUrl,
        primaryMuscleGroup = primaryMuscleGroup.name,
        secondaryMuscleGroups = secondaryMuscleGroups.map { it.name },
        isGlobal = isGlobal,
        createdBy = createdBy,
    )

internal fun FirebaseFirestoreException.toExerciseException(): ExerciseException =
    when (code) {
        FirebaseFirestoreException.Code.NOT_FOUND -> ExerciseException.ExerciseNotFound
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> ExerciseException.UnauthorizedAccess
        FirebaseFirestoreException.Code.UNAVAILABLE -> ExerciseException.NetworkError
        else -> ExerciseException.Unknown(message)
    }
