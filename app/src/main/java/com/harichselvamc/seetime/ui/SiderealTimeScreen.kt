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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.CelestialStar
import com.harichselvamc.seetime.util.GlobeMath
import com.harichselvamc.seetime.util.SiderealTimeEngine
import com.harichselvamc.seetime.util.StarTransitStatus
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiderealTimeScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    var selectedCityIndex by remember { mutableIntStateOf(0) }
    val city = GlobeMath.MAJOR_WORLD_CITIES[selectedCityIndex % GlobeMath.MAJOR_WORLD_CITIES.size]

    var liveEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live sidereal ticker loop (50 ms)
    LaunchedEffect(Unit) {
        while (true) {
            liveEpochMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(50L)
        }
    }

    val siderealDetails = remember(liveEpochMillis, city) {
        SiderealTimeEngine.calculateSiderealTimeDetails(liveEpochMillis, city.longitude)
    }

    val starTransits = remember(siderealDetails.lstHours, city) {
        SiderealTimeEngine.MAJOR_CELESTIAL_STARS.map { star ->
            SiderealTimeEngine.calculateStarTransit(star, siderealDetails.lstHours, city.latitude)
        }.sortedBy { it.hoursUntilTransit }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sidereal Time & Star Transit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Cyan60.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "23h 56m 04s DAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = Cyan60,
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
            // Location Chips
            item {
                Text(
                    text = "OBSERVATION LOCATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GlobeMath.MAJOR_WORLD_CITIES.indices.toList()) { idx ->
                        val itemCity = GlobeMath.MAJOR_WORLD_CITIES[idx]
                        val isSelected = selectedCityIndex == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCityIndex = idx },
                            label = { Text("${itemCity.name}, ${itemCity.country}", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan60.copy(alpha = 0.15f),
                                selectedLabelColor = Cyan60
                            )
                        )
                    }
                }
            }

            // 24-Hour Celestial Sidereal Dial Canvas Card
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
                                text = "CELESTIAL SPHERE SIDEREAL DIAL",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Text(
                                text = "Vernal Equinox ♈ ${String.format(Locale.US, "%.1f°", siderealDetails.vernalEquinoxAngleDeg)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = Amber60
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 24H Sidereal Celestial Dial Canvas
                        CelestialSiderealDialCanvas(
                            lstHours = siderealDetails.lstHours.toFloat(),
                            stars = SiderealTimeEngine.MAJOR_CELESTIAL_STARS,
                            modifier = Modifier.size(230.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Monospace Local Sidereal Time
                        Text(
                            text = siderealDetails.lstFormatted,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Greenwich Mean: ${siderealDetails.gmstFormatted}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sidereal clocks run +3m 55.9s faster per solar day, tracking Earth's true 360° inertial rotation.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cyan60
                        )
                    }
                }
            }

            // Astronomy Explanation Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Star Culmination Law (LST = RA)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Any celestial object crosses your local meridian at highest elevation when Local Sidereal Time matches its Right Ascension (LST = RA). Telescope astronomers align celestial equatorial mounts using this exact calculation.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Major Navigation Stars Meridian Transit Timeline Section
            item {
                Text(
                    text = "NAVIGATION STARS MERIDIAN CULMINATIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(starTransits) { transit ->
                StarTransitRowCard(transit = transit)
            }
        }
    }
}

@Composable
private fun StarTransitRowCard(transit: StarTransitStatus) {
    val star = transit.star
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (transit.isCulminatingNow) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (transit.isCulminatingNow) SuccessGreen.copy(alpha = 0.2f) else Cyan60.copy(alpha = 0.15f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(star.emoji, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = star.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${star.constellation})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = "RA: ${String.format(Locale.US, "%.2fh", star.rightAscensionHours)} · Dec: ${String.format(Locale.US, "%+.1f°", star.declinationDeg)} · Mag: ${star.magnitude}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = Cobalt60
                    )
                    Text(
                        text = "Meridian Altitude: ${String.format(Locale.US, "%.1f°", transit.meridianAltitudeDeg)} above horizon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (transit.isCulminatingNow) SuccessGreen else Amber60.copy(alpha = 0.15f)
            ) {
                Text(
                    text = transit.formattedTransitIn,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (transit.isCulminatingNow) Color.White else Amber60,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CelestialSiderealDialCanvas(
    lstHours: Float,
    stars: List<CelestialStar>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.85f

        // Starry Night Sphere Base
        drawCircle(color = Color(0xFF0F172A), radius = radius, center = center)
        drawCircle(color = Color(0xFF1E293B), radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

        // Rotating Celestial Grid based on LST
        val rotationAngle = (lstHours / 24f) * 360f

        rotate(-rotationAngle, pivot = center) {
            // 24 Hour Ticks (Right Ascension 0h..23h)
            for (i in 0 until 24) {
                val angleDeg = (i * 15f) - 90f
                val rad = Math.toRadians(angleDeg.toDouble())
                val start = Offset((center.x + radius * 0.88f * cos(rad)).toFloat(), (center.y + radius * 0.88f * sin(rad)).toFloat())
                val end = Offset((center.x + radius * 0.96f * cos(rad)).toFloat(), (center.y + radius * 0.96f * sin(rad)).toFloat())

                drawLine(
                    color = if (i == 0) Amber60 else Color.Gray.copy(alpha = 0.4f),
                    start = start,
                    end = end,
                    strokeWidth = if (i == 0) 2.5.dp.toPx() else 1.dp.toPx()
                )
            }

            // Vernal Equinox 0h Aries Point Marker
            val ariesRad = Math.toRadians(-90.0)
            val ariesX = (center.x + radius * 0.78f * cos(ariesRad)).toFloat()
            val ariesY = (center.y + radius * 0.78f * sin(ariesRad)).toFloat()
            drawCircle(color = Amber60, radius = 4.dp.toPx(), center = Offset(ariesX, ariesY))

            // Stars Placed along their Right Ascension & Declination
            stars.forEach { star ->
                val starAngleDeg = (star.rightAscensionHours.toFloat() * 15f) - 90f
                val rad = Math.toRadians(starAngleDeg.toDouble())
                // Radial distance based on declination (-90..+90 mapped to inner/outer radius)
                val decNorm = ((star.declinationDeg.toFloat() + 90f) / 180f).coerceIn(0.1f, 0.9f)
                val starR = radius * (0.25f + decNorm * 0.55f)

                val starX = (center.x + starR * cos(rad)).toFloat()
                val starY = (center.y + starR * sin(rad)).toFloat()

                val starDotRadius = when {
                    star.magnitude < 0.0 -> 5.dp.toPx()
                    star.magnitude < 1.0 -> 3.5.dp.toPx()
                    else -> 2.5.dp.toPx()
                }

                drawCircle(color = Color.White.copy(alpha = 0.9f), radius = starDotRadius, center = Offset(starX, starY))
            }
        }

        // Local Meridian Zenith Line (Top of dial = Zenith)
        drawLine(
            color = SuccessGreen,
            start = Offset(center.x, center.y - radius),
            end = Offset(center.x, center.y),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(color = Cyan60, radius = 4.dp.toPx(), center = center)
    }
}
