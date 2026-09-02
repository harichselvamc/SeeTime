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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.harichselvamc.seetime.util.GlobeMath
import com.harichselvamc.seetime.util.LahainaNoonEvent
import com.harichselvamc.seetime.util.ObserverZenithTelemetry
import com.harichselvamc.seetime.util.SubsolarCoordinates
import com.harichselvamc.seetime.util.SubsolarEngine
import com.harichselvamc.seetime.util.SubsolarEngine.calculateLahainaNoon
import com.harichselvamc.seetime.util.SubsolarEngine.calculateObserverZenith
import com.harichselvamc.seetime.util.SubsolarEngine.calculateSubsolarPoint
import com.harichselvamc.seetime.util.SubsolarEngine.TROPICAL_CITY_PRESETS
import kotlinx.coroutines.delay
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubsolarScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isSimulating by remember { mutableStateOf(false) }
    var simHourOffset by remember { mutableFloatStateOf(0f) }

    // Selected Observer Coordinates
    var selectedCityName by remember { mutableStateOf("Honolulu") }
    var selectedCountry by remember { mutableStateOf("USA (Hawaii)") }
    var observerLat by remember { mutableDoubleStateOf(21.3069) }
    var observerLon by remember { mutableDoubleStateOf(-157.8583) }

    // Live clock ticker
    LaunchedEffect(isSimulating) {
        while (!isSimulating) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val activeDateTime: ZonedDateTime = remember(currentTimeMillis, isSimulating, simHourOffset) {
        val base = ZonedDateTime.now(ZoneOffset.UTC)
        if (isSimulating) {
            val totalSeconds = (simHourOffset * 3600).toLong()
            base.plusSeconds(totalSeconds)
        } else {
            base
        }
    }

    val subsolarCoords: SubsolarCoordinates = remember(activeDateTime) {
        SubsolarEngine.calculateSubsolarPoint(activeDateTime)
    }

    val observerTelemetry: ObserverZenithTelemetry = remember(observerLat, observerLon, selectedCityName, selectedCountry, activeDateTime) {
        SubsolarEngine.calculateObserverZenith(
            observerLat = observerLat,
            observerLon = observerLon,
            observerCityName = selectedCityName,
            observerCountry = selectedCountry,
            dateTime = activeDateTime
        )
    }

    val lahainaNoonEvent: LahainaNoonEvent = remember(selectedCityName, selectedCountry, observerLat, observerLon) {
        SubsolarEngine.calculateLahainaNoon(
            cityName = selectedCityName,
            country = selectedCountry,
            latitude = observerLat,
            longitude = observerLon,
            currentDate = activeDateTime.toLocalDate()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Subsolar Point & Solar Zenith",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "90° Overhead Sun & Lahaina Noon HUD",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Hero Subsolar Coordinates Card ─────────────────────────────────
            item {
                SubsolarHeroCard(
                    subsolar = subsolarCoords,
                    isSimulating = isSimulating,
                    simHourOffset = simHourOffset,
                    onToggleSimulate = { isSimulating = !isSimulating },
                    onSimHourChange = { simHourOffset = it }
                )
            }

            // ── 2D Interactive Solar Zenith World Map Canvas ────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Public,
                                contentDescription = null,
                                tint = Amber60,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Global Solar Zenith World Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Tap map to reposition observer pin · Golden marker = 90° Subsolar Point",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        SubsolarWorldMapCanvas(
                            subsolar = subsolarCoords,
                            observerLat = observerLat,
                            observerLon = observerLon,
                            onMapTap = { tappedLat, tappedLon ->
                                selectedCityName = "Custom Pin"
                                selectedCountry = String.format(Locale.US, "%.1f°%s, %.1f°%s",
                                    abs(tappedLat), if (tappedLat >= 0) "N" else "S",
                                    abs(tappedLon), if (tappedLon >= 0) "E" else "W"
                                )
                                observerLat = tappedLat
                                observerLon = tappedLon
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                }
            }

            // ── Observer Location Preset Chips ────────────────────────────────
            item {
                Column {
                    Text(
                        "Observer City Preset",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(SubsolarEngine.TROPICAL_CITY_PRESETS) { city ->
                            val isSelected = selectedCityName == city.name
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCityName = city.name
                                    selectedCountry = city.country
                                    observerLat = city.latitude
                                    observerLon = city.longitude
                                },
                                label = { Text(city.name) },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // ── Observer Zenith & Great-Circle Telemetry Card ─────────────────
            item {
                ObserverTelemetryCard(telemetry = observerTelemetry)
            }

            // ── 3D Upright 1m Pole Shadow Simulator Canvas ─────────────────────
            item {
                PoleShadowSimulatorCard(telemetry = observerTelemetry)
            }

            // ── Lahaina Noon (Zero-Shadow Day) Almanac Card ───────────────────
            item {
                LahainaNoonCard(event = lahainaNoonEvent)
            }

            // ── Educational Astronomical Explainer ───────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "What is the Subsolar Point & Lahaina Noon?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The Subsolar Point is the exact terrestrial coordinate where the Sun's rays strike Earth perpendicularly at a 90° angle. Anyone standing at this location experiences the Sun directly at their zenith.\n\nIn the tropics (between 23.44° N and 23.44° S), the Sun reaches true 90° zenith twice each year on dates known in Hawaii as 'Lāhainā Noon' (meaning 'cruel sun'). During this solar noon event, vertical objects cast absolutely zero shadow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Hero Subsolar Coordinates Card ────────────────────────────────────────────

@Composable
private fun SubsolarHeroCard(
    subsolar: SubsolarCoordinates,
    isSimulating: Boolean,
    simHourOffset: Float,
    onToggleSimulate: () -> Unit,
    onSimHourChange: (Float) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_corona")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corona_scale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Amber60.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp * pulseScale)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "SUBSOLAR POINT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            subsolar.tropicZone,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = subsolar.utcDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss 'UTC'", Locale.US)),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Coordinates Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Latitude (Declination δ)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    val latDir = if (subsolar.latitude >= 0) "N" else "S"
                    Text(
                        text = String.format(Locale.US, "%.4f° %s", abs(subsolar.latitude), latDir),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Amber60
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Longitude (GHA Meridian)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    val lonDir = if (subsolar.longitude >= 0) "E" else "W"
                    Text(
                        text = String.format(Locale.US, "%.4f° %s", abs(subsolar.longitude), lonDir),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan60
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Equation of Time: ${String.format(Locale.US, "%+.1f min", subsolar.equationOfTimeMin)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "GHA: ${String.format(Locale.US, "%.2f°", subsolar.ghaDeg)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Simulation Slider Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSimulating) "Simulation Time Offset: ${String.format(Locale.US, "%+.1f h", simHourOffset)}" else "Live Real-Time Clock",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = onToggleSimulate,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSimulating) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isSimulating) "Reset Live" else "Time Scrub",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            AnimatedVisibility(visible = isSimulating) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Slider(
                        value = simHourOffset,
                        onValueChange = onSimHourChange,
                        valueRange = -24f..24f,
                        colors = SliderDefaults.colors(
                            thumbColor = Amber60,
                            activeTrackColor = Amber60
                        )
                    )
                }
            }
        }
    }
}

// ── 2D Interactive Solar Zenith World Map Canvas ──────────────────────────────

@Composable
private fun SubsolarWorldMapCanvas(
    subsolar: SubsolarCoordinates,
    observerLat: Double,
    observerLon: Double,
    onMapTap: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_anim")
    val coronaRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "corona_radius"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A)) // Deep space slate navy
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val normX = (offset.x / w).coerceIn(0f, 1f)
                    val normY = (offset.y / h).coerceIn(0f, 1f)
                    val tappedLon = (normX * 360.0) - 180.0
                    val tappedLat = 90.0 - (normY * 180.0)
                    onMapTap(tappedLat, tappedLon)
                }
            }
    ) {
        val w = size.width
        val h = size.height

        fun mapToScreen(lat: Double, lon: Double): Offset {
            val x = ((lon + 180.0) / 360.0 * w).toFloat()
            val y = ((90.0 - lat) / 180.0 * h).toFloat()
            return Offset(x, y)
        }

        // Draw Lat/Lon Graticule Grid
        val gridColor = Color.White.copy(alpha = 0.08f)
        for (gridLat in -60..60 step 30) {
            val y = ((90.0 - gridLat) / 180.0 * h).toFloat()
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }
        for (gridLon in -150..150 step 30) {
            val x = ((gridLon + 180.0) / 360.0 * w).toFloat()
            drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
        }

        // Equator Line
        val equatorY = (h / 2f)
        drawLine(
            color = Color.White.copy(alpha = 0.25f),
            start = Offset(0f, equatorY),
            end = Offset(w, equatorY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )

        // Tropics of Cancer & Capricorn Lines (+23.44° & -23.44°)
        val cancerY = ((90.0 - 23.44) / 180.0 * h).toFloat()
        val capricornY = ((90.0 - (-23.44)) / 180.0 * h).toFloat()
        val tropicColor = Amber60.copy(alpha = 0.35f)
        drawLine(tropicColor, Offset(0f, cancerY), Offset(w, cancerY), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
        drawLine(tropicColor, Offset(0f, capricornY), Offset(w, capricornY), strokeWidth = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))

        // Draw Continent Outlines
        val continentColor = Color(0xFF334155) // Slate blue
        GlobeMath.CONTINENT_POLYLINES.forEach { polyline ->
            if (polyline.isNotEmpty()) {
                val path = Path()
                val first = mapToScreen(polyline.first().first, polyline.first().second)
                path.moveTo(first.x, first.y)
                for (i in 1 until polyline.size) {
                    val pt = mapToScreen(polyline[i].first, polyline[i].second)
                    path.lineTo(pt.x, pt.y)
                }
                drawPath(path, color = continentColor, style = Stroke(width = 1.2f, cap = StrokeCap.Round))
            }
        }

        // Subsolar Point & Observer Screen Coordinates
        val subsolarPos = mapToScreen(subsolar.latitude, subsolar.longitude)
        val observerPos = mapToScreen(observerLat, observerLon)

        // Geodesic Great-Circle line from Observer to Subsolar Point
        val linePath = Path().apply {
            moveTo(observerPos.x, observerPos.y)
            lineTo(subsolarPos.x, subsolarPos.y)
        }
        drawPath(
            path = linePath,
            color = Cyan60.copy(alpha = 0.7f),
            style = Stroke(
                width = 2f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )

        // Observer Pin Marker
        drawCircle(
            color = Cyan60.copy(alpha = 0.3f),
            radius = 12f,
            center = observerPos
        )
        drawCircle(
            color = Cyan60,
            radius = 5f,
            center = observerPos
        )

        // Subsolar Point Glowing Pulsing Sun Orb
        drawCircle(
            color = Amber60.copy(alpha = (1f - (coronaRadius / 24f)).coerceIn(0f, 0.6f)),
            radius = coronaRadius,
            center = subsolarPos
        )
        drawCircle(
            color = Amber60.copy(alpha = 0.35f),
            radius = 12f,
            center = subsolarPos
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF7ED), Amber60, Color(0xFFEA580C)),
                center = subsolarPos,
                radius = 8f
            ),
            radius = 7f,
            center = subsolarPos
        )
    }
}

