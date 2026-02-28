package com.ruma.repnote.feature.auth.di

import com.ruma.repnote.feature.auth.domain.usecase.DefaultSignInWithEmailUseCase
import com.ruma.repnote.feature.auth.domain.usecase.DefaultSignInWithGoogleUseCase
import com.ruma.repnote.feature.auth.domain.usecase.DefaultSignUpWithEmailUseCase
import com.ruma.repnote.feature.auth.domain.usecase.SignInWithEmailUseCase
import com.ruma.repnote.feature.auth.domain.usecase.SignInWithGoogleUseCase
import com.ruma.repnote.feature.auth.domain.usecase.SignUpWithEmailUseCase
import com.ruma.repnote.feature.auth.presentation.login.LoginViewModel
import com.ruma.repnote.feature.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authFeatureModule =
    module {
        singleOf(::DefaultSignInWithEmailUseCase) { bind<SignInWithEmailUseCase>() }
        singleOf(::DefaultSignUpWithEmailUseCase) { bind<SignUpWithEmailUseCase>() }
        singleOf(::DefaultSignInWithGoogleUseCase) { bind<SignInWithGoogleUseCase>() }

        viewModelOf(::LoginViewModel)
        viewModelOf(::RegisterViewModel)
    }
