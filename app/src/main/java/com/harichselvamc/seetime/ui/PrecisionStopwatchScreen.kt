package com.harichselvamc.seetime.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.LapTelemetry
import com.harichselvamc.seetime.util.PrecisionStopwatchEngine
import com.harichselvamc.seetime.util.StopwatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrecisionStopwatchScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var stopwatchState by remember { mutableStateOf(StopwatchState.STOPPED) }
    var startTimeNano by remember { mutableLongStateOf(0L) }
    var accumulatedTimeMs by remember { mutableLongStateOf(0L) }
    var currentElapsedMs by remember { mutableLongStateOf(0L) }

    val rawLapSplits = remember { mutableStateListOf<Long>() }

    // High frequency chrono ticker loop (16 ms / ~60 FPS)
    LaunchedEffect(stopwatchState) {
        if (stopwatchState == StopwatchState.RUNNING) {
            val start = System.nanoTime()
            startTimeNano = start
            val baseAccum = accumulatedTimeMs
            while (stopwatchState == StopwatchState.RUNNING) {
                val nowNano = System.nanoTime()
                val deltaMs = (nowNano - start) / 1_000_000L
                currentElapsedMs = baseAccum + deltaMs
                kotlinx.coroutines.delay(16L)
            }
        }
    }

    val telemetry = remember(rawLapSplits.toList(), currentElapsedMs) {
        PrecisionStopwatchEngine.evaluateLapTelemetry(rawLapSplits.toList())
    }

    val activeLapStartMs = rawLapSplits.lastOrNull() ?: 0L
    val activeLapDurationMs = (currentElapsedMs - activeLapStartMs).coerceAtLeast(0L)

    fun handleStart() {
        stopwatchState = StopwatchState.RUNNING
    }

    fun handlePause() {
        accumulatedTimeMs = currentElapsedMs
        stopwatchState = StopwatchState.PAUSED
    }

    fun handleLap() {
        if (stopwatchState == StopwatchState.RUNNING && currentElapsedMs > 0L) {
            rawLapSplits.add(currentElapsedMs)
        }
    }

    fun handleReset() {
        stopwatchState = StopwatchState.STOPPED
        accumulatedTimeMs = 0L
        currentElapsedMs = 0L
        rawLapSplits.clear()
    }

    fun exportCsv() {
        val fullSplits = if (currentElapsedMs > activeLapStartMs && stopwatchState != StopwatchState.STOPPED) {
            rawLapSplits.toList() + currentElapsedMs
        } else {
            rawLapSplits.toList()
        }
        val fullTelemetry = PrecisionStopwatchEngine.evaluateLapTelemetry(fullSplits)
        val csv = PrecisionStopwatchEngine.generateLapCsv(fullTelemetry)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Stopwatch Lap CSV", csv)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied ${fullTelemetry.lapsCount} Laps CSV to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "High-Precision Chrono & Laps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    if (rawLapSplits.isNotEmpty() || currentElapsedMs > 0L) {
                        IconButton(onClick = { exportCsv() }) {
                            Icon(Icons.Filled.FileDownload, contentDescription = "Export CSV", tint = Cyan60)
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
            // Main Digital LED Chronometer Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DIGITAL CHRONO HUD",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (stopwatchState) {
                                    StopwatchState.RUNNING -> SuccessGreen.copy(alpha = 0.2f)
                                    StopwatchState.PAUSED -> Amber60.copy(alpha = 0.2f)
                                    StopwatchState.STOPPED -> Color.Gray.copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = stopwatchState.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = when (stopwatchState) {
                                        StopwatchState.RUNNING -> SuccessGreen
                                        StopwatchState.PAUSED -> Amber60
                                        StopwatchState.STOPPED -> Color.LightGray
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Monospace Main Elapsed Time
                        Text(
                            text = PrecisionStopwatchEngine.formatElapsed(currentElapsedMs),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        // Current Lap Timer
                        if (stopwatchState != StopwatchState.STOPPED) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Lap: ${PrecisionStopwatchEngine.formatElapsed(activeLapDurationMs)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                fontWeight = FontWeight.SemiBold,
                                color = Cyan60
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Interactive Control Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (stopwatchState) {
                                StopwatchState.STOPPED -> {
                                    Button(
                                        onClick = { handleStart() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.fillMaxWidth(0.7f)
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Start Chrono", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                                StopwatchState.RUNNING -> {
                                    OutlinedButton(
                                        onClick = { handleLap() },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Flag, contentDescription = null, tint = Cyan60, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Lap / Split", fontWeight = FontWeight.Bold, color = Cyan60)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Button(
                                        onClick = { handlePause() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Amber60),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pause", fontWeight = FontWeight.Bold)
                                    }
                                }
                                StopwatchState.PAUSED -> {
                                    OutlinedButton(
                                        onClick = { handleReset() },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Replay, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reset", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Button(
                                        onClick = { handleStart() },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Resume", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Lap Statistics Telemetry Summary Card
            if (telemetry.lapsCount >= 2) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FASTEST LAP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = SuccessGreen)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = PrecisionStopwatchEngine.formatElapsed(telemetry.fastestLapMs ?: 0L),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("AVERAGE PACE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Cobalt60)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = PrecisionStopwatchEngine.formatElapsed(telemetry.averageLapMs),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = Cobalt60
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SLOWEST LAP", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFFEF4444))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = PrecisionStopwatchEngine.formatElapsed(telemetry.slowestLapMs ?: 0L),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            // Recorded Laps Section Title
            if (telemetry.laps.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECORDED LAPS (${telemetry.laps.size})",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { exportCsv() }
                        ) {
                            Text(
                                text = "Export CSV",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Cobalt60,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                items(telemetry.laps) { lap ->
                    LapRowCard(lap = lap)
                }
            }
        }
    }
}

@Composable
private fun LapRowCard(lap: LapTelemetry) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                lap.isFastest -> SuccessGreen.copy(alpha = 0.12f)
                lap.isSlowest -> Color(0xFFEF4444).copy(alpha = 0.10f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        lap.isFastest -> SuccessGreen.copy(alpha = 0.2f)
                        lap.isSlowest -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${lap.lapNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when {
                                lap.isFastest -> SuccessGreen
                                lap.isSlowest -> Color(0xFFEF4444)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = lap.formattedLapTime,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Split: ${lap.formattedSplitTime}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when {
                    lap.isFastest -> SuccessGreen
                    lap.isSlowest -> Color(0xFFEF4444)
                    lap.deltaBadgeText.startsWith("-") -> SuccessGreen.copy(alpha = 0.15f)
                    lap.deltaBadgeText.startsWith("+") -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Text(
                    text = if (lap.isFastest) "FASTEST ⚡" else if (lap.isSlowest) "SLOWEST" else lap.deltaBadgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    ),
                    color = if (lap.isFastest || lap.isSlowest) Color.White else if (lap.deltaBadgeText.startsWith("-")) SuccessGreen else Color(0xFFEF4444),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
