package com.ruma.repnote.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.model.AuthResult
import com.ruma.repnote.core.auth.domain.usecase.GetCurrentUserUseCase
import com.ruma.repnote.core.auth.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val signOutUseCase: SignOutUseCase,
    getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<ProfileNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            getCurrentUserUseCase().collect { user ->
                _uiState.update {
                    it.copy(
                        userEmail = user?.email ?: "Guest",
                        displayName = user?.displayName,
                    )
                }
            }
        }
    }

    fun onSignOutClick() {
        viewModelScope.launch {
            when (signOutUseCase()) {
                is AuthResult.Success -> {
                    _navigationEvent.emit(ProfileNavigationEvent.NavigateToLogin)
                }
                is AuthResult.Error -> {
                    // Error handling can be added here if needed
                }
            }
        }
    }
}

sealed interface ProfileNavigationEvent {
    data object NavigateToLogin : ProfileNavigationEvent
}
