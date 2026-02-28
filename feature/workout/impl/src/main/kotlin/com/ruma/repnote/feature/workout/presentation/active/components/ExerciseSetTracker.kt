package com.ruma.repnote.feature.workout.presentation.active.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.core.domain.model.CompletedSet
import com.ruma.repnote.core.domain.model.WorkoutExercise
import com.ruma.repnote.feature.workout.presentation.active.SetInputState

/**
 * Component for tracking exercise sets during an active workout.
 * Displays exercise info, input fields for set data, and completed sets.
 */
@Composable
internal fun ExerciseSetTracker(
    exercise: WorkoutExercise,
    currentSetNumber: Int,
    exerciseNumber: Int,
    totalExercises: Int,
    inputState: SetInputState,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCompleteSet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacings.spacing16),
        ) {
            ExerciseHeader(
                exerciseName = exercise.exerciseName,
                exerciseNumber = exerciseNumber,
                totalExercises = totalExercises,
            )

            Spacer(modifier = Modifier.height(Spacings.spacing16))

            SetProgress(
                currentSetNumber = currentSetNumber,
                targetSets = exercise.targetSets,
                targetReps = exercise.targetReps,
                targetRestSeconds = exercise.targetRestSeconds,
            )

            Spacer(modifier = Modifier.height(Spacings.spacing24))

            SetInputFields(
                reps = inputState.reps,
                weight = inputState.weight,
                notes = inputState.notes,
                onRepsChange = onRepsChange,
                onWeightChange = onWeightChange,
                onNotesChange = onNotesChange,
            )

            Spacer(modifier = Modifier.height(Spacings.spacing16))

            CompleteSetButton(
                onClick = onCompleteSet,
                enabled = inputState.reps.isNotBlank() && inputState.reps.toIntOrNull() != null,
            )

            if (exercise.completedSets.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacings.spacing24))
                CompletedSetsList(
                    completedSets = exercise.completedSets,
                )
            }
        }
    }
}

@Composable
private fun ExerciseHeader(
    exerciseName: String,
    exerciseNumber: Int,
    totalExercises: Int,
) {
    Column {
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing4))
        Text(
            text = "Exercise $exerciseNumber of $totalExercises",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SetProgress(
    currentSetNumber: Int,
    targetSets: Int,
    targetReps: Int?,
    targetRestSeconds: Int?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacings.spacing12),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = "Set $currentSetNumber of $targetSets",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                if (targetReps != null) {
                    Spacer(modifier = Modifier.height(Spacings.spacing4))
                    Text(
                        text = "Target: $targetReps reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            if (targetRestSeconds != null && targetRestSeconds > 0) {
                Column {
                    Text(
                        text = "Rest Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "${targetRestSeconds}s",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun SetInputFields(
    reps: String,
    weight: String,
    notes: String,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
        ) {
            OutlinedTextField(
                value = reps,
                onValueChange = onRepsChange,
                label = { Text("Reps *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = weight,
                onValueChange = onWeightChange,
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(Spacings.spacing8))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3,
        )
    }
}

@Composable
private fun CompleteSetButton(
    onClick: () -> Unit,
    enabled: Boolean,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Complete Set")
    }
}

@Composable
private fun CompletedSetsList(completedSets: List<CompletedSet>) {
    Column {
        Text(
            text = "Completed Sets",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(Spacings.spacing8))
        completedSets.forEach { set ->
            CompletedSetItem(set)
            Spacer(modifier = Modifier.height(Spacings.spacing8))
        }
    }
}

@Composable
private fun CompletedSetItem(set: CompletedSet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacings.spacing12),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Set ${set.setNumber}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row {
                Text(
                    text = "${set.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (set.weight != null) {
                    Spacer(modifier = Modifier.width(Spacings.spacing8))
                    Text(
                        text = "× ${set.weight} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        val notes = set.notes
        if (notes != null) {
            Text(
                text = notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier.padding(
                        start = Spacings.spacing12,
                        end = Spacings.spacing12,
                        bottom = Spacings.spacing12,
                    ),
            )
        }
    }
}
