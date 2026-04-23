package com.bydnews.briefing.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bydnews.briefing.data.prefs.SecurePrefs
import com.bydnews.briefing.di.ServiceLocator
import com.bydnews.briefing.net.Http
import com.bydnews.briefing.net.TtsRequest
import com.bydnews.briefing.work.Scheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SettingsViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs get() = ServiceLocator.prefs

    data class UiState(
        val anthropicKey: String = "",
        val googleKey: String = "",
        val voice: String = SecurePrefs.DEFAULT_VOICE,
        val hour: Int = 6,
        val minute: Int = 0,
        val prompt: String = SecurePrefs.DEFAULT_PROMPT,
        val testMessage: String? = null,
        val testing: Boolean = false,
    )

    private val _state = MutableStateFlow(
        UiState(
            anthropicKey = prefs.anthropicKey,
            googleKey = prefs.googleTtsKey,
            voice = prefs.voiceName,
            hour = prefs.scheduleHour,
            minute = prefs.scheduleMinute,
            prompt = prefs.prompt,
        )
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun update(block: (UiState) -> UiState) { _state.value = block(_state.value) }

    fun save() {
        val s = _state.value
        prefs.anthropicKey = s.anthropicKey.trim()
        prefs.googleTtsKey = s.googleKey.trim()
        prefs.voiceName = s.voice
        prefs.scheduleHour = s.hour
        prefs.scheduleMinute = s.minute
        prefs.prompt = s.prompt.trim().ifBlank { SecurePrefs.DEFAULT_PROMPT }
        Scheduler.reschedule(getApplication(), s.hour, s.minute)
    }

    fun resetPrompt() {
        _state.value = _state.value.copy(prompt = SecurePrefs.DEFAULT_PROMPT)
    }

    fun testConnection() {
        _state.value = _state.value.copy(testing = true, testMessage = null)
        viewModelScope.launch {
            val result = runCatching {
                val s = _state.value
                pingAnthropic(s.anthropicKey.trim())
                pingGoogle(s.googleKey.trim(), s.voice)
            }
            _state.value = _state.value.copy(
                testing = false,
                testMessage = result.fold(
                    onSuccess = { "Conexão OK com Claude e Google TTS." },
                    onFailure = { it.message ?: "Falha no teste" },
                ),
            )
        }
    }

    private suspend fun pingAnthropic(key: String) = withContext(Dispatchers.IO) {
        val body = """
            {"model":"claude-sonnet-4-5","max_tokens":8,"messages":[{"role":"user","content":"ping"}]}
        """.trimIndent()
        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", key)
            .header("anthropic-version", "2023-06-01")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        Http.client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Claude ${resp.code}: ${resp.body?.string()?.take(200)}")
        }
    }

    private suspend fun pingGoogle(key: String, voice: String) = withContext(Dispatchers.IO) {
        val req = TtsRequest(
            TtsRequest.Input("olá"),
            TtsRequest.Voice("pt-BR", voice),
            TtsRequest.AudioConfig("MP3"),
        )
        val json = Json.encodeToString(TtsRequest.serializer(), req)
        val http = Request.Builder()
            .url("https://texttospeech.googleapis.com/v1/text:synthesize?key=$key")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        Http.client.newCall(http).execute().use { resp ->
            if (!resp.isSuccessful) error("Google TTS ${resp.code}: ${resp.body?.string()?.take(200)}")
        }
    }
}
