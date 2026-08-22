package com.pawan.autovoice.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawan.autovoice.domain.model.VoiceFailureReason
import com.pawan.autovoice.domain.model.VoiceOutcome
import com.pawan.autovoice.domain.usecase.ProcessVoiceCommandUseCase
import com.pawan.voicesdk.model.MediaAction
import com.pawan.voicesdk.model.VoiceCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ## DashboardViewModel
 *
 * The presentation-layer brain of the head-unit dashboard. Follows strict
 * Unidirectional Data Flow: [uiState] is the ONLY thing the Compose screen
 * reads, [onIntent] is the ONLY thing it calls.
 *
 * ### `viewModelScope.launch` and exception isolation
 * [androidx.lifecycle.viewModelScope] is built on a [kotlinx.coroutines.SupervisorJob]
 * internally (this is exactly the answer to the interview doc's "Exception
 * Handling in Coroutines" question: *"how do you prevent the entire scope
 * from cancelling if one launch fails?"*). Because it's a SupervisorJob, an
 * unhandled exception in ONE `launch { }` block does not cancel sibling
 * coroutines in the same scope — but it's still good practice (and what we
 * do below) to catch expected failure paths explicitly via the
 * [VoiceOutcome.NotUnderstood] sealed branch rather than relying on
 * SupervisorJob as a safety net for logic we can already anticipate.
 *
 * ### Why StateFlow and not LiveData?
 * StateFlow is lifecycle-agnostic, testable with plain `runTest`/Turbine
 * (no `InstantTaskExecutorRule` needed), and composes naturally with other
 * Flow operators — the modern, Compose-first choice over LiveData.
 */
@HiltViewModel
public class DashboardViewModel @Inject constructor(
    private val processVoiceCommand: ProcessVoiceCommandUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    public val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /**
     * Single entry point for every user action from the UI. Kept as one
     * function with an exhaustive `when` (rather than five separate public
     * methods) so every new [DashboardIntent] the team adds is a compiler
     * error here until handled — the same "exhaustive sealed class" safety
     * net used throughout this project.
     */
    public fun onIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.SimulateVoiceCommand -> handleVoiceCommand(intent.utterance)
            DashboardIntent.ToggleMediaPlayback -> togglePlayback()
            is DashboardIntent.MediaControlRequested -> applyMediaAction(intent.action)
            DashboardIntent.ErrorMessageShown -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun handleVoiceCommand(utterance: String) {
        // launch (not async) — we don't need a return value here, just a
        // fire-and-forget coroutine tied to this ViewModel's lifecycle.
        viewModelScope.launch {
            _uiState.update { it.copy(voiceSessionState = VoiceSessionState.Thinking(utterance)) }

            when (val outcome = processVoiceCommand(utterance = utterance)) {
                is VoiceOutcome.Understood -> applyCommand(outcome.command)
                is VoiceOutcome.NotUnderstood -> applyFailure(outcome.reason)
            }
        }
    }

    /**
     * Maps an understood [VoiceCommand] onto both a spoken confirmation
     * (`Speaking` state, would drive TTS + `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE`
     * in a full build — see `VoiceAudioFocusManager`) and the concrete widget
     * state change the driver actually sees on screen.
     */
    private fun applyCommand(command: VoiceCommand) {
        val confirmation: String
        _uiState.update { current ->
            when (command) {
                is VoiceCommand.ClimateControl -> {
                    confirmation = if (command.powerOn) {
                        "Climate set to ${command.temperatureCelsius ?: current.climateState.temperatureCelsius}°"
                    } else {
                        "Turning off the AC"
                    }
                    current.copy(
                        climateState = current.climateState.copy(
                            isOn = command.powerOn,
                            temperatureCelsius = command.temperatureCelsius ?: current.climateState.temperatureCelsius,
                        ),
                    )
                }

                is VoiceCommand.Navigate -> {
                    confirmation = "Navigating to ${command.destination}"
                    current
                }

                is VoiceCommand.MediaControl -> {
                    confirmation = "Okay"
                    current.copy(nowPlaying = current.nowPlaying.applyMediaAction(command.action))
                }

                is VoiceCommand.LowConfidence -> {
                    confirmation = "Sorry, could you say that again?"
                    current
                }

                is VoiceCommand.Unrecognized -> {
                    confirmation = "I can't help with that yet"
                    current
                }
            }.copy(voiceSessionState = VoiceSessionState.Speaking(confirmation))
        }
    }

    private fun applyFailure(reason: VoiceFailureReason) {
        val message = when (reason) {
            VoiceFailureReason.NO_CONNECTIVITY -> "No connection — try again once you're back online"
            VoiceFailureReason.BACKEND_UNAVAILABLE -> "Voice service is temporarily unavailable"
            VoiceFailureReason.COULD_NOT_UNDERSTAND -> "Sorry, I didn't catch that"
            VoiceFailureReason.UNKNOWN -> "Something went wrong — please try again"
        }
        _uiState.update {
            it.copy(voiceSessionState = VoiceSessionState.Idle, errorMessage = message)
        }
    }

    private fun togglePlayback() {
        _uiState.update { it.copy(nowPlaying = it.nowPlaying.copy(isPlaying = !it.nowPlaying.isPlaying)) }
    }

    private fun applyMediaAction(action: MediaAction) {
        _uiState.update { it.copy(nowPlaying = it.nowPlaying.applyMediaAction(action)) }
    }

    private fun MediaUiState.applyMediaAction(action: MediaAction): MediaUiState = when (action) {
        MediaAction.PLAY -> copy(isPlaying = true)
        MediaAction.PAUSE -> copy(isPlaying = false)
        MediaAction.NEXT_TRACK, MediaAction.PREVIOUS_TRACK -> copy(isPlaying = true)
    }
}
