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
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.util.IssCoordinates
import com.harichselvamc.seetime.util.IssGroundTrackPoint
import com.harichselvamc.seetime.util.IssOverheadPass
import com.harichselvamc.seetime.util.IssPassPredictorEngine
import com.harichselvamc.seetime.util.IssVisibilityType
import com.harichselvamc.seetime.util.ObserverLookAngle
import com.harichselvamc.seetime.util.TwilightCalculatorEngine
import com.harichselvamc.seetime.util.TwilightCity
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun IssPassScreen(
    viewModel: TimeViewModel? = null
) {
    val predefinedCities = remember { TwilightCalculatorEngine.getPredefinedCities() }
    var selectedCity by remember { mutableStateOf(predefinedCities.first()) }
    var selectedVisibilityFilter by remember { mutableStateOf("All") }

    var isLiveOrbit by remember { mutableStateOf(true) }
    var timeWarpMinutes by remember { mutableFloatStateOf(0f) }
    var tickerMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isLiveOrbit) {
        while (isLiveOrbit) {
            tickerMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val activeEpochMillis = remember(isLiveOrbit, timeWarpMinutes, tickerMillis) {
        if (isLiveOrbit) tickerMillis else (tickerMillis + (timeWarpMinutes * 60000L).toLong())
    }

    val issPosition = remember(activeEpochMillis) {
        IssPassPredictorEngine.calculateIssPosition(activeEpochMillis)
    }

    val lookAngle = remember(selectedCity, activeEpochMillis) {
        IssPassPredictorEngine.calculateLookAngle(
            observerLat = selectedCity.latitude,
            observerLon = selectedCity.longitude,
            epochMillis = activeEpochMillis
        )
    }

    val groundTrack = remember(activeEpochMillis) {
        IssPassPredictorEngine.calculateGroundTrack(
            currentEpochMillis = activeEpochMillis,
            pastMinutes = 45,
            futureMinutes = 90,
            stepMinutes = 2
        )
    }

    val upcomingPasses = remember(selectedCity, activeEpochMillis) {
        IssPassPredictorEngine.predictUpcomingPasses(
            observerLat = selectedCity.latitude,
            observerLon = selectedCity.longitude,
            startEpochMillis = activeEpochMillis,
            durationHours = 72,
            minElevationDeg = 10.0,
            observerCityName = selectedCity.cityName
        )
    }

    val filteredPasses = remember(upcomingPasses, selectedVisibilityFilter) {
        when (selectedVisibilityFilter) {
            "🌟 Visible Only" -> upcomingPasses.filter { it.visibilityType.isOpticallyVisible }
            "☀️ Daylight" -> upcomingPasses.filter { it.visibilityType == IssVisibilityType.DAYLIGHT_PASS }
            "🌑 Eclipsed" -> upcomingPasses.filter { it.visibilityType == IssVisibilityType.ECLIPSED_PASS }
            else -> upcomingPasses
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // ── Screen Top Header ───────────────────────────────────────────
        item {
            IssHeaderSection(
                selectedCity = selectedCity,
                isLiveOrbit = isLiveOrbit,
                onResetLive = {
                    isLiveOrbit = true
                    timeWarpMinutes = 0f
                }
            )
        }

        // ── Observer City Selector Filter Chips ─────────────────────────
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(predefinedCities) { city ->
                    val isSelected = city.id == selectedCity.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCity = city },
                        label = { Text("${city.flagEmoji} ${city.cityName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // ── Live ISS Telemetry Hero Card ────────────────────────────────
        item {
            IssTelemetryHeroCard(
                issPos = issPosition,
                epochMillis = activeEpochMillis
            )
        }

        // ── 2D Ground Track Orbit Canvas ────────────────────────────────
        item {
            IssGroundTrackCanvasCard(
                issPos = issPosition,
                observerCity = selectedCity,
                groundTrack = groundTrack
            )
        }

        // ── Ground Look-Angle & Sky Horizon Arc ─────────────────────────
        item {
            ObserverLookAngleCard(
                lookAngle = lookAngle,
                city = selectedCity
            )
        }

        // ── Next Visible Pass Highlight Hero Card ───────────────────────
        item {
            val nextVisiblePass = upcomingPasses.firstOrNull { it.visibilityType.isOpticallyVisible }
            NextPassHighlightCard(
                pass = nextVisiblePass ?: upcomingPasses.firstOrNull(),
                zoneId = selectedCity.zoneId
            )
        }

        // ── Interactive Orbit Time Warp Scrubber ────────────────────────
        item {
            OrbitScrubberCard(
                isLiveOrbit = isLiveOrbit,
                timeWarpMinutes = timeWarpMinutes,
                epochMillis = activeEpochMillis,
                zoneId = selectedCity.zoneId,
                onWarpChanged = {
                    isLiveOrbit = false
                    timeWarpMinutes = it
                },
                onGoLive = {
                    isLiveOrbit = true
                    timeWarpMinutes = 0f
                }
            )
        }

        // ── 72-Hour Pass Forecast Header & Filters ──────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "72-Hour Overhead Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${upcomingPasses.size} Passes Found",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filters = listOf("All", "🌟 Visible Only", "☀️ Daylight", "🌑 Eclipsed")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f ->
                    FilterChip(
                        selected = f == selectedVisibilityFilter,
                        onClick = { selectedVisibilityFilter = f },
                        label = { Text(f) }
                    )
                }
            }
        }

        // ── Pass Forecast Items ─────────────────────────────────────────
        items(filteredPasses) { pass ->
            IssPassItemCard(
                pass = pass,
                zoneId = selectedCity.zoneId
            )
        }
    }
}

// ── Top Header Section ──────────────────────────────────────────────────

@Composable
private fun IssHeaderSection(
    selectedCity: TwilightCity,
    isLiveOrbit: Boolean,
    onResetLive: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ISS Space Station HUD",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "LEO 420km",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "Ground Observer: ${selectedCity.flagEmoji} ${selectedCity.cityName} (${String.format(Locale.US, "%.2f°, %.2f°", selectedCity.latitude, selectedCity.longitude)})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isLiveOrbit) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.clickable { onResetLive() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reset Live",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Live Orbit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

// ── Live Telemetry Hero Card ────────────────────────────────────────────

@Composable
private fun IssTelemetryHeroCard(
    issPos: IssCoordinates,
    epochMillis: Long
) {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.of("UTC"))
    val timeStr = zdt.format(DateTimeFormatter.ofPattern("HH:mm:ss 'UTC'"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sub-Satellite Coordinates",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "%.2f° %s, %.2f° %s",
                            kotlin.math.abs(issPos.latitude),
                            if (issPos.latitude >= 0) "N" else "S",
                            kotlin.math.abs(issPos.longitude),
                            if (issPos.longitude >= 0) "E" else "W"
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Sunlight / Shadow Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (issPos.isSunlit) Color(0xFFF59E0B).copy(alpha = 0.18f) else Color(0xFF64748B).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (issPos.isSunlit) Color(0xFFF59E0B) else Color(0xFF64748B)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (issPos.isSunlit) "☀️ SUNLIT" else "🌑 ECLIPSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (issPos.isSunlit) Color(0xFFF59E0B) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatColumn(
                    label = "Altitude",
                    value = String.format(Locale.US, "%.1f km", issPos.altitudeKm),
                    icon = Icons.Default.Public
                )
                TelemetryStatColumn(
                    label = "Orbital Velocity",
                    value = String.format(Locale.US, "%.2f km/s", issPos.orbitalVelocityKmS),
                    icon = Icons.Default.Speed
                )
                TelemetryStatColumn(
                    label = "Ground Speed",
                    value = String.format(Locale.US, "%,.0f km/h", issPos.groundSpeedKmH),
                    icon = Icons.Default.RocketLaunch
                )
            }
        }
    }
}

