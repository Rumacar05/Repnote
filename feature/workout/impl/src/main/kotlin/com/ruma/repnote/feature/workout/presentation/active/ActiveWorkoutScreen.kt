package com.ruma.repnote.feature.workout.presentation.active

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.core.designsystem.theme.Spacings
import com.ruma.repnote.feature.workout.presentation.active.components.AbandonWorkoutDialog
import com.ruma.repnote.feature.workout.presentation.active.components.CompleteWorkoutDialog
import com.ruma.repnote.feature.workout.presentation.active.components.ExerciseSetTracker
import com.ruma.repnote.feature.workout.presentation.active.components.RestTimerBottomSheet
import org.koin.androidx.compose.koinViewModel

/**
 * Screen for active workout session tracking.
 * Displays current exercise, set tracking, rest timer, and workout controls.
 */
class ActiveWorkoutScreen {
    @Composable
    fun Screen(
        onNavigateBack: () -> Unit,
        onNavigateToSessionSummary: (String) -> Unit,
    ) {
        ActiveWorkoutRoot(
            onNavigateBack = onNavigateBack,
            onNavigateToSessionSummary = onNavigateToSessionSummary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActiveWorkoutRoot(
    onNavigateBack: () -> Unit,
    onNavigateToSessionSummary: (String) -> Unit,
    viewModel: ActiveWorkoutViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val inputState by viewModel.setInputState.collectAsState()
    val restTimerSheetState = rememberModalBottomSheetState()

    NavigationHandler(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        onNavigateToSessionSummary = onNavigateToSessionSummary,
    )

    RepnoteScreen(
        topBar = {
            ActiveWorkoutTopBar(
                workoutName = uiState.session?.routineName ?: "Workout",
                elapsedTime = formatElapsedTime(uiState.totalElapsedSeconds),
                onAbandon = viewModel::onAbandonWorkout,
                onComplete = { viewModel.onCompleteWorkout() },
            )
        },
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            WorkoutSessionContent(
                uiState = uiState,
                inputState = inputState,
                viewModel = viewModel,
            )

            WorkoutDialogs(
                uiState = uiState,
                viewModel = viewModel,
                restTimerSheetState = restTimerSheetState,
            )
        }
    }
}

@Composable
private fun WorkoutSessionContent(
    uiState: ActiveWorkoutUiState,
    inputState: SetInputState,
    viewModel: ActiveWorkoutViewModel,
) {
    when {
        uiState.isLoading -> {
            LoadingState()
        }

        uiState.session == null -> {
            ErrorState(
                message = uiState.errorMessage ?: "No active workout session",
            )
        }

        else -> {
            val session = uiState.session
            val currentExercise = session.exercises.getOrNull(uiState.currentExerciseIndex)

            if (currentExercise != null) {
                ActiveWorkoutContent(
                    exercise = currentExercise,
                    currentSetNumber = uiState.currentSetNumber,
                    exerciseNumber = uiState.currentExerciseIndex + 1,
                    totalExercises = session.exercises.size,
                    inputState = inputState,
                    onRepsChange = viewModel::onRepsChange,
                    onWeightChange = viewModel::onWeightChange,
                    onNotesChange = viewModel::onNotesChange,
                    onCompleteSet = viewModel::onSetComplete,
                    onPreviousExercise = viewModel::onPreviousExercise,
                    onNextExercise = viewModel::onNextExercise,
                    canNavigatePrevious = uiState.currentExerciseIndex > 0,
                    canNavigateNext = uiState.currentExerciseIndex < session.exercises.size - 1,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDialogs(
    uiState: ActiveWorkoutUiState,
    viewModel: ActiveWorkoutViewModel,
    restTimerSheetState: androidx.compose.material3.SheetState,
) {
    if (uiState.isRestTimerActive && uiState.session != null) {
        val currentExercise = uiState.session.exercises.getOrNull(uiState.currentExerciseIndex)
        val totalRestSeconds = currentExercise?.targetRestSeconds ?: 0

        RestTimerBottomSheet(
            secondsRemaining = uiState.restTimerSecondsRemaining,
            totalSeconds = totalRestSeconds,
            onSkip = viewModel::onSkipRestTimer,
            onDismiss = viewModel::onSkipRestTimer,
            sheetState = restTimerSheetState,
        )
    }

    if (uiState.showCompleteDialog) {
        CompleteWorkoutDialog(
            onConfirm = viewModel::onCompleteWorkout,
            onDismiss = viewModel::onDismissCompleteDialog,
        )
    }

    if (uiState.showAbandonDialog) {
        AbandonWorkoutDialog(
            onConfirm = viewModel::onAbandonConfirm,
            onDismiss = viewModel::onDismissAbandonDialog,
        )
    }
}

@Composable
private fun NavigationHandler(
    viewModel: ActiveWorkoutViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSessionSummary: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                ActiveWorkoutNavigationEvent.NavigateBack -> {
                    onNavigateBack()
                }

                is ActiveWorkoutNavigationEvent.NavigateToSessionSummary -> {
                    onNavigateToSessionSummary(event.sessionId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveWorkoutTopBar(
    workoutName: String,
    elapsedTime: String,
    onAbandon: () -> Unit,
    onComplete: () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = workoutName,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = elapsedTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onAbandon) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Abandon workout",
                )
            }
        },
        actions = {
            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete workout",
                )
            }
        },
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun ActiveWorkoutContent(
    exercise: com.ruma.repnote.core.domain.model.WorkoutExercise,
    currentSetNumber: Int,
    exerciseNumber: Int,
    totalExercises: Int,
    inputState: SetInputState,
    onRepsChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCompleteSet: () -> Unit,
    onPreviousExercise: () -> Unit,
    onNextExercise: () -> Unit,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacings.spacing16),
    ) {
        ExerciseSetTracker(
            exercise = exercise,
            currentSetNumber = currentSetNumber,
            exerciseNumber = exerciseNumber,
            totalExercises = totalExercises,
            inputState = inputState,
            onRepsChange = onRepsChange,
            onWeightChange = onWeightChange,
            onNotesChange = onNotesChange,
            onCompleteSet = onCompleteSet,
        )

        Spacer(modifier = Modifier.height(Spacings.spacing24))

        ExerciseNavigation(
            onPrevious = onPreviousExercise,
            onNext = onNextExercise,
            canNavigatePrevious = canNavigatePrevious,
            canNavigateNext = canNavigateNext,
        )
    }
}

@Composable
private fun ExerciseNavigation(
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    canNavigatePrevious: Boolean,
    canNavigateNext: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacings.spacing8),
    ) {
        OutlinedButton(
            onClick = onPrevious,
            enabled = canNavigatePrevious,
            modifier = Modifier.weight(1f),
        ) {
            Text("Previous Exercise")
        }
        OutlinedButton(
            onClick = onNext,
            enabled = canNavigateNext,
            modifier = Modifier.weight(1f),
        ) {
            Text("Next Exercise")
        }
    }
}

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600

/**
 * Formats elapsed seconds into HH:MM:SS format.
 */
private fun formatElapsedTime(totalSeconds: Long): String {
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    return String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}
