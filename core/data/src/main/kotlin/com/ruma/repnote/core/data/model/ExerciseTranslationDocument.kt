package com.ruma.repnote.core.data.model

/**
 * Firestore document model for exercise translations.
 *
 * Stored in: exercises/{exerciseId}/translations/{languageCode}
 * Example paths:
 * - exercises/global-bench-press/translations/en
 * - exercises/global-bench-press/translations/es
 */
data class ExerciseTranslationDocument(
    val name: String = "",
    val description: String = "",
)
