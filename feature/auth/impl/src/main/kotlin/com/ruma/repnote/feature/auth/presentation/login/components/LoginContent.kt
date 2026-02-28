package com.ruma.repnote.feature.auth.presentation.login.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.core.stringresources.R as StringRes

/**
 * Main content composable for the Login screen.
 *
 * Provides a scrollable container with proper spacing and padding.
 */
@Composable
fun LoginContent(
    email: String,
    password: String,
    isEmailValid: Boolean,
    isPasswordValid: Boolean,
    errorMessage: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(Spacings.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(StringRes.string.auth_welcome_back),
            style = MaterialTheme.typography.headlineLarge,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing8))

        LoginForm(
            email = email,
            password = password,
            isEmailValid = isEmailValid,
            isPasswordValid = isPasswordValid,
            errorMessage = errorMessage,
            isLoading = isLoading,
            onEmailChange = onEmailChange,
            onPasswordChange = onPasswordChange,
            onSignInClick = onSignInClick,
            onRegisterClick = onRegisterClick,
        )
    }
}
