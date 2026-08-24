package com.pawan.autovoice.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pawan.autovoice.domain.model.VoiceFailureReason
import com.pawan.autovoice.domain.model.VoiceOutcome
import com.pawan.autovoice.domain.usecase.ProcessVoiceCommandUseCase
import com.pawan.voicesdk.audio.VoiceAudioFocusManager
import com.pawan.voicesdk.model.MediaAction
import com.pawan.voicesdk.model.VoiceCommand
import com.pawan.voicesdk.stt.SpeechToTextEngine
import com.pawan.voicesdk.stt.SpeechToTextEvent
import com.pawan.voicesdk.tts.TextToSpeechEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ## DashboardViewModel
 *
 * Now owns the FULL voice pipeline: mic → STT → NLU (via the use case) →
 * TTS, with audio focus requested/released at each stage boundary. This is
 * the direct, working answer to interview doc Q6/Q7/Q9.
 *
 * ### The audio-focus lifecycle in this class
 * - [startListening] requests **ducking** focus for the STT phase — music
 *   gets quieter, not silenced, because we haven't committed to a response.
 * - The moment we have a final transcript, ducking focus is released
 *   (`focusJob.cancel()`), NLU processing runs with no focus held at all.
 * - [speakResult] requests **exclusive** focus only for the TTS phase, then
 *   releases it the instant speech finishes — both via structured
 *   concurrency (`awaitClose` inside [VoiceAudioFocusManager], triggered by
 *   cancelling the collecting job), never a manual abandon() call we could
 *   forget.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val processVoiceCommand: ProcessVoiceCommandUseCase,
    private val speechToTextEngine: SpeechToTextEngine,
    private val textToSpeechEngine: TextToSpeechEngine,
    private val audioFocusManager: VoiceAudioFocusManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    public val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    /** Tracks the current end-to-end voice interaction so a new tap cancels any in-flight one cleanly. */
    private var voiceInteractionJob: Job? = null

    public fun onIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.StartListening -> startListening()
            DashboardIntent.ToggleMediaPlayback -> togglePlayback()
            is DashboardIntent.MediaControlRequested -> applyMediaAction(intent.action)
            DashboardIntent.ErrorMessageShown -> _uiState.update { it.copy(errorMessage = null) }
            else -> {}
        }
    }

    private fun startListening() {
        voiceInteractionJob?.cancel()
        voiceInteractionJob = viewModelScope.launch {
            _uiState.update { it.copy(voiceSessionState = VoiceSessionState.Listening) }

            // Ducking focus lives ONLY for the duration of this child job —
            // cancelling it below releases focus deterministically via
            // VoiceAudioFocusManager's awaitClose, no manual bookkeeping.
            val duckingFocusJob = launch { audioFocusManager.requestDuckingFocus().collect { /* observe LOST if needed */ } }

            val transcript = speechToTextEngine.listen()
                .filterIsInstance<SpeechToTextEvent.FinalResult>()
                .map { it.text }
                .firstOrNull()

            duckingFocusJob.cancel()

            if (transcript.isNullOrBlank()) {
                _uiState.update { it.copy(voiceSessionState = VoiceSessionState.Idle, errorMessage = "Didn't catch that") }
                return@launch
            }

            processTranscript(transcript)
        }
    }

    private suspend fun processTranscript(utterance: String) {
        _uiState.update { it.copy(voiceSessionState = VoiceSessionState.Thinking(utterance)) }

        val spokenResponse = when (val outcome = processVoiceCommand(utterance = utterance)) {
            is VoiceOutcome.Understood -> applyCommand(outcome.command)
            is VoiceOutcome.NotUnderstood -> applyFailure(outcome.reason)
        }

        speakResult(spokenResponse)
    }

    /** Applies the command to UI state and returns the confirmation text to speak. */
    private fun applyCommand(command: VoiceCommand): String {
        var confirmation = ""
        _uiState.update { current ->
            when (command) {
                is VoiceCommand.ClimateControl -> {
                    confirmation = if (command.powerOn) {
                        "Climate set to ${command.temperatureCelsius ?: current.climateState.temperatureCelsius} degrees"
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
        return confirmation
    }

    private fun applyFailure(reason: VoiceFailureReason): String {
        val message = when (reason) {
            VoiceFailureReason.NO_CONNECTIVITY -> "No connection — try again once you're back online"
            VoiceFailureReason.BACKEND_UNAVAILABLE -> "Voice service is temporarily unavailable"
            VoiceFailureReason.COULD_NOT_UNDERSTAND -> "Sorry, I didn't catch that"
            VoiceFailureReason.UNKNOWN -> "Something went wrong — please try again"
        }
        _uiState.update { it.copy(voiceSessionState = VoiceSessionState.Speaking(message), errorMessage = message) }
        return message
    }

    private suspend fun speakResult(text: String) {
        // Exclusive focus held for exactly the lifetime of this suspend
        // call: launched as a child, cancelled in `finally` once
        // textToSpeechEngine.speak() returns (which itself suspends until
        // TTS playback truly finishes — see AndroidTextToSpeechEngine).
        val exclusiveFocusJob = viewModelScope.launch {
            audioFocusManager.requestExclusiveFocus().collect { /* observe LOST if needed */ }
        }
        try {
            textToSpeechEngine.speak(text)
        } finally {
            exclusiveFocusJob.cancel()
            _uiState.update { it.copy(voiceSessionState = VoiceSessionState.Idle) }
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

    override fun onCleared() {
        textToSpeechEngine.shutdown()
        super.onCleared()
    }
}