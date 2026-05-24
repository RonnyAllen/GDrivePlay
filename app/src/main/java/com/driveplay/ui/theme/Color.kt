package com.driveplay.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ── Default palette (fallback values) ──────────────────────────────────────
val DefaultBackgroundDark = Color(0xFF0A0A0A)      // #0A0A0A (near-black)
val DefaultAccentPrimary = Color(0xFFFF8C00)        // #FF8C00 (amber-orange)

// ── Static colors (never change) ───────────────────────────────────────────
val TextPrimary = Color(0xFFFFFFFF)           // #FFFFFF
val TextSecondary = Color(0xFFB3B3B3)         // #B3B3B3
val TextMuted = Color(0xFF666666)             // #666666
val DividerColor = Color(0xFF2A2A2A)          // #2A2A2A
val ErrorColor = Color(0xFFCF6679)            // #CF6679
val TrueBlack = Color(0xFF000000)             // #000000 (AMOLED)

// ── CompositionLocals for user-customizable colors ─────────────────────────
val LocalAccentPrimary = compositionLocalOf { DefaultAccentPrimary }
val LocalBackgroundDark = compositionLocalOf { DefaultBackgroundDark }

// ── Dynamic accessors (use these everywhere instead of raw constants) ──────
// These are NOT @Composable properties — they delegate to compositionLocalOf
// which makes them readable in @Composable scope automatically.
val AccentPrimary: Color
    @Composable get() = LocalAccentPrimary.current

val AccentSecondary: Color
    @Composable get() = lerp(LocalAccentPrimary.current, Color.White, 0.3f)

val BackgroundDark: Color
    @Composable get() = LocalBackgroundDark.current

val SurfaceDark: Color
    @Composable get() = lightenColor(LocalBackgroundDark.current, 0.04f)

val SurfaceElevated: Color
    @Composable get() = lightenColor(LocalBackgroundDark.current, 0.08f)

val SurfaceSecondary: Color
    @Composable get() = lightenColor(LocalBackgroundDark.current, 0.11f)

val BufferedColor: Color
    @Composable get() = LocalAccentPrimary.current.copy(alpha = 0.4f)

// ── Helpers ────────────────────────────────────────────────────────────────
/**
 * Lightens a dark color by blending toward white by the given [fraction].
 * If the color is already fairly light, blends toward black instead to
 * keep surface contrast meaningful.
 */
fun lightenColor(base: Color, fraction: Float): Color {
    // Manual luminance: 0.2126*R + 0.7152*G + 0.0722*B
    val lum = 0.2126f * base.red + 0.7152f * base.green + 0.0722f * base.blue
    return if (lum < 0.5f) {
        lerp(base, Color.White, fraction)
    } else {
        lerp(base, Color.Black, fraction)
    }
}
