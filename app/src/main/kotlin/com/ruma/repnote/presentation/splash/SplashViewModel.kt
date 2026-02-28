package com.ruma.repnote.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruma.repnote.core.auth.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _navigationEvent = MutableStateFlow<SplashNavigationEvent?>(null)
    val navigationEvent = _navigationEvent.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            delay(DELAY_ANIMATION)
            val isAuthenticated = authRepository.isUserAuthenticated()
            _navigationEvent.value =
                if (isAuthenticated) {
                    SplashNavigationEvent.NavigateToHome
                } else {
                    SplashNavigationEvent.NavigateToLogin
                }
        }
    }
}

sealed interface SplashNavigationEvent {
    data object NavigateToHome : SplashNavigationEvent
    data object NavigateToLogin : SplashNavigationEvent
}

const val DELAY_ANIMATION: Long = 2000
