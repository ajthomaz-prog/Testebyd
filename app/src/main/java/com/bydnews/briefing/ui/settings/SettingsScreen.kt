package com.bydnews.briefing.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bydnews.briefing.R

private val VOICES = listOf(
    "pt-BR-Wavenet-A" to "Wavenet A (feminina)",
    "pt-BR-Wavenet-B" to "Wavenet B (masculina)",
    "pt-BR-Wavenet-C" to "Wavenet C (feminina)",
    "pt-BR-Neural2-A" to "Neural2 A (feminina)",
    "pt-BR-Neural2-B" to "Neural2 B (masculina)",
    "pt-BR-Neural2-C" to "Neural2 C (feminina)",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel()
    val s by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = s.anthropicKey,
                onValueChange = { v -> vm.update { it.copy(anthropicKey = v) } },
                label = { Text(stringResource(R.string.settings_anthropic_key)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = s.googleKey,
                onValueChange = { v -> vm.update { it.copy(googleKey = v) } },
                label = { Text(stringResource(R.string.settings_google_key)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )

            VoicePicker(
                current = s.voice,
                onChange = { name -> vm.update { it.copy(voice = name) } },
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = s.hour.toString(),
                    onValueChange = { txt ->
                        val h = txt.toIntOrNull()?.coerceIn(0, 23) ?: s.hour
                        vm.update { it.copy(hour = h) }
                    },
                    label = { Text("Hora") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = s.minute.toString(),
                    onValueChange = { txt ->
                        val m = txt.toIntOrNull()?.coerceIn(0, 59) ?: s.minute
                        vm.update { it.copy(minute = m) }
                    },
                    label = { Text("Minuto") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Text(
                stringResource(R.string.settings_help_schedule),
                style = MaterialTheme.typography.bodyMedium,
            )

            OutlinedTextField(
                value = s.prompt,
                onValueChange = { v -> vm.update { it.copy(prompt = v) } },
                label = { Text(stringResource(R.string.settings_prompt)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
            )
            OutlinedButton(onClick = vm::resetPrompt) {
                Text(stringResource(R.string.settings_reset_prompt))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = vm::testConnection,
                    enabled = !s.testing,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(R.string.settings_test)) }
                Button(
                    onClick = vm::save,
                    modifier = Modifier.weight(1f).height(64.dp),
                ) { Text(stringResource(R.string.settings_save)) }
            }

            s.testMessage?.let { msg ->
                Text(msg, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoicePicker(current: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = VOICES.firstOrNull { it.first == current }?.second ?: current
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.settings_voice)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        androidx.compose.material3.ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            VOICES.forEach { (name, lbl) ->
                DropdownMenuItem(
                    text = { Text(lbl) },
                    onClick = { onChange(name); expanded = false },
                )
            }
        }
    }
}
