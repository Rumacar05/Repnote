package com.ruma.repnote.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for LoadingState component using Paparazzi.
 *
 * Run tests with: ./gradlew :core:design-system:testDebug
 * Record screenshots with: ./gradlew :core:design-system:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :core:design-system:verifyPaparazziDebug
 */
class LoadingStateScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    @Test
    fun loadingState_default_lightTheme() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Test
    fun loadingState_default_darkTheme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Test
    fun loadingState_withCustomModifier() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoadingState(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                )
            }
        }
    }
}
