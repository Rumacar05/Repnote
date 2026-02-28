package com.ruma.repnote.feature.routine.presentation.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
internal fun NumberInputField(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    minValue: Int = 0,
    nullable: Boolean = false,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { text ->
            if (text.isEmpty() && nullable) {
                onValueChange(null)
            } else {
                text.toIntOrNull()?.let { newValue ->
                    if (newValue >= minValue) {
                        onValueChange(newValue)
                    }
                }
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier,
    )
}
