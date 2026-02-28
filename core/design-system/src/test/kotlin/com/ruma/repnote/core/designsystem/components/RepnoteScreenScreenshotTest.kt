package com.ruma.repnote.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for RepnoteScreen component using Paparazzi.
 *
 * Run tests with: ./gradlew :core:design-system:testDebug
 * Record screenshots with: ./gradlew :core:design-system:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :core:design-system:verifyPaparazziDebug
 */
class RepnoteScreenScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun repnoteScreen_withTopBar_lightTheme() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen(
                    topBar = {
                        TopAppBar(
                            title = { Text("Screen Title") },
                            navigationIcon = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                        )
                    },
                ) { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Content")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun repnoteScreen_withTopBar_darkTheme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                RepnoteScreen(
                    topBar = {
                        TopAppBar(
                            title = { Text("Screen Title") },
                            navigationIcon = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                        )
                    },
                ) { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Content")
                    }
                }
            }
        }
    }

    @Test
    fun repnoteScreen_withBottomBar() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen(
                    bottomBar = {
                        BottomAppBar {
                            Text(
                                "Bottom Bar",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    },
                ) { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Content with Bottom Bar")
                    }
                }
            }
        }
    }

    @Test
    fun repnoteScreen_withFAB() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                ) { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Content with FAB")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun repnoteScreen_fullConfiguration() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen(
                    topBar = {
                        TopAppBar(
                            title = { Text("Full Screen") },
                            navigationIcon = {
                                IconButton(onClick = {}) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                        )
                    },
                    bottomBar = {
                        BottomAppBar {
                            Text(
                                "Navigation",
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                ) { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Content with all components")
                    }
                }
            }
        }
    }

    @Test
    fun repnoteScreen_minimalConfiguration() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Minimal screen with just content")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun repnoteScreen_withLoadingContent() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen(
                    topBar = {
                        TopAppBar(title = { Text("Loading Example") })
                    },
                ) { modifier ->
                    LoadingState(modifier = modifier.fillMaxSize())
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun repnoteScreen_withErrorContent() {
        paparazzi.snapshot {
            RepnoteTheme {
                RepnoteScreen(
                    topBar = {
                        TopAppBar(title = { Text("Error Example") })
                    },
                ) { modifier ->
                    ErrorState(
                        errorMessage = "Failed to load data",
                        modifier = modifier,
                    )
                }
            }
        }
    }

    @Test
    fun repnoteScreen_darkTheme_withAllComponents() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                RepnoteScreen(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    },
                ) { modifier ->
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Dark theme content")
                    }
                }
            }
        }
    }
}
