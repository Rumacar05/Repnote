package com.ruma.repnote.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.model.AuthException
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.feature.auth.domain.AuthConstants
import com.ruma.repnote.feature.auth.domain.usecase.SignInWithEmailUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LoginViewModel(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<LoginNavigationEvent>()
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

    fun onSignInClick() {
        if (!validateInput()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (
                val result =
                    signInWithEmailUseCase(_uiState.value.email, _uiState.value.password)
            ) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _navigationEvent.emit(LoginNavigationEvent.NavigateToHome)
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

    fun onRegisterClick() {
        viewModelScope.launch {
            _navigationEvent.emit(LoginNavigationEvent.NavigateToRegister)
        }
    }

    private fun validateInput(): Boolean {
        val emailValid =
            _uiState.value.email.isNotBlank() &&
                android.util.Patterns.EMAIL_ADDRESS
                    .matcher(_uiState.value.email)
                    .matches()
        val passwordValid = _uiState.value.password.length >= AuthConstants.MIN_PASSWORD_LENGTH

        _uiState.update {
            it.copy(
                isEmailValid = emailValid,
                isPasswordValid = passwordValid,
            )
        }

        return emailValid && passwordValid
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

sealed interface LoginNavigationEvent {
    data object NavigateToHome : LoginNavigationEvent
    data object NavigateToRegister : LoginNavigationEvent
}
