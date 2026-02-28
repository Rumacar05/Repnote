package com.ruma.repnote.feature.auth.presentation.register

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isConfirmPasswordValid: Boolean = true,
)
