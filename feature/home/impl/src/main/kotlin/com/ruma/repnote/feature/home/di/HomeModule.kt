package com.ruma.repnote.feature.home.di

import com.ruma.repnote.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeFeatureModule =
    module {
        viewModelOf(::HomeViewModel)
    }
