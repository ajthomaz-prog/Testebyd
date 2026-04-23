package com.bydnews.briefing.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePrefs(context: Context) {

    private val sp: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var anthropicKey: String
        get() = sp.getString(KEY_ANTHROPIC, "") ?: ""
        set(v) = sp.edit().putString(KEY_ANTHROPIC, v).apply()

    var googleTtsKey: String
        get() = sp.getString(KEY_GOOGLE, "") ?: ""
        set(v) = sp.edit().putString(KEY_GOOGLE, v).apply()

    var voiceName: String
        get() = sp.getString(KEY_VOICE, DEFAULT_VOICE) ?: DEFAULT_VOICE
        set(v) = sp.edit().putString(KEY_VOICE, v).apply()

    var scheduleHour: Int
        get() = sp.getInt(KEY_HOUR, 6)
        set(v) = sp.edit().putInt(KEY_HOUR, v).apply()

    var scheduleMinute: Int
        get() = sp.getInt(KEY_MINUTE, 0)
        set(v) = sp.edit().putInt(KEY_MINUTE, v).apply()

    var prompt: String
        get() = sp.getString(KEY_PROMPT, DEFAULT_PROMPT) ?: DEFAULT_PROMPT
        set(v) = sp.edit().putString(KEY_PROMPT, v).apply()

    fun hasKeys(): Boolean = anthropicKey.isNotBlank() && googleTtsKey.isNotBlank()

    companion object {
        private const val FILE_NAME = "secure_prefs"
        private const val KEY_ANTHROPIC = "anthropic_key"
        private const val KEY_GOOGLE = "google_tts_key"
        private const val KEY_VOICE = "voice"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
        private const val KEY_PROMPT = "prompt"

        const val DEFAULT_VOICE = "pt-BR-Wavenet-B"

        const val DEFAULT_PROMPT =
            "me diga as principais notícias das últimas 48 horas foque no mercado financeiro brasileiro, " +
                "principais notícias no Brasil mercados globais, banco UBS, e novidades de IA que podem ser aplicadas na prática."
    }
}
