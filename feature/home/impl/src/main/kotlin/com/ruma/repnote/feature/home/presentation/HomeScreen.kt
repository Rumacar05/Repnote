package com.ruma.repnote.feature.home.presentation

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.analytics.compose.TrackScreenView
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.core.designsystem.extensions.toStringRes
import com.ruma.repnote.core.domain.model.WorkoutSession
import com.ruma.repnote.core.domain.model.WorkoutStatus
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ruma.repnote.core.stringresources.R as StringResources

private const val SECONDS_IN_HOUR = 3600
private const val SECONDS_IN_MINUTE = 60

class HomeScreen {
    @Composable
    fun Screen(
        bottomBar: @Composable () -> Unit = {},
        onNavigateToActiveWorkout: (String) -> Unit = {},
        onNavigateToSessionDetail: (String) -> Unit = {},
    ) {
        HomeRoot(
            bottomBar = bottomBar,
            onNavigateToActiveWorkout = onNavigateToActiveWorkout,
            onNavigateToSessionDetail = onNavigateToSessionDetail,
        )
    }
}

@Composable
internal fun HomeRoot(
    bottomBar: @Composable () -> Unit = {},
    onNavigateToActiveWorkout: (String) -> Unit = {},
    onNavigateToSessionDetail: (String) -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    TrackScreenView(AnalyticsScreen.HOME)

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is HomeNavigationEvent.NavigateToActiveWorkout -> {
                    onNavigateToActiveWorkout(event.sessionId)
                }
            }
        }
    }

    RepnoteScreen(
        topBar = { HomeTopAppBar() },
        bottomBar = bottomBar,
    ) { modifier ->
        HomeContent(
            displayName = uiState.displayName,
            hasActiveWorkout = uiState.hasActiveWorkout,
            recentWorkouts = uiState.recentWorkouts,
            isLoadingWorkouts = uiState.isLoadingWorkouts,
            onResumeWorkoutClick = viewModel::onResumeWorkoutClick,
            onSessionClick = onNavigateToSessionDetail,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(StringResources.string.app_name)) },
    )
}

@Suppress("LongMethod")
@Composable
private fun HomeContent(
    displayName: String?,
    hasActiveWorkout: Boolean,
    recentWorkouts: List<WorkoutSession>,
    isLoadingWorkouts: Boolean,
    onResumeWorkoutClick: () -> Unit,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Welcome back${displayName?.let { ", $it" } ?: ""}!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (hasActiveWorkout) {
            item {
                Button(
                    onClick = onResumeWorkoutClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(stringResource(StringResources.string.home_resume_active_workout))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(StringResources.string.home_recent_workouts),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        if (isLoadingWorkouts) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (!isLoadingWorkouts && recentWorkouts.isEmpty()) {
            item {
                Text(
                    text = stringResource(StringResources.string.home_no_workouts_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
        }

        items(
            items = recentWorkouts,
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
                        ?: stringResource(StringResources.string.workout_ad_hoc),
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
                Text(
                    text = stringResource(workout.status.toStringRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        when (workout.status) {
                            WorkoutStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                            WorkoutStatus.ABANDONED -> MaterialTheme.colorScheme.error
                            WorkoutStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
                        },
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    stringResource(
                        StringResources.string.workout_exercises_count,
                        workout.exercises.size,
                    ) +
                        (workout.totalDurationSeconds?.let { " • ${formatDuration(it)}" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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
