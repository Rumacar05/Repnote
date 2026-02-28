package com.ruma.repnote.core.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Firestore document model for exercises.
 *
 * Note: name and description fields have been moved to translations subcollection.
 * See [ExerciseTranslationDocument] for translation data.
 *
 * Stored in: exercises/{exerciseId}
 * Translations in: exercises/{exerciseId}/translations/{languageCode}
 */
data class ExerciseDocument(
    val id: String = "",
    val imageUrl: String? = null,
    val primaryMuscleGroup: String = "",
    val secondaryMuscleGroups: List<String> = emptyList(),
    @get:PropertyName("global")
    val isGlobal: Boolean = false,
    val createdBy: String? = null,
) {
    constructor() : this(
        id = "",
        imageUrl = null,
        primaryMuscleGroup = "",
        secondaryMuscleGroups = emptyList(),
        isGlobal = false,
        createdBy = null,
    )

    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "imageUrl" to imageUrl,
            "primaryMuscleGroup" to primaryMuscleGroup,
            "secondaryMuscleGroups" to secondaryMuscleGroups,
            "global" to isGlobal,
            "createdBy" to createdBy,
        )
}
