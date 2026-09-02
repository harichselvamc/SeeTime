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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Timer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.CircadianCalculator
import com.harichselvamc.seetime.util.SolarPhaseWindow
import com.harichselvamc.seetime.util.SolarPhotographyEngine
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class PhotoLocation(
    val title: String,
    val zoneId: String,
    val latitude: Double,
    val longitude: Double
)

private val PHOTO_LOCATIONS = listOf(
    PhotoLocation("Tokyo 🗼", "Asia/Tokyo", 35.6762, 139.6503),
    PhotoLocation("London 🎡", "Europe/London", 51.5074, -0.1278),
    PhotoLocation("New York 🗽", "America/New_York", 40.7128, -74.0060),
    PhotoLocation("Paris 🥐", "Europe/Paris", 48.8566, 2.3522),
    PhotoLocation("Sydney ⛵", "Australia/Sydney", -33.8688, 151.2093),
    PhotoLocation("Kolkata 🛕", "Asia/Kolkata", 22.5726, 88.3639)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldenHourPlannerScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedLocationIndex by remember { mutableIntStateOf(0) }
    val location = PHOTO_LOCATIONS[selectedLocationIndex % PHOTO_LOCATIONS.size]

    val targetZone = remember(location) {
        try { ZoneId.of(location.zoneId) } catch (_: Exception) { ZoneId.systemDefault() }
    }
    val nowInZone = remember(targetZone) { ZonedDateTime.now(targetZone) }
    val today = remember(nowInZone) { nowInZone.toLocalDate() }

    val schedule = remember(location, today, targetZone) {
        SolarPhotographyEngine.calculateDailySchedule(
            latitude = location.latitude,
            longitude = location.longitude,
            date = today,
            zoneId = targetZone
        )
    }

    val sunPosition = remember(location, nowInZone) {
        SolarPhotographyEngine.calculateSunPosition(
            latitude = location.latitude,
            longitude = location.longitude,
            dateTime = nowInZone
        )
    }

    val upcomingWindow = remember(schedule, nowInZone) {
        SolarPhotographyEngine.findNextUpcomingWindow(
            schedule = schedule,
            currentTime = nowInZone.toLocalTime()
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
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Golden Hour Planner",
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location Chips
            item {
                Text(
                    text = "TARGET PHOTOGRAPHY LOCATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PHOTO_LOCATIONS.indices.toList()) { idx ->
                        val itemLoc = PHOTO_LOCATIONS[idx]
                        val isSelected = selectedLocationIndex == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedLocationIndex = idx },
                            label = { Text(itemLoc.title, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber60.copy(alpha = 0.15f),
                                selectedLabelColor = Amber60
                            )
                        )
                    }
                }
            }

            // Live Countdown Hero Card
            item {
                val window = upcomingWindow.first
                val minutesDiff = upcomingWindow.second
                val isActiveNow = minutesDiff < 0

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActiveNow) SuccessGreen.copy(alpha = 0.15f) else Amber60.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isActiveNow) SuccessGreen else Amber60
                            ) {
                                Text(
                                    text = if (isActiveNow) "ACTIVE RIGHT NOW" else "UPCOMING WINDOW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = if (isActiveNow) SuccessGreen else Amber60,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = nowInZone.format(timeFormatter),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = window.phaseName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isActiveNow) SuccessGreen else Amber60
                        )

                        Text(
                            text = if (isActiveNow) {
                                "Ends in ${-minutesDiff} minutes (at ${window.endTime.format(timeFormatter)})"
                            } else {
                                val hrs = minutesDiff / 60
                                val mins = minutesDiff % 60
                                "Starts in ${if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"} (at ${window.startTime.format(timeFormatter)})"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = window.photoRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Azimuth Horizon Sun Compass Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            Column {
                                Text(
                                    text = "Azimuth Horizon Compass",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sun Heading: ${String.format(Locale.getDefault(), "%.1f°", sunPosition.azimuthDegrees)} · Elevation: ${String.format(Locale.getDefault(), "%.1f°", sunPosition.elevationDegrees)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.Explore,
                                contentDescription = null,
                                tint = Cobalt60,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sun Horizon Compass Canvas
                        SunHorizonCompassCanvas(
                            azimuthDeg = sunPosition.azimuthDegrees.toFloat(),
                            elevationDeg = sunPosition.elevationDegrees.toFloat(),
                            isAboveHorizon = sunPosition.isAboveHorizon,
                            modifier = Modifier
                                .size(220.dp)
                                .padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CompassKpiPill("Sunrise", schedule.sunriseTime.format(timeFormatter), Amber60)
                            CompassKpiPill("Solar Noon", schedule.solarNoonTime.format(timeFormatter), Cyan60)
                            CompassKpiPill("Sunset", schedule.sunsetTime.format(timeFormatter), Amber60)
                        }
                    }
                }
            }

            // Daily Phase Schedule Sequence
            item {
                Text(
                    text = "DAILY SOLAR PHOTOGRAPHY PHASES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(listOf(
                schedule.morningBlueHour,
                schedule.morningGoldenHour,
                schedule.eveningGoldenHour,
                schedule.eveningBlueHour
            )) { phase ->
                PhaseScheduleRowCard(phase = phase, timeFormatter = timeFormatter)
            }

            // Photography Tips Guide Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = Amber60,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pro Photography Tips",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "• Golden Hour: Use 5600K-6500K white balance or 'Shade' profile to enhance rich natural amber radiance.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Blue Hour: Use a tripod with ISO 100 and f/8-f/11 for sharp starburst city lights.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Compass Tracking: Position subjects facing ~45° away from the sun azimuth for cinematic rim lighting.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SunHorizonCompassCanvas(
    azimuthDeg: Float,
    elevationDeg: Float,
    isAboveHorizon: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.85f

        // Outer Ring
        drawCircle(
            color = Color.LightGray.copy(alpha = 0.3f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Cardinal Ticks & Labels
        val cardinals = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
        cardinals.forEach { (label, angle) ->
            val rad = Math.toRadians((angle - 90).toDouble())
            val startX = (center.x + (radius - 12.dp.toPx()) * cos(rad)).toFloat()
            val startY = (center.y + (radius - 12.dp.toPx()) * sin(rad)).toFloat()
            val endX = (center.x + radius * cos(rad)).toFloat()
            val endY = (center.y + radius * sin(rad)).toFloat()

            drawLine(
                color = if (label == "N") Cobalt60 else Color.Gray,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (label == "N") 3.dp.toPx() else 1.5.dp.toPx()
            )
        }

        // Sun Azimuth Direction Vector & Sun Marker
        val sunRad = Math.toRadians((azimuthDeg - 90).toDouble())
        val sunDistance = radius * 0.75f
        val sunX = (center.x + sunDistance * cos(sunRad)).toFloat()
        val sunY = (center.y + sunDistance * sin(sunRad)).toFloat()

        // Direction Pointer Line
        drawLine(
            brush = Brush.linearGradient(
                listOf(Amber60.copy(alpha = 0.3f), if (isAboveHorizon) Amber60 else Cobalt60)
            ),
            start = center,
            end = Offset(sunX, sunY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Pivot
        drawCircle(
            color = Color.Gray.copy(alpha = 0.6f),
            radius = 4.dp.toPx(),
            center = center
        )

        // Glowing Sun Marker
        val sunColor = if (isAboveHorizon) Amber60 else Cobalt60
        drawCircle(
            color = sunColor.copy(alpha = 0.3f),
            radius = 16.dp.toPx() * pulseScale,
            center = Offset(sunX, sunY)
        )
        drawCircle(
            color = sunColor,
            radius = 8.dp.toPx(),
            center = Offset(sunX, sunY)
        )
    }
}

@Composable
private fun CompassKpiPill(label: String, time: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = time, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun PhaseScheduleRowCard(
    phase: SolarPhaseWindow,
    timeFormatter: DateTimeFormatter
) {
    val phaseColor = Color(phase.hexColor)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = phaseColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (phase.phaseName.contains("Golden")) Icons.Filled.WbSunny else Icons.Filled.WbTwilight,
                        contentDescription = null,
                        tint = phaseColor,
                        modifier = Modifier.size(22.dp)
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
                        text = phase.phaseName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${phase.startTime.format(timeFormatter)} - ${phase.endTime.format(timeFormatter)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = phaseColor
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = phase.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
