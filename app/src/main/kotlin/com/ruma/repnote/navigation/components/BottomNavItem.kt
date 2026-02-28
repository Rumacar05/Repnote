package com.ruma.repnote.navigation.components

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

data class BottomNavItem(
    val targetRoute: NavKey,
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
)
