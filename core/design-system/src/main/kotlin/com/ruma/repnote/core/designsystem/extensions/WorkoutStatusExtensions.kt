package com.ruma.repnote.core.designsystem.extensions

import androidx.annotation.StringRes
import com.ruma.repnote.core.domain.model.WorkoutStatus
import com.ruma.repnote.core.stringresources.R as StringR

/**
 * Returns the string resource ID for the workout status text.
 */
@StringRes
fun WorkoutStatus.toStringRes(): Int =
    when (this) {
        WorkoutStatus.COMPLETED -> StringR.string.workout_status_completed
        WorkoutStatus.IN_PROGRESS -> StringR.string.workout_status_in_progress
        WorkoutStatus.ABANDONED -> StringR.string.workout_status_abandoned
    }
