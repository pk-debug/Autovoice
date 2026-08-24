package com.pawan.autovoice.presentation.dashboard

import com.pawan.voicesdk.model.MediaAction

/**
 * ## DashboardUiState
 *
 * The **single, immutable snapshot** of everything the Compose UI needs to
 * render the head-unit dashboard at any given moment. This is the
 * "Unidirectional Data Flow" (UDF) answer from the interview doc: the
 * ViewModel exposes exactly one `StateFlow<DashboardUiState>`, the UI is a
 * pure function of that one value, and the UI never mutates state directly
 * — it only sends [DashboardIntent]s back up.
 *
 *   User action -> DashboardIntent -> ViewModel -> new DashboardUiState -> Compose recomposes
 *
 * @property voiceSessionState where the voice pipeline currently is: Idle,
 *   Listening, Thinking (waiting on the cloud call), or Speaking. Directly
 *   drives [com.pawan.autovoice.presentation.components.VoiceOrb]'s
 *   animation.
 * @property climateState current on-screen climate-control widget values —
 *   updated only when a [com.pawan.voicesdk.model.VoiceCommand.ClimateControl]
 *   comes back from the SDK, or the driver taps the widget directly.
 * @property nowPlaying current media-playback display state.
 * @property errorMessage a one-shot, human-readable error string to surface
 *   (e.g. in a Snackbar). Modeled as nullable state rather than a
 *   [kotlinx.coroutines.flow.SharedFlow] event for simplicity in this
 *   screen; the ViewModel clears it via [DashboardIntent.ErrorMessageShown]
 *   once the UI has displayed it, which is the standard "consume-once"
 *   pattern for state-based (rather than event-based) one-off signals.
 */
public data class DashboardUiState(
    val voiceSessionState: VoiceSessionState = VoiceSessionState.Idle,
    val climateState: ClimateUiState = ClimateUiState(),
    val nowPlaying: MediaUiState = MediaUiState(),
    val errorMessage: String? = null,
)

/**
 * Every stage of a voice interaction the driver can visually distinguish.
 * A `sealed interface` here (rather than a plain enum) because [Thinking]
 * and [Speaking] both carry payload data the UI needs — an enum can't do
 * that without an accompanying "current text" field bolted on separately.
 */
public sealed interface VoiceSessionState {
    public data object Idle : VoiceSessionState
    public data object Listening : VoiceSessionState
    public data class Thinking(val utterance: String) : VoiceSessionState
    public data class Speaking(val responseText: String) : VoiceSessionState
}

public data class ClimateUiState(
    val isOn: Boolean = false,
    val temperatureCelsius: Int = 21,
)

public data class MediaUiState(
    val isPlaying: Boolean = false,
    val trackTitle: String = "Nothing playing",
)

/**
 * Every user-initiated action the Dashboard screen can produce. Routing all
 * UI events through one sealed hierarchy (rather than exposing five
 * separate public functions on the ViewModel) keeps the ViewModel's public
 * API to exactly two members — `uiState` and `onIntent(DashboardIntent)` —
 * which makes it trivial to log/replay every user action for debugging or
 * analytics, another nod to the JD's analytics requirement.
 */
public sealed interface DashboardIntent {
    /** Driver tapped the voice orb / pressed the steering-wheel voice button. */
    public data class SimulateVoiceCommand(val utterance: String) : DashboardIntent
    /** Driver taps the orb to START a real listening session (replaces SimulateVoiceCommand). */
    public data object StartListening : DashboardIntent
    public data object ToggleMediaPlayback : DashboardIntent
    public data class MediaControlRequested(val action: MediaAction) : DashboardIntent
    public data object ErrorMessageShown : DashboardIntent
}
