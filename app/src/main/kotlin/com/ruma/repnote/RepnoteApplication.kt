package com.ruma.repnote

import android.app.Application
import androidx.work.Configuration
import com.cloudinary.android.MediaManager
import com.ruma.repnote.core.analytics.di.analyticsModule
import com.ruma.repnote.core.auth.di.authDataModule
import com.ruma.repnote.core.data.di.dataModule
import com.ruma.repnote.core.database.di.databaseModule
import com.ruma.repnote.di.appModule
import com.ruma.repnote.feature.auth.di.authFeatureModule
import com.ruma.repnote.feature.home.di.homeFeatureModule
import com.ruma.repnote.feature.profile.di.profileFeatureModule
import com.ruma.repnote.feature.routine.di.routineFeatureModule
import com.ruma.repnote.feature.workout.di.workoutFeatureModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class RepnoteApplication :
    Application(),
    Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        initializeCloudinary()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger()
            androidContext(this@RepnoteApplication)
            workManagerFactory()
            modules(
                // Core modules
                analyticsModule,
                databaseModule,
                dataModule,
                authDataModule,
                // Feature modules
                appModule,
                authFeatureModule,
                homeFeatureModule,
                profileFeatureModule,
                routineFeatureModule,
                workoutFeatureModule,
            )
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    private fun initializeCloudinary() {
        val config =
            mapOf(
                "cloud_name" to (BuildConfig.CLOUDINARY_CLOUD_NAME),
                "api_key" to (BuildConfig.CLOUDINARY_API_KEY),
                "api_secret" to (BuildConfig.CLOUDINARY_API_SECRET),
            )
        MediaManager.init(this, config)
    }
}
