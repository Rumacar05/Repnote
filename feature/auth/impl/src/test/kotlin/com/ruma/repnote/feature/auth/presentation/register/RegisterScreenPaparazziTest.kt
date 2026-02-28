package com.ruma.repnote.feature.auth.presentation.register

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.ruma.repnote.core.designsystem.theme.RepnoteTheme
import com.ruma.repnote.feature.auth.presentation.register.components.RegisterContent
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot tests for RegisterScreen using Paparazzi.
 *
 * Run tests with: ./gradlew :feature:auth:impl:testDebug
 * Record screenshots with: ./gradlew :feature:auth:impl:recordPaparazziDebug
 * Verify screenshots with: ./gradlew :feature:auth:impl:verifyPaparazziDebug
 */
class RegisterScreenPaparazziTest {
    @get:Rule
    val paparazzi =
        Paparazzi(
            deviceConfig = DeviceConfig.PIXEL_5,
            theme = "android:Theme.Material3.DayNight.NoActionBar",
            showSystemUi = false,
        )

    @Test
    fun registerScreen_initial_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RegisterContent(
                    email = "",
                    password = "",
                    confirmPassword = "",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isConfirmPasswordValid = true,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }

    @Test
    fun registerScreen_with_filled_fields() {
        paparazzi.snapshot {
            RepnoteTheme {
                RegisterContent(
                    email = "newuser@example.com",
                    password = "securepassword123",
                    confirmPassword = "securepassword123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isConfirmPasswordValid = true,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }

    @Test
    fun registerScreen_with_validation_errors() {
        paparazzi.snapshot {
            RepnoteTheme {
                RegisterContent(
                    email = "invalid-email",
                    password = "123",
                    confirmPassword = "456",
                    isEmailValid = false,
                    isPasswordValid = false,
                    isConfirmPasswordValid = false,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }

    @Test
    fun registerScreen_with_password_mismatch() {
        paparazzi.snapshot {
            RepnoteTheme {
                RegisterContent(
                    email = "user@example.com",
                    password = "password123",
                    confirmPassword = "differentpassword",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isConfirmPasswordValid = false,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }

    @Test
    fun registerScreen_with_error_message() {
        paparazzi.snapshot {
            RepnoteTheme {
                RegisterContent(
                    email = "user@example.com",
                    password = "password123",
                    confirmPassword = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isConfirmPasswordValid = true,
                    errorMessage = "This email is already registered. Please use a different email.",
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }

    @Test
    fun registerScreen_loading_state() {
        paparazzi.snapshot {
            RepnoteTheme {
                RegisterContent(
                    email = "user@example.com",
                    password = "password123",
                    confirmPassword = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isConfirmPasswordValid = true,
                    errorMessage = null,
                    isLoading = true,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }

    @Test
    fun registerScreen_dark_theme() {
        paparazzi.snapshot {
            RepnoteTheme(darkTheme = true) {
                RegisterContent(
                    email = "user@example.com",
                    password = "password123",
                    confirmPassword = "password123",
                    isEmailValid = true,
                    isPasswordValid = true,
                    isConfirmPasswordValid = true,
                    errorMessage = null,
                    isLoading = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onConfirmPasswordChange = {},
                    onRegisterClick = {},
                    onBackClick = {},
                )
            }
        }
    }
}
