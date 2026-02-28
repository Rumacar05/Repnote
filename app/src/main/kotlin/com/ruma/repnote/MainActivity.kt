package com.ruma.repnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import com.ruma.repnote.navigation.Home
import com.ruma.repnote.navigation.NavigationScreens
import com.ruma.repnote.navigation.Navigator
import com.ruma.repnote.navigation.Profile
import com.ruma.repnote.navigation.RepnoteNavDisplay
import com.ruma.repnote.navigation.Routines
import com.ruma.repnote.navigation.rememberNavigationState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RepnoteTheme {
                val navigationState =
                    rememberNavigationState(
                        startRoute = Home,
                        topLevelRoutes =
                            setOf(
                                Home,
                                Routines,
                                Profile,
                            ),
                    )

                val navigator = remember { Navigator(navigationState) }
                val navigationScreens = remember { NavigationScreens() }

                RepnoteNavDisplay(
                    navigator = navigator,
                    navigationState = navigationState,
                    navigationScreens = navigationScreens,
                )
            }
        }
    }
}