@Composable
private fun TelemetryStatColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── 2D Ground Track Orbit Canvas ────────────────────────────────────────

@Composable
private fun IssGroundTrackCanvasCard(
    issPos: IssCoordinates,
    observerCity: TwilightCity,
    groundTrack: List<IssGroundTrackPoint>
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // 1. Deep Space / Earth Map Background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F172A), Color(0xFF020617))
                    )
                )

                // 2. Graticule Lines (Equator, ±51.64° Orbital Bounds, Longitudes)
                val eqY = height * 0.5f
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(0f, eqY),
                    end = Offset(width, eqY),
                    strokeWidth = 1.dp.toPx()
                )

                // ±51.64° ISS Inclination Limit Lines
                val incNorthY = height * (0.5f - (51.64f / 180f))
                val incSouthY = height * (0.5f + (51.64f / 180f))

                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                    start = Offset(0f, incNorthY),
                    end = Offset(width, incNorthY),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.25f),
                    start = Offset(0f, incSouthY),
                    end = Offset(width, incSouthY),
                    strokeWidth = 1.dp.toPx()
                )

                // Longitude Grid Meridians
                for (lon in -180..180 step 60) {
                    val x = width * ((lon + 180f) / 360f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 3. Draw Past & Future Orbital Ground Tracks
                var prevPoint: Offset? = null
                for (pt in groundTrack) {
                    val x = width * ((pt.longitude.toFloat() + 180f) / 360f)
                    val y = height * (0.5f - (pt.latitude.toFloat() / 180f))
                    val currentOffset = Offset(x, y)

                    if (prevPoint != null) {
                        // Avoid wrapping line across map seam (-180 to +180)
                        if (kotlin.math.abs(prevPoint.x - currentOffset.x) < width * 0.5f) {
                            val trackColor = if (pt.isPast) Color(0xFF94A3B8).copy(alpha = 0.45f)
                            else Color(0xFF38BDF8).copy(alpha = 0.85f)

                            drawLine(
                                color = trackColor,
                                start = prevPoint,
                                end = currentOffset,
                                strokeWidth = if (pt.isPast) 1.5.dp.toPx() else 2.5.dp.toPx()
                            )
                        }
                    }
                    prevPoint = currentOffset
                }

                // 4. Ground Observer Pin
                val obsX = width * ((observerCity.longitude.toFloat() + 180f) / 360f)
                val obsY = height * (0.5f - (observerCity.latitude.toFloat() / 180f))

                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.3f),
                    radius = 12.dp.toPx() * pulseScale,
                    center = Offset(obsX, obsY)
                )
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 4.dp.toPx(),
                    center = Offset(obsX, obsY)
                )

                // 5. Current ISS Satellite Position & Footprint Visibility Circle
                val issX = width * ((issPos.longitude.toFloat() + 180f) / 360f)
                val issY = height * (0.5f - (issPos.latitude.toFloat() / 180f))

                // Footprint Coverage Circle (~2,200 km radius ~ 19.8° on map)
                val footRadiusPx = width * (19.8f / 360f)
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                    radius = footRadiusPx,
                    center = Offset(issX, issY)
                )
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.4f),
                    radius = footRadiusPx,
                    center = Offset(issX, issY),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Pulsing Satellite Marker
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.4f),
                    radius = 14.dp.toPx() * pulseScale,
                    center = Offset(issX, issY)
                )
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 6.dp.toPx(),
                    center = Offset(issX, issY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(issX, issY)
                )
            }

            // Legend Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ISS Orbit Track", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(observerCity.cityName, style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
            }
        }
    }
}

