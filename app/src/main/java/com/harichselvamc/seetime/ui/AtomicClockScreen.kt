package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.AtomicClockEngine
import com.harichselvamc.seetime.util.NtpSyncResult
import java.time.ZonedDateTime
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtomicClockScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    var liveSecond by remember { mutableIntStateOf(0) }
    var liveMillis by remember { mutableIntStateOf(0) }

    // Live atomic oscillation ticker loop (50 ms)
    LaunchedEffect(Unit) {
        while (true) {
            val now = ZonedDateTime.now()
            liveSecond = now.second
            liveMillis = now.nano / 1_000_000
            kotlinx.coroutines.delay(50L)
        }
    }

    val currentOscillations = remember(liveSecond, liveMillis) {
        AtomicClockEngine.calculateCesiumOscillations(liveSecond, liveMillis)
    }

    // Quartz thermal slider state
    var quartzTempC by remember { mutableFloatStateOf(25.0f) }
    val quartzResult = remember(quartzTempC) {
        AtomicClockEngine.calculateQuartzThermalDrift(quartzTempC)
    }

    // NTP Server Sync Simulation State
    var selectedServerIndex by remember { mutableIntStateOf(0) }
    val selectedServer = AtomicClockEngine.NTP_SERVERS[selectedServerIndex % AtomicClockEngine.NTP_SERVERS.size]
    var ntpResult by remember { mutableStateOf(AtomicClockEngine.simulateNtpSync(selectedServer.first, selectedServer.second)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.WifiTethering,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cesium Atomic & NTP Sync",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "STRATUM 1 ATOMIC",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = SuccessGreen,
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
            // Cesium-133 Quantum Microwave Resonance Cavity Canvas Card
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
                                text = "CESIUM-133 QUANTUM RESONANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Text(
                                text = "9.192631770 GHz",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                fontWeight = FontWeight.Bold,
                                color = Amber60
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quantum Microwave Resonance Cavity Canvas
                        CesiumCavityCanvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Quantum Oscillation Counter
                        Text(
                            text = String.format(Locale.US, "%,d", currentOscillations),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Quantum Hyperfine Cycles This Minute",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "SI Definition: 1 Second = Exactly 9,192,631,770 ground-state Cesium-133 transitions.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cyan60
                        )
                    }
                }
            }

            // NTP Network Time Protocol Synchronization Diagnostic Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Radio, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("NTP Network Synchronization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = ntpResult.statusLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = SuccessGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Server Selector Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AtomicClockEngine.NTP_SERVERS.indices.toList()) { idx ->
                                val (serverName, _) = AtomicClockEngine.NTP_SERVERS[idx]
                                val isSelected = selectedServerIndex == idx
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedServerIndex = idx
                                        ntpResult = AtomicClockEngine.simulateNtpSync(serverName, AtomicClockEngine.NTP_SERVERS[idx].second)
                                    },
                                    label = { Text(serverName.substringBefore(" "), fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SuccessGreen.copy(alpha = 0.2f), selectedLabelColor = SuccessGreen)
                                )
                            }
                        }

                        // NTP Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Round-Trip Delay (δ)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
                                    Text("${String.format(Locale.US, "%.2f", ntpResult.roundTripDelayMs)} ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cobalt60)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Clock Offset (θ)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
                                    Text("${String.format(Locale.US, "%+.3f", ntpResult.clockOffsetMs)} ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Jitter", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
                                    Text("±${String.format(Locale.US, "%.2f", ntpResult.jitterMs)} ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Amber60)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                ntpResult = AtomicClockEngine.simulateNtpSync(selectedServer.first, selectedServer.second)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Re-Synchronize with ${selectedServer.first.substringBefore(" ")}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Hardware Quartz Crystal Thermal Drift Calibration Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Thermostat, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hardware Quartz Crystal Thermal Drift", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        // Temperature Slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Operating Temperature: ${quartzTempC.toInt()}°C", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (quartzResult.isOptimalTemp) "Optimal (Room Temp)" else "Thermal Drift Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quartzResult.isOptimalTemp) SuccessGreen else Amber60
                                )
                            }
                            Slider(
                                value = quartzTempC,
                                onValueChange = { quartzTempC = it },
                                valueRange = -10f..55f,
                                colors = SliderDefaults.colors(thumbColor = Amber60, activeTrackColor = Amber60)
                            )
                        }

                        // Drift Results
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Frequency Drift", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("${String.format(Locale.US, "%.1f", quartzResult.ppmDrift)} PPM", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (quartzResult.isOptimalTemp) SuccessGreen else Color(0xFFEF4444))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Clock Loss / Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("${String.format(Locale.US, "%.2f", quartzResult.dailyDriftSeconds)} s/day", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (quartzResult.isOptimalTemp) SuccessGreen else Color(0xFFEF4444))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Clock Loss / Month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("${String.format(Locale.US, "%.1f", quartzResult.monthlyDriftSeconds)} s/mo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Amber60)
                                }
                            }
                        }
                    }
                }
            }

            // Metrology Comparison Hierarchy Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Precision Timekeeping Hierarchy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("• Mechanical Watch: 4 Hz (±5 seconds/day)", style = MaterialTheme.typography.bodySmall)
                        Text("• Quartz Wristwatch: 32,768 Hz (±15 seconds/month)", style = MaterialTheme.typography.bodySmall)
                        Text("• Cesium-133 Atomic Standard: 9,192,631,770 Hz (1 second drift per 30 million years!)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Cyan60)
                        Text("• Strontium Optical Lattice: 429 THz (1 second drift per 15 billion years)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun CesiumCavityCanvas(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "quantum_flow")
    val atomOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "atoms"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2

        // Vacuum Chamber Tube
        drawRect(color = Color(0xFF0F172A))
        drawLine(color = Color.LightGray.copy(alpha = 0.3f), start = Offset(10.dp.toPx(), 10.dp.toPx()), end = Offset(w - 10.dp.toPx(), 10.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(color = Color.LightGray.copy(alpha = 0.3f), start = Offset(10.dp.toPx(), h - 10.dp.toPx()), end = Offset(w - 10.dp.toPx(), h - 10.dp.toPx()), strokeWidth = 2.dp.toPx())

        // Microwave Resonance Field (Center Cavity)
        val cavityStartX = w * 0.35f
        val cavityEndX = w * 0.65f
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Cyan60.copy(alpha = 0.25f), Color.Transparent),
                startX = cavityStartX,
                endX = cavityEndX
            ),
            topLeft = Offset(cavityStartX, 10.dp.toPx()),
            size = Size(cavityEndX - cavityStartX, h - 20.dp.toPx())
        )

        // 9.192 GHz Microwave Sine Wave
        val wavePath = Path()
        wavePath.moveTo(cavityStartX, midY)
        for (x in cavityStartX.toInt()..cavityEndX.toInt() step 3) {
            val prog = (x - cavityStartX) / (cavityEndX - cavityStartX)
            val y = midY + (8.dp.toPx() * sin((prog * 8 * Math.PI + atomOffset * 2 * Math.PI))).toFloat()
            wavePath.lineTo(x.toFloat(), y)
        }
        drawPath(path = wavePath, color = Amber60, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

        // Moving Cesium Atoms Beam
        for (i in 0..7) {
            val baseProg = (i / 8f) + atomOffset
            val atomX = (baseProg % 1f) * (w - 40.dp.toPx()) + 20.dp.toPx()
            val inCavity = atomX in cavityStartX..cavityEndX
            val atomColor = if (inCavity) Amber60 else Cyan60

            drawCircle(color = atomColor.copy(alpha = 0.4f), radius = 5.dp.toPx(), center = Offset(atomX, midY))
            drawCircle(color = atomColor, radius = 2.5.dp.toPx(), center = Offset(atomX, midY))
        }
    }
}
