package com.ruma.repnote.presentation.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ruma.repnote.core.designsystem.components.RepnoteScreen
import org.koin.androidx.compose.koinViewModel
import com.ruma.repnote.core.stringresources.R as StringRes

private const val ANIMATION_DURATION = 1500

class SplashScreen {
    @Composable
    fun Screen(
        onNavigateToLogin: () -> Unit,
        onNavigateToHome: () -> Unit,
    ) {
        SplashRoot(
            onNavigateToLogin = onNavigateToLogin,
            onNavigateToHome = onNavigateToHome,
        )
    }
}

@Composable
internal fun SplashRoot(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    // Animated scale for logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(ANIMATION_DURATION, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "scale",
    )

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            SplashNavigationEvent.NavigateToHome -> {
                onNavigateToHome()
            }

            SplashNavigationEvent.NavigateToLogin -> {
                onNavigateToLogin()
            }

            null -> {} // Still loading
        }
    }

    RepnoteScreen(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) { modifier ->
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(StringRes.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale).size(200.dp),
            )
        }
    }
}
