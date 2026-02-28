package com.ruma.repnote.feature.auth.presentation.login

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import com.ruma.repnote.feature.auth.presentation.login.components.LoginContent
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for LoginScreen using Paparazzi.
 *
 * Run tests with: ./gradlew :feature:auth:impl:testDebug
 * Record screenshots with: ./gradlew :feature:auth:impl:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :feature:auth:impl:verifyPaparazziDebug
 */
class LoginScreenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            showSystemUi = false,
        )

    @Test
    fun loginScreen_initial_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoginContent(
                    email = "",
                    password = "",
                    isEmailValid = true,
                    isPasswordValid = true,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onRegisterClick = {},
                )
            }
        }
    }

    @Test
    fun loginScreen_with_filled_fields() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoginContent(
                    email = "user@example.com",
                    password = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onRegisterClick = {},
                )
            }
        }
    }

    @Test
    fun loginScreen_with_validation_errors() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoginContent(
                    email = "invalid-email",
                    password = "123",
                    isEmailValid = false,
                    isPasswordValid = false,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onRegisterClick = {},
                )
            }
        }
    }

    @Test
    fun loginScreen_with_error_message() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoginContent(
                    email = "user@example.com",
                    password = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    errorMessage = "Invalid email or password. Please try again.",
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onRegisterClick = {},
                )
            }
        }
    }

    @Test
    fun loginScreen_loading_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                LoginContent(
                    email = "user@example.com",
                    password = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    errorMessage = null,
                    isLoading = true,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onRegisterClick = {},
                )
            }
        }
    }

    @Test
    fun loginScreen_dark_theme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                LoginContent(
                    email = "user@example.com",
                    password = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onRegisterClick = {},
                )
            }
        }
    }
}
