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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Surfing
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.NauticalWatchEngine
import com.harichselvamc.seetime.util.NauticalWatchType
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NauticalBellScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var nowTime by remember { mutableStateOf(LocalTime.now()) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            nowTime = LocalTime.now()
            kotlinx.coroutines.delay(1000L)
        }
    }

    val watchStatus = remember(nowTime) {
        NauticalWatchEngine.calculateWatchStatus(nowTime)
    }

    val timetable = remember { NauticalWatchEngine.generate24HourTimetable() }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm:ss") else DateTimeFormatter.ofPattern("hh:mm:ss a")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Anchor,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nautical Bell & Ship Chronometer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Amber60.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "${watchStatus.bellCount} BELLS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = Amber60,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            // Brass Marine Chronometer Dial Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                text = "BRASS SHIP CHRONOMETER",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Amber60
                            )
                            Text(
                                text = watchStatus.watchType.timeSpan,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Brass Marine Chronometer Canvas
                        BrassChronometerCanvas(
                            localTime = nowTime,
                            bellCount = watchStatus.bellCount,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = watchStatus.watchName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "${watchStatus.bellCount} Bells: ${watchStatus.bellStrikesNotation}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Amber60
                        )

                        Text(
                            text = "\"${watchStatus.bellPhoneticText}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive Sound Button
                        Button(
                            onClick = {
                                NauticalWatchEngine.playShipBellAcoustic(watchStatus.bellCount)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber60),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Strike ${watchStatus.bellCount} Ship's Bells", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Watch Status & Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WATCHKEEPING DUTY STATUS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cobalt60
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cobalt60.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "NEXT BELL: ${watchStatus.nextBellTimeFormatted}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = Cobalt60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "${watchStatus.minutesRemainingInWatch} Minutes Remaining in Watch",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = watchStatus.watchType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Maritime Watch Lore Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DirectionsBoat, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("The Dog Watch Tradition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "To prevent the same watch team from always standing duty during the middle of the night (the 'graveyard watch'), the 16:00 to 20:00 period is split into two 2-hour 'Dog Watches' (First Dog 16:00-18:00 and Last Dog 18:00-20:00). This odd number (7 watches in 24 hours) automatically rotates the daily watch schedule!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 24-Hour 7-Watch Master Timetable Section
            item {
                Text(
                    text = "24-HOUR MARITIME WATCH TIMETABLE (7 WATCHES)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(NauticalWatchType.values()) { watchType ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (watchStatus.watchType == watchType) Amber60.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = watchType.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = watchType.timeSpan,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Cobalt60,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = watchType.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (watchStatus.watchType == watchType) Amber60 else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (watchStatus.watchType == watchType) "ON DUTY" else "8 BELLS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (watchStatus.watchType == watchType) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrassChronometerCanvas(
    localTime: LocalTime,
    bellCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bell_swing")
    val bellAngle by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swing"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val outerRadius = (w / 2) * 0.92f
        val dialRadius = outerRadius * 0.82f

        // Gimbal Brass Outer Ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFDE047), Amber60, Color(0xFF78350F)),
                center = center,
                radius = outerRadius
            ),
            radius = outerRadius,
            center = center,
            style = Stroke(width = (outerRadius - dialRadius))
        )

        // White Enamel Dial Base
        drawCircle(color = Color(0xFFF8FAFC), radius = dialRadius, center = center)

        // Hour Ticks
        for (i in 0 until 12) {
            val angleDeg = (i * 30f) - 90f
            val rad = Math.toRadians(angleDeg.toDouble())
            val start = Offset((center.x + dialRadius * 0.85f * cos(rad)).toFloat(), (center.y + dialRadius * 0.85f * sin(rad)).toFloat())
            val end = Offset((center.x + dialRadius * 0.95f * cos(rad)).toFloat(), (center.y + dialRadius * 0.95f * sin(rad)).toFloat())
            drawLine(color = Color(0xFF0F172A), start = start, end = end, strokeWidth = 2.5.dp.toPx())
        }

        // Hour & Minute Hands (Blued Steel)
        val hourDeg = ((localTime.hour % 12) + localTime.minute / 60f) * 30f
        val minDeg = (localTime.minute + localTime.second / 60f) * 6f

        // Hour Hand
        rotate(hourDeg, pivot = center) {
            drawLine(color = Color(0xFF1E3A8A), start = center, end = Offset(center.x, center.y - dialRadius * 0.55f), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
        }

        // Minute Hand
        rotate(minDeg, pivot = center) {
            drawLine(color = Color(0xFF1E3A8A), start = center, end = Offset(center.x, center.y - dialRadius * 0.80f), strokeWidth = 2.5.dp.toPx(), cap = StrokeCap.Round)
        }

        // Center Pin
        drawCircle(color = Amber60, radius = 4.dp.toPx(), center = center)
    }
}
