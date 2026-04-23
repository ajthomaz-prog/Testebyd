package com.bydnews.briefing.data.storage

import android.content.Context
import com.bydnews.briefing.data.model.Briefing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class BriefingStore(context: Context) {

    private val root: File = File(context.filesDir, "briefings").apply { mkdirs() }
    val audioDir: File = File(root, "audio").apply { mkdirs() }
    private val indexFile: File = File(root, "index.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val mutex = Mutex()

    suspend fun readAll(): List<Briefing> = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!indexFile.exists()) return@withLock emptyList()
            runCatching {
                json.decodeFromString<List<Briefing>>(indexFile.readText())
            }.getOrDefault(emptyList())
        }
    }

    suspend fun writeAll(list: List<Briefing>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            indexFile.writeText(json.encodeToString(list))
        }
    }

    fun newAudioFile(id: String): File = File(audioDir, "$id.mp3")
}
