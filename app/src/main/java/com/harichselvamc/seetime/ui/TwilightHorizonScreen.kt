package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.util.DailyTwilightMilestone
import com.harichselvamc.seetime.util.PhotoLightingWindow
import com.harichselvamc.seetime.util.SolarElevationTelemetry
import com.harichselvamc.seetime.util.TwilightCalculatorEngine
import com.harichselvamc.seetime.util.TwilightCity
import com.harichselvamc.seetime.util.TwilightPhase
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TwilightHorizonScreen(
    viewModel: TimeViewModel? = null
) {
    val predefinedCities = remember { TwilightCalculatorEngine.getPredefinedCities() }
    var selectedCity by remember { mutableStateOf(predefinedCities.first()) }
    var selectedRegionFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var isLiveTime by remember { mutableStateOf(true) }
    var simulatedHourFraction by remember { mutableFloatStateOf(12.0f) }
    var tickerMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isLiveTime) {
        while (isLiveTime) {
            tickerMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val activeZonedDateTime = remember(selectedCity, isLiveTime, simulatedHourFraction, tickerMillis) {
        if (isLiveTime) {
            ZonedDateTime.now(selectedCity.zoneId)
        } else {
            val totalSeconds = (simulatedHourFraction * 3600f).toInt().coerceIn(0, 86399)
            val hour = totalSeconds / 3600
            val minute = (totalSeconds % 3600) / 60
            val second = totalSeconds % 60
            val date = LocalDate.now(selectedCity.zoneId)
            ZonedDateTime.of(date, LocalTime.of(hour, minute, second), selectedCity.zoneId)
        }
    }

    val telemetry = remember(selectedCity, activeZonedDateTime) {
        TwilightCalculatorEngine.calculateTelemetry(
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude,
            dateTime = activeZonedDateTime
        )
    }

    val dailySchedule = remember(selectedCity, activeZonedDateTime) {
        TwilightCalculatorEngine.calculateDailySchedule(
            latitude = selectedCity.latitude,
            longitude = selectedCity.longitude,
            date = activeZonedDateTime.toLocalDate(),
            zoneId = selectedCity.zoneId
        )
    }

    val regions = remember {
        listOf("All", "Europe", "Americas", "Asia-Pacific", "Middle East & Africa", "Nordic / High Lat")
    }

    val filteredCities = remember(selectedRegionFilter, searchQuery) {
        predefinedCities.filter { city ->
            val matchesRegion = selectedRegionFilter == "All" || city.region == selectedRegionFilter
            val matchesSearch = searchQuery.isBlank() ||
                    city.cityName.contains(searchQuery, ignoreCase = true) ||
                    city.country.contains(searchQuery, ignoreCase = true)
            matchesRegion && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // ── Top Bar Header ──────────────────────────────────────────────
        item {
            TwilightHeaderSection(
                selectedCity = selectedCity,
                activeTime = activeZonedDateTime,
                isLiveTime = isLiveTime,
                onResetLive = {
                    isLiveTime = true
                }
            )
        }

        // ── City Quick Selector Filter Chips ────────────────────────────
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(predefinedCities.take(6)) { city ->
                    val isSelected = city.id == selectedCity.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedCity = city
                        },
                        label = {
                            Text("${city.flagEmoji} ${city.cityName}")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // ── Dynamic Sky Horizon Canvas ──────────────────────────────────
        item {
            DynamicSkyHorizonCanvasCard(
                telemetry = telemetry,
                city = selectedCity
            )
        }

        // ── Solar Elevation & Twilight Phase HUD ────────────────────────
        item {
            SolarElevationTelemetryCard(telemetry = telemetry)
        }

        // ── 5-Band Twilight Spectrum Ribbon Gauge ───────────────────────
        item {
            TwilightSpectrumGaugeCard(telemetry = telemetry)
        }

        // ── Next Phase Transition Hero Card ─────────────────────────────
        item {
            NextTransitionHeroCard(
                telemetry = telemetry,
                city = selectedCity
            )
        }

        // ── 24-Hour Time Simulation Scrubber ────────────────────────────
        item {
            TimeScrubberControlCard(
                isLiveTime = isLiveTime,
                simulatedHour = simulatedHourFraction,
                activeTime = activeZonedDateTime,
                onHourChanged = { newHour ->
                    isLiveTime = false
                    simulatedHourFraction = newHour
                },
                onGoLive = {
                    isLiveTime = true
                }
            )
        }

        // ── 24-Hour Daily Milestone Timeline ────────────────────────────
        item {
            DailyMilestonesCard(
                schedule = dailySchedule,
                currentTime = activeZonedDateTime.toLocalTime()
            )
        }

        // ── Multi-City Comparison Header & Search ───────────────────────
        item {
            Text(
                text = "Global Twilight Progressions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search world cities...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Region Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(regions) { reg ->
                    val isSelected = reg == selectedRegionFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRegionFilter = reg },
                        label = { Text(reg) }
                    )
                }
            }
        }

        // ── Multi-City Comparison List Cards ────────────────────────────
        items(filteredCities) { city ->
            val cityZdt = if (isLiveTime) {
                ZonedDateTime.now(city.zoneId)
            } else {
                val totalSeconds = (simulatedHourFraction * 3600f).toInt().coerceIn(0, 86399)
                val hour = totalSeconds / 3600
                val minute = (totalSeconds % 3600) / 60
                val second = totalSeconds % 60
                val date = LocalDate.now(city.zoneId)
                ZonedDateTime.of(date, LocalTime.of(hour, minute, second), city.zoneId)
            }

            val cityTelemetry = remember(city, cityZdt) {
                TwilightCalculatorEngine.calculateTelemetry(
                    latitude = city.latitude,
                    longitude = city.longitude,
                    dateTime = cityZdt
                )
            }

            CityTwilightComparisonCard(
                city = city,
                telemetry = cityTelemetry,
                isSelected = city.id == selectedCity.id,
                onClick = {
                    selectedCity = city
                }
            )
        }
    }
}

