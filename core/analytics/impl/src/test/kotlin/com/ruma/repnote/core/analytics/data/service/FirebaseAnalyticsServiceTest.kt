package com.ruma.repnote.core.analytics.data.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.ruma.repnote.core.analytics.domain.model.AnalyticsEvent
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FirebaseAnalyticsServiceTest {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseCrashlytics: FirebaseCrashlytics
    private lateinit var service: FirebaseAnalyticsService

    @BeforeEach
    fun setup() {
        firebaseAnalytics = mockk(relaxed = true)
        firebaseCrashlytics = mockk(relaxed = true)
        service = FirebaseAnalyticsService(firebaseAnalytics, firebaseCrashlytics)
    }

    @Nested
    inner class LogScreenView {
        @Test
        fun `WHEN logScreenView called THEN logs screen_view event`() {
            val screen = AnalyticsScreen.HOME

            service.logScreenView(screen)

            verify {
                firebaseAnalytics.logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW,
                    any<Bundle>(),
                )
            }
        }

        @Test
        fun `WHEN logScreenView called with different screens THEN logs for each screen`() {
            val screens =
                listOf(
                    AnalyticsScreen.LOGIN,
                    AnalyticsScreen.PROFILE,
                    AnalyticsScreen.ACTIVE_WORKOUT,
                )

            screens.forEach { screen ->
                service.logScreenView(screen)
            }

            verify(exactly = 3) {
                firebaseAnalytics.logEvent(
                    FirebaseAnalytics.Event.SCREEN_VIEW,
                    any<Bundle>(),
                )
            }
        }
    }

    @Nested
    inner class LogEvent {
        @Test
        fun `WHEN logEvent called with simple event THEN logs event name`() {
            val event = AnalyticsEvent.LoginStarted

            service.logEvent(event)

            verify { firebaseAnalytics.logEvent("login_started", any<Bundle>()) }
        }

        @Test
        fun `WHEN logEvent called with parameterized event THEN logs event`() {
            val event = AnalyticsEvent.LoginSuccess(method = "email")

            service.logEvent(event)

            verify { firebaseAnalytics.logEvent("login_success", any<Bundle>()) }
        }

        @Test
        fun `WHEN logEvent called with workout completed THEN logs workout_completed`() {
            val event =
                AnalyticsEvent.WorkoutCompleted(
                    sessionId = "session-123",
                    durationSeconds = 3600L,
                    exerciseCount = 5,
                    totalSets = 15,
                )

            service.logEvent(event)

            verify { firebaseAnalytics.logEvent("workout_completed", any<Bundle>()) }
        }

        @Test
        fun `WHEN logEvent called with set completed THEN logs set_completed`() {
            val event =
                AnalyticsEvent.SetCompleted(
                    exerciseName = "Bench Press",
                    setNumber = 1,
                    reps = 10,
                    weight = 100.5,
                )

            service.logEvent(event)

            verify { firebaseAnalytics.logEvent("set_completed", any<Bundle>()) }
        }

        @Test
        fun `WHEN logEvent called with routine events THEN logs correct event names`() {
            val events =
                listOf(
                    AnalyticsEvent.RoutineCreateStarted to "routine_create_started",
                    AnalyticsEvent.RoutineCreated("r1", 5) to "routine_created",
                    AnalyticsEvent.RoutineEdited("r1") to "routine_edited",
                    AnalyticsEvent.RoutineDeleted("r1") to "routine_deleted",
                    AnalyticsEvent.RoutineViewed("r1") to "routine_viewed",
                )

            events.forEach { (event, expectedName) ->
                service.logEvent(event)
                verify { firebaseAnalytics.logEvent(expectedName, any<Bundle>()) }
            }
        }

        @Test
        fun `WHEN logEvent called with workout events THEN logs correct event names`() {
            val events =
                listOf(
                    AnalyticsEvent.WorkoutStarted("r1", "Push", 5) to "workout_started",
                    AnalyticsEvent.WorkoutAbandoned("s1", 100L, 2) to "workout_abandoned",
                    AnalyticsEvent.WorkoutResumed("s1") to "workout_resumed",
                )

            events.forEach { (event, expectedName) ->
                service.logEvent(event)
                verify { firebaseAnalytics.logEvent(expectedName, any<Bundle>()) }
            }
        }

        @Test
        fun `WHEN logEvent called with auth events THEN logs correct event names`() {
            val events =
                listOf(
                    AnalyticsEvent.SignUpStarted to "sign_up_started",
                    AnalyticsEvent.SignUpSuccess("email") to "sign_up_success",
                    AnalyticsEvent.SignUpFailed("error") to "sign_up_failed",
                    AnalyticsEvent.SignOut to "sign_out",
                    AnalyticsEvent.LoginFailed("invalid") to "login_failed",
                )

            events.forEach { (event, expectedName) ->
                service.logEvent(event)
                verify { firebaseAnalytics.logEvent(expectedName, any<Bundle>()) }
            }
        }

        @Test
        fun `WHEN logEvent called with error events THEN logs correct event names`() {
            val events =
                listOf(
                    AnalyticsEvent.SyncError("network") to "sync_error",
                    AnalyticsEvent.ApiError("/api", 500, "error") to "api_error",
                )

            events.forEach { (event, expectedName) ->
                service.logEvent(event)
                verify { firebaseAnalytics.logEvent(expectedName, any<Bundle>()) }
            }
        }
    }

    @Nested
    inner class SetUserId {
        @Test
        fun `WHEN setUserId called with userId THEN sets on both analytics and crashlytics`() {
            val userId = "user-123"

            service.setUserId(userId)

            verify { firebaseAnalytics.setUserId(userId) }
            verify { firebaseCrashlytics.setUserId(userId) }
        }

        @Test
        fun `WHEN setUserId called with null THEN sets null on analytics only`() {
            service.setUserId(null)

            verify { firebaseAnalytics.setUserId(null) }
            verify(exactly = 0) { firebaseCrashlytics.setUserId(any()) }
        }
    }

    @Nested
    inner class SetUserProperty {
        @Test
        fun `WHEN setUserProperty called with value THEN sets on both services`() {
            service.setUserProperty("subscription_type", "premium")

            verify { firebaseAnalytics.setUserProperty("subscription_type", "premium") }
            verify { firebaseCrashlytics.setCustomKey("subscription_type", "premium") }
        }

        @Test
        fun `WHEN setUserProperty called with null THEN sets on analytics only`() {
            service.setUserProperty("subscription_type", null)

            verify { firebaseAnalytics.setUserProperty("subscription_type", null) }
            verify(exactly = 0) { firebaseCrashlytics.setCustomKey(any(), any<String>()) }
        }
    }

    @Nested
    inner class LogError {
        @Test
        fun `WHEN logError called with throwable THEN records exception`() {
            val exception = RuntimeException("Test error")

            service.logError(exception)

            verify { firebaseCrashlytics.recordException(exception) }
        }

        @Test
        fun `WHEN logError called with throwable and message THEN logs message and records exception`() {
            val exception = RuntimeException("Test error")
            val message = "Error during sync"

            service.logError(exception, message)

            verify { firebaseCrashlytics.log(message) }
            verify { firebaseCrashlytics.recordException(exception) }
        }

        @Test
        fun `WHEN logError called with message and params THEN logs with custom keys`() {
            val message = "API call failed"
            val params =
                mapOf(
                    "endpoint" to "/api/workouts",
                    "status_code" to 500,
                    "retry_count" to 3L,
                    "is_retryable" to true,
                    "latency" to 1.5,
                )

            service.logError(message, params)

            verify { firebaseCrashlytics.log(message) }
            verify { firebaseCrashlytics.setCustomKey("endpoint", "/api/workouts") }
            verify { firebaseCrashlytics.setCustomKey("status_code", 500) }
            verify { firebaseCrashlytics.setCustomKey("retry_count", 3L) }
            verify { firebaseCrashlytics.setCustomKey("is_retryable", true) }
            verify { firebaseCrashlytics.setCustomKey("latency", 1.5) }
        }

        @Test
        fun `WHEN logError called with non-standard param type THEN converts to string`() {
            val customObject =
                object {
                    override fun toString() = "CustomValue"
                }
            val params = mapOf("custom" to customObject)

            service.logError("Error", params)

            verify { firebaseCrashlytics.setCustomKey("custom", "CustomValue") }
        }
    }
}
