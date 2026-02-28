package com.ruma.repnote.navigation

import com.ruma.repnote.feature.auth.presentation.login.LoginScreen
import com.ruma.repnote.feature.auth.presentation.register.RegisterScreen
import com.ruma.repnote.feature.home.presentation.HomeScreen
import com.ruma.repnote.feature.profile.presentation.ProfileScreen
import com.ruma.repnote.feature.routine.presentation.RoutinesScreen
import com.ruma.repnote.feature.routine.presentation.createedit.CreateEditRoutineScreen
import com.ruma.repnote.feature.routine.presentation.detail.RoutineDetailScreen
import com.ruma.repnote.feature.workout.presentation.active.ActiveWorkoutScreen
import com.ruma.repnote.feature.workout.presentation.history.WorkoutHistoryScreen
import com.ruma.repnote.feature.workout.presentation.summary.SessionSummaryScreen
import com.ruma.repnote.presentation.splash.SplashScreen

/**
 * Container for all navigation screens.
 * Simple class that provides screen instances.
 */
class NavigationScreens {
    val splashScreen = SplashScreen()
    val loginScreen = LoginScreen()
    val registerScreen = RegisterScreen()
    val homeScreen = HomeScreen()
    val routinesScreen = RoutinesScreen()
    val createEditRoutineScreen = CreateEditRoutineScreen()
    val routineDetailScreen = RoutineDetailScreen()
    val profileScreen = ProfileScreen()
    val activeWorkoutScreen = ActiveWorkoutScreen()
    val workoutHistoryScreen = WorkoutHistoryScreen()
    val sessionSummaryScreen = SessionSummaryScreen()
}
