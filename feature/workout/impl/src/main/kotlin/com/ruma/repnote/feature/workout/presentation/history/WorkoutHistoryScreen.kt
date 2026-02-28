package com.ruma.repnote.feature.workout.presentation.history

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.core.designsystem.extensions.toStringRes
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ruma.repnote.core.stringresources.R as StringRes

private const val SECONDS_IN_HOUR = 3600
private const val SECONDS_IN_MINUTE = 60

class WorkoutHistoryScreen {
    @Composable
    fun Screen(
        onNavigateBack: () -> Unit,
        onNavigateToSessionDetail: (String) -> Unit,
    ) {
        WorkoutHistoryRoot(
            onNavigateBack = onNavigateBack,
            onNavigateToSessionDetail = onNavigateToSessionDetail,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutHistoryRoot(
    onNavigateBack: () -> Unit,
    onNavigateToSessionDetail: (String) -> Unit,
    viewModel: WorkoutHistoryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    RepnoteScreen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(StringRes.string.workout_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back",
                        )
                    }
                },
            )
        },
    ) { modifier ->
        WorkoutHistoryContent(
            workouts = uiState.workouts,
            isLoading = uiState.isLoading,
            onSessionClick = onNavigateToSessionDetail,
            modifier = modifier,
        )
    }
}

@Composable
private fun WorkoutHistoryContent(
    workouts: List<WorkoutSession>,
    isLoading: Boolean,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isLoading) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (!isLoading && workouts.isEmpty()) {
            item {
                Text(
                    text = stringResource(StringRes.string.workout_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
        }

        items(
            items = workouts,
            key = { it.id },
        ) { workout ->
            WorkoutSessionCard(
                workout = workout,
                onClick = { onSessionClick(workout.id) },
            )
        }
    }
}

@Composable
private fun WorkoutSessionCard(
    workout: WorkoutSession,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text =
                    workout.routineName.takeIf { it.isNotBlank() }
                        ?: stringResource(StringRes.string.workout_free_workout),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDate(workout.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WorkoutStatusBadge(status = workout.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    stringResource(
                        StringRes.string.workout_exercises_count,
                        workout.exercises.size,
                    ) +
                        (workout.totalDurationSeconds?.let { " • ${formatDuration(it)}" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WorkoutStatusBadge(status: WorkoutStatus) {
    Text(
        text = stringResource(status.toStringRes()),
        style = MaterialTheme.typography.bodySmall,
        color =
            when (status) {
                WorkoutStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                WorkoutStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                WorkoutStatus.ABANDONED -> MaterialTheme.colorScheme.error
            },
        fontWeight = FontWeight.Medium,
    )
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / SECONDS_IN_HOUR
    val minutes = (seconds % SECONDS_IN_HOUR) / SECONDS_IN_MINUTE
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
