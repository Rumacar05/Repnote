package com.ruma.repnote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.ruma.repnote.navigation.components.BottomNavigationBar
import kotlin.reflect.KClass

@Composable
fun RepnoteNavDisplay(
    navigator: Navigator,
    navigationState: NavigationState,
    navigationScreens: NavigationScreens,
) {
    val navigateToBottomNavRoute = rememberBottomNavNavigator(navigator, navigationState)

    NavDisplay(
        entries =
            navigationState.toEntries(
                createNavEntries(
                    navigator = navigator,
                    navigationState = navigationState,
                    navigationScreens = navigationScreens,
                    navigateToBottomNavRoute = navigateToBottomNavRoute,
                ),
            ),
        onBack = { navigator.goBack() },
    )
}

@Composable
private fun rememberBottomNavNavigator(
    navigator: Navigator,
    navigationState: NavigationState,
): (KClass<*>) -> Unit =
    remember(navigator, navigationState) {
        { route ->
            val currentRoute =
                navigationState.backStacks[navigationState.topLevelRoute]
                    ?.lastOrNull()

            if (currentRoute != null &&
                currentRoute::class != route
            ) {
                // Navigate to the new bottom bar route
                when (route) {
                    Home::class -> navigator.navigate(Home)
                    Routines::class -> navigator.navigate(Routines)
                    Profile::class -> navigator.navigate(Profile)
                }
            }
        }
    }

@Composable
@Suppress("LongMethod")
private fun createNavEntries(
    navigator: Navigator,
    navigationState: NavigationState,
    navigationScreens: NavigationScreens,
    navigateToBottomNavRoute: (KClass<*>) -> Unit,
) = entryProvider {
    entry<Splash> {
        navigationScreens.splashScreen.Screen(
            onNavigateToLogin = {
                // Reset all navigation state before navigating to Login
                navigator.resetAllStacks()
                navigator.navigate(Login)
            },
            onNavigateToHome = {
                // Reset all navigation state before navigating to Home
                navigator.resetAllStacks()
                navigator.navigate(Home)
            },
        )
    }

    entry<Login> {
        navigationScreens.loginScreen.Screen(
            onNavigateToRegister = { navigator.navigate(Register) },
            onNavigateToHome = {
                // Reset all navigation state after successful login
                navigator.resetAllStacks()
                navigator.navigate(Home)
            },
        )
    }

    entry<Register> {
        navigationScreens.registerScreen.Screen(
            onNavigateBack = { navigator.goBack() },
            onNavigateToHome = {
                // Reset all navigation state after successful registration
                navigator.resetAllStacks()
                navigator.navigate(Home)
            },
        )
    }

    entry<Home> {
        val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

        navigationScreens.homeScreen.Screen(
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = navigateToBottomNavRoute,
                )
            },
            onNavigateToActiveWorkout = {
                navigator.navigate(ActiveWorkout)
            },
            onNavigateToSessionDetail = { sessionId ->
                navigator.navigate(SessionSummary(sessionId))
            },
        )
    }

    entry<Routines> {
        val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

        navigationScreens.routinesScreen.Screen(
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = navigateToBottomNavRoute,
                )
            },
            onRoutineClick = { routine ->
                navigator.navigate(RoutineDetail(routine.id))
            },
            onCreateRoutineClick = {
                navigator.navigate(CreateRoutine)
            },
            onStartWorkout = {
                navigator.navigate(ActiveWorkout)
            },
            onResumeWorkout = {
                navigator.navigate(ActiveWorkout)
            },
        )
    }

    entry<CreateRoutine> {
        navigationScreens.createEditRoutineScreen.Screen(
            onNavigateBack = { navigator.goBack() },
            onRoutineCreated = { routineId ->
                navigator.goBack()
                navigator.navigate(RoutineDetail(routineId))
            },
        )
    }

    entry<EditRoutine> { route ->
        navigationScreens.createEditRoutineScreen.Screen(
            routineId = route.routineId,
            onNavigateBack = { navigator.goBack() },
            onRoutineCreated = { routineId ->
                navigator.goBack()
                navigator.navigate(RoutineDetail(routineId))
            },
        )
    }

    entry<RoutineDetail> { route ->
        navigationScreens.routineDetailScreen.Screen(
            routineId = route.routineId,
            onNavigateBack = { navigator.goBack() },
            onNavigateToEdit = { routineId ->
                navigator.navigate(EditRoutine(routineId))
            },
            onNavigateToActiveWorkout = {
                navigator.navigate(ActiveWorkout)
            },
        )
    }

    entry<Profile> {
        val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

        navigationScreens.profileScreen.Screen(
            bottomBar = {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigate = navigateToBottomNavRoute,
                )
            },
            onSignOutClick = {
                // Reset all navigation state to prevent corruption after re-login
                navigator.resetAllStacks()
                navigator.navigate(Login)
            },
            onNavigateToRoutines = {
                navigator.navigate(WorkoutHistory)
            },
        )
    }

    entry<ActiveWorkout> {
        navigationScreens.activeWorkoutScreen.Screen(
            onNavigateBack = { navigator.goBack() },
            onNavigateToSessionSummary = { sessionId ->
                navigator.goBack()
                navigator.navigate(SessionSummary(sessionId))
            },
        )
    }

    entry<WorkoutHistory> {
        navigationScreens.workoutHistoryScreen.Screen(
            onNavigateBack = { navigator.goBack() },
            onNavigateToSessionDetail = { sessionId ->
                navigator.navigate(SessionSummary(sessionId))
            },
        )
    }

    entry<SessionSummary> { route ->
        navigationScreens.sessionSummaryScreen.Screen(
            sessionId = route.sessionId,
            onNavigateToHome = {
                // Simply go back to the previous screen (Home or Routines)
                // This maintains the bottom bar state correctly
                navigator.goBack()
            },
        )
    }
}
