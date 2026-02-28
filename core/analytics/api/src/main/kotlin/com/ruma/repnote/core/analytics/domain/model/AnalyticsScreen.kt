package com.ruma.repnote.core.analytics.domain.model

/**
 * Enum representing all screens in the app for analytics tracking.
 */
enum class AnalyticsScreen(
    val screenName: String,
    val screenClass: String,
) {
    SPLASH("splash", "SplashScreen"),
    LOGIN("login", "LoginScreen"),
    REGISTER("register", "RegisterScreen"),
    HOME("home", "HomeScreen"),
    ROUTINES("routines", "RoutinesScreen"),
    ROUTINE_DETAIL("routine_detail", "RoutineDetailScreen"),
    CREATE_ROUTINE("create_routine", "CreateEditRoutineScreen"),
    EDIT_ROUTINE("edit_routine", "CreateEditRoutineScreen"),
    PROFILE("profile", "ProfileScreen"),
    ACTIVE_WORKOUT("active_workout", "ActiveWorkoutScreen"),
    SESSION_SUMMARY("session_summary", "SessionSummaryScreen"),
    WORKOUT_HISTORY("workout_history", "WorkoutHistoryScreen"),
}