// ── Top Header Section ──────────────────────────────────────────────────

@Composable
private fun TwilightHeaderSection(
    selectedCity: TwilightCity,
    activeTime: ZonedDateTime,
    isLiveTime: Boolean,
    onResetLive: () -> Unit
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${selectedCity.flagEmoji} ${selectedCity.cityName}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = selectedCity.zoneId.id.substringAfter('/'),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "${activeTime.format(dateFormatter)} • ${activeTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isLiveTime) {
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
                        text = "Go Live",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

// ── Dynamic Sky Horizon Canvas Card ─────────────────────────────────────

@Composable
private fun DynamicSkyHorizonCanvasCard(
    telemetry: SolarElevationTelemetry,
    city: TwilightCity
) {
    val phase = telemetry.phase

    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val horizonY = height * 0.65f

                // 1. Sky Gradient Shading
                val topColor = Color(phase.gradientTopHex)
                val bottomColor = Color(phase.gradientBottomHex)
                val groundColor = Color(0xFF0F172A)

                // Sky background brush
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor),
                        startY = 0f,
                        endY = horizonY
                    ),
                    topLeft = Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(width, horizonY)
                )

                // Ground / Sea silhouette brush
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(groundColor.copy(alpha = 0.95f), Color(0xFF030712)),
                        startY = horizonY,
                        endY = height
                    ),
                    topLeft = Offset(0f, horizonY),
                    size = androidx.compose.ui.geometry.Size(width, height - horizonY)
                )

                // 2. Render Cosmic Stars if Sun is below horizon
                if (telemetry.elevationDegrees < -6.0) {
                    val starAlpha = when {
                        telemetry.elevationDegrees < -18.0 -> 0.95f
                        telemetry.elevationDegrees < -12.0 -> 0.70f
                        else -> 0.35f
                    }

                    val starSeed = (city.latitude * 1000 + city.longitude * 100).toInt()
                    val starPositions = listOf(
                        Offset(width * 0.12f, height * 0.15f),
                        Offset(width * 0.25f, height * 0.28f),
                        Offset(width * 0.38f, height * 0.10f),
                        Offset(width * 0.52f, height * 0.22f),
                        Offset(width * 0.68f, height * 0.12f),
                        Offset(width * 0.82f, height * 0.26f),
                        Offset(width * 0.91f, height * 0.16f),
                        Offset(width * 0.18f, height * 0.42f),
                        Offset(width * 0.45f, height * 0.38f),
                        Offset(width * 0.75f, height * 0.45f)
                    )

                    for (pos in starPositions) {
                        drawCircle(
                            color = Color.White.copy(alpha = starAlpha),
                            radius = 2.0.dp.toPx(),
                            center = pos
                        )
                    }
                }

                // 3. Horizon Baseline
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(0f, horizonY),
                    end = Offset(width, horizonY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // 4. Calculate Sun Position along visual arc
                // Map elevation (-90°..+90°) to Y coordinate
                val normElev = (telemetry.elevationDegrees / 90.0).toFloat().coerceIn(-1.0f, 1.0f)
                val sunY = if (normElev >= 0f) {
                    horizonY - (normElev * (horizonY - 30.dp.toPx()))
                } else {
                    horizonY + (-normElev * (height - horizonY - 20.dp.toPx()))
                }
                val sunX = width * 0.5f

                // Sun Glow Rings
                val sunColor = if (telemetry.isAboveHorizon) Color(0xFFFBBF24) else Color(0xFFF97316)
                drawCircle(
                    color = sunColor.copy(alpha = 0.25f),
                    radius = (28.dp.toPx()) * pulseScale,
                    center = Offset(sunX, sunY)
                )
                drawCircle(
                    color = sunColor.copy(alpha = 0.5f),
                    radius = 18.dp.toPx(),
                    center = Offset(sunX, sunY)
                )
                // Core Sun Disc
                drawCircle(
                    color = Color.White,
                    radius = 10.dp.toPx(),
                    center = Offset(sunX, sunY)
                )

                // 5. Zenith (+90°) and Nadir (-90°) Guide Markers
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f),
                    radius = 3.dp.toPx(),
                    center = Offset(sunX, 16.dp.toPx())
                )
            }

            // Overlay Metadata Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phase.iconEmoji,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = phase.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = String.format(Locale.US, "Azimuth: %.1f°", telemetry.azimuthDegrees),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Horizon Baseline Label Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 60.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    text = "0° HORIZON",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ── Solar Elevation & Twilight Phase HUD ────────────────────────────────

