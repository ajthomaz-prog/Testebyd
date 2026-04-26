package com.bydnews.briefing.audio

import java.io.File
import java.io.FileOutputStream

object Mp3Concat {

    /**
     * Concatenates Google TTS MP3 segments (CBR, same bitrate/sample rate)
     * by byte-appending frames. Safe as long as every segment is produced
     * with identical audioConfig.
     */
    fun write(segments: List<ByteArray>, out: File) {
        out.parentFile?.mkdirs()
        FileOutputStream(out).use { fos ->
            segments.forEach { fos.write(it) }
            fos.flush()
        }
    }
}
