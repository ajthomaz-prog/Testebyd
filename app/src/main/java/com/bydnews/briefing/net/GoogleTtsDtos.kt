package com.bydnews.briefing.net

import kotlinx.serialization.Serializable

@Serializable
data class TtsRequest(
    val input: Input,
    val voice: Voice,
    val audioConfig: AudioConfig,
) {
    @Serializable data class Input(val text: String)
    @Serializable data class Voice(val languageCode: String, val name: String)
    @Serializable data class AudioConfig(
        val audioEncoding: String,
        val speakingRate: Double = 1.0,
        val pitch: Double = 0.0,
        val sampleRateHertz: Int = 24000,
    )
}

@Serializable
data class TtsResponse(val audioContent: String? = null)
