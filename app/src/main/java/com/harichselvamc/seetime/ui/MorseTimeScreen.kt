package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.MorseCharEntry
import com.harichselvamc.seetime.util.MorsePulse
import com.harichselvamc.seetime.util.MorseTimeEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorseTimeScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var wpmSpeed by remember { mutableIntStateOf(15) }
    var useTorch by remember { mutableStateOf(false) }

    var isTransmitting by remember { mutableStateOf(false) }
    var isCurrentPulseHigh by remember { mutableStateOf(false) }
    var activeCharDisplay by remember { mutableStateOf<Char?>(' ') }
    var activeSymbolDisplay by remember { mutableStateOf("") }

    var transmissionText by remember { mutableStateOf("TIME 14:30") }
    var transmitJob by remember { mutableStateOf<Job?>(null) }

    val now = remember { ZonedDateTime.now() }
    val formattedLocalTime = remember(now) { now.format(DateTimeFormatter.ofPattern("HH:mm")) }

    fun stopTransmission() {
        transmitJob?.cancel()
        transmitJob = null
        isTransmitting = false
        isCurrentPulseHigh = false
        activeCharDisplay = null
        activeSymbolDisplay = ""
        if (useTorch) {
            MorseTimeEngine.setCameraTorchMode(context, false)
        }
    }

    fun startTransmission(textToTransmit: String) {
        stopTransmission()
        isTransmitting = true

        val pulses = MorseTimeEngine.encodeTextToPulses(textToTransmit, wpmSpeed)

        transmitJob = scope.launch {
            try {
                for (pulse in pulses) {
                    isCurrentPulseHigh = pulse.isHigh
                    activeCharDisplay = pulse.sourceChar
                    activeSymbolDisplay = pulse.morseSymbol ?: ""

                    if (useTorch && pulse.isHigh) {
                        MorseTimeEngine.setCameraTorchMode(context, true)
                    } else if (useTorch) {
                        MorseTimeEngine.setCameraTorchMode(context, false)
                    }

                    delay(pulse.durationMs)
                }
            } finally {
                stopTransmission()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopTransmission()
        }
    }

    val morseNotation = remember(transmissionText) {
        MorseTimeEngine.textToMorseNotation(transmissionText)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FlashOn,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optical Morse Time Beacon",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTransmitting) Amber60.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isTransmitting) "TRANSMITTING" else "STANDBY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = if (isTransmitting) Amber60 else MaterialTheme.colorScheme.outline
                            )
                        }
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
            // Optical Beacon Flashing Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "OPTICAL BEACON EMITTER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Amber60
                            )
                            Text(
                                text = "$wpmSpeed WPM",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Optical Light Flasher Canvas
                        OpticalBeaconCanvas(
                            isHigh = isCurrentPulseHigh,
                            activeChar = activeCharDisplay,
                            activeSymbol = activeSymbolDisplay,
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isTransmitting) "Broadcasting: \"$transmissionText\"" else "Ready to Transmit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = morseNotation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.Bold,
                            color = Amber60,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Action Button (Play / Stop)
                        Button(
                            onClick = {
                                if (isTransmitting) {
                                    stopTransmission()
                                } else {
                                    startTransmission(transmissionText)
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTransmitting) Color(0xFFEF4444) else Amber60
                            ),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(
                                imageVector = if (isTransmitting) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTransmitting) "Stop Transmission" else "Transmit Morse Flash",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Quick Preset Transmission Chips
            item {
                Text(
                    text = "QUICK TIME BEACON PRESETS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = transmissionText.startsWith("TIME"),
                            onClick = {
                                val text = "TIME $formattedLocalTime"
                                transmissionText = text
                                startTransmission(text)
                            },
                            label = { Text("Local Time ($formattedLocalTime)", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Amber60.copy(alpha = 0.2f), selectedLabelColor = Amber60)
                        )
                    }

                    item {
                        FilterChip(
                            selected = transmissionText == "SOS",
                            onClick = {
                                transmissionText = "SOS"
                                startTransmission("SOS")
                            },
                            label = { Text("Emergency SOS", fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Filled.CrisisAlert, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.2f), selectedLabelColor = Color(0xFFEF4444))
                        )
                    }

                    item {
                        FilterChip(
                            selected = transmissionText.startsWith("TYO"),
                            onClick = {
                                val text = "TYO 23:30"
                                transmissionText = text
                                startTransmission(text)
                            },
                            label = { Text("Tokyo (TYO)", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            // Hardware Controls & Speed Slider Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Beacon Hardware & Speed Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FlashOn, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Camera Flashlight Torch", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("Flashes rear camera LED pulse", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                            Switch(
                                checked = useTorch,
                                onCheckedChange = { useTorch = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Amber60, checkedTrackColor = Amber60.copy(alpha = 0.4f))
                            )
                        }

                        // Speed Slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transmission Speed (WPM)", style = MaterialTheme.typography.bodySmall)
                                Text("$wpmSpeed WPM", fontWeight = FontWeight.Bold, color = Amber60)
                            }
                            Slider(
                                value = wpmSpeed.toFloat(),
                                onValueChange = { wpmSpeed = it.toInt() },
                                valueRange = 5f..35f,
                                steps = 6,
                                colors = SliderDefaults.colors(thumbColor = Amber60, activeTrackColor = Amber60)
                            )
                        }

                        // Custom Text Input
                        OutlinedTextField(
                            value = transmissionText,
                            onValueChange = { transmissionText = it },
                            label = { Text("Custom Message / Time String") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Interactive Morse Code Alphabet Cheat-Sheet
            item {
                Text(
                    text = "ITU MORSE CODE CHEAT-SHEET",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(MorseTimeEngine.CHEAT_SHEET_ENTRIES.chunked(4)) { rowEntries ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowEntries.forEach { entry ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    transmissionText = entry.char.toString()
                                    startTransmission(entry.char.toString())
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = entry.char.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = entry.morse,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = Amber60
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpticalBeaconCanvas(
    isHigh: Boolean,
    activeChar: Char?,
    activeSymbol: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_halo")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.82f

        if (isHigh) {
            // Radiant Flashing Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Amber60.copy(alpha = 0.8f), Amber60.copy(alpha = 0.2f), Color.Transparent),
                    center = center,
                    radius = radius * 1.35f * haloScale
                ),
                radius = radius * 1.35f * haloScale,
                center = center
            )

            // High Center Disc (Bright White/Yellow Light)
            drawCircle(color = Color(0xFFFEF08A), radius = radius, center = center)
            drawCircle(color = Color.White, radius = radius * 0.65f, center = center)
        } else {
            // Standby Dark Lens Disc
            drawCircle(color = Color(0xFF0F172A), radius = radius, center = center)
            drawCircle(color = Color(0xFF1E293B), radius = radius * 0.7f, center = center)
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
