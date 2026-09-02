package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.drawscope.clipPath
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
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.hypot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobeScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var centerLat by remember { mutableFloatStateOf(20.0f) }
    var centerLon by remember { mutableFloatStateOf(0.0f) }
    var isAutoRotating by remember { mutableStateOf(true) }
    var selectedCity by remember { mutableStateOf<GlobeCityPin?>(GlobeMath.MAJOR_WORLD_CITIES.first()) }

    val now = remember { ZonedDateTime.now() }
    val subsolarPoint = remember(now) { GlobeMath.calculateSubsolarPoint(now) }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }

    // Auto-rotation loop
    LaunchedEffect(isAutoRotating) {
        while (isAutoRotating) {
            kotlinx.coroutines.delay(40L)
            centerLon = ((centerLon + 0.35f + 180f).mod(360f)) - 180f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interactive 3D Earth Globe",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isAutoRotating = !isAutoRotating }) {
                        Icon(
                            imageVector = Icons.Filled.Autorenew,
                            contentDescription = "Toggle Auto-Rotation",
                            tint = if (isAutoRotating) Cyan60 else MaterialTheme.colorScheme.outline
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
            // Interactive 3D Globe Canvas Container
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF030712) // Deep space black
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
                                text = "REAL-TIME DAY / NIGHT TERMINATOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Text(
                                text = "Drag to Rotate",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3D Globe Canvas
                        EarthGlobeCanvas(
                            centerLat = centerLat,
                            centerLon = centerLon,
                            subsolarPoint = subsolarPoint,
                            onDragDelta = { dx, dy ->
                                isAutoRotating = false
                                centerLon = ((centerLon - dx * 0.4f + 180f).mod(360f)) - 180f
                                centerLat = (centerLat + dy * 0.4f).coerceIn(-80f, 80f)
                            },
                            onSelectCity = { city ->
                                selectedCity = city
                            },
                            modifier = Modifier
                                .size(280.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Focus Preset Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            RegionFocusChip("Americas", -90f, 15f) { lon, lat -> centerLon = lon; centerLat = lat; isAutoRotating = false }
                            RegionFocusChip("Europe / Africa", 15f, 30f) { lon, lat -> centerLon = lon; centerLat = lat; isAutoRotating = false }
                            RegionFocusChip("Asia / Pacific", 120f, 20f) { lon, lat -> centerLon = lon; centerLat = lat; isAutoRotating = false }
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
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
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = cityTime.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
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
                                        imageVector = if (isDay) Icons.Filled.WbSunny else Icons.Filled.Public,
                                        contentDescription = null,
                                        tint = if (isDay) Amber60 else Cobalt60,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isDay) "Sunlit" else "Night",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isDay) Amber60 else Cobalt60
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Major World Capitals Grid
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
                            onClick = {
                                selectedCity = city
                                centerLon = city.longitude.toFloat()
                                centerLat = city.latitude.toFloat().coerceIn(-60f, 60f)
                                isAutoRotating = false
                            },
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
private fun RegionFocusChip(
    label: String,
    lon: Float,
    lat: Float,
    onClick: (Float, Float) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.clickable { onClick(lon, lat) }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EarthGlobeCanvas(
    centerLat: Float,
    centerLon: Float,
    subsolarPoint: com.harichselvamc.seetime.util.SubsolarPoint,
    onDragDelta: (Float, Float) -> Unit,
    onSelectCity: (GlobeCityPin) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onDragDelta(dragAmount.x, dragAmount.y)
            }
        }
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.88f

        // Starfield background
        drawCircle(color = Color(0xFF020617), radius = radius * 1.15f, center = center)

        // Atmosphere glow halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Cyan60.copy(alpha = 0.35f), Color.Transparent),
                center = center,
                radius = radius * 1.15f
            ),
            radius = radius * 1.15f,
            center = center
        )

        // Ocean Base Sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF082F49)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Clip everything inside the sphere
        val spherePath = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius))
        }

        clipPath(spherePath) {
            // Graticule Lines (Latitude circles)
            listOf(-60.0, -30.0, 0.0, 30.0, 60.0).forEach { latLine ->
                val linePath = Path()
                var hasStarted = false
                for (lonStep in -180..180 step 10) {
                    val pt = GlobeMath.projectOrthographic(
                        latDeg = latLine,
                        lonDeg = lonStep.toDouble(),
                        centerLatDeg = centerLat.toDouble(),
                        centerLonDeg = centerLon.toDouble(),
                        radius = radius,
                        subsolarPoint = subsolarPoint
                    )
                    if (pt.isVisible) {
                        val screenX = center.x + pt.x
                        val screenY = center.y + pt.y
                        if (!hasStarted) {
                            linePath.moveTo(screenX, screenY)
                            hasStarted = true
                        } else {
                            linePath.lineTo(screenX, screenY)
                        }
                    } else {
                        hasStarted = false
                    }
                }
                drawPath(
                    path = linePath,
                    color = Color.White.copy(alpha = 0.12f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Continents Outlines & Landmass
            GlobeMath.CONTINENT_POLYLINES.forEach { polyline ->
                val continentPath = Path()
                var hasStarted = false
                polyline.forEach { (lat, lon) ->
                    val pt = GlobeMath.projectOrthographic(
                        latDeg = lat,
                        lonDeg = lon,
                        centerLatDeg = centerLat.toDouble(),
                        centerLonDeg = centerLon.toDouble(),
                        radius = radius,
                        subsolarPoint = subsolarPoint
                    )
                    if (pt.isVisible) {
                        val screenX = center.x + pt.x
                        val screenY = center.y + pt.y
                        if (!hasStarted) {
                            continentPath.moveTo(screenX, screenY)
                            hasStarted = true
                        } else {
                            continentPath.lineTo(screenX, screenY)
                        }
                    } else {
                        hasStarted = false
                    }
                }

                drawPath(
                    path = continentPath,
                    color = Color(0xFF15803D).copy(alpha = 0.85f), // Lush Green
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Solar Day / Night Shadow Terminator Layer
            // Project subsolar point to determine sun orientation
            val subPt = GlobeMath.projectOrthographic(
                latDeg = subsolarPoint.latitude,
                lonDeg = subsolarPoint.longitude,
                centerLatDeg = centerLat.toDouble(),
                centerLonDeg = centerLon.toDouble(),
                radius = radius,
                subsolarPoint = subsolarPoint
            )

            // Night hemisphere shadow gradient
            val shadowCenter = Offset(center.x - subPt.x * 0.8f, center.y - subPt.y * 0.8f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF020617).copy(alpha = 0.75f), Color.Transparent),
                    center = shadowCenter,
                    radius = radius * 1.1f
                ),
                radius = radius,
                center = center
            )

            // Subsolar Point Sun Marker (if visible)
            if (subPt.isVisible) {
                val sunScreen = Offset(center.x + subPt.x, center.y + subPt.y)
                drawCircle(color = Amber60.copy(alpha = 0.4f), radius = 10.dp.toPx(), center = sunScreen)
                drawCircle(color = Amber60, radius = 5.dp.toPx(), center = sunScreen)
            }

            // City Pins
            GlobeMath.MAJOR_WORLD_CITIES.forEach { city ->
                val pt = GlobeMath.projectOrthographic(
                    latDeg = city.latitude,
                    lonDeg = city.longitude,
                    centerLatDeg = centerLat.toDouble(),
                    centerLonDeg = centerLon.toDouble(),
                    radius = radius,
                    subsolarPoint = subsolarPoint
                )
                if (pt.isVisible) {
                    val cityScreen = Offset(center.x + pt.x, center.y + pt.y)
                    val pinColor = if (pt.isDaylit) Amber60 else Cyan60

                    drawCircle(color = pinColor.copy(alpha = 0.35f), radius = 6.dp.toPx(), center = cityScreen)
                    drawCircle(color = pinColor, radius = 3.5.dp.toPx(), center = cityScreen)
                }
            }
        }

        // Outer Globe Rim
        drawCircle(
            color = Cyan60.copy(alpha = 0.5f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
