package com.ruma.repnote.feature.routine.presentation.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.feature.routine.presentation.components.RoutineExerciseDetailCard
import com.ruma.repnote.feature.routine.presentation.detail.ExerciseDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ruma.repnote.core.stringresources.R as StringRes

@Composable
internal fun RoutineDetailContent(
    routine: Routine?,
    exercises: List<ExerciseDetail>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when {
            isLoading -> CircularProgressIndicator()
            errorMessage != null -> ErrorMessage(errorMessage)
            routine != null -> RoutineDetailList(routine, exercises)
        }
    }
}

@Composable
private fun ErrorMessage(errorMessage: String) {
    Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(16.dp),
    )
}

@Composable
private fun RoutineDetailList(
    routine: Routine,
    exercises: List<ExerciseDetail>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { RoutineHeader(routine) }
        item { ExercisesHeader(exercises.size) }
        items(exercises) { exercise ->
            RoutineExerciseDetailCard(exerciseDetail = exercise)
        }
    }
}

@Composable
private fun RoutineHeader(routine: Routine) {
    Column {
        Text(
            text = routine.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        routine.description?.let { desc ->
            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Created: ${formatDate(routine.createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ExercisesHeader(count: Int) {
    Text(
        text = "${stringResource(StringRes.string.routine_exercises)} ($count)",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
