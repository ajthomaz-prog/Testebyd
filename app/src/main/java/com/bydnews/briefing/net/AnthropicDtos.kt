package com.bydnews.briefing.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val messages: List<Message>,
    val tools: List<JsonElement>? = null,
) {
    @Serializable
    data class Message(
        val role: String,
        val content: String,
    )
}

@Serializable
data class AnthropicResponse(
    val id: String? = null,
    val role: String? = null,
    val model: String? = null,
    @SerialName("stop_reason") val stopReason: String? = null,
    val content: List<ContentBlock> = emptyList(),
) {
    @Serializable
    data class ContentBlock(
        val type: String,
        val text: String? = null,
    )
}

@Serializable
data class AnthropicError(
    val type: String? = null,
    val error: ErrorBody? = null,
) {
    @Serializable
    data class ErrorBody(val type: String? = null, val message: String? = null)
}
