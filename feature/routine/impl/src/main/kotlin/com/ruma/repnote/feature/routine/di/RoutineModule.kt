package com.ruma.repnote.feature.routine.di

import com.ruma.repnote.feature.routine.presentation.RoutinesViewModel
import com.ruma.repnote.feature.routine.presentation.createedit.CreateEditRoutineViewModel
import com.ruma.repnote.feature.routine.presentation.detail.RoutineDetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val routineFeatureModule =
    module {
        viewModelOf(::RoutinesViewModel)
        viewModelOf(::RoutineDetailViewModel)
        viewModelOf(::CreateEditRoutineViewModel)
    }
