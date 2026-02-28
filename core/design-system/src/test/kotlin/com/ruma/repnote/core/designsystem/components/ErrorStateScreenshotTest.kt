package com.ruma.repnote.core.designsystem.components

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for ErrorState component using Paparazzi.
 *
 * Run tests with: ./gradlew :core:design-system:testDebug
 * Record screenshots with: ./gradlew :core:design-system:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :core:design-system:verifyPaparazziDebug
 */
class ErrorStateScreenshotTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    @Test
    fun errorState_shortMessage_lightTheme() {
        paparazzi.snapshot {
            RepnoteTheme {
                ErrorState(errorMessage = "Error loading data")
            }
        }
    }

    @Test
    fun errorState_shortMessage_darkTheme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                ErrorState(errorMessage = "Error loading data")
            }
        }
    }

    @Test
    fun errorState_longMessage_lightTheme() {
        paparazzi.snapshot {
            RepnoteTheme {
                ErrorState(
                    errorMessage =
                        "An unexpected error occurred while trying to load your workout data. " +
                            "Please check your internet connection and try again.",
                )
            }
        }
    }

    @Test
    fun errorState_longMessage_darkTheme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                ErrorState(
                    errorMessage =
                        "An unexpected error occurred while trying to load your workout data. " +
                            "Please check your internet connection and try again.",
                )
            }
        }
    }

    @Test
    fun errorState_networkError() {
        paparazzi.snapshot {
            RepnoteTheme {
                ErrorState(errorMessage = "Network connection failed. Please try again.")
            }
        }
    }

    @Test
    fun errorState_authenticationError() {
        paparazzi.snapshot {
            RepnoteTheme {
                ErrorState(errorMessage = "Session expired. Please log in again.")
            }
        }
    }

    @Test
    fun errorState_multilineMessage() {
        paparazzi.snapshot {
            RepnoteTheme {
                ErrorState(
                    errorMessage =
                        "Failed to sync your data.\n\n" +
                            "This could be due to:\n" +
                            "• Network connectivity issues\n" +
                            "• Server maintenance\n" +
                            "• Authentication problems",
                )
            }
        }
    }
}
