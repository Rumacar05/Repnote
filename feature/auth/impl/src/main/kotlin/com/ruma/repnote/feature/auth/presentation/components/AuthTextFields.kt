package com.ruma.repnote.feature.auth.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.ruma.repnote.core.stringresources.R as StringRes

/**
 * Reusable email text field for authentication screens.
 */
@Composable
internal fun EmailTextField(
    email: String,
    onEmailChange: (String) -> Unit,
    isEmailValid: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(StringRes.string.auth_email)) },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
        isError = !isEmailValid,
        supportingText =
            if (!isEmailValid) {
                { Text(stringResource(StringRes.string.error_invalid_email)) }
            } else {
                null
            },
        modifier = modifier,
        enabled = !isLoading,
    )
}

/**
 * Reusable password text field for authentication screens.
 *
 * @param labelResId Resource ID for the label text (defaults to auth_password)
 * @param errorMessageResId Resource ID for the error message (defaults to error_weak_password)
 */
@Composable
internal fun PasswordTextField(
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordValid: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Done,
    labelResId: Int = StringRes.string.auth_password,
    errorMessageResId: Int = StringRes.string.error_weak_password,
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(labelResId)) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction,
            ),
        isError = !isPasswordValid,
        supportingText =
            if (!isPasswordValid) {
                { Text(stringResource(errorMessageResId)) }
            } else {
                null
            },
        modifier = modifier,
        enabled = !isLoading,
    )
}
