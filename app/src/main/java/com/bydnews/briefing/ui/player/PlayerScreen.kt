package com.bydnews.briefing.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bydnews.briefing.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(briefingId: String, onBack: () -> Unit) {
    val vm: PlayerViewModel = viewModel()
    LaunchedEffect(briefingId) { vm.load(briefingId) }
    val briefing by vm.briefing.collectAsStateWithLifecycle()
    val isPlaying by vm.isPlaying.collectAsStateWithLifecycle()
    val position by vm.positionMs.collectAsStateWithLifecycle()

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            vm.tickPosition()
            delay(500)
        }
    }

    val fmt = remember { SimpleDateFormat("EEEE, dd 'de' MMMM 'às' HH:mm", Locale("pt", "BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        val b = briefing
        if (b == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("…") }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
        ) {
            Text(fmt.format(Date(b.createdAtEpochMs)), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            val duration = b.durationMs.coerceAtLeast(1L)
            Slider(
                value = (position.coerceAtLeast(0L).toFloat() / duration.toFloat()).coerceIn(0f, 1f),
                onValueChange = { frac -> vm.seekTo((frac * duration).toLong()) },
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(position))
                Text(formatTime(b.durationMs))
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { vm.seekBy(-15_000) }, modifier = Modifier.size(88.dp)) {
                    Icon(Icons.Filled.Replay10, contentDescription = stringResource(R.string.player_skip_back),
                        modifier = Modifier.size(56.dp))
                }
                Spacer(Modifier.size(24.dp))
                FilledIconButton(
                    onClick = vm::playPause,
                    modifier = Modifier.size(112.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                    )
                }
                Spacer(Modifier.size(24.dp))
                IconButton(onClick = { vm.seekBy(30_000) }, modifier = Modifier.size(88.dp)) {
                    Icon(Icons.Filled.Forward30, contentDescription = stringResource(R.string.player_skip_forward),
                        modifier = Modifier.size(56.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                b.responseText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val s = (ms.coerceAtLeast(0L) / 1000)
    return "%d:%02d".format(s / 60, s % 60)
}
