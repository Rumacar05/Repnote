package com.ruma.repnote.core.designsystem.extensions

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.ruma.repnote.core.domain.model.MuscleGroup
import com.ruma.repnote.core.designsystem.R as DesignR
import com.ruma.repnote.core.stringresources.R as StringR

@StringRes
fun MuscleGroup.getDisplayNameResId(): Int =
    when (this) {
        MuscleGroup.CHEST -> StringR.string.muscle_chest
        MuscleGroup.BACK -> StringR.string.muscle_back
        MuscleGroup.SHOULDERS -> StringR.string.muscle_shoulders
        MuscleGroup.BICEPS -> StringR.string.muscle_biceps
        MuscleGroup.TRICEPS -> StringR.string.muscle_triceps
        MuscleGroup.FOREARMS -> StringR.string.muscle_forearms
        MuscleGroup.QUADS -> StringR.string.muscle_quads
        MuscleGroup.HAMSTRINGS -> StringR.string.muscle_hamstrings
        MuscleGroup.GLUTES -> StringR.string.muscle_glutes
        MuscleGroup.CALVES -> StringR.string.muscle_calves
        MuscleGroup.ABS -> StringR.string.muscle_abs
        MuscleGroup.OBLIQUES -> StringR.string.muscle_obliques
    }

@DrawableRes
fun MuscleGroup.getIconResId(): Int =
    when (this) {
        MuscleGroup.CHEST -> DesignR.drawable.ic_muscle_chest
        MuscleGroup.BACK -> DesignR.drawable.ic_muscle_back
        MuscleGroup.SHOULDERS -> DesignR.drawable.ic_muscle_shoulders
        MuscleGroup.BICEPS -> DesignR.drawable.ic_muscle_biceps
        MuscleGroup.TRICEPS -> DesignR.drawable.ic_muscle_triceps
        MuscleGroup.FOREARMS -> DesignR.drawable.ic_muscle_forearms
        MuscleGroup.QUADS -> DesignR.drawable.ic_muscle_quads
        MuscleGroup.HAMSTRINGS -> DesignR.drawable.ic_muscle_hamstrings
        MuscleGroup.GLUTES -> DesignR.drawable.ic_muscle_glutes
        MuscleGroup.CALVES -> DesignR.drawable.ic_muscle_calves
        MuscleGroup.ABS -> DesignR.drawable.ic_muscle_abs
        MuscleGroup.OBLIQUES -> DesignR.drawable.ic_muscle_obliques
    }

fun MuscleGroup.getDisplayName(context: Context): String = context.getString(getDisplayNameResId())
