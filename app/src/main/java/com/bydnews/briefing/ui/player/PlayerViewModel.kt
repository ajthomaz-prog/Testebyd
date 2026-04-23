package com.bydnews.briefing.ui.player

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.bydnews.briefing.audio.PlaybackService
import com.bydnews.briefing.data.model.Briefing
import com.bydnews.briefing.di.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val _briefing = MutableStateFlow<Briefing?>(null)
    val briefing: StateFlow<Briefing?> = _briefing.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private var controller: MediaController? = null
    private var controllerFuture: com.google.common.util.concurrent.ListenableFuture<MediaController>? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(playing: Boolean) { _isPlaying.value = playing }
        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) { _positionMs.value = controller?.currentPosition ?: 0L }
    }

    fun load(briefingId: String) {
        viewModelScope.launch {
            val all = ServiceLocator.repo.list()
            val b = all.firstOrNull { it.id == briefingId } ?: return@launch
            _briefing.value = b
            connectController(b)
        }
    }

    private fun connectController(b: Briefing) {
        val token = SessionToken(getApplication(), ComponentName(getApplication(), PlaybackService::class.java))
        controllerFuture = MediaController.Builder(getApplication(), token).buildAsync().also { future ->
            future.addListener({
                val c = future.get()
                controller = c
                c.addListener(listener)
                c.setMediaItem(MediaItem.fromUri("file://${b.audioPath}"))
                c.prepare()
            }, ContextCompat.getMainExecutor(getApplication()))
        }
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun seekBy(deltaMs: Long) {
        val c = controller ?: return
        val target = (c.currentPosition + deltaMs).coerceIn(0, c.duration.coerceAtLeast(0))
        c.seekTo(target)
        _positionMs.value = target
    }

    fun seekTo(ms: Long) {
        controller?.seekTo(ms)
        _positionMs.value = ms
    }

    fun tickPosition() {
        _positionMs.value = controller?.currentPosition ?: 0L
    }

    override fun onCleared() {
        super.onCleared()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }
}
