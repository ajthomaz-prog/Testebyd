package com.bydnews.briefing.work

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bydnews.briefing.audio.Mp3Concat
import com.bydnews.briefing.data.model.Briefing
import com.bydnews.briefing.di.ServiceLocator
import com.bydnews.briefing.net.TextChunker
import com.bydnews.briefing.notify.Notifications

class DailyBriefingWorker(
    ctx: Context,
    params: WorkerParameters,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val prefs = ServiceLocator.prefs
        if (!prefs.hasKeys()) {
            Log.w(TAG, "Missing API keys — skipping run.")
            maybeRescheduleDaily()
            return Result.failure()
        }

        val result = runCatching {
            val text = ServiceLocator.anthropic.fetchBriefing(prefs.anthropicKey, prefs.prompt)
            Log.i(TAG, "Briefing text length=${text.length}")

            val chunks = TextChunker.split(text)
            Log.i(TAG, "Chunks: ${chunks.size}")

            val segments = chunks.map { chunk ->
                ServiceLocator.tts.synthesize(chunk, prefs.voiceName, prefs.googleTtsKey)
            }

            val (id, file) = ServiceLocator.repo.newAudioFile()
            Mp3Concat.write(segments, file)

            val durationMs = probeDuration(file.absolutePath)
            val briefing = Briefing(
                id = id,
                createdAtEpochMs = System.currentTimeMillis(),
                promptText = prefs.prompt,
                responseText = text,
                audioPath = file.absolutePath,
                durationMs = durationMs,
            )
            ServiceLocator.repo.save(briefing)
            Notifications.showReady(applicationContext, id)
        }

        maybeRescheduleDaily()

        return result.fold(
            onSuccess = { Result.success() },
            onFailure = { t ->
                Log.e(TAG, "Briefing run failed", t)
                if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
            }
        )
    }

    private fun maybeRescheduleDaily() {
        if (tags.contains(Scheduler.UNIQUE_DAILY)) {
            Scheduler.reschedule(
                applicationContext,
                ServiceLocator.prefs.scheduleHour,
                ServiceLocator.prefs.scheduleMinute,
            )
        }
    }

    private fun probeDuration(path: String): Long {
        val mmr = MediaMetadataRetriever()
        return try {
            mmr.setDataSource(path)
            mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Exception) {
            0L
        } finally {
            runCatching { mmr.release() }
        }
    }

    companion object {
        private const val TAG = "DailyBriefingWorker"
        private const val MAX_ATTEMPTS = 3
    }
}