// ── Observer Look-Angle Card ────────────────────────────────────────────

@Composable
private fun ObserverLookAngleCard(
    lookAngle: ObserverLookAngle,
    city: TwilightCity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sky Look-Angles (${city.cityName})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (lookAngle.isAboveHorizon) "🛰️ Space Station is above horizon!"
                        else "Station is currently below local horizon",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (lookAngle.isAboveHorizon) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (lookAngle.isAboveHorizon) Color(0xFF22C55E).copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (lookAngle.isAboveHorizon) "IN RANGE" else "BELOW HORIZON",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (lookAngle.isAboveHorizon) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryStatColumn(
                    label = "Compass Azimuth",
                    value = String.format(Locale.US, "%.1f° %s", lookAngle.azimuthDeg, lookAngle.compassDirection),
                    icon = Icons.Default.Explore
                )
                TelemetryStatColumn(
                    label = "Sky Elevation",
                    value = String.format(Locale.US, "%+.1f°", lookAngle.elevationDeg),
                    icon = Icons.Default.Navigation
                )
                TelemetryStatColumn(
                    label = "Slant Range",
                    value = String.format(Locale.US, "%,.0f km", lookAngle.slantRangeKm),
                    icon = Icons.Default.Radar
                )
            }
        }
    }
}

