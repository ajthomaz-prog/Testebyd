package com.bydnews.briefing.data.repo

import com.bydnews.briefing.data.model.Briefing
import com.bydnews.briefing.data.storage.BriefingStore
import java.io.File
import java.util.UUID

class BriefingRepository(private val store: BriefingStore) {

    suspend fun list(): List<Briefing> =
        store.readAll().sortedByDescending { it.createdAtEpochMs }

    fun newAudioFile(): Pair<String, File> {
        val id = UUID.randomUUID().toString()
        return id to store.newAudioFile(id)
    }

    suspend fun save(b: Briefing, keep: Int = 7) {
        val current = store.readAll().toMutableList()
        current.removeAll { it.id == b.id }
        current.add(b)
        val sorted = current.sortedByDescending { it.createdAtEpochMs }
        val (keepers, losers) = sorted.withIndex().partition { it.index < keep }
        losers.forEach { runCatching { File(it.value.audioPath).delete() } }
        store.writeAll(keepers.map { it.value })
    }

    suspend fun delete(id: String) {
        val current = store.readAll().toMutableList()
        current.firstOrNull { it.id == id }?.let {
            runCatching { File(it.audioPath).delete() }
        }
        current.removeAll { it.id == id }
        store.writeAll(current)
    }
}
