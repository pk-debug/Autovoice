package com.pawan.autovoice.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pawan.autovoice.presentation.dashboard.VoiceSessionState
import com.pawan.autovoice.presentation.theme.CarDimens
import com.pawan.autovoice.presentation.theme.StateError
import com.pawan.autovoice.presentation.theme.StateListening
import com.pawan.autovoice.presentation.theme.StateSpeaking
import com.pawan.autovoice.presentation.theme.StateThinking
import com.pawan.autovoice.presentation.theme.VoiceAccentDim

/**
 * ## VoiceOrb
 *
 * The primary voice-assistant affordance on the dashboard: a large circular
 * button that both **triggers** a voice interaction (tap target) and
 * **communicates state** (color + subtle pulse) at a glance.
 *
 * ### Driving-safety choices baked into this composable, on purpose
 * - **Size**: [CarDimens.MinTouchTarget] (76dp) — this is not a stylistic
 *   choice, it's the documented minimum for AAOS touch targets. Making the
 *   single most important control in the whole app any smaller would be a
 *   guideline violation on the app's most critical interaction.
 * - **Animation**: only a gentle scale pulse (0.94x–1.0x) while
 *   [VoiceSessionState.Listening] or `is Thinking` — not a spinner, not
 *   moving text, not color-cycling. "Avoid irrelevant movement" in the
 *   driving guidelines specifically calls out that animation must aid the
 *   driver's *situational understanding*; a pulse that means "I'm listening
 *   right now" clears that bar, an ornamental one wouldn't.
 * - **Color-coded state, not text-coded**: the orb's fill color alone
 *   communicates Idle/Listening/Thinking/Speaking/Error so a driver reads
 *   it in the sub-2-second glance budget the guidelines require, without
 *   needing to read a word.
 *
 * @param onTap invoked when the driver taps the orb to (in this portfolio
 *   build) simulate a voice command; in a full build this would instead
 *   start a `SpeechRecognizer` session.
 */
@Composable
public fun VoiceOrb(
    sessionState: VoiceSessionState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val orbColor = sessionState.toOrbColor()
    val isActive = sessionState is VoiceSessionState.Listening || sessionState is VoiceSessionState.Thinking

    val infiniteTransition = rememberInfiniteTransition(label = "voice_orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.94f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "voice_orb_pulse_scale",
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .size(CarDimens.MinTouchTarget)
                .scale(if (isActive) pulseScale else 1f)
                .background(orbColor, CircleShape)
                .clickable(onClick = onTap)
                .semantics { contentDescription = sessionState.toAccessibilityLabel() },
        )
        Text(
            text = sessionState.toLabel(),
            style = MaterialTheme.typography.labelLarge,
            color = orbColor,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

private fun VoiceSessionState.toOrbColor(): Color = when (this) {
    VoiceSessionState.Idle -> VoiceAccentDim
    VoiceSessionState.Listening -> StateListening
    is VoiceSessionState.Thinking -> StateThinking
    is VoiceSessionState.Speaking -> StateSpeaking
}

private fun VoiceSessionState.toLabel(): String = when (this) {
    VoiceSessionState.Idle -> "Tap to speak"
    VoiceSessionState.Listening -> "Listening…"
    is VoiceSessionState.Thinking -> "Thinking…"
    is VoiceSessionState.Speaking -> responseText
}

/** Separate, screen-reader-oriented copy — kept distinct from the visible label per Compose a11y best practice. */
private fun VoiceSessionState.toAccessibilityLabel(): String = when (this) {
    VoiceSessionState.Idle -> "Voice assistant, tap to speak"
    VoiceSessionState.Listening -> "Voice assistant is listening"
    is VoiceSessionState.Thinking -> "Voice assistant is thinking"
    is VoiceSessionState.Speaking -> "Voice assistant says: $responseText"
}

/** Referenced for the Error state color from callers that build their own error surface (e.g. a Snackbar). */
public val VoiceOrbErrorColor: Color = StateError
