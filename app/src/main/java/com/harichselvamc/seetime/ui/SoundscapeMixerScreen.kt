package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.Purple60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.SoundLayer
import com.harichselvamc.seetime.util.SoundscapeMixerEngine
import com.harichselvamc.seetime.util.SoundscapePreset
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundscapeMixerScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mixerEngine = remember { SoundscapeMixerEngine.getInstance(context) }

    val isPlaying by mixerEngine.isPlaying.collectAsState()
    val masterVolume by mixerEngine.masterVolume.collectAsState()
    val isMuted by mixerEngine.isMuted.collectAsState()
    val trackVolumes by mixerEngine.trackVolumes.collectAsState()
    val activePresetName by mixerEngine.activePresetName.collectAsState()
    val customPresets by mixerEngine.customPresets.collectAsState()
    val sleepTimerRemaining by mixerEngine.sleepTimerRemainingMillis.collectAsState()

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    val allPresets = remember(customPresets) {
        SoundscapeMixerEngine.createDefaultPresets() + customPresets
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Soundscape Mixer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Zero Asset PCM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = SuccessGreen
                            )
                        }
                    }
                    IconButton(onClick = { showSavePresetDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save Custom Mix",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Soundscape Control Hero Card
            item {
                MasterControlHeroCard(
                    isPlaying = isPlaying,
                    masterVolume = masterVolume,
                    isMuted = isMuted,
                    activePresetName = activePresetName,
                    sleepTimerRemaining = sleepTimerRemaining,
                    onTogglePlay = { mixerEngine.togglePlayback() },
                    onMasterVolumeChange = { mixerEngine.setMasterVolume(it) },
                    onToggleMute = { mixerEngine.toggleMute() },
                    onOpenSleepTimer = { showSleepTimerDialog = true },
                    onCancelSleepTimer = { mixerEngine.cancelSleepTimer() }
                )
            }

            // Preset Soundscape Selector
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOUNDSCAPE PRESETS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (activePresetName != null) {
                            Text(
                                text = "Active: $activePresetName",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Cobalt60
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allPresets) { preset ->
                            val isSelected = activePresetName == preset.name
                            FilterChip(
                                selected = isSelected,
                                onClick = { mixerEngine.applyPreset(preset) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (preset.isCustom) Icons.Filled.Bookmark else Icons.Filled.Bedtime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(preset.name, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                    selectedLabelColor = Cobalt60
                                )
                            )
                        }
                    }
                }
            }

            // 6-Channel Sound Layer Fader Cards
            item {
                Text(
                    text = "6-CHANNEL MULTI-TRACK MIXER",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(SoundLayer.values(), key = { it.name }) { layer ->
                val volume = trackVolumes[layer] ?: 0f
                SoundChannelStripCard(
                    layer = layer,
                    volume = volume,
                    isPlaying = isPlaying,
                    onVolumeChange = { mixerEngine.setTrackVolume(layer, it) }
                )
            }
        }
    }

    // Dialog: Save Custom Preset
    if (showSavePresetDialog) {
        SaveCustomMixDialog(
            onDismiss = { showSavePresetDialog = false },
            onSave = { name ->
                mixerEngine.saveCustomPreset(name)
                showSavePresetDialog = false
            }
        )
    }

    // Dialog: Sleep Timer
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentRemainingMillis = sleepTimerRemaining,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = { minutes ->
                mixerEngine.startSleepTimer(minutes)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                mixerEngine.cancelSleepTimer()
                showSleepTimerDialog = false
            }
        )
    }
}

// ── Composable Components ─────────────────────────────────────────────

