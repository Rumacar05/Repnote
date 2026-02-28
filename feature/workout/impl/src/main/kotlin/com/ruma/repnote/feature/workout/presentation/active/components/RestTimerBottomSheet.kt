package com.ruma.repnote.feature.workout.presentation.active.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ruma.repnote.core.designsystem.theme.Spacings

/**
 * Bottom sheet displaying a countdown rest timer.
 * Shows circular progress indicator and allows skipping the rest.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestTimerBottomSheet(
    secondsRemaining: Int,
    totalSeconds: Int,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        RestTimerContent(
            secondsRemaining = secondsRemaining,
            totalSeconds = totalSeconds,
            onSkip = onSkip,
        )
    }
}

@Composable
private fun RestTimerContent(
    secondsRemaining: Int,
    totalSeconds: Int,
    onSkip: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(Spacings.spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Rest Time",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing32))

        RestTimerDisplay(
            secondsRemaining = secondsRemaining,
            totalSeconds = totalSeconds,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing32))

        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Skip Rest")
        }

        Spacer(modifier = Modifier.height(Spacings.spacing24))
    }
}

@Composable
private fun RestTimerDisplay(
    secondsRemaining: Int,
    totalSeconds: Int,
) {
    val progress =
        if (totalSeconds > 0) {
            secondsRemaining.toFloat() / totalSeconds.toFloat()
        } else {
            0f
        }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "timer_progress",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp),
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(200.dp),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = formatTime(secondsRemaining),
                style =
                    MaterialTheme.typography.displayLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = "seconds remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val SECONDS_PER_MINUTE = 60

/**
 * Formats seconds into MM:SS format.
 */
private fun formatTime(totalSeconds: Int): String {
    val minutes = totalSeconds / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}
