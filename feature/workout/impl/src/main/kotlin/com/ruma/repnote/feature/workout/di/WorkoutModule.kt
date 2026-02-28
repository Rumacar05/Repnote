package com.ruma.repnote.feature.workout.di

import com.ruma.repnote.feature.workout.presentation.active.ActiveWorkoutViewModel
import com.ruma.repnote.feature.workout.presentation.history.WorkoutHistoryViewModel
import com.ruma.repnote.feature.workout.presentation.summary.SessionSummaryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val workoutFeatureModule =
    module {
        viewModel { ActiveWorkoutViewModel(get(), get(), enableTimeTracking = true) }
        viewModelOf(::WorkoutHistoryViewModel)
        viewModelOf(::SessionSummaryViewModel)
    }
