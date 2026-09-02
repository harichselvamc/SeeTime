package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.PlanetId
import com.harichselvamc.seetime.util.PlanetOrbitState
import com.harichselvamc.seetime.util.SolarOrreryEngine
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolarOrreryScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    var simInstant by remember { mutableStateOf(Instant.now()) }
    var isSimRunning by remember { mutableStateOf(true) }
    var simSpeedMultiplier by remember { mutableFloatStateOf(1000f) } // 1000x default simulation speed
    var isInnerSystemZoom by remember { mutableStateOf(true) } // True: Inner 4 planets, False: All 8 planets
    var selectedPlanetId by remember { mutableStateOf(PlanetId.EARTH) }

    // Simulation loop
    LaunchedEffect(isSimRunning, simSpeedMultiplier) {
        var lastTime = System.currentTimeMillis()
        while (true) {
            val now = System.currentTimeMillis()
            val dt = (now - lastTime).coerceAtMost(100L)
            lastTime = now

            if (isSimRunning) {
                val advanceMillis = (dt * simSpeedMultiplier).toLong()
                simInstant = simInstant.plusMillis(advanceMillis)
            }
            delay(33L) // ~30 FPS
        }
    }

    val planetStates = remember(simInstant) {
        SolarOrreryEngine.calculateAllPlanets(simInstant)
    }

    val selectedState = remember(planetStates, selectedPlanetId) {
        planetStates.find { it.planet == selectedPlanetId } ?: planetStates[2]
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("MMMM dd, yyyy · HH:mm 'UTC'", Locale.getDefault())
    }
    val simDateStr = remember(simInstant) {
        simInstant.atZone(ZoneId.of("UTC")).format(dateFormatter)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Heliocentric Solar Orrery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Amber60.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 6.dp)
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
                                text = "Keplerian 8-Orbit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Amber60
                            )
                        }
                    }
                    IconButton(onClick = { simInstant = Instant.now() }) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Reset to Present",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
            // Simulation HUD Date & Viewport Controls Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            Column {
                                Text(
                                    text = "SIMULATION EPOCH",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Amber60
                                )
                                Text(
                                    text = simDateStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Zoom Toggle Button
                            FilterChip(
                                selected = isInnerSystemZoom,
                                onClick = { isInnerSystemZoom = !isInnerSystemZoom },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isInnerSystemZoom) Icons.Filled.ZoomIn else Icons.Filled.ZoomOut,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(if (isInnerSystemZoom) "Inner Solar (4 Planets)" else "Full Solar (8 Planets)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Amber60.copy(alpha = 0.15f),
                                    selectedLabelColor = Amber60
                                )
                            )
                        }

                        // Simulation Time Warp Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSimRunning) Amber60 else MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clickable { isSimRunning = !isSimRunning }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSimRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = "Play/Pause",
                                            tint = if (isSimRunning) Color.Black else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Time-Warp Speed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = when (simSpeedMultiplier) {
                                            1f -> "1x (Real-time)"
                                            100f -> "100x"
                                            1000f -> "1,000x (~1 day/sec)"
                                            10000f -> "10,000x (~10 days/sec)"
                                            else -> "50,000x (~2 months/sec)"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Amber60
                                    )
                                }
                            }

                            // Speed Step Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1f, 100f, 1000f, 10000f, 50000f).forEach { speed ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (simSpeedMultiplier == speed) Amber60.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable { simSpeedMultiplier = speed }
                                    ) {
                                        Text(
                                            text = when (speed) {
                                                1f -> "1x"
                                                100f -> "100x"
                                                1000f -> "1k"
                                                10000f -> "10k"
                                                else -> "50k"
                                            },
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (simSpeedMultiplier == speed) Amber60 else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive 2D Heliocentric Orrery Canvas
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF070B14) // Deep Space Background
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    OrreryCanvas(
                        planetStates = planetStates,
                        isInnerSystemZoom = isInnerSystemZoom,
                        selectedPlanetId = selectedPlanetId,
                        onPlanetSelected = { selectedPlanetId = it }
                    )
                }
            }

            // Horizontal Planet Selection Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SELECT PLANET TO INSPECT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PlanetId.values()) { planet ->
                            val isSelected = selectedPlanetId == planet
                            val pColor = Color(planet.colorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPlanetId = planet },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(pColor)
                                    )
                                },
                                label = { Text("${planet.symbol} ${planet.displayName}", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = pColor.copy(alpha = 0.20f),
                                    selectedLabelColor = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }

            // Selected Planet Detailed Telemetry Hero Card
            item {
                PlanetTelemetryCard(selectedState = selectedState)
            }
        }
    }
}

// ── Composable Orrery Canvas ──────────────────────────────────────────

