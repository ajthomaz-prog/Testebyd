package com.bydnews.briefing.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AnthropicClient(private val http: OkHttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchBriefing(apiKey: String, prompt: String): String = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Missing Anthropic API key" }

        val webSearchTool = buildJsonObject {
            put("type", "web_search_20250305")
            put("name", "web_search")
            put("max_uses", 5)
        }

        val body = AnthropicRequest(
            model = "claude-sonnet-4-5",
            maxTokens = 4096,
            messages = listOf(AnthropicRequest.Message("user", prompt)),
            tools = listOf(webSearchTool),
        )

        val bodyJson = json.encodeToString(AnthropicRequest.serializer(), body)
        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("anthropic-beta", "web-search-2025-03-05")
            .post(bodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(req).execute().use { resp ->
            val raw = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                val err = runCatching { json.decodeFromString(AnthropicError.serializer(), raw) }.getOrNull()
                throw RuntimeException("Anthropic ${resp.code}: ${err?.error?.message ?: raw.take(400)}")
            }
            val parsed = json.decodeFromString(AnthropicResponse.serializer(), raw)
            val text = parsed.content
                .filter { it.type == "text" && !it.text.isNullOrBlank() }
                .joinToString("\n\n") { stripCitations(it.text!!) }
            text.ifBlank {
                throw RuntimeException("Anthropic returned empty content (stop=${parsed.stopReason})")
            }
        }
    }

    /**
     * Removes citation markers like [1], [2][3] and bare URLs so the TTS
     * doesn't read "abre colchete um fecha colchete".
     */
    private fun stripCitations(text: String): String =
        text
            .replace(Regex("""\[\s*\d+(\s*,\s*\d+)*\s*]"""), "")
            .replace(Regex("""https?://\S+"""), "")
            .replace(Regex("""[ \t]{2,}"""), " ")
            .lineSequence()
            .map { it.trim() }
            .joinToString("\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
}
