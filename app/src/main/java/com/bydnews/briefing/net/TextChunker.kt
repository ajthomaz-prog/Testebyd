package com.bydnews.briefing.net

object TextChunker {

    private val SENTENCE_RX = Regex("(?<=[.!?])\\s+")

    /**
     * Splits text into UTF-8 chunks no larger than [maxBytes] each.
     * Breaks on sentence boundaries when possible, then commas, then word
     * boundaries. Never splits mid-multibyte-character.
     */
    fun split(text: String, maxBytes: Int = 4500): List<String> {
        if (text.isBlank()) return emptyList()
        val out = mutableListOf<String>()
        val sentences = text.split(SENTENCE_RX).filter { it.isNotBlank() }
        val buf = StringBuilder()

        fun flush() {
            if (buf.isNotEmpty()) {
                out.add(buf.toString().trim())
                buf.clear()
            }
        }

        for (sentence in sentences) {
            val pending = if (buf.isEmpty()) sentence else "${buf.trimEnd()} $sentence"
            if (pending.toByteArray(Charsets.UTF_8).size <= maxBytes) {
                if (buf.isNotEmpty()) buf.append(' ')
                buf.append(sentence)
            } else {
                flush()
                if (sentence.toByteArray(Charsets.UTF_8).size <= maxBytes) {
                    buf.append(sentence)
                } else {
                    out.addAll(splitLong(sentence, maxBytes))
                }
            }
        }
        flush()
        return out
    }

    private fun splitLong(sentence: String, maxBytes: Int): List<String> {
        val parts = mutableListOf<String>()
        val buf = StringBuilder()
        val pieces = sentence.split(Regex(",\\s+")).flatMap { chunk ->
            if (chunk.toByteArray(Charsets.UTF_8).size <= maxBytes) listOf(chunk)
            else chunk.split(" ").filter { it.isNotBlank() }
        }
        for (piece in pieces) {
            val candidate = if (buf.isEmpty()) piece else "${buf.trimEnd()} $piece"
            if (candidate.toByteArray(Charsets.UTF_8).size <= maxBytes) {
                if (buf.isNotEmpty()) buf.append(' ')
                buf.append(piece)
            } else {
                if (buf.isNotEmpty()) {
                    parts.add(buf.toString().trim())
                    buf.clear()
                }
                if (piece.toByteArray(Charsets.UTF_8).size > maxBytes) {
                    parts.addAll(hardByteSplit(piece, maxBytes))
                } else {
                    buf.append(piece)
                }
            }
        }
        if (buf.isNotEmpty()) parts.add(buf.toString().trim())
        return parts
    }

    private fun hardByteSplit(text: String, maxBytes: Int): List<String> {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val out = mutableListOf<String>()
        var i = 0
        while (i < bytes.size) {
            var end = minOf(i + maxBytes, bytes.size)
            while (end > i && end < bytes.size && (bytes[end].toInt() and 0xC0) == 0x80) end--
            out.add(String(bytes, i, end - i, Charsets.UTF_8))
            i = end
        }
        return out
    }
}
