package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.HistoricalLeapSecond
import com.harichselvamc.seetime.util.LeapSecondEngine
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeapSecondScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    var utcNow by remember { mutableStateOf(ZonedDateTime.now(ZoneOffset.UTC)) }

    // Live clock ticker loop (100 ms)
    LaunchedEffect(Unit) {
        while (true) {
            utcNow = ZonedDateTime.now(ZoneOffset.UTC)
            kotlinx.coroutines.delay(100L)
        }
    }

    val multiTime = remember(utcNow) {
        LeapSecondEngine.calculateMultiStandardTime(utcNow)
    }

    // 23:59:60 Simulator State
    var simStep by remember { mutableIntStateOf(0) }
    var isSimPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isSimPlaying) {
        while (isSimPlaying) {
            kotlinx.coroutines.delay(1000L)
            simStep = (simStep + 1) % 5
        }
    }

    val currentSimFrame = remember(simStep) {
        LeapSecondEngine.getLeapSecondSimulationFrame(simStep)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.MoreTime,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Leap Second & UT1 Tracker",
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
                            text = "TAI = UTC + 37s",
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
            // 4-Way Multi-Standard Clock Divergence Hero Card
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
                                text = "MULTI-STANDARD TIME DIVERGENCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Amber60
                            )
                            Text(
                                text = "IERS Reference",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Standards Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StandardTimePill("UTC (Civil Standard)", multiTime.utcFormatted, Color.White, Modifier.weight(1f))
                            StandardTimePill("TAI (Atomic Standard)", multiTime.taiFormatted, Cyan60, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StandardTimePill("GPS (Navigation Time)", multiTime.gpsFormatted, Amber60, Modifier.weight(1f))
                            StandardTimePill("UT1 (Earth Rotation)", multiTime.ut1Formatted, SuccessGreen, Modifier.weight(1f))
                        }
                    }
                }
            }

            // 23:59:60 Leap Second Insertion Interactive Simulator Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "23:59:60 Leap Second Simulation",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (simStep == 2) Color(0xFFEF4444).copy(alpha = 0.2f) else Cobalt60.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (simStep == 2) "LEAP SECOND INSERTED" else "FRAME $simStep/4",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = if (simStep == 2) Color(0xFFEF4444) else Cobalt60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Big Simulated Clock Display
                        Text(
                            text = currentSimFrame.first,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (simStep == 2) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 2.sp
                        )

                        Text(
                            text = currentSimFrame.second,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (simStep == 2) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { simStep = (simStep + 1) % 5 },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Step Next")
                            }

                            Button(
                                onClick = { isSimPlaying = !isSimPlaying },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSimPlaying) Amber60 else Cobalt60
                                )
                            ) {
                                Text(if (isSimPlaying) "Pause Animation" else "Play Animation", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2035 BIPM / CGPM Leap Second Phase-Out Resolution Card
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
                            Icon(Icons.Filled.Public, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("BIPM 2035 Leap Second Phase-Out", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "At the 27th General Conference on Weights and Measures (CGPM 2022), international delegates resolved to cease inserting leap seconds by 2035. This allows the difference between astronomical solar time (UT1) and civil time (UTC) to drift up to ~1 minute over roughly a century, avoiding severe distributed server crashes caused by the 23:59:60 anomaly.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Historical 27 Leap Seconds Archive
            item {
                Text(
                    text = "HISTORICAL LEAP SECONDS ARCHIVE (${LeapSecondEngine.HISTORICAL_LEAP_SECONDS.size} INSERTIONS)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(LeapSecondEngine.HISTORICAL_LEAP_SECONDS.reversed()) { leap ->
                HistoricalLeapRowCard(leap = leap)
            }
        }
    }
}

@Composable
private fun StandardTimePill(
    label: String,
    timeValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeValue,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun HistoricalLeapRowCard(leap: HistoricalLeapSecond) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = leap.dateStr,
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = leap.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Amber60.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "TAI-UTC: +${leap.taiUtcOffsetSeconds}s",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Amber60,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
