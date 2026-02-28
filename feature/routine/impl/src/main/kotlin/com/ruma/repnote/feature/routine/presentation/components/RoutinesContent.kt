package com.ruma.repnote.feature.routine.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.components.ErrorState
import com.ruma.repnote.core.designsystem.components.LoadingState
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.core.stringresources.R as StringRes

@Composable
internal fun RoutinesContent(
    routines: List<Routine>,
    isLoading: Boolean,
    errorMessage: String?,
    hasActiveWorkout: Boolean,
    onRoutineClick: (Routine) -> Unit,
    onStartWorkout: (Routine) -> Unit,
    onResumeWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                LoadingState(modifier = Modifier.fillMaxSize())
            }

            errorMessage != null -> {
                ErrorState(errorMessage = errorMessage)
            }

            routines.isEmpty() -> {
                EmptyRoutinesContent()
            }

            else -> {
                RoutinesListContent(
                    routines = routines,
                    hasActiveWorkout = hasActiveWorkout,
                    onRoutineClick = onRoutineClick,
                    onStartWorkout = onStartWorkout,
                    onResumeWorkout = onResumeWorkout,
                )
            }
        }
    }
}

@Composable
private fun EmptyRoutinesContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        EmptyRoutinesState()
    }
}

@Composable
private fun RoutinesListContent(
    routines: List<Routine>,
    hasActiveWorkout: Boolean,
    onRoutineClick: (Routine) -> Unit,
    onStartWorkout: (Routine) -> Unit,
    onResumeWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (hasActiveWorkout) {
            item(key = "resume-workout") {
                ResumeWorkoutButton(onClick = onResumeWorkout)
            }
        }

        items(
            items = routines,
            key = { it.id },
        ) { routine ->
            RoutineCard(
                routine = routine,
                onClick = { onRoutineClick(routine) },
                onStartWorkout = { onStartWorkout(routine) },
            )
        }
    }
}

@Composable
private fun ResumeWorkoutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(stringResource(StringRes.string.routine_resume_active_workout))
    }
}

@Composable
private fun EmptyRoutinesState() {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(StringRes.string.routine_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(StringRes.string.routine_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
