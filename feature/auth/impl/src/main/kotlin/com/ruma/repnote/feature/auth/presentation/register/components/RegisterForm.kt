package com.ruma.repnote.feature.auth.presentation.register.components

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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.feature.auth.presentation.components.EmailTextField
import com.ruma.repnote.feature.auth.presentation.components.PasswordTextField
import com.ruma.repnote.core.stringresources.R as StringRes

/**
 * Register form with email, password, confirm password fields, and action buttons.
 */
@Composable
fun RegisterForm(
    email: String,
    password: String,
    confirmPassword: String,
    isEmailValid: Boolean,
    isPasswordValid: Boolean,
    isConfirmPasswordValid: Boolean,
    errorMessage: String?,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
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
            imeAction = ImeAction.Next,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing16))

        PasswordTextField(
            password = confirmPassword,
            onPasswordChange = onConfirmPasswordChange,
            isPasswordValid = isConfirmPasswordValid,
            isLoading = isLoading,
            modifier = Modifier.fillMaxWidth(),
            labelResId = StringRes.string.auth_confirm_password,
            errorMessageResId = StringRes.string.error_passwords_dont_match,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing8))

        ErrorMessage(errorMessage)

        // Register Button
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(StringRes.string.auth_register))
            }
        }

        Spacer(modifier = Modifier.height(Spacings.spacing8))

        // Back to Login Link
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(StringRes.string.auth_have_account))
        }
    }
}

@Composable
private fun ErrorMessage(errorMessage: String?) {
    errorMessage?.let { error ->
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing8))
    }
}
