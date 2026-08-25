package com.pawan.autovoice.domain.model

/**
 * ## VoiceInteraction
 *
 * Represents a single turn in a voice conversation.
 *
 * @property utterance The user's spoken text.
 * @property response The assistant's spoken response.
 * @property timestamp The time the interaction occurred.
 */
public data class VoiceInteraction(
    val utterance: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis(),
)
