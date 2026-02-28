package com.ruma.repnote.feature.workout.presentation.summary.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.theme.Spacings
import java.util.Locale
import kotlin.math.abs

/**
 * Displays a visual comparison indicator showing improvement, decline, or neutral change.
 *
 * @param difference The numeric difference (positive = improvement for volume/weight metrics)
 * @param percentChange Optional percentage change to display
 * @param unit The unit to display after the value (e.g., "kg", "s")
 * @param modifier Optional modifier
 * @param invertColors If true, positive is shown as error (red) and negative as primary (blue).
 *                     Used for metrics where lower is better (e.g., duration)
 */
@Suppress("CyclomaticComplexMethod")
@Composable
fun ComparisonIndicator(
    difference: Double,
    modifier: Modifier = Modifier,
    percentChange: Double? = null,
    unit: String = "",
    invertColors: Boolean = false,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isPositive = difference > 0
        val isNeutral = difference == 0.0

        val color =
            when {
                isNeutral -> MaterialTheme.colorScheme.onSurfaceVariant
                isPositive && !invertColors -> MaterialTheme.colorScheme.primary
                isPositive && invertColors -> MaterialTheme.colorScheme.error
                !isPositive && !invertColors -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }

        if (!isNeutral) {
            val icon =
                if (isPositive) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(Spacings.spacing4))
        }

        val displayText =
            buildString {
                if (isPositive) append("+")
                append(String.format(Locale.US, "%.1f", abs(difference)))
                if (unit.isNotEmpty()) append(" $unit")
                if (percentChange != null) {
                    append(" (")
                    if (percentChange > 0) append("+")
                    append(String.format(Locale.US, "%.1f", percentChange))
                    append("%)")
                }
            }

        Text(
            text = displayText,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}
