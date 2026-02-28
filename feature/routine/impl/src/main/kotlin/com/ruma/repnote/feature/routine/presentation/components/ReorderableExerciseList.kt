package com.ruma.repnote.feature.routine.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ruma.repnote.feature.routine.presentation.createedit.ExerciseWithConfig
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun ReorderableExerciseList(
    exercises: List<ExerciseWithConfig>,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onExerciseConfigChange: (
        index: Int,
        sets: Int,
        reps: Int?,
        restSeconds: Int?,
        notes: String,
    ) -> Unit,
    onRemoveExercise: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState =
        androidx.compose.foundation.lazy
            .rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(
            lazyListState = lazyListState,
            onMove = { from, to -> onReorder(from.index, to.index) },
        )

    LazyColumn(
        modifier = modifier,
        state = lazyListState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(exercises, key = { _, exercise -> exercise.exerciseId }) { index, exercise ->
            ReorderableItem(reorderableLazyListState, key = exercise.exerciseId) { isDragging ->
                ConfigurableExerciseCard(
                    exercise = exercise,
                    index = index,
                    isDragging = isDragging,
                    onExerciseConfigChange = onExerciseConfigChange,
                    onRemoveExercise = onRemoveExercise,
                    modifier = Modifier.draggableHandle(onDragStarted = {}, onDragStopped = {}),
                )
            }
        }
    }
}

@Composable
private fun ConfigurableExerciseCard(
    exercise: ExerciseWithConfig,
    index: Int,
    isDragging: Boolean,
    onExerciseConfigChange: (Int, Int, Int?, Int?, String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    modifier: Modifier,
) {
    val notes = exercise.notes ?: ""
    ExerciseConfigCard(
        exerciseName = exercise.exerciseName,
        sets = exercise.sets,
        reps = exercise.reps,
        restSeconds = exercise.restSeconds,
        notes = notes,
        onSetsChange = {
            onExerciseConfigChange(index, it, exercise.reps, exercise.restSeconds, notes)
        },
        onRepsChange = {
            onExerciseConfigChange(index, exercise.sets, it, exercise.restSeconds, notes)
        },
        onRestSecondsChange = {
            onExerciseConfigChange(index, exercise.sets, exercise.reps, it, notes)
        },
        onNotesChange = {
            onExerciseConfigChange(index, exercise.sets, exercise.reps, exercise.restSeconds, it)
        },
        onRemove = { onRemoveExercise(index) },
        isDragging = isDragging,
        dragHandleModifier = modifier,
        modifier = Modifier.fillMaxWidth(),
    )
}
