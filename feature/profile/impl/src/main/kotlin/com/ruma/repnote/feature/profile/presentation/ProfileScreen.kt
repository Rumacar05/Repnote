package com.ruma.repnote.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.analytics.compose.TrackScreenView
import com.ruma.repnote.core.analytics.domain.model.AnalyticsScreen
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import org.koin.androidx.compose.koinViewModel
import com.ruma.repnote.core.stringresources.R as StringRes

class ProfileScreen {
    @Composable
    fun Screen(
        bottomBar: @Composable () -> Unit = {},
        onSignOutClick: () -> Unit,
        onNavigateToRoutines: () -> Unit = {},
    ) {
        ProfileRoot(
            bottomBar = bottomBar,
            onNavigateToLogin = onSignOutClick,
            onNavigateToRoutines = onNavigateToRoutines,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProfileRoot(
    bottomBar: @Composable () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    TrackScreenView(AnalyticsScreen.PROFILE)

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                ProfileNavigationEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    RepnoteScreen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(StringRes.string.profile_title)) },
                actions = {
                    IconButton(onClick = viewModel::onSignOutClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(StringRes.string.profile_sign_out),
                        )
                    }
                },
            )
        },
        bottomBar = bottomBar,
    ) { modifier ->
        ProfileContent(
            displayName = uiState.displayName,
            userEmail = uiState.userEmail,
            onNavigateToRoutines = onNavigateToRoutines,
            modifier = modifier,
        )
    }
}

@Composable
private fun ProfileContent(
    displayName: String?,
    userEmail: String,
    onNavigateToRoutines: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(StringRes.string.profile_logged_in_as),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayName ?: userEmail,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(48.dp))

        ProfileMenuButton(
            icon = Icons.AutoMirrored.Filled.List,
            text = stringResource(StringRes.string.workout_history_title),
            onClick = onNavigateToRoutines,
        )
    }
}

@Composable
private fun ProfileMenuButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(fraction = 0.8f),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(text)
    }
}
