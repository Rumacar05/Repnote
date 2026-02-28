package com.ruma.repnote.feature.routine.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.ruma.repnote.core.analytics.compose.TrackScreenView
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import com.ruma.repnote.core.domain.model.Routine
import com.ruma.repnote.feature.routine.presentation.components.RoutinesContent
import org.koin.androidx.compose.koinViewModel
import com.ruma.repnote.core.stringresources.R as StringRes

class RoutinesScreen {
    @Composable
    fun Screen(
        bottomBar: @Composable () -> Unit = {},
        onRoutineClick: (Routine) -> Unit,
        onCreateRoutineClick: () -> Unit,
        onStartWorkout: () -> Unit,
        onResumeWorkout: (String) -> Unit = {},
    ) {
        RoutinesRoot(
            bottomBar = bottomBar,
            onRoutineClick = onRoutineClick,
            onCreateRoutineClick = onCreateRoutineClick,
            onStartWorkout = onStartWorkout,
            onResumeWorkout = onResumeWorkout,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutinesRoot(
    bottomBar: @Composable () -> Unit = {},
    onRoutineClick: (Routine) -> Unit,
    onCreateRoutineClick: () -> Unit,
    onStartWorkout: () -> Unit,
    onResumeWorkout: (String) -> Unit,
    viewModel: RoutinesViewModel = koinViewModel(),
) {
    TrackScreenView(AnalyticsScreen.ROUTINES)

    val uiState by viewModel.uiState.collectAsState()

    NavigationHandler(
        viewModel = viewModel,
        onStartWorkout = onStartWorkout,
        onResumeWorkout = onResumeWorkout,
    )

    RepnoteScreen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(StringRes.string.routine_my_routines)) },
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoutineClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(StringRes.string.routine_create),
                )
            }
        },
    ) { modifier ->
        RoutinesContent(
            routines = uiState.routines,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            hasActiveWorkout = uiState.hasActiveWorkout,
            onRoutineClick = onRoutineClick,
            onStartWorkout = viewModel::onStartWorkoutClick,
            onResumeWorkout = viewModel::onResumeWorkoutClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun NavigationHandler(
    viewModel: RoutinesViewModel,
    onStartWorkout: () -> Unit,
    onResumeWorkout: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                RoutinesNavigationEvent.NavigateToActiveWorkout -> {
                    onStartWorkout()
                }

                is RoutinesNavigationEvent.NavigateToResumeWorkout -> {
                    onResumeWorkout(
                        event.sessionId,
                    )
                }
            }
        }
    }
}