// ── Observer Zenith & Great-Circle Telemetry Card ─────────────────────────────

@Composable
private fun ObserverTelemetryCard(telemetry: ObserverZenithTelemetry) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        telemetry.observerCityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        telemetry.observerCountry,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (telemetry.isDaylight) Amber60.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = telemetry.zenithStatus,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (telemetry.isDaylight) Amber60 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4-Column Grid Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TelemetryMetricItem(
                    label = "Solar Elevation",
                    value = String.format(Locale.US, "%+.1f°", telemetry.solarElevationDeg),
                    color = if (telemetry.solarElevationDeg >= 0) Amber60 else Color.Gray
                )

                TelemetryMetricItem(
                    label = "Zenith Angle",
                    value = String.format(Locale.US, "%.1f°", telemetry.zenithAngleDeg),
                    color = Cyan60
                )

                TelemetryMetricItem(
                    label = "Great-Circle Dist",
                    value = String.format(Locale.US, "%,.0f km", telemetry.greatCircleDistanceKm),
                    color = Cobalt60
                )

                TelemetryMetricItem(
                    label = "Solar Azimuth",
                    value = String.format(Locale.US, "%.0f°", telemetry.solarAzimuthDeg),
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

// ── 3D Upright 1m Pole Shadow Simulator Canvas ────────────────────────────────

@Composable
private fun PoleShadowSimulatorCard(telemetry: ObserverZenithTelemetry) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "1-Meter Vertical Pole Shadow Simulator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                val shadowText = when {
                    !telemetry.isDaylight -> "Night (No Shadow)"
                    telemetry.shadowLength1mPoleMeters <= 0.02 -> "0.00 m (ZERO SHADOW)"
                    else -> String.format(Locale.US, "%.2f m", telemetry.shadowLength1mPoleMeters)
                }

                Text(
                    text = shadowText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (telemetry.shadowLength1mPoleMeters <= 0.02) SuccessGreen else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
            ) {
                val w = size.width
                val h = size.height
                val groundY = h * 0.75f
                val poleBaseX = w * 0.45f
                val poleHeightPx = 50f
                val poleTopY = groundY - poleHeightPx

                // Ground Horizon Line
                drawLine(
                    color = Color.White.copy(alpha = 0.2f),
                    start = Offset(20f, groundY),
                    end = Offset(w - 20f, groundY),
                    strokeWidth = 2f
                )

                // Draw Vertical 1m Pole
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(poleBaseX, groundY),
                    end = Offset(poleBaseX, poleTopY),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )

                // Draw Ground Cast Shadow
                if (telemetry.isDaylight && telemetry.solarElevationDeg > 0.5) {
                    val shadowLengthScaledPx = (telemetry.shadowLength1mPoleMeters.toFloat() * poleHeightPx).coerceIn(0f, w * 0.45f)
                    // If azimuth is East vs West
                    val shadowDirectionSign = if (telemetry.solarAzimuthDeg in 0.0..180.0) 1f else -1f
                    val shadowEndX = poleBaseX + (shadowDirectionSign * shadowLengthScaledPx)

                    // Ground shadow ellipse / line
                    drawLine(
                        color = Color(0xFF020617).copy(alpha = 0.85f),
                        start = Offset(poleBaseX, groundY),
                        end = Offset(shadowEndX, groundY),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )

                    // Solar Incident Ray from Sun
                    val sunRayLength = 100f
                    val elevRad = Math.toRadians(telemetry.solarElevationDeg)
                    val sunX = poleBaseX - (shadowDirectionSign * cos(elevRad) * sunRayLength).toFloat()
                    val sunY = poleTopY - (sin(elevRad) * sunRayLength).toFloat()

                    drawLine(
                        color = Amber60.copy(alpha = 0.4f),
                        start = Offset(sunX, sunY),
                        end = Offset(poleBaseX, poleTopY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    // Mini Sun Orb at top of ray
                    drawCircle(
                        color = Amber60,
                        radius = 6f,
                        center = Offset(sunX, sunY)
                    )
                }

                // Zero Shadow celebration badge
                if (telemetry.solarElevationDeg >= 89.0) {
                    drawCircle(
                        color = SuccessGreen.copy(alpha = 0.3f),
                        radius = 16f,
                        center = Offset(poleBaseX, groundY)
                    )
                }
            }
        }
    }
}

// ── Lahaina Noon (Zero-Shadow Day) Almanac Card ───────────────────────────────

@Composable
private fun LahainaNoonCard(event: LahainaNoonEvent) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Brightness7, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Lahaina Noon (Zero Shadow) Almanac",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (event.isTropical) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Amber60.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Tropical Zone",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Amber60,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (event.isTropical && event.firstDate != null && event.secondDate != null) {
                val fmt = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Spring Zenith Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            event.firstDate.format(fmt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Autumn Zenith Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            event.secondDate.format(fmt),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = if (event.isTodayZeroShadow) SuccessGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (event.isTodayZeroShadow) {
                                "TODAY IS LAHAINA NOON! The sun is directly overhead with zero shadow at solar noon."
                            } else {
                                "Next Zero Shadow Day: ${event.nextDate?.format(fmt)} (${event.daysUntilNext ?: 0} days remaining)"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${event.cityName} (${String.format(Locale.US, "%.1f°", abs(event.latitude))}) lies outside the Tropical Zone (±23.44°). The Sun never reaches true 90° zenith here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
