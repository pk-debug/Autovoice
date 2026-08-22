package com.pawan.autovoice.domain.repository

import com.pawan.autovoice.domain.model.VoiceOutcome

/**
 * ## VoiceRepository
 *
 * Domain-layer contract. Lives in `domain/repository` (an *interface only*,
 * no implementation) so that:
 *
 *  - The **domain** module/package can be compiled and unit-tested with
 *    ZERO knowledge of `:voice-sdk`, Retrofit, or Android at all.
 *  - The **data** layer (`data/repository/VoiceRepositoryImpl.kt`) depends
 *    ON this interface, never the other way around — this is the
 *    Dependency Inversion Principle in practice, and it's what lets
 *    [com.pawan.autovoice.domain.usecase.ProcessVoiceCommandUseCase] and
 *    the ViewModel be unit-tested with a trivial hand-written fake instead
 *    of standing up a real `VoiceEngine`.
 */
public interface VoiceRepository {
    public suspend fun interpretUtterance(utterance: String, sessionId: String): VoiceOutcome
}
