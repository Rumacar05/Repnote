package com.ruma.repnote.feature.profile.di

import com.ruma.repnote.feature.profile.presentation.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileFeatureModule =
    module {
        viewModelOf(::ProfileViewModel)
    }
