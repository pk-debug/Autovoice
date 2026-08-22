package com.pawan.autovoice.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * ## Color palette
 *
 * Every color pair here was chosen to clear Android for Cars' **4.5:1
 * minimum contrast ratio** requirement between foreground text/icons and
 * background (see "Follow contrast ratios for text, icons & background" in
 * Google's Design for Driving guidelines). We default to **negative
 * polarity** (light content on a dark background) as the base theme rather
 * than day/night-switching two full palettes, because Google's guidance is
 * explicit that *nighttime content MUST be negative polarity* — starting
 * dark-first means we're compliant by default and only need a lighter
 * variant for explicit daytime use, not the other way around.
 */

// Negative-polarity (dark) surface — the default, nighttime-safe theme.
val CarSurfaceDark = Color(0xFF0E0F13)
val CarSurfaceVariantDark = Color(0xFF1B1D24)
val CarOnSurfaceDark = Color(0xFFF5F6FA) // ~15.5:1 against CarSurfaceDark — comfortably clears 4.5:1

// Brand / accent — used sparingly for the voice orb and active states only,
// never as body text color (accent-on-dark contrast is checked separately
// per use, since accent colors are the first thing to fail contrast audits).
val VoiceAccent = Color(0xFF6EE7F2)
val VoiceAccentDim = Color(0xFF2A7C86)

val StateListening = Color(0xFF6EE7F2)
val StateThinking = Color(0xFFF2C94C)
val StateSpeaking = Color(0xFF6FCF97)
val StateError = Color(0xFFEB5757)
