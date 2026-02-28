package com.ruma.repnote.core.analytics.domain.service

import com.ruma.repnote.core.analytics.domain.model.AnalyticsEvent
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen

/**
 * Service interface for analytics tracking.
 * Implementations can use Firebase Analytics, Mixpanel, or any other provider.
 */
interface AnalyticsService {
    /**
     * Log a screen view event.
     * Should be called when navigating to a new screen.
     */
    fun logScreenView(screen: AnalyticsScreen)

    /**
     * Log a custom analytics event.
     */
    fun logEvent(event: AnalyticsEvent)

    /**
     * Set user ID for analytics tracking.
     * Call this after successful login, pass null on logout.
     */
    fun setUserId(userId: String?)

    /**
     * Set user properties for segmentation.
     */
    fun setUserProperty(
        name: String,
        value: String?,
    )

    /**
     * Log a non-fatal error/exception.
     */
    fun logError(
        throwable: Throwable,
        message: String? = null,
    )

    /**
     * Log a custom non-fatal error with context.
     */
    fun logError(
        message: String,
        params: Map<String, Any> = emptyMap(),
    )
}
