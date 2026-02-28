package com.ruma.repnote.feature.routine.presentation.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.feature.routine.presentation.detail.components.RoutineDetailContent
import org.koin.androidx.compose.koinViewModel
import com.ruma.repnote.core.stringresources.R as StringRes

class RoutineDetailScreen {
    @Composable
    fun Screen(
        routineId: String,
        onNavigateBack: () -> Unit,
        onNavigateToEdit: (String) -> Unit,
        onNavigateToActiveWorkout: () -> Unit,
    ) {
        RoutineDetailRoot(
            routineId = routineId,
            onNavigateBack = onNavigateBack,
            onNavigateToEdit = onNavigateToEdit,
            onNavigateToActiveWorkout = onNavigateToActiveWorkout,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineDetailRoot(
    routineId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit,
    viewModel: RoutineDetailViewModel = koinViewModel(key = routineId),
) {
    LaunchedEffect(routineId) {
        viewModel.initialize(routineId)
    }

    val uiState by viewModel.uiState.collectAsState()

    NavigationHandler(viewModel, onNavigateBack, onNavigateToEdit, onNavigateToActiveWorkout)

    RepnoteScreen(
        topBar = {
            RoutineDetailTopBar(
                title =
                    uiState.routine?.name
                        ?: stringResource(StringRes.string.routine_detail_title),
                onNavigateBack = onNavigateBack,
                onEditClick = viewModel::onEditClick,
                onDeleteClick = viewModel::onDeleteClick,
            )
        },
        floatingActionButton = {
            if (uiState.routine != null && !uiState.isLoading) {
                FloatingActionButton(
                    onClick = viewModel::onStartWorkoutClick,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Workout",
                    )
                }
            }
        },
    ) { modifier ->
        RoutineDetailContent(
            routine = uiState.routine,
            exercises = uiState.exercisesWithDetails,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            modifier = modifier,
        )

        if (uiState.showDeleteDialog) {
            DeleteConfirmationDialog(
                onConfirm = viewModel::onDeleteConfirm,
                onDismiss = viewModel::onDeleteCancel,
            )
        }
    }
}

@Composable
private fun NavigationHandler(
    viewModel: RoutineDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToActiveWorkout: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is RoutineDetailNavigationEvent.NavigateToEdit -> onNavigateToEdit(event.routineId)
                RoutineDetailNavigationEvent.NavigateBack -> onNavigateBack()
                RoutineDetailNavigationEvent.NavigateToActiveWorkout -> onNavigateToActiveWorkout()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoutineDetailTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(StringRes.string.routine_cancel),
                )
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(StringRes.string.routine_edit),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(StringRes.string.routine_delete),
                )
            }
        },
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(StringRes.string.routine_delete_confirm_title)) },
        text = { Text(stringResource(StringRes.string.routine_delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(StringRes.string.routine_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(StringRes.string.routine_cancel))
            }
        },
    )
}
