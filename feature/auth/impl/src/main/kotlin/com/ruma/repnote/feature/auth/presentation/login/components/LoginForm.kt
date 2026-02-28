package com.ruma.repnote.feature.auth.presentation.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.feature.auth.presentation.components.EmailTextField
import com.ruma.repnote.feature.auth.presentation.components.PasswordTextField
import com.ruma.repnote.core.stringresources.R as StringRes

/**
 * Login form with email, password fields, and action buttons.
 */
@Composable
fun LoginForm(
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
    Column(modifier = modifier.fillMaxWidth()) {
        EmailTextField(
            email = email,
            onEmailChange = onEmailChange,
            isEmailValid = isEmailValid,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacings.spacing16))

        PasswordTextField(
            password = password,
            onPasswordChange = onPasswordChange,
            isPasswordValid = isPasswordValid,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(Spacings.spacing8))

        // Error Message
        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
        }

        // Sign In Button
        Button(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(StringRes.string.auth_sign_in))
            }
        }

        Spacer(modifier = Modifier.height(Spacings.spacing8))

        // Register Link
        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(StringRes.string.auth_no_account))
        }
    }
}
