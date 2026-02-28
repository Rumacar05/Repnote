package com.ruma.repnote.feature.workout.presentation.summary.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.feature.workout.presentation.summary.ExerciseComparison
import com.ruma.repnote.feature.workout.presentation.summary.ExerciseStatistics
import com.ruma.repnote.feature.workout.presentation.summary.SetInfo
import java.util.Locale

/**
 * Card component displaying detailed statistics for a single exercise.
 *
 * @param exerciseStats Statistics for the exercise
 * @param comparison Optional comparison data with previous session
 * @param modifier Optional modifier
 */
@Suppress("LongMethod")
@Composable
fun ExerciseSummaryCard(
    exerciseStats: ExerciseStatistics,
    comparison: ExerciseComparison?,
    modifier: Modifier = Modifier,
) {
    // Debug: Log exercise name
    android.util.Log.d(
        "ExerciseSummaryCard",
        "Exercise: ${exerciseStats.exerciseName}, ID: ${exerciseStats.exerciseId}",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(Spacings.spacing16),
        ) {
            val displayName =
                exerciseStats.exerciseName.ifBlank {
                    "Unknown Exercise (${exerciseStats.exerciseId})"
                }

            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(Spacings.spacing12))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ExerciseMetric(
                    label = "Sets",
                    value = exerciseStats.sets.toString(),
                    modifier = Modifier.weight(1f),
                )
                ExerciseMetric(
                    label = "Reps",
                    value = exerciseStats.totalReps.toString(),
                    modifier = Modifier.weight(1f),
                )
                ExerciseMetric(
                    label = "Volume",
                    value = formatVolume(exerciseStats.totalVolume),
                    modifier = Modifier.weight(1f),
                )
                if (exerciseStats.maxWeight != null) {
                    ExerciseMetric(
                        label = "Max",
                        value = "${String.format(Locale.US, "%.1f", exerciseStats.maxWeight)} kg",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Individual sets section
            if (exerciseStats.completedSets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacings.spacing12))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacings.spacing8))

                Text(
                    text = "Sets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(Spacings.spacing4))

                exerciseStats.completedSets.forEach { set ->
                    SetRow(setInfo = set)
                }
            }

            if (comparison != null) {
                Spacer(modifier = Modifier.height(Spacings.spacing12))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(Spacings.spacing12))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacings.spacing8),
                ) {
                    Text(
                        text = "vs Previous Session",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Volume:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        ComparisonIndicator(
                            difference = comparison.volumeDifference,
                            percentChange = null,
                            unit = "kg",
                        )
                    }

                    if (comparison.maxWeightDifference != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Max Weight:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            ComparisonIndicator(
                                difference = comparison.maxWeightDifference,
                                percentChange = null,
                                unit = "kg",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing4))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SetRow(
    setInfo: SetInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = Spacings.spacing4),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Set ${setInfo.setNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text =
                if (setInfo.weight != null) {
                    "${setInfo.reps} reps × ${String.format(Locale.US, "%.1f", setInfo.weight)} kg"
                } else {
                    "${setInfo.reps} reps"
                },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

private const val VOLUME_THRESHOLD = 1000.0
private const val VOLUME_DIVISOR = 1000.0

private fun formatVolume(volume: Double): String =
    when {
        volume >= VOLUME_THRESHOLD -> String.format(Locale.US, "%.1f t", volume / VOLUME_DIVISOR)
        else -> String.format(Locale.US, "%.0f kg", volume)
    }
