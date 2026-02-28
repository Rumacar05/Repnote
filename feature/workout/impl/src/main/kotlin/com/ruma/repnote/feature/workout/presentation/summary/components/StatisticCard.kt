package com.ruma.repnote.feature.workout.presentation.summary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.theme.Spacings

/**
 * Card component for displaying a single workout statistic.
 *
 * @param title The title of the statistic (e.g., "Total Volume")
 * @param value The main value to display (e.g., "1,250 kg")
 * @param subtitle Optional subtitle text
 * @param comparisonIndicator Optional composable to show comparison with previous workout
 * @param modifier Optional modifier
 */
@Composable
fun StatisticCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    comparisonIndicator: @Composable (() -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(Spacings.spacing8))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(Spacings.spacing4))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (comparisonIndicator != null) {
                Spacer(modifier = Modifier.height(Spacings.spacing8))
                comparisonIndicator()
            }
        }
    }
}
