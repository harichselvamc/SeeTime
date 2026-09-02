package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.GlobeMath
import com.harichselvamc.seetime.util.SupportedVoiceLanguage
import com.harichselvamc.seetime.util.VoiceAnnouncerManager
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAnnouncerScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val announcer = remember { VoiceAnnouncerManager.getInstance() }

    LaunchedEffect(Unit) {
        announcer.initialize(context)
    }

    val isSpeaking by announcer.isSpeakingState.collectAsState()
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedLanguage by remember { mutableStateOf(SupportedVoiceLanguage.ENGLISH) }
    var selectedCityName by remember { mutableStateOf("Tokyo") }
    var selectedZoneId by remember { mutableStateOf("Asia/Tokyo") }

    var speechRate by remember { mutableFloatStateOf(1.0f) }
    var speechPitch by remember { mutableFloatStateOf(1.0f) }
    var hourlyChimeEnabled by remember { mutableStateOf(false) }

    val now = remember { ZonedDateTime.now() }
    val localZoneId = remember { ZoneId.systemDefault().id }

    val announcementScript = remember(selectedCityName, selectedZoneId, selectedLanguage, now) {
        VoiceAnnouncerManager.generateAnnouncementScript(
            cityName = selectedCityName,
            targetZoneId = selectedZoneId,
            language = selectedLanguage,
            localZoneId = localZoneId,
            referenceTime = now
        )
    }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.RecordVoiceOver,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multilingual Voice Announcer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSpeaking) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Filled.VolumeUp else Icons.Filled.GraphicEq,
                                contentDescription = null,
                                tint = if (isSpeaking) SuccessGreen else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isSpeaking) "Speaking..." else "Idle",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSpeaking) SuccessGreen else MaterialTheme.colorScheme.outline
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
            // Audio Waveform Visualizer Canvas Hero Card
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
                                text = "SPEECH WAVEFORM SYNTHESIZER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cobalt60
                            )
                            Text(
                                text = "${selectedLanguage.flagEmoji} ${selectedLanguage.displayName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Animated Audio Waveform Canvas
                        AudioWaveformCanvas(
                            isSpeaking = isSpeaking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Speak / Stop Primary Action Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    if (isSpeaking) {
                                        announcer.stop()
                                    } else {
                                        announcer.speak(
                                            text = announcementScript,
                                            language = selectedLanguage,
                                            pitch = speechPitch,
                                            speechRate = speechRate
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSpeaking) Color(0xFFEF4444) else Cobalt60
                                )
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSpeaking) "Stop Speaking" else "Announce $selectedCityName Time",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Language Selection Chips
            item {
                Text(
                    text = "SELECT ANNOUNCEMENT LANGUAGE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SupportedVoiceLanguage.values()) { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLanguage = lang },
                            label = { Text("${lang.flagEmoji} ${lang.displayName}", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                selectedLabelColor = Cobalt60
                            )
                        )
                    }
                }
            }

            // Script Preview Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Spoken Script Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "\"$announcementScript\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Timezone City Cards with 1-Tap Speak
            item {
                Text(
                    text = "SELECT CITY TO ANNOUNCE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(GlobeMath.MAJOR_WORLD_CITIES) { city ->
                val cityZone = remember(city) {
                    try { ZoneId.of(city.zoneId) } catch (_: Exception) { ZoneId.systemDefault() }
                }
                val cityTime = remember(cityZone) { ZonedDateTime.now(cityZone) }
                val isSelected = selectedCityName == city.name

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Cobalt60.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.clickable {
                        selectedCityName = city.name
                        selectedZoneId = city.zoneId
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${city.name}, ${city.country}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cityTime.format(timeFormatter),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        IconButton(
                            onClick = {
                                selectedCityName = city.name
                                selectedZoneId = city.zoneId
                                val script = VoiceAnnouncerManager.generateAnnouncementScript(
                                    cityName = city.name,
                                    targetZoneId = city.zoneId,
                                    language = selectedLanguage,
                                    localZoneId = localZoneId,
                                    referenceTime = ZonedDateTime.now()
                                )
                                announcer.speak(script, selectedLanguage, speechPitch, speechRate)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = "Speak ${city.name}",
                                tint = Cobalt60,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }

            // Voice & Speed Controls Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Speech Voice & Speed Controls", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        // Speed Slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Speech Rate", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format(Locale.getDefault(), "%.2f", speechRate)}x", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Slider(
                                value = speechRate,
                                onValueChange = { speechRate = it },
                                valueRange = 0.75f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = Cobalt60, activeTrackColor = Cobalt60)
                            )
                        }

                        // Pitch Slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Voice Pitch", style = MaterialTheme.typography.bodySmall)
                                Text("${String.format(Locale.getDefault(), "%.2f", speechPitch)}x", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Slider(
                                value = speechPitch,
                                onValueChange = { speechPitch = it },
                                valueRange = 0.75f..1.25f,
                                colors = SliderDefaults.colors(thumbColor = Amber60, activeTrackColor = Amber60)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioWaveformCanvas(
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2

        val amplitude = if (isSpeaking) h * 0.35f else h * 0.05f

        // Draw 3 layered sine waves with phase shifts
        listOf(
            Triple(0f, Cobalt60, 3.dp.toPx()),
            Triple(0.8f, Cyan60, 2.dp.toPx()),
            Triple(1.6f, Amber60, 1.5.dp.toPx())
        ).forEach { (offset, color, strokeWidth) ->
            val path = Path()
            path.moveTo(0f, midY)

            for (x in 0..w.toInt() step 4) {
                val progress = x / w
                val y = midY + amplitude * sin((progress * 4 * Math.PI + phase + offset)).toFloat()
                path.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = path,
                color = if (isSpeaking) color else Color.Gray.copy(alpha = 0.3f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
