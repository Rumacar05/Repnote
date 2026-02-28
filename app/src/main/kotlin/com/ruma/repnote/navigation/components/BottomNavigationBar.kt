package com.ruma.repnote.navigation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.ruma.repnote.navigation.Home
import com.ruma.repnote.navigation.Profile
import com.ruma.repnote.navigation.Routines
import kotlin.reflect.KClass

@Composable
fun BottomNavigationBar(
    currentRoute: NavKey?,
    onNavigate: (KClass<*>) -> Unit,
) {
    val items =
        listOf(
            BottomNavItem(Home, Home::class, "Home", Icons.Default.Home),
            BottomNavItem(Routines, Routines::class, "Routines", Icons.Default.DateRange),
            BottomNavItem(Profile, Profile::class, "Profile", Icons.Default.AccountCircle),
        )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute != null && currentRoute::class == item.routeClass,
                onClick = { onNavigate(item.routeClass) },
            )
        }
    }
}
