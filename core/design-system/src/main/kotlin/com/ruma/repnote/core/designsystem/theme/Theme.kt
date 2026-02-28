package com.ruma.repnote.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        // Primary colors - used for key components like buttons, app bars, FAB
        primary = BluePrimaryDark,
        onPrimary = Black, // Black text/icons on blue buttons
        primaryContainer = BluePrimaryDark, // FAB uses this - solid color
        onPrimaryContainer = Black, // Black icons on FAB
        // Secondary colors - used for less prominent components
        secondary = CyanSecondaryDark,
        onSecondary = Black,
        secondaryContainer = CyanSecondaryDark.copy(alpha = 0.3f),
        onSecondaryContainer = CyanSecondaryDark,
        // Tertiary colors - used for accents
        tertiary = GreenTertiaryDark,
        onTertiary = Black,
        tertiaryContainer = GreenTertiaryDark.copy(alpha = 0.3f),
        onTertiaryContainer = GreenTertiaryDark,
        // Error colors
        error = RedErrorDark,
        onError = White,
        errorContainer = RedErrorDark.copy(alpha = 0.3f),
        onErrorContainer = RedErrorDark,
        // Surface colors - for cards, sheets, dialogs
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceDark.copy(alpha = 0.8f),
        onSurfaceVariant = OnSurfaceVariantDark,
        // Background colors
        background = BackgroundDark,
        onBackground = OnSurfaceDark,
        // Outline colors
        outline = OnSurfaceVariantDark.copy(alpha = 0.5f),
    )

private val LightColorScheme =
    lightColorScheme(
        // Primary colors - used for key components like buttons, app bars, FAB
        primary = BluePrimary,
        onPrimary = White, // White text/icons on blue buttons
        primaryContainer = BluePrimary, // FAB uses this - solid color
        onPrimaryContainer = White, // White icons on FAB
        // Secondary colors - used for less prominent components
        secondary = CyanSecondary,
        onSecondary = White,
        secondaryContainer = CyanSecondary.copy(alpha = 0.15f),
        onSecondaryContainer = CyanSecondary,
        // Tertiary colors - used for accents
        tertiary = GreenTertiary,
        onTertiary = White,
        tertiaryContainer = GreenTertiary.copy(alpha = 0.15f),
        onTertiaryContainer = GreenTertiary,
        // Error colors
        error = RedError,
        onError = White,
        errorContainer = RedError.copy(alpha = 0.15f),
        onErrorContainer = RedError,
        // Surface colors - for cards, sheets, dialogs
        surface = BackgroundLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        // Background colors
        background = BackgroundLight,
        onBackground = OnSurfaceLight,
        // Outline colors
        outline = OnSurfaceVariantLight.copy(alpha = 0.5f),
    )

@Composable
fun RepnoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                DarkColorScheme
            }

            else -> {
                LightColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
