package com.driveplay.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DrivePlayColorScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceElevated,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    outline = DividerColor
)

private val AmoledColorScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    background = TrueBlack,
    surface = TrueBlack,
    surfaceVariant = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
    outline = DividerColor
)

@Composable
fun DrivePlayTheme(
    isAmoledMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isAmoledMode) AmoledColorScheme else DrivePlayColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