@Composable
private fun SolarElevationTelemetryCard(telemetry: SolarElevationTelemetry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Solar Elevation",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.US, "%+.1f°", telemetry.elevationDegrees),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(telemetry.phase.primaryColorHex)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = if (telemetry.isRising) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = if (telemetry.isRising) "Rising" else "Setting",
                            tint = if (telemetry.isRising) Color(0xFF22C55E) else Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (telemetry.isRising) "Rising" else "Setting",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (telemetry.isRising) Color(0xFF22C55E) else Color(0xFFEF4444)
                        )
                    }
                }

                // Photography Window Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(telemetry.photoWindow.badgeColorHex).copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(telemetry.photoWindow.badgeColorHex).copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${telemetry.photoWindow.emoji} ${telemetry.photoWindow.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(telemetry.photoWindow.badgeColorHex)
                        )
                        Text(
                            text = if (telemetry.photoWindow == PhotoLightingWindow.GOLDEN_HOUR ||
                                telemetry.photoWindow == PhotoLightingWindow.BLUE_HOUR
                            ) "ACTIVE" else "WINDOW",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(telemetry.photoWindow.badgeColorHex).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Illuminance, Star Rating, Peak Elevation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem(
                    label = "Natural Light",
                    value = String.format(Locale.US, "%.1f Lux", telemetry.approximateLux),
                    icon = Icons.Default.WbSunny
                )
                TelemetryMetricItem(
                    label = "Star Visibility",
                    value = "${telemetry.starVisibilityRating}/5 Stars",
                    icon = Icons.Default.Star
                )
                TelemetryMetricItem(
                    label = "Solar Noon Peak",
                    value = String.format(Locale.US, "%+.1f°", telemetry.solarNoonElevation),
                    icon = Icons.Default.Schedule
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = telemetry.phase.visualSkyDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TelemetryMetricItem(
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ── 5-Band Twilight Spectrum Ribbon Gauge ───────────────────────────────

@Composable
private fun TwilightSpectrumGaugeCard(telemetry: SolarElevationTelemetry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Twilight Spectrum Bands (-90° to +90°)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Canvas Bar with the 5 discrete twilight color zones
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                val w = size.width
                val h = size.height

                // Total range = 180° (-90 to +90)
                // Night: -90° to -18° -> 72° / 180° = 40%
                val nightWidth = w * (72f / 180f)
                // Astro: -18° to -12° -> 6° / 180° = 3.33%
                val astroWidth = w * (6f / 180f)
                // Nautical: -12° to -6° -> 6° / 180° = 3.33%
                val nauticalWidth = w * (6f / 180f)
                // Civil: -6° to 0° -> 6° / 180° = 3.33%
                val civilWidth = w * (6f / 180f)
                // Daylight: 0° to +90° -> 90° / 180° = 50%
                val dayWidth = w * (90f / 180f)

                var currentX = 0f

                // Night
                drawRect(
                    color = Color(TwilightPhase.NIGHT.primaryColorHex),
                    topLeft = Offset(currentX, 0f),
                    size = androidx.compose.ui.geometry.Size(nightWidth, h)
                )
                currentX += nightWidth

                // Astro
                drawRect(
                    color = Color(TwilightPhase.ASTRONOMICAL_TWILIGHT.primaryColorHex),
                    topLeft = Offset(currentX, 0f),
                    size = androidx.compose.ui.geometry.Size(astroWidth, h)
                )
                currentX += astroWidth

                // Nautical
                drawRect(
                    color = Color(TwilightPhase.NAUTICAL_TWILIGHT.primaryColorHex),
                    topLeft = Offset(currentX, 0f),
                    size = androidx.compose.ui.geometry.Size(nauticalWidth, h)
                )
                currentX += nauticalWidth

                // Civil
                drawRect(
                    color = Color(TwilightPhase.CIVIL_TWILIGHT.primaryColorHex),
                    topLeft = Offset(currentX, 0f),
                    size = androidx.compose.ui.geometry.Size(civilWidth, h)
                )
                currentX += civilWidth

                // Daylight
                drawRect(
                    color = Color(TwilightPhase.DIRECT_SUNLIGHT.primaryColorHex),
                    topLeft = Offset(currentX, 0f),
                    size = androidx.compose.ui.geometry.Size(dayWidth, h)
                )

                // Current Sun Pointer Position
                val sunNorm = ((telemetry.elevationDegrees + 90.0) / 180.0).toFloat().coerceIn(0f, 1f)
                val pointerX = w * sunNorm

                drawCircle(
                    color = Color.White,
                    radius = 7.dp.toPx(),
                    center = Offset(pointerX, h / 2f)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 3.5.dp.toPx(),
                    center = Offset(pointerX, h / 2f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpectrumLegendItem("Night", "<-18°", Color(TwilightPhase.NIGHT.primaryColorHex))
                SpectrumLegendItem("Astro", "-18°", Color(TwilightPhase.ASTRONOMICAL_TWILIGHT.primaryColorHex))
                SpectrumLegendItem("Naut", "-12°", Color(TwilightPhase.NAUTICAL_TWILIGHT.primaryColorHex))
                SpectrumLegendItem("Civil", "-6°", Color(TwilightPhase.CIVIL_TWILIGHT.primaryColorHex))
                SpectrumLegendItem("Day", ">0°", Color(TwilightPhase.DIRECT_SUNLIGHT.primaryColorHex))
            }
        }
    }
}

@Composable
private fun SpectrumLegendItem(name: String, threshold: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(text = threshold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
        }
    }
}

// ── Next Phase Transition Hero Card ─────────────────────────────────────

@Composable
private fun NextTransitionHeroCard(
    telemetry: SolarElevationTelemetry,
    city: TwilightCity
) {
    val nextEvent = telemetry.nextTransitionEvent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Next Transition",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nextEvent?.name ?: "Solar Window In Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = nextEvent?.description ?: "Sun altitude changing smoothly",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            if (nextEvent != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(nextEvent.phaseColorHex).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(nextEvent.phaseColorHex).copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "in ${nextEvent.formattedCountdown}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(nextEvent.phaseColorHex)
                        )
                        Text(
                            text = "@ ${nextEvent.localTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(nextEvent.phaseColorHex).copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

// ── 24-Hour Time Simulation Scrubber ────────────────────────────────────

@Composable
private fun TimeScrubberControlCard(
    isLiveTime: Boolean,
    simulatedHour: Float,
    activeTime: ZonedDateTime,
    onHourChanged: (Float) -> Unit,
    onGoLive: () -> Unit
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
                        text = "24-Hour Solar Scrubber",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isLiveTime) "Tracking live local clock" else "Simulating custom solar hour",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLiveTime) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isLiveTime) "LIVE" else "SIMULATED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLiveTime) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = if (isLiveTime) {
                    val lt = activeTime.toLocalTime()
                    lt.hour + (lt.minute / 60f) + (lt.second / 3600f)
                } else simulatedHour,
                onValueChange = { onHourChanged(it) },
                valueRange = 0f..24f,
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
                    text = "00:00 Midnight",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = activeTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "24:00 Next Day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── 24-Hour Daily Milestone Timeline ────────────────────────────────────

@Composable
private fun DailyMilestonesCard(
    schedule: com.harichselvamc.seetime.util.DailyTwilightSchedule,
    currentTime: LocalTime
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Solar & Twilight Progression",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                schedule.milestones.forEach { milestone ->
                    MilestoneRowItem(
                        milestone = milestone,
                        currentTime = currentTime
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneRowItem(
    milestone: DailyTwilightMilestone,
    currentTime: LocalTime
) {
    val timeStr = milestone.time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "N/A"
    val isPassed = milestone.time?.let { currentTime.isAfter(it) } ?: false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isPassed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color(milestone.colorHex))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = milestone.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isPassed) FontWeight.Normal else FontWeight.Bold,
                    color = if (isPassed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isPassed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isPassed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

// ── City Comparison Card ────────────────────────────────────────────────

@Composable
private fun CityTwilightComparisonCard(
    city: TwilightCity,
    telemetry: SolarElevationTelemetry,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val phase = telemetry.phase
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(18.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
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
                Text(text = city.flagEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = city.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = city.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = telemetry.localTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(phase.primaryColorHex).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = String.format(Locale.US, "%+.1f°", telemetry.elevationDegrees),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(phase.primaryColorHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${phase.iconEmoji} ${phase.shortName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
