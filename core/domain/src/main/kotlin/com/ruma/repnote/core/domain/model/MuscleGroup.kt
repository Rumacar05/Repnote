package com.ruma.repnote.core.domain.model

enum class MuscleGroup(
    val displayNameKey: String,
    val iconKey: String,
) {
    CHEST("muscle_chest", "ic_muscle_chest"),
    BACK("muscle_back", "ic_muscle_back"),
    SHOULDERS("muscle_shoulders", "ic_muscle_shoulders"),
    BICEPS("muscle_biceps", "ic_muscle_biceps"),
    TRICEPS("muscle_triceps", "ic_muscle_triceps"),
    FOREARMS("muscle_forearms", "ic_muscle_forearms"),
    QUADS("muscle_quads", "ic_muscle_quads"),
    HAMSTRINGS("muscle_hamstrings", "ic_muscle_hamstrings"),
    GLUTES("muscle_glutes", "ic_muscle_glutes"),
    CALVES("muscle_calves", "ic_muscle_calves"),
    ABS("muscle_abs", "ic_muscle_abs"),
    OBLIQUES("muscle_obliques", "ic_muscle_obliques"),
}
