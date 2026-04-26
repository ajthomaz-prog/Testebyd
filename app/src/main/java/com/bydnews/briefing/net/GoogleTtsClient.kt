package com.bydnews.briefing.net

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GoogleTtsClient(private val http: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun synthesize(text: String, voiceName: String, apiKey: String): ByteArray =
        withContext(Dispatchers.IO) {
            require(apiKey.isNotBlank()) { "Missing Google Cloud TTS API key" }
            require(text.isNotBlank()) { "Empty TTS input" }

            val body = TtsRequest(
                input = TtsRequest.Input(text),
                voice = TtsRequest.Voice("pt-BR", voiceName),
                audioConfig = TtsRequest.AudioConfig("MP3"),
            )
            val bodyJson = json.encodeToString(TtsRequest.serializer(), body)
            val req = Request.Builder()
                .url("https://texttospeech.googleapis.com/v1/text:synthesize?key=$apiKey")
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            http.newCall(req).execute().use { resp ->
                val raw = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    throw RuntimeException("Google TTS ${resp.code}: ${raw.take(400)}")
                }
                val parsed = json.decodeFromString(TtsResponse.serializer(), raw)
                val audio = parsed.audioContent
                    ?: throw RuntimeException("Google TTS: missing audioContent")
                Base64.decode(audio, Base64.DEFAULT)
            }
        }
}
