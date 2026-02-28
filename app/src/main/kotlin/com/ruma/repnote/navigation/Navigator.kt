package com.ruma.repnote.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(
    val state: NavigationState,
) {
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // This is a top level route, just switch to it.
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack =
            state.backStacks[state.topLevelRoute]
                ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }

    /**
     * Resets all navigation state by clearing all backStacks and resetting to initial state.
     * This is useful when logging out or when you need to completely reset the navigation.
     */
    fun resetAllStacks() {
        state.backStacks.values.forEach { stack ->
            stack.clear()
        }
        // Reinitialize each top-level route with itself
        state.backStacks.keys.forEach { route ->
            state.backStacks[route]?.add(route)
        }
    }
}
