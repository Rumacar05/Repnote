package com.ruma.repnote.feature.auth.presentation.register

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.feature.auth.presentation.register.components.RegisterContent
import org.koin.androidx.compose.koinViewModel

class RegisterScreen {
    @Composable
    fun Screen(
        onNavigateBack: () -> Unit,
        onNavigateToHome: () -> Unit,
    ) {
        RegisterRoot(
            onNavigateBack = onNavigateBack,
            onNavigateToHome = onNavigateToHome,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegisterRoot(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                RegisterNavigationEvent.NavigateToHome -> onNavigateToHome()
                RegisterNavigationEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    RepnoteScreen { modifier ->
        RegisterContent(
            email = uiState.email,
            password = uiState.password,
            confirmPassword = uiState.confirmPassword,
            isEmailValid = uiState.isEmailValid,
            isPasswordValid = uiState.isPasswordValid,
            isConfirmPasswordValid = uiState.isConfirmPasswordValid,
            errorMessage = uiState.errorMessage,
            isLoading = uiState.isLoading,
            onEmailChange = viewModel::onEmailChange,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onRegisterClick = viewModel::onRegisterClick,
            onBackClick = viewModel::onBackClick,
            modifier = modifier,
        )
    }
}
