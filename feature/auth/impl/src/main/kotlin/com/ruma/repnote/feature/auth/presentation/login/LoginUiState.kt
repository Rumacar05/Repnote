package com.ruma.repnote.feature.auth.presentation.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true,
)
