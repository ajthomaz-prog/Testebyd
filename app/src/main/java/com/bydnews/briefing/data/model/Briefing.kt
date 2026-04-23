package com.bydnews.briefing.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Briefing(
    val id: String,
    val createdAtEpochMs: Long,
    val promptText: String,
    val responseText: String,
    val audioPath: String,
    val durationMs: Long,
)
