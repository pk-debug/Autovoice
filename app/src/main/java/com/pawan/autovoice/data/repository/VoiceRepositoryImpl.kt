package com.pawan.autovoice.data.repository

import com.pawan.autovoice.domain.model.VoiceFailureReason
import com.pawan.autovoice.domain.model.VoiceOutcome
import com.pawan.autovoice.domain.repository.VoiceRepository
import com.pawan.voicesdk.engine.VoiceEngine
import com.pawan.voicesdk.engine.VoiceEngineResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ## VoiceRepositoryImpl
 *
 * The concrete [VoiceRepository] backed by the [VoiceEngine] from
 * `:voice-sdk`. This class — and *only* this class in the entire `:app`
 * module — is allowed to import `com.pawan.voicesdk.engine.*`. That's not a
 * comment-only convention: it's enforced by [com.pawan.autovoice.di.AppModule]
 * being the only place a [VoiceEngine] instance is ever created, and by
 * every other class in the app depending on [VoiceRepository] (an
 * interface) rather than this implementation.
 *
 * Its only real job is the translation shown in [toVoiceOutcome]: converting
 * the SDK's own [VoiceEngineResult] failure taxonomy (HTTP codes, "malformed
 * response", etc — infrastructure concerns) into the domain's
 * [VoiceFailureReason] (UI-meaningful concepts like "no connectivity"). This
 * mapping function is exactly the kind of pure, side-effect-free logic that
 * gets a dedicated unit test with zero mocking required.
 */
@Singleton
public class VoiceRepositoryImpl @Inject constructor(
    private val voiceEngine: VoiceEngine,
) : VoiceRepository {

    override suspend fun interpretUtterance(utterance: String, sessionId: String): VoiceOutcome {
        val engineResult = voiceEngine.processCommand(utterance = utterance, sessionId = sessionId)
        return engineResult.toVoiceOutcome()
    }
}

private fun VoiceEngineResult.toVoiceOutcome(): VoiceOutcome = when (this) {
    is VoiceEngineResult.Success -> VoiceOutcome.Understood(command)
    VoiceEngineResult.Failure.NetworkUnavailable ->
        VoiceOutcome.NotUnderstood(VoiceFailureReason.NO_CONNECTIVITY)
    is VoiceEngineResult.Failure.ServerError ->
        VoiceOutcome.NotUnderstood(VoiceFailureReason.BACKEND_UNAVAILABLE)
    VoiceEngineResult.Failure.MalformedResponse ->
        VoiceOutcome.NotUnderstood(VoiceFailureReason.COULD_NOT_UNDERSTAND)
    is VoiceEngineResult.Failure.Unknown ->
        VoiceOutcome.NotUnderstood(VoiceFailureReason.UNKNOWN)
}