@Composable
private fun MasterControlHeroCard(
    isPlaying: Boolean,
    masterVolume: Float,
    isMuted: Boolean,
    activePresetName: String?,
    sleepTimerRemaining: Long,
    onTogglePlay: () -> Unit,
    onMasterVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onCancelSleepTimer: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Big Play/Pause Button
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) Cobalt60 else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(56.dp)
                            .clickable(onClick = onTogglePlay)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = if (isPlaying) Color.White else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isPlaying) "Playing Ambient Mix" else "Mixer Paused",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activePresetName?.let { "Preset: $it" } ?: "Custom Real-time Mix",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sleep Timer Quick Button
                if (sleepTimerRemaining > 0) {
                    val minutes = (sleepTimerRemaining / 1000) / 60
                    val seconds = (sleepTimerRemaining / 1000) % 60
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Amber60.copy(alpha = 0.15f),
                        modifier = Modifier.clickable(onClick = onOpenSleepTimer)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = Amber60,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Amber60
                            )
                        }
                    }
                } else {
                    IconButton(onClick = onOpenSleepTimer) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Set Sleep Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Master Volume Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onToggleMute)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.VolumeMute else Icons.Filled.VolumeUp,
                            contentDescription = "Toggle Mute",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else Cobalt60,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMuted) "Master Muted" else "Master Volume",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = if (isMuted) "0%" else "${(masterVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Cobalt60
                    )
                }

                Slider(
                    value = if (isMuted) 0f else masterVolume,
                    onValueChange = onMasterVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = Cobalt60,
                        activeTrackColor = Cobalt60,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun SoundChannelStripCard(
    layer: SoundLayer,
    volume: Float,
    isPlaying: Boolean,
    onVolumeChange: (Float) -> Unit
) {
    val layerColor = getLayerColor(layer)
    val layerIcon = getLayerIcon(layer)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Layer Icon Badge
            Surface(
                shape = CircleShape,
                color = layerColor.copy(alpha = if (volume > 0.05f && isPlaying) 0.22f else 0.10f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = layerIcon,
                        contentDescription = null,
                        tint = if (volume > 0.05f) layerColor else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = layer.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (volume > 0) layerColor else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = layerColor,
                        activeTrackColor = layerColor,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.height(28.dp)
                )

                Text(
                    text = layer.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SaveCustomMixDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var presetName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Custom Soundscape", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Give your current 6-track audio mix a name:")
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    placeholder = { Text("e.g. Rainy Night Train") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (presetName.isNotBlank()) onSave(presetName.trim()) },
                enabled = presetName.isNotBlank()
            ) {
                Text("Save Preset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun SleepTimerDialog(
    currentRemainingMillis: Long,
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Soundscape Sleep Timer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Sound will gently and exponentially fade out as the timer expires so you stay asleep.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (currentRemainingMillis > 0) {
                    val minLeft = (currentRemainingMillis / 1000) / 60
                    Text(
                        text = "Current Active Timer: ~$minLeft minutes remaining",
                        fontWeight = FontWeight.Bold,
                        color = Amber60
                    )
                }

                Text(
                    text = "Select Duration:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(15, 30, 45, 60, 90).forEach { mins ->
                        item {
                            FilterChip(
                                selected = false,
                                onClick = { onSetTimer(mins) },
                                label = { Text("$mins min", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentRemainingMillis > 0) {
                Button(
                    onClick = onCancelTimer,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Timer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// ── Helper Category Icons & Colors ────────────────────────────────────

private fun getLayerIcon(layer: SoundLayer): ImageVector {
    return when (layer) {
        SoundLayer.BINAURAL_ALPHA -> Icons.Filled.Psychology
        SoundLayer.PINK_NOISE -> Icons.Filled.GraphicEq
        SoundLayer.OCEAN_SURF -> Icons.Filled.Water
        SoundLayer.GENTLE_RAIN -> Icons.Filled.Cloud
        SoundLayer.CAMPFIRE -> Icons.Filled.LocalFireDepartment
        SoundLayer.NIGHT_WIND -> Icons.Filled.Air
    }
}

private fun getLayerColor(layer: SoundLayer): Color {
    return when (layer) {
        SoundLayer.BINAURAL_ALPHA -> Purple60
        SoundLayer.PINK_NOISE -> Cyan60
        SoundLayer.OCEAN_SURF -> Cobalt60
        SoundLayer.GENTLE_RAIN -> Color(0xFF0288D1)
        SoundLayer.CAMPFIRE -> Amber60
        SoundLayer.NIGHT_WIND -> Color(0xFF78909C)
    }
}
