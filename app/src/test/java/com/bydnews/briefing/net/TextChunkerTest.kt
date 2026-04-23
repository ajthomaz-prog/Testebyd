package com.bydnews.briefing.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextChunkerTest {

    @Test
    fun emptyText_returnsEmpty() {
        assertEquals(emptyList<String>(), TextChunker.split(""))
    }

    @Test
    fun shortText_oneChunk() {
        val out = TextChunker.split("Uma frase curta.", maxBytes = 100)
        assertEquals(1, out.size)
        assertEquals("Uma frase curta.", out[0])
    }

    @Test
    fun manySentences_allUnderLimit() {
        val text = buildString {
            repeat(30) { append("Esta é a frase ${it + 1}. ") }
        }
        val out = TextChunker.split(text, maxBytes = 120)
        out.forEach { assertTrue(it.toByteArray(Charsets.UTF_8).size <= 120) }
    }

    @Test
    fun overlongSentence_fallsBackToCommaSplit() {
        val words = (1..100).joinToString(", ") { "palavra$it" } + "."
        val out = TextChunker.split(words, maxBytes = 80)
        out.forEach { assertTrue("chunk over limit: $it", it.toByteArray(Charsets.UTF_8).size <= 80) }
    }

    @Test
    fun hardByteSplit_noMidMultibyteCharacter() {
        val text = "ação ".repeat(500)
        val out = TextChunker.split(text, maxBytes = 40)
        out.forEach { assertTrue(it.toByteArray(Charsets.UTF_8).size <= 40) }
        out.forEach { assertEquals(it, String(it.toByteArray(Charsets.UTF_8), Charsets.UTF_8)) }
    }
}
