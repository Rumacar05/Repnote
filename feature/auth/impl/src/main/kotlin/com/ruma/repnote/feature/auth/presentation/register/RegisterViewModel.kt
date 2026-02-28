package com.ruma.repnote.feature.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.feature.auth.domain.AuthConstants
import com.ruma.repnote.feature.auth.domain.usecase.SignUpWithEmailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RegisterViewModel(
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<RegisterNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, isEmailValid = true, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                isPasswordValid = true,
                errorMessage = null,
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            it.copy(
                confirmPassword = confirmPassword,
                isConfirmPasswordValid = true,
                errorMessage = null,
            )
        }
    }

    fun onRegisterClick() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (
                val result =
                    signUpWithEmailUseCase(
                        _uiState.value.email,
                        _uiState.value.password,
                    )
            ) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigationEvent.emit(RegisterNavigationEvent.NavigateToHome)
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exception.toErrorMessage(),
                        )
                    }
                }
            }
        }
    }

    fun onBackClick() {
        viewModelScope.launch {
            _navigationEvent.emit(RegisterNavigationEvent.NavigateBack)
        }
    }

    private fun validateInput(): Boolean {
        val emailValid =
            _uiState.value.email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS
                    .matcher(_uiState.value.email)
                    .matches()
        val passwordValid = _uiState.value.password.length >= AuthConstants.MIN_PASSWORD_LENGTH
        val confirmPasswordValid = _uiState.value.password == _uiState.value.confirmPassword

        _uiState.update {
            it.copy(
                isEmailValid = emailValid,
                isPasswordValid = passwordValid,
                isConfirmPasswordValid = confirmPasswordValid,
            )
        }

        return emailValid && passwordValid && confirmPasswordValid
    }

    private fun AuthException.toErrorMessage(): String =
        when (this) {
            is AuthException.InvalidCredentials -> "Invalid email or password"
            is AuthException.UserNotFound -> "User not found"
            is AuthException.NetworkError -> "Network error. Please check your connection"
            is AuthException.EmailAlreadyInUse -> "Email already in use"
            is AuthException.WeakPassword -> "Password is too weak"
            is AuthException.Unknown -> "An error occurred: ${this.message}"
        }
}

sealed interface RegisterNavigationEvent {
    data object NavigateToHome : RegisterNavigationEvent
    data object NavigateBack : RegisterNavigationEvent
}
