package com.pawan.autovoice.domain.usecase

import com.pawan.autovoice.domain.model.VoiceOutcome
import com.pawan.autovoice.domain.repository.VoiceRepository
import java.util.UUID
import javax.inject.Inject

/**
 * ## ProcessVoiceCommandUseCase
 *
 * A single-responsibility "Use Case" (a.k.a. Interactor) — the Clean
 * Architecture building block that sits between the ViewModel and the
 * Repository.
 *
 * ### Why bother with a use case class instead of calling the repository
 * directly from the ViewModel?
 * For a one-line delegation like this, a reviewer might reasonably ask that
 * exact question — and it's a fair one. The honest answer, worth saying out
 * loud in an interview: a thin use case pays for itself the moment business
 * logic grows beyond "call the repository." Concretely, THIS use case
 * already owns two pieces of logic that do not belong in the ViewModel:
 *
 *  1. **Session-id generation** — a voice "session" is a domain concept
 *     (used for multi-turn conversations and for correlating analytics
 *     events end-to-end, per the JD's "contact centre analytics"
 *     requirement), not a UI concept. If two different screens ever need to
 *     start a voice session, they both get identical behaviour for free.
 *  2. **A single seam for cross-cutting concerns** — analytics logging,
 *     retry policy, or rate limiting would all be added HERE, once, rather
 *     than duplicated across every ViewModel that triggers a voice command.
 *
 * Implementing Kotlin's `operator fun invoke` lets call sites read almost
 * like a function call — `processVoiceCommand(utterance)` — while still
 * being a fully mockable/fake-able injected dependency in tests.
 */
public class ProcessVoiceCommandUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository,
) {
    public suspend operator fun invoke(utterance: String, sessionId: String = UUID.randomUUID().toString()): VoiceOutcome =
        voiceRepository.interpretUtterance(utterance = utterance, sessionId = sessionId)
}
