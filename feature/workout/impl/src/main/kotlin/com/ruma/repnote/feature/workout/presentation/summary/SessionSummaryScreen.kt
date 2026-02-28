package com.ruma.repnote.feature.workout.presentation.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ruma.repnote.core.designsystem.components.ErrorState
import com.ruma.repnote.core.designsystem.components.LoadingState
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.feature.workout.presentation.summary.components.ComparisonIndicator
import com.ruma.repnote.feature.workout.presentation.summary.components.ExerciseSummaryCard
import com.ruma.repnote.feature.workout.presentation.summary.components.StatisticCard
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen for displaying a completed workout session summary.
 * Shows statistics, exercise breakdown, and comparison with previous sessions.
 */
class SessionSummaryScreen {
    @Composable
    fun Screen(
        sessionId: String,
        onNavigateToHome: () -> Unit,
    ) {
        SessionSummaryRoot(
            sessionId = sessionId,
            onNavigateToHome = onNavigateToHome,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionSummaryRoot(
    sessionId: String,
    onNavigateToHome: () -> Unit,
    viewModel: SessionSummaryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.initialize(sessionId)
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                SessionSummaryNavigationEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    RepnoteScreen(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.padding(start = Spacings.spacing8))
                        Text(
                            text = "Workout Complete!",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                },
                actions = {
                    TextButton(onClick = viewModel::onNavigateToHome) {
                        Text("Done")
                    }
                },
            )
        },
    ) { modifier ->
        SessionSummaryContent(
            uiState = uiState,
            modifier = modifier,
        )
    }
}

@Composable
private fun SessionSummaryContent(
    uiState: SessionSummaryUiState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            LoadingState(modifier = modifier.fillMaxSize())
        }

        uiState.errorMessage != null -> {
            ErrorState(
                errorMessage = uiState.errorMessage,
                modifier = modifier.fillMaxSize(),
            )
        }

        uiState.session != null && uiState.statistics != null -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacings.spacing16),
                verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
            ) {
                item {
                    SessionHeader(
                        routineName = uiState.session.routineName,
                        date = uiState.session.startTime,
                    )
                }

                item {
                    OverallStatistics(
                        statistics = uiState.statistics,
                        comparison = uiState.comparison,
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Spacings.spacing8))
                    Text(
                        text = "Exercise Breakdown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                items(
                    items = uiState.statistics.exerciseStats,
                    key = { it.exerciseId },
                ) { exerciseStat ->
                    ExerciseSummaryCard(
                        exerciseStats = exerciseStat,
                        comparison =
                            uiState.comparison
                                ?.exerciseComparisons
                                ?.find { it.exerciseId == exerciseStat.exerciseId },
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionHeader(
    routineName: String,
    date: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = routineName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing4))
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun OverallStatistics(
    statistics: WorkoutStatistics,
    comparison: WorkoutComparison?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacings.spacing16),
    ) {
        Text(
            text = "Summary",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            StatisticCard(
                title = "Duration",
                value = formatDuration(statistics.totalDurationSeconds),
                modifier = Modifier.weight(1f),
                comparisonIndicator =
                    if (comparison != null) {
                        {
                            ComparisonIndicator(
                                difference = comparison.durationDifference.toDouble(),
                                percentChange = null,
                                unit = "s",
                                invertColors = true,
                            )
                        }
                    } else {
                        null
                    },
            )

            StatisticCard(
                title = "Total Volume",
                value = formatVolume(statistics.totalVolume),
                modifier = Modifier.weight(1f),
                comparisonIndicator =
                    if (comparison != null) {
                        {
                            ComparisonIndicator(
                                difference = comparison.volumeDifference,
                                percentChange = comparison.volumePercentChange,
                                unit = "kg",
                            )
                        }
                    } else {
                        null
                    },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing12),
        ) {
            StatisticCard(
                title = "Exercises",
                value = statistics.exerciseStats.size.toString(),
                modifier = Modifier.weight(1f),
            )

            StatisticCard(
                title = "Total Sets",
                value = statistics.exerciseStats.sumOf { it.sets }.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private const val SECONDS_IN_HOUR = 3600
private const val SECONDS_IN_MINUTE = 60

private const val VOLUME_THRESHOLD = 1000.0
private const val VOLUME_DIVISOR = 1000.0

private fun formatDuration(seconds: Long): String {
    val hours = seconds / SECONDS_IN_HOUR
    val minutes = (seconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
    val secs = seconds % SECONDS_IN_MINUTE

    return when {
        hours > 0 -> String.format(Locale.US, "%dh %dm", hours, minutes)
        minutes > 0 -> String.format(Locale.US, "%dm %ds", minutes, secs)
        else -> String.format(Locale.US, "%ds", secs)
    }
}

private fun formatVolume(volume: Double): String =
    when {
        volume >= VOLUME_THRESHOLD -> String.format(Locale.US, "%.1f t", volume / VOLUME_DIVISOR)
        volume > 0 -> String.format(Locale.US, "%.0f kg", volume)
        else -> "0 kg"
    }

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
