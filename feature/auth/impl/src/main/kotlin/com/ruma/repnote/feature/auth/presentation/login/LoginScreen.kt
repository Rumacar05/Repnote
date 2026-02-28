package com.ruma.repnote.feature.auth.presentation.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.feature.auth.presentation.login.components.LoginContent
import org.koin.androidx.compose.koinViewModel

class LoginScreen {
    @Composable
    fun Screen(
        onNavigateToRegister: () -> Unit,
        onNavigateToHome: () -> Unit,
    ) {
        LoginRoot(
            onNavigateToRegister = onNavigateToRegister,
            onNavigateToHome = onNavigateToHome,
        )
    }
}

@Composable
internal fun LoginRoot(
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                LoginNavigationEvent.NavigateToHome -> onNavigateToHome()
                LoginNavigationEvent.NavigateToRegister -> onNavigateToRegister()
            }
        }
    }

    RepnoteScreen { modifier ->
        LoginContent(
            email = uiState.email,
            password = uiState.password,
            isEmailValid = uiState.isEmailValid,
            isPasswordValid = uiState.isPasswordValid,
            errorMessage = uiState.errorMessage,
            isLoading = uiState.isLoading,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSignInClick = viewModel::onSignInClick,
            onRegisterClick = viewModel::onRegisterClick,
            modifier = modifier,
        )
    }
}
