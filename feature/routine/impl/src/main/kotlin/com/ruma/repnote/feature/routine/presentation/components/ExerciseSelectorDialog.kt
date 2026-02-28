package com.ruma.repnote.feature.routine.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ruma.repnote.core.designsystem.components.LoadingState
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.core.domain.model.Exercise
import com.ruma.repnote.core.stringresources.R as StringRes

@Composable
internal fun ExerciseSelectorDialog(
    exercises: List<Exercise>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onExerciseSelected: (String, String) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null,
    modifier: Modifier = Modifier,
) {
    val filteredExercises = filterExercises(exercises, searchQuery)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(StringRes.string.exercise_select))
        },
        text = {
            ExerciseSelectorDialogContent(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                filteredExercises = filteredExercises,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onExerciseSelected = onExerciseSelected,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(StringRes.string.routine_cancel))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun ExerciseSelectorDialogContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filteredExercises: List<Exercise>,
    isLoading: Boolean,
    errorMessage: String?,
    onExerciseSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(stringResource(StringRes.string.exercise_search)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing16))

        ExerciseListContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            filteredExercises = filteredExercises,
            onExerciseSelected = onExerciseSelected,
        )
    }
}

@Composable
private fun ExerciseListContent(
    isLoading: Boolean,
    errorMessage: String?,
    filteredExercises: List<Exercise>,
    onExerciseSelected: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> {
            LoadingState(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            )
        }

        errorMessage != null -> {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        filteredExercises.isEmpty() -> {
            Text(
                text = "No exercises found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.padding(16.dp),
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredExercises) { exercise ->
                    ExerciseListItem(
                        exercise = exercise,
                        onClick = {
                            onExerciseSelected(exercise.id, exercise.name)
                        },
                    )
                }
            }
        }
    }
}

private fun filterExercises(
    exercises: List<Exercise>,
    query: String,
): List<Exercise> =
    if (query.isBlank()) {
        exercises
    } else {
        exercises.filter { it.name.contains(query, ignoreCase = true) }
    }

@Composable
private fun ExerciseListItem(
    exercise: Exercise,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Exercise image thumbnail
        exercise.imageUrl?.let { imageUrl ->
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = exercise.name,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(4.dp)),
            )

            Spacer(modifier = Modifier.width(Spacings.spacing12))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = exercise.primaryMuscleGroup.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
