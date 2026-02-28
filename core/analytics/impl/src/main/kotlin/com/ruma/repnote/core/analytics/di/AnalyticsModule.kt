package com.ruma.repnote.core.analytics.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ruma.repnote.core.analytics.data.service.FirebaseAnalyticsService
import com.ruma.repnote.core.analytics.domain.service.AnalyticsService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsModule =
    module {
        single<FirebaseAnalytics> { FirebaseAnalytics.getInstance(androidContext()) }
        single<FirebaseCrashlytics> { FirebaseCrashlytics.getInstance() }

        single<AnalyticsService> {
            FirebaseAnalyticsService(
                firebaseAnalytics = get(),
                firebaseCrashlytics = get(),
            )
        }
    }
