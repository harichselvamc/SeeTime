package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.GlobeCityPin
import com.harichselvamc.seetime.util.GlobeMath
import com.harichselvamc.seetime.util.WorldMapEngine
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldMapScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var showTerminator by remember { mutableStateOf(true) }
    var showTimezoneBands by remember { mutableStateOf(true) }
    var selectedCity by remember { mutableStateOf<GlobeCityPin?>(GlobeMath.MAJOR_WORLD_CITIES.first()) }

    val now = remember { ZonedDateTime.now() }
    val subsolarPoint = remember(now) { GlobeMath.calculateSubsolarPoint(now) }
    val terminatorWave = remember(subsolarPoint) { WorldMapEngine.calculateTerminatorWave(subsolarPoint) }
    val timezoneBands = remember { WorldMapEngine.generateTimezoneBands() }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "World Time Map & Terminator",
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
            // Interactive 2D World Map Canvas Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF030712) // Space black
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GLOBAL SOLAR SHADOW & TIME MAP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Text(
                                text = "Tap City Pin",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // World Map Canvas
                        WorldMap2DCanvas(
                            subsolarPoint = subsolarPoint,
                            terminatorWave = terminatorWave,
                            timezoneBands = timezoneBands,
                            showTerminator = showTerminator,
                            showTimezoneBands = showTimezoneBands,
                            selectedCity = selectedCity,
                            onCityTapped = { city ->
                                selectedCity = city
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Map Layer Toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = showTerminator,
                                    onCheckedChange = { showTerminator = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Amber60, checkedTrackColor = Amber60.copy(alpha = 0.4f))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Day/Night Wave", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = showTimezoneBands,
                                    onCheckedChange = { showTimezoneBands = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Cobalt60, checkedTrackColor = Cobalt60.copy(alpha = 0.4f))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("UTC Iso-Bands", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Selected City Live Status Card
            selectedCity?.let { city ->
                item {
                    val cityZone = remember(city) {
                        try { ZoneId.of(city.zoneId) } catch (_: Exception) { ZoneId.systemDefault() }
                    }
                    val cityTime = remember(cityZone) { ZonedDateTime.now(cityZone) }
                    val isDay = cityTime.hour in 6..18

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = if (isDay) Amber60 else Cobalt60,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${city.name}, ${city.country}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cityTime.format(timeFormatter),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = cityTime.format(DateTimeFormatter.ofPattern("EEEE, MMM d · zzz")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDay) Amber60.copy(alpha = 0.15f) else Cobalt60.copy(alpha = 0.15f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (isDay) Icons.Filled.WbSunny else Icons.Filled.Map,
                                        contentDescription = null,
                                        tint = if (isDay) Amber60 else Cobalt60,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isDay) "Daylight" else "Night",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDay) Amber60 else Cobalt60
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Major World Timezone Pins Chips
            item {
                Text(
                    text = "WORLD TIMEZONE PINS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GlobeMath.MAJOR_WORLD_CITIES) { city ->
                        val isSelected = selectedCity?.name == city.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCity = city },
                            label = { Text(city.name, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan60.copy(alpha = 0.15f),
                                selectedLabelColor = Cyan60
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorldMap2DCanvas(
    subsolarPoint: com.harichselvamc.seetime.util.SubsolarPoint,
    terminatorWave: List<Pair<Double, Double>>,
    timezoneBands: List<com.harichselvamc.seetime.util.TimezoneBand>,
    showTerminator: Boolean,
    showTimezoneBands: Boolean,
    selectedCity: GlobeCityPin?,
    onCityTapped: (GlobeCityPin) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { tapOffset ->
                val w = size.width.toFloat()
                val h = size.height.toFloat()
                // Find nearest city to tap
                val tapped = GlobeMath.MAJOR_WORLD_CITIES.minByOrNull { city ->
                    val pt = WorldMapEngine.projectEquirectangular(city.latitude, city.longitude, w, h)
                    hypot(pt.x - tapOffset.x, pt.y - tapOffset.y)
                }
                if (tapped != null) {
                    val pt = WorldMapEngine.projectEquirectangular(tapped.latitude, tapped.longitude, w, h)
                    if (hypot(pt.x - tapOffset.x, pt.y - tapOffset.y) < 40.dp.toPx()) {
                        onCityTapped(tapped)
                    }
                }
            }
        }
    ) {
        val w = size.width
        val h = size.height

        // Ocean Background
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0369A1), Color(0xFF075985), Color(0xFF0C4A6E))
            ),
            size = Size(w, h)
        )

        // Timezone Iso-Contour Bands
        if (showTimezoneBands) {
            timezoneBands.forEach { band ->
                val x = ((band.centerLonDeg + 180f) / 360f) * w
                drawLine(
                    color = Color.White.copy(alpha = 0.08f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Graticule Latitude Lines (Equator, Tropics, Arctic)
        listOf(-66.5, -23.5, 0.0, 23.5, 66.5).forEach { lat ->
            val y = ((90.0 - lat) / 180.0 * h).toFloat()
            drawLine(
                color = if (lat == 0.0) Amber60.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = if (lat == 0.0) 1.5.dp.toPx() else 1.dp.toPx()
            )
        }

        // Continents Outlines
        GlobeMath.CONTINENT_POLYLINES.forEach { polyline ->
            val path = Path()
            var hasStarted = false
            polyline.forEach { (lat, lon) ->
                val pt = WorldMapEngine.projectEquirectangular(lat, lon, w, h)
                if (!hasStarted) {
                    path.moveTo(pt.x, pt.y)
                    hasStarted = true
                } else {
                    path.lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = path,
                color = Color(0xFF15803D).copy(alpha = 0.85f), // Emerald green
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Solar Day / Night Terminator Wave Shading
        if (showTerminator && terminatorWave.isNotEmpty()) {
            val wavePath = Path()
            val firstPt = WorldMapEngine.projectEquirectangular(terminatorWave.first().first, terminatorWave.first().second, w, h)
            wavePath.moveTo(firstPt.x, firstPt.y)

            terminatorWave.forEach { (lat, lon) ->
                val pt = WorldMapEngine.projectEquirectangular(lat, lon, w, h)
                wavePath.lineTo(pt.x, pt.y)
            }

            // Close wave over the night pole (South if subsolar is North, North if subsolar is South)
            val isSubsolarNorth = subsolarPoint.latitude >= 0
            if (isSubsolarNorth) {
                wavePath.lineTo(w, h)
                wavePath.lineTo(0f, h)
            } else {
                wavePath.lineTo(w, 0f)
                wavePath.lineTo(0f, 0f)
            }
            wavePath.close()

            drawPath(
                path = wavePath,
                color = Color(0xFF020617).copy(alpha = 0.65f) // Night shadow
            )

            // Terminator Glowing Border Line
            val linePath = Path()
            linePath.moveTo(firstPt.x, firstPt.y)
            terminatorWave.forEach { (lat, lon) ->
                val pt = WorldMapEngine.projectEquirectangular(lat, lon, w, h)
                linePath.lineTo(pt.x, pt.y)
            }
            drawPath(path = linePath, color = Amber60.copy(alpha = 0.5f), style = Stroke(width = 2.dp.toPx()))
        }

        // Subsolar Zenith Sun Marker
        val sunPt = WorldMapEngine.projectEquirectangular(subsolarPoint.latitude, subsolarPoint.longitude, w, h)
        drawCircle(color = Amber60.copy(alpha = 0.4f), radius = 10.dp.toPx(), center = Offset(sunPt.x, sunPt.y))
        drawCircle(color = Amber60, radius = 5.dp.toPx(), center = Offset(sunPt.x, sunPt.y))

        // City Pins
        GlobeMath.MAJOR_WORLD_CITIES.forEach { city ->
            val pt = WorldMapEngine.projectEquirectangular(city.latitude, city.longitude, w, h)
            val isSelected = selectedCity?.name == city.name
            val pinColor = if (isSelected) Amber60 else Cyan60

            drawCircle(color = pinColor.copy(alpha = 0.4f), radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(), center = Offset(pt.x, pt.y))
            drawCircle(color = pinColor, radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(), center = Offset(pt.x, pt.y))
        }
    }
}
