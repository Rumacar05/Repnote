package com.ruma.repnote.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Splash : NavKey

@Serializable
data object Login : NavKey

@Serializable
data object Register : NavKey

@Serializable
data object Home : NavKey

@Serializable
data object Routines : NavKey

@Serializable
data object CreateRoutine : NavKey

@Serializable
data class EditRoutine(
    val routineId: String,
) : NavKey

@Serializable
data class RoutineDetail(
    val routineId: String,
) : NavKey

@Serializable
data object Profile : NavKey

@Serializable
data object ActiveWorkout : NavKey

@Serializable
data class SessionSummary(
    val sessionId: String,
) : NavKey

@Serializable
data object WorkoutHistory : NavKey
