package com.ruma.repnote.di

import com.ruma.repnote.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule =
    module {
        viewModelOf(::SplashViewModel)
    }