// ── Next Visible Pass Highlight Hero Card ───────────────────────────────

@Composable
private fun NextPassHighlightCard(
    pass: IssOverheadPass?,
    zoneId: ZoneId
) {
    if (pass == null) return

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val zdtAos = ZonedDateTime.ofInstant(pass.aosTime, zoneId)
    val zdtPeak = ZonedDateTime.ofInstant(pass.culminationTime, zoneId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(pass.visibilityType.badgeColorHex).copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(pass.visibilityType.badgeColorHex).copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = pass.visibilityType.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Next Flyover Opportunity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pass.visibilityType.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(pass.visibilityType.badgeColorHex)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(pass.visibilityType.badgeColorHex)
                ) {
                    Text(
                        text = String.format(Locale.US, "Peak: %.0f°", pass.maxElevationDeg),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Rise @ ${zdtAos.format(timeFormatter)} (${pass.riseDirection})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${zdtAos.format(dateFormatter)} • Duration: ${pass.durationSeconds / 60}m ${pass.durationSeconds % 60}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Trajectory: ${pass.riseDirection} ➔ ${pass.setDirection}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "Est. Mag: %.1f", pass.brightnessMagnitudeEstimate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Interactive Orbit Time Warp Scrubber ────────────────────────────────

@Composable
private fun OrbitScrubberCard(
    isLiveOrbit: Boolean,
    timeWarpMinutes: Float,
    epochMillis: Long,
    zoneId: ZoneId,
    onWarpChanged: (Float) -> Unit,
    onGoLive: () -> Unit
) {
    val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Orbital Time Warp Scrubber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isLiveOrbit) "Tracking real-time orbital path"
                        else String.format(Locale.US, "Warp: %+d minutes", timeWarpMinutes.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLiveOrbit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isLiveOrbit) "LIVE" else "SIMULATED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLiveOrbit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = timeWarpMinutes,
                onValueChange = { onWarpChanged(it) },
                valueRange = -60f..180f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "-1 Orbit (-60m)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = zdt.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "+2 Orbits (+180m)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Pass Forecast Item Card ─────────────────────────────────────────────

@Composable
private fun IssPassItemCard(
    pass: IssOverheadPass,
    zoneId: ZoneId
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
    val zdtAos = ZonedDateTime.ofInstant(pass.aosTime, zoneId)

    val maxElevColor = when {
        pass.maxElevationDeg >= 60.0 -> Color(0xFF22C55E)
        pass.maxElevationDeg >= 30.0 -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Peak Elevation Circular Badge
                Surface(
                    shape = CircleShape,
                    color = maxElevColor.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, maxElevColor),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${pass.maxElevationDeg.toInt()}°",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = maxElevColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = zdtAos.format(timeFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = pass.visibilityType.emoji,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "${zdtAos.format(dateFormatter)} • ${pass.durationSeconds / 60}m ${pass.durationSeconds % 60}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(pass.visibilityType.badgeColorHex).copy(alpha = 0.18f)
                ) {
                    Text(
                        text = pass.visibilityType.displayName.substringBefore('(').trim(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(pass.visibilityType.badgeColorHex),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${pass.riseDirection} ➔ ${pass.setDirection}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
