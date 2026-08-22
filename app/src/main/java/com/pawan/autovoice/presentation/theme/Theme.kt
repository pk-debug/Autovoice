package com.pawan.autovoice.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * ## AutoVoiceTheme
 *
 * We only ship a `darkColorScheme` on purpose (`isSystemInDarkTheme()` is
 * read but currently always routed to the same dark palette) — see the
 * negative-polarity rationale in `Color.kt`. A real production app would
 * branch here on the car's actual day/night signal (from `CarUxRestrictions`
 * / the vehicle's own ambient light sensor via VHAL), not the phone's system
 * setting, which is exactly the kind of subtlety worth calling out in an
 * interview: **automotive day/night mode is a vehicle signal, not an OS
 * setting**.
 */
@Composable
public fun AutoVoiceTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = darkColorScheme(
        background = CarSurfaceDark,
        surface = CarSurfaceDark,
        surfaceVariant = CarSurfaceVariantDark,
        onBackground = CarOnSurfaceDark,
        onSurface = CarOnSurfaceDark,
        primary = VoiceAccent,
        onPrimary = CarSurfaceDark,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AutomotiveTypography,
        content = content,
    )
}

/**
 * ## CarDimens
 *
 * Hard numbers straight from Google's Design for Driving spec, defined once
 * so every interactive composable in this app references the SAME constant
 * instead of a magic number that could silently drift out of compliance:
 *
 * - [MinTouchTarget] — 76dp minimum touch target size for AAOS.
 * - [MinTouchTargetSpacing] — 23dp minimum spacing between adjacent targets
 *   to prevent mis-taps from road vibration.
 */
public object CarDimens {
    public val MinTouchTarget = 76.dp
    public val MinTouchTargetSpacing = 24.dp
}
