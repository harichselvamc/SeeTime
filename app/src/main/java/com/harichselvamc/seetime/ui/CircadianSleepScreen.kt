package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.CircadianCalculator
import com.harichselvamc.seetime.util.SleepCycleRecommendation
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class NapPreset(
    val title: String,
    val minutes: Int,
    val icon: ImageVector,
    val description: String,
    val color: Color
)

private val NAP_PRESETS = listOf(
    NapPreset("Power Nap", 20, Icons.Filled.Timer, "Boosts alertness & focus without grogginess (stage 2 NREM)", Cobalt60),
    NapPreset("Coffee Nap", 25, Icons.Filled.Coffee, "Drink caffeine, nap 20m, wake refreshed as caffeine peaks", Amber60),
    NapPreset("Full Cycle", 90, Icons.Filled.Bedtime, "Complete REM + deep sleep cycle, optimizes creativity & recovery", Cyan60)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircadianSleepScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedModeTab by remember { mutableIntStateOf(0) } // 0: Sleep Calculator, 1: Solar Cycles, 2: Nap Presets, 3: Jet Lag Plan

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }

    val primaryZone = uiState.pairs.firstOrNull()?.toZone ?: "Asia/Tokyo"
    val coords = remember(primaryZone) { CircadianCalculator.getCoordinatesForZone(primaryZone) }
    val solarTimes = remember(coords) {
        CircadianCalculator.calculateSolarTimes(
            latitude = coords.first,
            longitude = coords.second,
            date = LocalDate.now(),
            zoneId = try { ZoneId.of(primaryZone) } catch (_: Exception) { ZoneId.systemDefault() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bedtime,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Circadian & Solar Sleep",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mode Tab Selector
            TabRow(
                selectedTabIndex = selectedModeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedModeTab == 0,
                    onClick = { selectedModeTab = 0 },
                    text = { Text("Sleep", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedModeTab == 1,
                    onClick = { selectedModeTab = 1 },
                    text = { Text("Sunlight", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedModeTab == 2,
                    onClick = { selectedModeTab = 2 },
                    text = { Text("Naps", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedModeTab == 3,
                    onClick = { selectedModeTab = 3 },
                    text = { Text("Jet Lag", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedModeTab == 4,
                    onClick = { selectedModeTab = 4 },
                    text = { Text("Chrono-Meals", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedModeTab == 4) {
                JetLagRecoveryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedModeTab) {
                    0 -> {
                        // Section 1: Sleep Cycles Calculator (Sleep Now vs Wake Target)
                        item {
                            SleepCyclesContent(
                                timeFormatter = timeFormatter,
                                onSetAlarm = { /* Optional alarm quick-schedule hook */ }
                            )
                        }
                    }
                    1 -> {
                        // Section 2: Solar Sun & Twilight Curve
                        item {
                            SolarCurveCard(
                                solarTimes = solarTimes,
                                zoneName = primaryZone.substringAfterLast('/').replace('_', ' '),
                                timeFormatter = timeFormatter
                            )
                        }
                    }
                    2 -> {
                        // Section 3: Nap Timers
                        item {
                            NapPresetsContent(timeFormatter = timeFormatter)
                        }
                    }
                    3 -> {
                        // Section 4: Jet Lag Pre-Adaptation Plan
                        item {
                            JetLagPlanContent(
                                originZone = uiState.pairs.firstOrNull()?.fromZone ?: "Asia/Kolkata",
                                destinationZone = uiState.pairs.firstOrNull()?.toZone ?: "Europe/London",
                                timeFormatter = timeFormatter
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
private fun SleepCyclesContent(
    timeFormatter: DateTimeFormatter,
    onSetAlarm: (LocalTime) -> Unit
) {
    var sleepNowMode by remember { mutableStateOf(true) }
    val now = remember { LocalTime.now() }
    val wakeTimes = remember(now) { CircadianCalculator.calculateWakeTimes(now) }
    val caffeineCutoff = remember(now) { CircadianCalculator.calculateCaffeineCutoff(now.plusHours(8)) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Mode Switch (If I go to sleep now vs If I want to wake up at)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "If you go to bed right now:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Includes 15 min average sleep latency across natural 90m ultradian cycles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "OPTIMAL WAKE-UP TIMES",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        wakeTimes.forEach { rec ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (rec.isRecommended) Cobalt60.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (rec.isRecommended) 2.dp else 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rec.targetTime.format(timeFormatter),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (rec.isRecommended) Cobalt60 else MaterialTheme.colorScheme.onSurface
                            )
                            if (rec.isRecommended) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Cobalt60
                                ) {
                                    Text(
                                        text = "SUGGESTED",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Alarm,
                        contentDescription = "Alarm Icon",
                        tint = if (rec.isRecommended) Cobalt60 else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Caffeine Cutoff Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Amber60.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Coffee,
                    contentDescription = null,
                    tint = Amber60,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Caffeine Curfew",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Amber60
                    )
                    Text(
                        text = "Stop caffeine 9 hours prior to target bedtime (${caffeineCutoff.format(timeFormatter)}) for deep restorative delta sleep.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SolarCurveCard(
    solarTimes: com.harichselvamc.seetime.util.SolarTimes,
    zoneName: String,
    timeFormatter: DateTimeFormatter
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Solar Sun Elevation & Twilight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Location: $zoneName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Amber60,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Drawing Solar Sinusoid Arc
            SolarCurveCanvas(
                solarTimes = solarTimes,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sunlight KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SolarTimePill("Dawn", solarTimes.dawnTwilight.format(timeFormatter), Amber60)
                SolarTimePill("Sunrise", solarTimes.sunrise.format(timeFormatter), Amber60)
                SolarTimePill("Noon", solarTimes.solarNoon.format(timeFormatter), Cyan60)
                SolarTimePill("Sunset", solarTimes.sunset.format(timeFormatter), Amber60)
                SolarTimePill("Dusk", solarTimes.duskTwilight.format(timeFormatter), Cobalt60)
            }

            Spacer(modifier = Modifier.height(12.dp))

            val daylightHrs = solarTimes.daylightMinutes / 60
            val daylightMins = solarTimes.daylightMinutes % 60
            Text(
                text = "Total Natural Sunlight Today: ${daylightHrs}h ${daylightMins}m",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Cobalt60
            )
        }
    }
}

@Composable
private fun SolarTimePill(label: String, timeStr: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = timeStr, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SolarCurveCanvas(
    solarTimes: com.harichselvamc.seetime.util.SolarTimes,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val horizonY = h * 0.7f

        // Horizon Line
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, horizonY),
            end = Offset(w, horizonY),
            strokeWidth = 2.dp.toPx()
        )

        // Arc Path (Sinusoidal approximation of sun arc)
        val path = Path()
        path.moveTo(0f, horizonY)

        val sunriseX = (solarTimes.sunrise.toSecondOfDay() / 86400f) * w
        val noonX = (solarTimes.solarNoon.toSecondOfDay() / 86400f) * w
        val sunsetX = (solarTimes.sunset.toSecondOfDay() / 86400f) * w

        path.cubicTo(
            sunriseX, horizonY,
            noonX, 15.dp.toPx(),
            sunsetX, horizonY
        )

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(Amber60, Cyan60.copy(alpha = 0.6f))
            ),
            style = Stroke(width = 4.dp.toPx())
        )

        // Sun Marker at Solar Noon
        drawCircle(
            color = Amber60,
            radius = 6.dp.toPx(),
            center = Offset(noonX, 15.dp.toPx())
        )
    }
}

@Composable
private fun NapPresetsContent(timeFormatter: DateTimeFormatter) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "SCIENTIFIC NAP PRESETS",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        NAP_PRESETS.forEach { preset ->
            val wakeTime = remember { LocalTime.now().plusMinutes(preset.minutes.toLong()) }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = preset.color.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = preset.icon,
                                contentDescription = null,
                                tint = preset.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${preset.minutes} min",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = preset.color
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = preset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wake up at: ${wakeTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Cobalt60
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JetLagPlanContent(
    originZone: String,
    destinationZone: String,
    timeFormatter: DateTimeFormatter
) {
    val plans = remember(originZone, destinationZone) {
        CircadianCalculator.calculateJetLagPreAdaptation(originZone, destinationZone)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Cobalt60.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4-Day Pre-Flight Jet Lag Protocol",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Cobalt60
                )
                Text(
                    text = "Gradually shift your circadian rhythm prior to departure to minimize fatigue on arrival.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        plans.forEach { plan ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Day ${plan.dayNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Bed: ${plan.recommendedBedtime.format(timeFormatter)} · Wake: ${plan.recommendedWakeTime.format(timeFormatter)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = plan.lightExposureWindow,
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = plan.lightAvoidanceWindow,
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber60,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
