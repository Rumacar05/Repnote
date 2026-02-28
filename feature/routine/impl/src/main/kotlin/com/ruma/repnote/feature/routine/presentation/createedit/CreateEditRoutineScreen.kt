package com.ruma.repnote.feature.routine.presentation.createedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.feature.routine.presentation.components.ExerciseConfigCard
import com.ruma.repnote.feature.routine.presentation.components.ExerciseSelectorDialog
import org.koin.androidx.compose.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.ruma.repnote.core.stringresources.R as StringRes

class CreateEditRoutineScreen {
    @Composable
    fun Screen(
        onNavigateBack: () -> Unit,
        onRoutineCreated: (String) -> Unit,
        routineId: String? = null,
    ) {
        CreateEditRoutineRoot(
            onNavigateBack = onNavigateBack,
            onRoutineCreated = onRoutineCreated,
            routineId = routineId,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateEditRoutineRoot(
    onNavigateBack: () -> Unit,
    onRoutineCreated: (String) -> Unit,
    routineId: String? = null,
    viewModel: CreateEditRoutineViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(routineId) {
        routineId?.let { viewModel.loadRoutineForEdit(it) }
    }

    HandleNavigationEvents(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onRoutineCreated = onRoutineCreated,
    )

    RepnoteScreen(
        topBar = {
            CreateEditRoutineTopBar(
                isEditMode = uiState.isEditMode,
                isSaving = uiState.isSaving,
                onCancelClick = viewModel::onCancelClick,
                onSaveClick = viewModel::onSaveClick,
            )
        },
        floatingActionButton = {
            AddExerciseFab(onClick = viewModel::onAddExerciseClick)
        },
    ) { modifier ->
        CreateEditRoutineContent(
            uiState = uiState,
            onNameChange = viewModel::onNameChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onReorderExercises = viewModel::onReorderExercises,
            onExerciseConfigChange = viewModel::onExerciseConfigChange,
            onRemoveExercise = viewModel::onRemoveExercise,
            modifier = modifier.fillMaxSize(),
        )

        if (uiState.showExerciseSelector) {
            ExerciseSelectorDialog(
                exercises = uiState.availableExercises,
                searchQuery = uiState.exerciseSearchQuery,
                onSearchQueryChange = viewModel::onExerciseSearchQueryChange,
                onExerciseSelected = viewModel::onExerciseSelected,
                onDismiss = viewModel::onDismissExerciseSelector,
                isLoading = uiState.availableExerciseLoading,
                errorMessage = uiState.errorMessage,
            )
        }
    }
}

@Composable
private fun HandleNavigationEvents(
    viewModel: CreateEditRoutineViewModel,
    onNavigateBack: () -> Unit,
    onRoutineCreated: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is CreateEditRoutineNavigationEvent.NavigateToDetail -> {
                    onRoutineCreated(event.routineId)
                }

                CreateEditRoutineNavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEditRoutineTopBar(
    isEditMode: Boolean,
    isSaving: Boolean,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                if (isEditMode) {
                    stringResource(StringRes.string.routine_edit_title)
                } else {
                    stringResource(StringRes.string.routine_create_title)
                },
            )
        },
        navigationIcon = {
            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(StringRes.string.routine_cancel),
                )
            }
        },
        actions = {
            TextButton(
                onClick = onSaveClick,
                enabled = !isSaving,
            ) {
                Text(stringResource(StringRes.string.routine_save))
            }
        },
    )
}

@Composable
private fun AddExerciseFab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(StringRes.string.routine_add_exercise),
        )
    }
}

private const val FIXED_ITEMS_COUNT = 6

@Composable
private fun CreateEditRoutineContent(
    uiState: CreateEditRoutineUiState,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReorderExercises: (Int, Int) -> Unit,
    onExerciseConfigChange: (Int, Int, Int?, Int?, String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState =
        rememberReorderableLazyListState(
            lazyListState = lazyListState,
            onMove = { from, to ->
                onReorderExercises(from.index - FIXED_ITEMS_COUNT, to.index - FIXED_ITEMS_COUNT)
            },
        )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        routineNameField(uiState.name, uiState.isNameValid, onNameChange)
        item { Spacer(modifier = Modifier.height(16.dp)) }
        routineDescriptionField(uiState.description, onDescriptionChange)
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item {
            Text(
                text = stringResource(StringRes.string.routine_exercises),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
        errorMessageItem(uiState.errorMessage)
        exercisesList(
            uiState.exercisesWithConfig,
            reorderableLazyListState,
            onExerciseConfigChange,
            onRemoveExercise,
        )
    }
}

private fun LazyListScope.routineNameField(
    name: String,
    isNameValid: Boolean,
    onNameChange: (String) -> Unit,
) {
    item {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(StringRes.string.routine_name)) },
            isError = !isNameValid,
            supportingText =
                if (!isNameValid) {
                    { Text(stringResource(StringRes.string.error_routine_name_empty)) }
                } else {
                    null
                },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

private fun LazyListScope.routineDescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
) {
    item {
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(StringRes.string.routine_description_optional)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4,
        )
    }
}

private fun LazyListScope.errorMessageItem(errorMessage: String?) {
    errorMessage?.let { error ->
        item {
            Column {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun LazyListScope.exercisesList(
    exercises: List<ExerciseWithConfig>,
    reorderableLazyListState: ReorderableLazyListState,
    onExerciseConfigChange: (Int, Int, Int?, Int?, String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
) {
    if (exercises.isEmpty()) {
        item {
            Text(
                text = "No exercises added yet. Tap the + button to add exercises.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    } else {
        itemsIndexed(
            exercises,
            key = { _, exercise -> exercise.exerciseId },
        ) { index, exercise ->
            ReorderableItem(reorderableLazyListState, key = exercise.exerciseId) { isDragging ->
                ExerciseItemContent(
                    exercise = exercise,
                    index = index,
                    isDragging = isDragging,
                    onExerciseConfigChange = onExerciseConfigChange,
                    onRemoveExercise = onRemoveExercise,
                )
            }
        }
    }
}

@Composable
private fun ExerciseItemContent(
    exercise: ExerciseWithConfig,
    index: Int,
    isDragging: Boolean,
    onExerciseConfigChange: (Int, Int, Int?, Int?, String) -> Unit,
    onRemoveExercise: (Int) -> Unit,
) {
    Column {
        if (index > 0) {
            Spacer(modifier = Modifier.height(12.dp))
        }
        ExerciseConfigCard(
            exerciseName = exercise.exerciseName,
            sets = exercise.sets,
            reps = exercise.reps,
            restSeconds = exercise.restSeconds,
            notes = exercise.notes ?: "",
            onSetsChange = { newSets ->
                onExerciseConfigChange(
                    index,
                    newSets,
                    exercise.reps,
                    exercise.restSeconds,
                    exercise.notes ?: "",
                )
            },
            onRepsChange = { newReps ->
                onExerciseConfigChange(
                    index,
                    exercise.sets,
                    newReps,
                    exercise.restSeconds,
                    exercise.notes ?: "",
                )
            },
            onRestSecondsChange = { newRest ->
                onExerciseConfigChange(
                    index,
                    exercise.sets,
                    exercise.reps,
                    newRest,
                    exercise.notes ?: "",
                )
            },
            onNotesChange = { newNotes ->
                onExerciseConfigChange(
                    index,
                    exercise.sets,
                    exercise.reps,
                    exercise.restSeconds,
                    newNotes,
                )
            },
            onRemove = { onRemoveExercise(index) },
            isDragging = isDragging,
            dragHandleModifier = Modifier,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
