package com.ruma.repnote.feature.routine.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.stringresources.R as StringRes

@Composable
internal fun ExerciseConfigCard(
    exerciseName: String,
    sets: Int,
    reps: Int?,
    restSeconds: Int?,
    notes: String,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestSecondsChange: (Int?) -> Unit,
    onNotesChange: (String) -> Unit,
    onRemove: () -> Unit,
    isDragging: Boolean,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 2.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DragHandleIcon(dragHandleModifier)
            Spacer(modifier = Modifier.width(8.dp))
            ExerciseConfigContent(
                exerciseName = exerciseName,
                sets = sets,
                reps = reps,
                restSeconds = restSeconds,
                notes = notes,
                onSetsChange = onSetsChange,
                onRepsChange = onRepsChange,
                onRestSecondsChange = onRestSecondsChange,
                onNotesChange = onNotesChange,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            RemoveButton(onRemove)
        }
    }
}

@Composable
private fun DragHandleIcon(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.Menu,
        contentDescription = "Drag handle",
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun ExerciseConfigContent(
    exerciseName: String,
    sets: Int,
    reps: Int?,
    restSeconds: Int?,
    notes: String,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestSecondsChange: (Int?) -> Unit,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        ExerciseConfigFields(
            sets = sets,
            reps = reps,
            restSeconds = restSeconds,
            notes = notes,
            onSetsChange = onSetsChange,
            onRepsChange = onRepsChange,
            onRestSecondsChange = onRestSecondsChange,
            onNotesChange = onNotesChange,
        )
    }
}

@Composable
private fun ExerciseConfigFields(
    sets: Int,
    reps: Int?,
    restSeconds: Int?,
    notes: String,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int?) -> Unit,
    onRestSecondsChange: (Int?) -> Unit,
    onNotesChange: (String) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        NumberInputField(
            label = stringResource(StringRes.string.exercise_sets),
            value = sets,
            onValueChange = { it?.let(onSetsChange) },
            minValue = 1,
            nullable = false,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        NumberInputField(
            label = stringResource(StringRes.string.exercise_reps),
            value = reps,
            onValueChange = onRepsChange,
            minValue = 1,
            nullable = true,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    NumberInputField(
        label = stringResource(StringRes.string.exercise_rest),
        value = restSeconds,
        onValueChange = onRestSecondsChange,
        minValue = 0,
        nullable = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text(stringResource(StringRes.string.exercise_notes)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 3,
    )
}

@Composable
private fun RemoveButton(onRemove: () -> Unit) {
    IconButton(onClick = onRemove) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(StringRes.string.exercise_remove),
            tint = MaterialTheme.colorScheme.error,
        )
    }
}
