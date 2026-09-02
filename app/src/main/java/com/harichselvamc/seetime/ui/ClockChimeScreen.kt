package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.harichselvamc.seetime.util.ChimeMelody
import com.harichselvamc.seetime.util.ChimeQuarter
import com.harichselvamc.seetime.util.ClockChimeEngine
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockChimeScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = remember { ClockChimeEngine.getInstance(context) }

    val isHourlyEnabled by engine.isHourlyChimeEnabled.collectAsState()
    val isQuarterEnabled by engine.isQuarterChimeEnabled.collectAsState()
    val selectedMelody by engine.selectedMelody.collectAsState()
    val chimeVolume by engine.chimeVolume.collectAsState()
    val quietStart by engine.quietHoursStart.collectAsState()
    val quietEnd by engine.quietHoursEnd.collectAsState()
    val isPlayingPreview by engine.isPlayingPreview.collectAsState()

    var nowTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowTime = LocalTime.now()
            delay(1000L)
        }
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Grandfather Clock Chimes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Amber60.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = Amber60,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Big Ben PCM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Amber60
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
            // Grandfather Horology Dial & Swinging Pendulum Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Horology Clock & Pendulum Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GrandfatherPendulumCanvas(
                                localTime = nowTime,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Live Time & Next Chime Status
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = nowTime.format(timeFormatter),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            val minutesToNextHour = 60 - nowTime.minute
                            val nextHour = (nowTime.hour + 1) % 24
                            val isQuiet = !engine.shouldChimeAtTime(nowTime.plusMinutes(minutesToNextHour.toLong()))

                            Text(
                                text = if (isHourlyEnabled) {
                                    if (isQuiet) "Next chime at ${String.format("%02d:00", nextHour)} (Suppressed during Quiet Hours)"
                                    else "Next chime in $minutesToNextHour min (${String.format("%02d:00", nextHour)})"
                                } else {
                                    "Periodic Chimes Paused"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isQuiet || !isHourlyEnabled) MaterialTheme.colorScheme.outline else Cobalt60
                            )
                        }
                    }
                }
            }

            // Master Configuration & Volume Controls Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "CHIME PREFERENCES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Hourly Chime Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hourly Bell Chimes", fontWeight = FontWeight.Bold)
                                Text("Strikes melody + hour gong at :00", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = isHourlyEnabled,
                                onCheckedChange = { engine.setHourlyChimeEnabled(it) }
                            )
                        }

                        // Quarter-Hour Chime Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Quarter-Hour Melodies", fontWeight = FontWeight.Bold)
                                Text("Rings at :15, :30, :45, and :00", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Switch(
                                checked = isQuarterEnabled,
                                onCheckedChange = { engine.setQuarterChimeEnabled(it) }
                            )
                        }

                        // Volume Slider
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Chime Volume", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Text("${(chimeVolume * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Cobalt60)
                            }
                            Slider(
                                value = chimeVolume,
                                onValueChange = { engine.setChimeVolume(it) },
                                colors = SliderDefaults.colors(thumbColor = Cobalt60, activeTrackColor = Cobalt60)
                            )
                        }
                    }
                }
            }

            // Quiet Hours / Do-Not-Disturb Sleep Gate Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = null,
                                tint = Amber60,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Quiet Hours (Nighttime Sleep Gate)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Chimes are automatically silenced between $quietStart:00 and $quietEnd:00 to preserve uninterrupted sleep.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Quiet Start", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("$quietStart:00 PM", fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Quiet End", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("0$quietEnd:00 AM", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Melody Selector Cards Header
            item {
                Text(
                    text = "AUTHENTIC CHIME MELODIES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 4 Melody Selector Cards
            items(ChimeMelody.values()) { melody ->
                val isSelected = selectedMelody == melody
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Cobalt60.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { engine.setSelectedMelody(melody) }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = melody.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Cobalt60 else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Origin: ${melody.origin}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Cobalt60
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = melody.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Test Strike Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cobalt60.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { engine.playChime(melody, ChimeQuarter.FIRST_QUARTER, 1) }
                            ) {
                                Text(
                                    text = "Test 15m",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Cobalt60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cobalt60.copy(alpha = 0.15f),
                                modifier = Modifier.clickable { engine.playChime(melody, ChimeQuarter.HALF_HOUR, 1) }
                            ) {
                                Text(
                                    text = "Test 30m",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Cobalt60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Amber60.copy(alpha = 0.20f),
                                modifier = Modifier.clickable { engine.playChime(melody, ChimeQuarter.FULL_HOUR, 3) }
                            ) {
                                Text(
                                    text = "Full Hour + Strikes",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Amber60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Grandfather Clock Dial & Swinging Pendulum Canvas ─────────────────

@Composable
private fun GrandfatherPendulumCanvas(
    localTime: LocalTime,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pendulum")
    // Harmonic oscillation: 1 Hz oscillation (2 seconds per cycle)
    val pendulumAngleDeg by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pendulum_angle"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val dialCenterY = 56.dp.toPx()
        val dialRadius = 42.dp.toPx()

        // 1. Clock Dial Base
        drawCircle(
            color = Color(0xFFF8FAFC),
            radius = dialRadius,
            center = Offset(centerX, dialCenterY)
        )
        drawCircle(
            color = Color(0xFFB45309), // Antique Brass rim
            radius = dialRadius,
            center = Offset(centerX, dialCenterY),
            style = Stroke(width = 3.dp.toPx())
        )

        // 12-hour hour marks
        for (i in 0 until 12) {
            val angleRad = (i * 30 - 90) * (Math.PI / 180.0)
            val outer = Offset(
                (centerX + (dialRadius - 2.dp.toPx()) * cos(angleRad)).toFloat(),
                (dialCenterY + (dialRadius - 2.dp.toPx()) * sin(angleRad)).toFloat()
            )
            val inner = Offset(
                (centerX + (dialRadius - (if (i % 3 == 0) 6.dp.toPx() else 3.dp.toPx())) * cos(angleRad)).toFloat(),
                (dialCenterY + (dialRadius - (if (i % 3 == 0) 6.dp.toPx() else 3.dp.toPx())) * sin(angleRad)).toFloat()
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = inner,
                end = outer,
                strokeWidth = if (i % 3 == 0) 2.dp.toPx() else 1.dp.toPx()
            )
        }

        // Clock Hands
        val secAngle = localTime.second * 6f - 90f
        val minAngle = (localTime.minute + localTime.second / 60f) * 6f - 90f
        val hrAngle = ((localTime.hour % 12) + localTime.minute / 60f) * 30f - 90f

        // Hour Hand
        val hrRad = hrAngle * (Math.PI / 180.0)
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(centerX, dialCenterY),
            end = Offset((centerX + dialRadius * 0.5f * cos(hrRad)).toFloat(), (dialCenterY + dialRadius * 0.5f * sin(hrRad)).toFloat()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minRad = minAngle * (Math.PI / 180.0)
        drawLine(
            color = Color(0xFF0F172A),
            start = Offset(centerX, dialCenterY),
            end = Offset((centerX + dialRadius * 0.75f * cos(minRad)).toFloat(), (dialCenterY + dialRadius * 0.75f * sin(minRad)).toFloat()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second Hand
        val secRad = secAngle * (Math.PI / 180.0)
        drawLine(
            color = Amber60,
            start = Offset(centerX, dialCenterY),
            end = Offset((centerX + dialRadius * 0.85f * cos(secRad)).toFloat(), (dialCenterY + dialRadius * 0.85f * sin(secRad)).toFloat()),
            strokeWidth = 1.dp.toPx()
        )

        // Center Pin
        drawCircle(color = Amber60, radius = 3.dp.toPx(), center = Offset(centerX, dialCenterY))

        // 2. Swinging Pendulum
        val pivotY = dialCenterY + dialRadius + 4.dp.toPx()
        val pendulumLength = 95.dp.toPx()
        val pendRad = (pendulumAngleDeg + 90f) * (Math.PI / 180.0)

        val bobCenter = Offset(
            (centerX + pendulumLength * cos(pendRad)).toFloat(),
            (pivotY + pendulumLength * sin(pendRad)).toFloat()
        )

        // Pendulum Rod (Polished Brass)
        drawLine(
            color = Color(0xFFD97706),
            start = Offset(centerX, pivotY),
            end = bobCenter,
            strokeWidth = 2.5.dp.toPx()
        )

        // Pendulum Bob (Heavy Brass Disc)
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = 16.dp.toPx(),
            center = bobCenter
        )
        drawCircle(
            color = Color(0xFFB45309),
            radius = 16.dp.toPx(),
            center = bobCenter,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFFEF3C7),
            radius = 6.dp.toPx(),
            center = bobCenter
        )
    }
}
