package com.ruma.repnote.core.analytics.domain.model

/**
 * Typed analytics events for the application.
 * Each event includes its name and parameters.
 */
sealed class AnalyticsEvent(
    val name: String,
    val params: Map<String, Any> = emptyMap(),
) {
    // Authentication Events
    data object LoginStarted : AnalyticsEvent("login_started")

    data class LoginSuccess(
        val method: String,
    ) : AnalyticsEvent(
            "login_success",
            mapOf("method" to method),
        )

    data class LoginFailed(
        val errorType: String,
    ) : AnalyticsEvent(
            "login_failed",
            mapOf("error_type" to errorType),
        )

    data object SignUpStarted : AnalyticsEvent("sign_up_started")

    data class SignUpSuccess(
        val method: String,
    ) : AnalyticsEvent(
            "sign_up_success",
            mapOf("method" to method),
        )

    data class SignUpFailed(
        val errorType: String,
    ) : AnalyticsEvent(
            "sign_up_failed",
            mapOf("error_type" to errorType),
        )

    data object SignOut : AnalyticsEvent("sign_out")

    // Routine Events
    data object RoutineCreateStarted : AnalyticsEvent("routine_create_started")

    data class RoutineCreated(
        val routineId: String,
        val exerciseCount: Int,
    ) : AnalyticsEvent(
            "routine_created",
            mapOf(
                "routine_id" to routineId,
                "exercise_count" to exerciseCount,
            ),
        )

    data class RoutineEdited(
        val routineId: String,
    ) : AnalyticsEvent(
            "routine_edited",
            mapOf("routine_id" to routineId),
        )

    data class RoutineDeleted(
        val routineId: String,
    ) : AnalyticsEvent(
            "routine_deleted",
            mapOf("routine_id" to routineId),
        )

    data class RoutineViewed(
        val routineId: String,
    ) : AnalyticsEvent(
            "routine_viewed",
            mapOf("routine_id" to routineId),
        )

    // Workout Events
    data class WorkoutStarted(
        val routineId: String?,
        val routineName: String?,
        val exerciseCount: Int,
    ) : AnalyticsEvent(
            "workout_started",
            buildMap {
                routineId?.let { put("routine_id", it) }
                routineName?.let { put("routine_name", it) }
                put("exercise_count", exerciseCount)
                put("is_ad_hoc", routineId == null)
            },
        )

    data class WorkoutCompleted(
        val sessionId: String,
        val durationSeconds: Long,
        val exerciseCount: Int,
        val totalSets: Int,
    ) : AnalyticsEvent(
            "workout_completed",
            mapOf(
                "session_id" to sessionId,
                "duration_seconds" to durationSeconds,
                "exercise_count" to exerciseCount,
                "total_sets" to totalSets,
            ),
        )

    data class WorkoutAbandoned(
        val sessionId: String,
        val durationSeconds: Long,
        val completedExercises: Int,
    ) : AnalyticsEvent(
            "workout_abandoned",
            mapOf(
                "session_id" to sessionId,
                "duration_seconds" to durationSeconds,
                "completed_exercises" to completedExercises,
            ),
        )

    data class WorkoutResumed(
        val sessionId: String,
    ) : AnalyticsEvent(
            "workout_resumed",
            mapOf("session_id" to sessionId),
        )

    data class SetCompleted(
        val exerciseName: String,
        val setNumber: Int,
        val reps: Int,
        val weight: Double?,
    ) : AnalyticsEvent(
            "set_completed",
            buildMap {
                put("exercise_name", exerciseName)
                put("set_number", setNumber)
                put("reps", reps)
                weight?.let { put("weight", it) }
            },
        )

    // Error Events
    data class SyncError(
        val errorType: String,
    ) : AnalyticsEvent(
            "sync_error",
            mapOf("error_type" to errorType),
        )

    data class ApiError(
        val endpoint: String,
        val errorCode: Int?,
        val errorMessage: String?,
    ) : AnalyticsEvent(
            "api_error",
            buildMap {
                put("endpoint", endpoint)
                errorCode?.let { put("error_code", it) }
                errorMessage?.let { put("error_message", it) }
            },
        )
}