@Composable
private fun OrreryCanvas(
    planetStates: List<PlanetOrbitState>,
    isInnerSystemZoom: Boolean,
    selectedPlanetId: PlanetId,
    onPlanetSelected: (PlanetId) -> Unit
) {
    val displayedPlanets = remember(planetStates, isInnerSystemZoom) {
        if (isInnerSystemZoom) {
            planetStates.filter { it.planet.isInnerPlanet }
        } else {
            planetStates
        }
    }

    val maxRadiusAu = if (isInnerSystemZoom) 1.8 else 32.0

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(displayedPlanets, isInnerSystemZoom) {
                detectTapGestures { tapOffset ->
                    val minDim = minOf(size.width, size.height).toFloat()
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val scale = (minDim / 2f * 0.88f) / maxRadiusAu

                    var closestPlanet: PlanetId? = null
                    var minDistance = Float.MAX_VALUE

                    for (p in displayedPlanets) {
                        val px = center.x + (p.xAu * scale).toFloat()
                        val py = center.y - (p.yAu * scale).toFloat()
                        val dx = tapOffset.x - px
                        val dy = tapOffset.y - py
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist < 32.dp.toPx() && dist < minDistance) {
                            minDistance = dist
                            closestPlanet = p.planet
                        }
                    }
                    if (closestPlanet != null) {
                        onPlanetSelected(closestPlanet)
                    }
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val canvasRadius = size.minDimension / 2f * 0.88f
        val scale = canvasRadius / maxRadiusAu

        // 1. Draw glowing central Sun
        val sunRadius = if (isInnerSystemZoom) 14.dp.toPx() else 8.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFEA00), Color(0xFFF59E0B), Color(0x00D97706)),
                center = center,
                radius = sunRadius * 2.5f
            ),
            radius = sunRadius * 2.5f,
            center = center
        )
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = sunRadius,
            center = center
        )

        // 2. Draw Elliptical Orbit Rings
        for (p in displayedPlanets) {
            val orbitRadiusPx = (p.planet.semiMajorAxisAu * scale).toFloat()
            val isSelected = p.planet == selectedPlanetId

            drawCircle(
                color = if (isSelected) Color(p.planet.colorHex).copy(alpha = 0.6f) else Color(0xFF334155).copy(alpha = 0.4f),
                radius = orbitRadiusPx,
                center = center,
                style = Stroke(
                    width = if (isSelected) 2.dp.toPx() else 1.dp.toPx(),
                    pathEffect = if (isSelected) null else PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )
        }

        // 3. Draw Planets along Orbit Coordinates
        for (p in displayedPlanets) {
            val px = center.x + (p.xAu * scale).toFloat()
            val py = center.y - (p.yAu * scale).toFloat()
            val planetColor = Color(p.planet.colorHex)
            val isSelected = p.planet == selectedPlanetId

            val sphereRadius = when (p.planet) {
                PlanetId.MERCURY -> 3.5.dp.toPx()
                PlanetId.VENUS -> 5.5.dp.toPx()
                PlanetId.EARTH -> 6.0.dp.toPx()
                PlanetId.MARS -> 4.5.dp.toPx()
                PlanetId.JUPITER -> 10.0.dp.toPx()
                PlanetId.SATURN -> 8.5.dp.toPx()
                PlanetId.URANUS -> 6.5.dp.toPx()
                PlanetId.NEPTUNE -> 6.5.dp.toPx()
            }

            // Selection Glow Halo
            if (isSelected) {
                drawCircle(
                    color = planetColor.copy(alpha = 0.35f),
                    radius = sphereRadius * 2.6f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = sphereRadius * 1.6f,
                    center = Offset(px, py),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Planet Body
            drawCircle(
                color = planetColor,
                radius = sphereRadius,
                center = Offset(px, py)
            )

            // Saturn Ring Effect
            if (p.planet == PlanetId.SATURN) {
                drawCircle(
                    color = Color(0xFFFDE68A).copy(alpha = 0.8f),
                    radius = sphereRadius * 1.8f,
                    center = Offset(px, py),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }
    }
}

// ── Composable Telemetry Card ─────────────────────────────────────────

@Composable
private fun PlanetTelemetryCard(selectedState: PlanetOrbitState) {
    val planet = selectedState.planet
    val pColor = Color(planet.colorHex)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = pColor.copy(alpha = 0.20f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = planet.symbol,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = pColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = planet.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (planet.isInnerPlanet) "Terrestrial Rocky Planet" else "Gas / Ice Giant",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Amber60.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${selectedState.zodiacEmoji} ${selectedState.zodiacSign}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Amber60,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Key Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryMetricTile(
                    title = "Distance to Sun",
                    primaryVal = String.format(Locale.getDefault(), "%.3f AU", selectedState.distanceAu),
                    secondaryVal = String.format(Locale.getDefault(), "%,.1f M km", selectedState.distanceKm / 1_000_000.0),
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricTile(
                    title = "Orbital Velocity",
                    primaryVal = String.format(Locale.getDefault(), "%.2f km/s", selectedState.orbitalVelocityKmPerSec),
                    secondaryVal = String.format(Locale.getDefault(), "%,.0f km/h", selectedState.orbitalVelocityKmPerSec * 3600.0),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryMetricTile(
                    title = "Orbital Period",
                    primaryVal = String.format(Locale.getDefault(), "%.1f days", planet.orbitalPeriodDays),
                    secondaryVal = String.format(Locale.getDefault(), "%.2f Earth yrs", planet.orbitalPeriodDays / 365.25),
                    modifier = Modifier.weight(1f)
                )
                TelemetryMetricTile(
                    title = "Eccentricity (e)",
                    primaryVal = String.format(Locale.getDefault(), "%.4f", planet.eccentricity),
                    secondaryVal = "Longitude: ${selectedState.heliocentricLongitudeDeg.toInt()}°",
                    modifier = Modifier.weight(1f)
                )
            }

            // Educational Fun Fact Banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = null,
                        tint = Cobalt60,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = planet.funFact,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricTile(
    title: String,
    primaryVal: String,
    secondaryVal: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Text(primaryVal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(secondaryVal, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Cobalt60)
        }
    }
}
