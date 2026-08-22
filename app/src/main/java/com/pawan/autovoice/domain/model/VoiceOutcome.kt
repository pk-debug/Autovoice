package com.pawan.autovoice.domain.model

import com.pawan.voicesdk.model.VoiceCommand

/**
 * ## VoiceOutcome
 *
 * Domain-layer result of interpreting a spoken utterance.
 *
 * ### Why not just re-use `VoiceEngineResult` from `:voice-sdk` everywhere?
 * This is the direct, concrete answer to the interview doc's Clean
 * Architecture question: *"Should the Domain layer have Android SDK
 * dependencies?"* — the answer is no, and more specifically here: **the
 * domain layer should not even depend on the exact failure taxonomy of a
 * specific data source**, because tomorrow the data layer might add a
 * second [com.pawan.autovoice.domain.repository.VoiceRepository]
 * implementation backed by an on-device model instead of `:voice-sdk`'s
 * cloud engine, with a completely different set of failure modes (e.g. "model
 * not downloaded yet" instead of "HTTP 503"). If every ViewModel `when`-
 * matched on `VoiceEngineResult.Failure.ServerError(httpCode)`, that
 * swap would ripple into the presentation layer. Instead:
 *
 *   `:voice-sdk` (data source) -> [VoiceFailureReason] (domain contract) -> ViewModel
 *
 * [com.pawan.autovoice.data.repository.VoiceRepositoryImpl] is the ONLY
 * class in the whole app allowed to import `com.pawan.voicesdk.engine.*`.
 * That import boundary is the actual, enforceable definition of "Clean
 * Architecture" in this project — not just a folder name.
 */
public sealed interface VoiceOutcome {
    public data class Understood(val command: VoiceCommand) : VoiceOutcome
    public data class NotUnderstood(val reason: VoiceFailureReason) : VoiceOutcome
}

/** Domain-level, UI-copy-friendly failure taxonomy — no HTTP codes, no exception types. */
public enum class VoiceFailureReason {
    NO_CONNECTIVITY,
    BACKEND_UNAVAILABLE,
    COULD_NOT_UNDERSTAND,
    UNKNOWN,
}
