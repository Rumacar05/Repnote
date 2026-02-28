package com.ruma.repnote.feature.workout.presentation.active.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Dialog to confirm completing the workout.
 */
@Composable
internal fun CompleteWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Complete Workout?") },
        text = {
            Text(
                "Are you sure you want to complete this workout? " +
                    "All your progress will be saved.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Complete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/**
 * Dialog to confirm abandoning the workout.
 */
@Composable
internal fun AbandonWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abandon Workout?") },
        text = {
            Text(
                "Are you sure you want to abandon this workout? " +
                    "Your progress will NOT be saved and will be lost.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Abandon",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
