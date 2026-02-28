package com.ruma.repnote.core.analytics.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen
import com.ruma.repnote.core.analytics.domain.service.AnalyticsService
import org.koin.compose.koinInject

/**
 * Side-effect composable to track screen views.
 * Call this at the top level of each screen composable.
 *
 * Usage:
 * ```
 * @Composable
 * fun HomeRoot(...) {
 *     TrackScreenView(AnalyticsScreen.HOME)
 *     // ... rest of the composable
 * }
 * ```
 */
@Composable
fun TrackScreenView(
    screen: AnalyticsScreen,
    analyticsService: AnalyticsService = koinInject(),
) {
    LaunchedEffect(screen) {
        analyticsService.logScreenView(screen)
    }
}
