package com.driveplay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Composable
fun DrivePlayTheme(
    isAmoledMode: Boolean = false,
    customBackgroundColor: Color? = null,
    customAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val resolvedBackground = when {
        isAmoledMode -> TrueBlack
        customBackgroundColor != null -> customBackgroundColor
        else -> DefaultBackgroundDark
    }

    val resolvedAccent = customAccentColor ?: DefaultAccentPrimary

    val resolvedSurface = if (isAmoledMode) TrueBlack else lightenColor(resolvedBackground, 0.04f)
    val resolvedSurfaceVariant = if (isAmoledMode) lightenColor(resolvedBackground, 0.04f) else lightenColor(resolvedBackground, 0.08f)
    val resolvedAccentSecondary = lerp(resolvedAccent, Color.White, 0.3f)

    val colorScheme = darkColorScheme(
        primary = resolvedAccent,
        secondary = resolvedAccentSecondary,
        background = resolvedBackground,
        surface = resolvedSurface,
        surfaceVariant = resolvedSurfaceVariant,
        onPrimary = Color.White,
        onSecondary = Color.Black,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary,
        error = ErrorColor,
        outline = DividerColor
    )

    CompositionLocalProvider(
        LocalAccentPrimary provides resolvedAccent,
        LocalBackgroundDark provides resolvedBackground
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
