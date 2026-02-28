@file:Suppress("MagicNumber")

package com.ruma.repnote.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Common Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// Light Theme Colors
val BluePrimary = Color(0xFF1E88E5) // Vibrant blue - trust, focus and motivation
val CyanSecondary = Color(0xFF00ACC1) // Cyan - energy and progress
val GreenTertiary = Color(0xFF4CAF50) // Success green - completion and achievement
val RedError = Color(0xFFD32F2F) // Clear error state

val SurfaceLight = Color(0xFFFAFAFA) // Very light gray for surfaces
val BackgroundLight = White // Pure white background
val OnSurfaceLight = Color(0xFF1C1C1E) // Almost black text
val OnSurfaceVariantLight = Color(0xFF666666) // Gray for secondary text

// Dark Theme Colors
val BluePrimaryDark = Color(0xFF42A5F5) // Brighter blue for dark backgrounds
val CyanSecondaryDark = Color(0xFF26C6DA) // Lighter cyan for visibility
val GreenTertiaryDark = Color(0xFF66BB6A) // Lighter green for dark mode
val RedErrorDark = Color(0xFFEF5350) // Softer red for dark backgrounds

val SurfaceDark = Color(0xFF1E1E1E) // Dark gray, not pure black
val BackgroundDark = Color(0xFF121212) // Material dark background
val OnSurfaceDark = Color(0xFFF5F5F5) // Almost white text for maximum contrast
val OnSurfaceVariantDark = Color(0xFFCCCCCC) // Light gray for secondary text
