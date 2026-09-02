package com.harichselvamc.seetime.ui

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.PlanetarySolarEngine
import com.harichselvamc.seetime.util.SolsticeEquinoxEvent
import com.harichselvamc.seetime.util.VisiblePlanet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolarAlmanacScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val today = remember { LocalDate.now() }

    val seasonalDetails = remember(today) {
        PlanetarySolarEngine.calculateSeasonalProgress(today)
    }

    val visiblePlanets = remember(today) {
        PlanetarySolarEngine.calculateVisiblePlanets(today)
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.WbSunny,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seasonal & Planetary Almanac",
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
            // Earth Orbit Seasonal Wheel Canvas Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF030712) // Space black
                    ),
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
                                text = "EARTH SOLAR ORBIT CYCLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Amber60
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Amber60.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${seasonalDetails.currentSeason.displayName} ${seasonalDetails.currentSeason.emoji}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Amber60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Orbit Wheel Canvas
                        EarthOrbitSeasonalWheelCanvas(
                            dayOfYear = today.dayOfYear,
                            modifier = Modifier
                                .size(230.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${seasonalDetails.currentSeason.displayName} Season Progress: ${seasonalDetails.seasonProgressPercent.toInt()}%",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { seasonalDetails.seasonProgressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Amber60,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }

            // Next Solstice / Equinox Hero Card
            item {
                val nextEv = seasonalDetails.nextEvent
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Amber60.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Amber60
                            ) {
                                Text(
                                    text = "UPCOMING SOLAR MILESTONE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "in ${nextEv.daysUntil} days",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Amber60
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = nextEv.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = nextEv.date.format(dateFormatter),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Amber60
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = nextEv.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Cobalt60,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Daylight Trend: ${seasonalDetails.daylightTrend}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Cobalt60
                            )
                        }
                    }
                }
            }

            // 4 Year Astronomical Solstices & Equinoxes List
            item {
                Text(
                    text = "ASTRONOMICAL SOLSTICE & EQUINOX SCHEDULE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(seasonalDetails.allYearEvents) { ev ->
                SolsticeEventRowCard(event = ev)
            }

            // Visible Planets Guide
            item {
                Text(
                    text = "5 NAKED-EYE VISIBLE PLANETS TONIGHT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(visiblePlanets) { planet ->
                VisiblePlanetCard(planet = planet)
            }
        }
    }
}

@Composable
private fun EarthOrbitSeasonalWheelCanvas(
    dayOfYear: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val orbitRadius = (w / 2) * 0.72f

        // Central Sun
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Amber60, Amber60.copy(alpha = 0.2f), Color.Transparent),
                center = center,
                radius = 28.dp.toPx() * pulseScale
            ),
            radius = 28.dp.toPx() * pulseScale,
            center = center
        )
        drawCircle(color = Color(0xFFFBBF24), radius = 12.dp.toPx(), center = center)

        // 4 Season Orbit Quadrant Arcs
        val strokeWidth = 5.dp.toPx()
        val rect = androidx.compose.ui.geometry.Rect(center.x - orbitRadius, center.y - orbitRadius, center.x + orbitRadius, center.y + orbitRadius)

        // Spring (Mar 20 ~ Day 80) -> Top right: -90° to 0°
        drawArc(color = SuccessGreen, startAngle = -90f, sweepAngle = 90f, useCenter = false, topLeft = rect.topLeft, size = rect.size, style = Stroke(width = strokeWidth))
        // Summer (Jun 21 ~ Day 172) -> Bottom right: 0° to 90°
        drawArc(color = Amber60, startAngle = 0f, sweepAngle = 90f, useCenter = false, topLeft = rect.topLeft, size = rect.size, style = Stroke(width = strokeWidth))
        // Autumn (Sep 22 ~ Day 265) -> Bottom left: 90° to 180°
        drawArc(color = Color(0xFFEA580C), startAngle = 90f, sweepAngle = 90f, useCenter = false, topLeft = rect.topLeft, size = rect.size, style = Stroke(width = strokeWidth))
        // Winter (Dec 21 ~ Day 355) -> Top left: 180° to 270°
        drawArc(color = Cyan60, startAngle = 180f, sweepAngle = 90f, useCenter = false, topLeft = rect.topLeft, size = rect.size, style = Stroke(width = strokeWidth))

        // Earth Orbiting Marker Position
        val earthAngleDeg = ((dayOfYear / 365.25f) * 360f) - 90f - 80f // Aligned with Spring Equinox start
        val earthRad = Math.toRadians(earthAngleDeg.toDouble())
        val earthX = (center.x + orbitRadius * cos(earthRad)).toFloat()
        val earthY = (center.y + orbitRadius * sin(earthRad)).toFloat()

        // Glowing Earth
        drawCircle(color = Cyan60.copy(alpha = 0.4f), radius = 10.dp.toPx(), center = Offset(earthX, earthY))
        drawCircle(color = Color(0xFF0284C7), radius = 6.dp.toPx(), center = Offset(earthX, earthY))
        drawCircle(color = Color(0xFF22C55E), radius = 2.5.dp.toPx(), center = Offset(earthX - 1.5f, earthY - 1.5f))
    }
}

@Composable
private fun SolsticeEventRowCard(event: SolsticeEquinoxEvent) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${event.date.format(DateTimeFormatter.ofPattern("MMM d"))} · ${event.daylightCharacteristic}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Amber60.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "${event.daysUntil}d",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Amber60,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun VisiblePlanetCard(planet: VisiblePlanet) {
    val planetColor = Color(planet.hexColor)
    Card(
        shape = RoundedCornerShape(18.dp),
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
                color = planetColor.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = planet.emoji, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = planet.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = planetColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = planet.visibilityTier,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = planetColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Best Viewing: ${planet.bestViewingTime} (${planet.constellation})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Cobalt60
                )
                Text(
                    text = planet.opticalRecommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
