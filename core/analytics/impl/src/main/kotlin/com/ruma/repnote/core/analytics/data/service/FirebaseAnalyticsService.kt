package com.ruma.repnote.core.analytics.data.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ruma.repnote.core.analytics.domain.model.AnalyticsEvent
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen
import com.ruma.repnote.core.analytics.domain.service.AnalyticsService

class FirebaseAnalyticsService(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseCrashlytics: FirebaseCrashlytics,
) : AnalyticsService {
    override fun logScreenView(screen: AnalyticsScreen) {
        val bundle =
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screen.screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screen.screenClass)
            }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    override fun logEvent(event: AnalyticsEvent) {
        val bundle = event.params.toBundle()
        firebaseAnalytics.logEvent(event.name, bundle)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
        if (userId != null) {
            firebaseCrashlytics.setUserId(userId)
        }
    }

    override fun setUserProperty(
        name: String,
        value: String?,
    ) {
        firebaseAnalytics.setUserProperty(name, value)
        if (value != null) {
            firebaseCrashlytics.setCustomKey(name, value)
        }
    }

    override fun logError(
        throwable: Throwable,
        message: String?,
    ) {
        message?.let { firebaseCrashlytics.log(it) }
        firebaseCrashlytics.recordException(throwable)
    }

    override fun logError(
        message: String,
        params: Map<String, Any>,
    ) {
        firebaseCrashlytics.log(message)
        params.forEach { (key, value) ->
            when (value) {
                is String -> firebaseCrashlytics.setCustomKey(key, value)
                is Int -> firebaseCrashlytics.setCustomKey(key, value)
                is Long -> firebaseCrashlytics.setCustomKey(key, value)
                is Double -> firebaseCrashlytics.setCustomKey(key, value)
                is Boolean -> firebaseCrashlytics.setCustomKey(key, value)
                else -> firebaseCrashlytics.setCustomKey(key, value.toString())
            }
        }
    }

    private fun Map<String, Any>.toBundle(): Bundle =
        Bundle().apply {
            forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
}
