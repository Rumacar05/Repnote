package com.ruma.repnote.core.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.ruma.repnote.core.auth.data.repository.DefaultAuthRepository
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import com.ruma.repnote.core.auth.domain.usecase.DefaultGetCurrentUserUseCase
import com.ruma.repnote.core.auth.domain.usecase.DefaultSignOutUseCase
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.auth.domain.usecase.SignOutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val APPLICATION_SCOPE = named("ApplicationScope")

val authDataModule =
    module {
        single<FirebaseAuth> { FirebaseAuth.getInstance() }
        single<CoroutineScope>(APPLICATION_SCOPE) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        singleOf(::DefaultAuthRepository) { bind<AuthRepository>() }
        singleOf(::DefaultGetCurrentUserUseCase) { bind<GetCurrentUserUseCase>() }
        singleOf(::DefaultSignOutUseCase) { bind<SignOutUseCase>() }
    }
