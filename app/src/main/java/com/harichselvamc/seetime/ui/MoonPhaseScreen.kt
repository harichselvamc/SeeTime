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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.LunarCelestialEngine
import com.harichselvamc.seetime.util.LunarPhaseType
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoonPhaseScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    val primaryZone = uiState.pairs.firstOrNull()?.toZone ?: "Asia/Tokyo"
    val zoneId = remember(primaryZone) {
        try { ZoneId.of(primaryZone) } catch (_: Exception) { ZoneId.systemDefault() }
    }
    val now = remember(zoneId) { ZonedDateTime.now(zoneId) }

    val moonDetails = remember(now) { LunarCelestialEngine.calculateMoonPhase(now) }
    val forecast = remember(now) { LunarCelestialEngine.generateMultiDayForecast(startDate = now.toLocalDate(), daysCount = 7, zoneId = zoneId) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.DarkMode,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Moon & Celestial Stargazing",
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
            // Realistic Moon Graphic Hero Card
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
                            Text(
                                text = "LUNAR ILLUMINATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cyan60.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = primaryZone.substringAfterLast('/').replace('_', ' '),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Cyan60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Moon Realistic Graphic Canvas
                        RealisticMoonCanvas(
                            illumination = moonDetails.illuminationPercentage.toFloat(),
                            isWaxing = moonDetails.isWaxing,
                            modifier = Modifier.size(170.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "${moonDetails.phaseType.displayName} ${moonDetails.phaseType.emoji}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%% Illuminated · Lunar Age: %.1f days", moonDetails.illuminationPercentage, moonDetails.ageDays),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Cyan60
                        )
                    }
                }
            }

            // Stargazing & Dark Sky Window Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            moonDetails.stargazingScore >= 8 -> SuccessGreen.copy(alpha = 0.12f)
                            moonDetails.stargazingScore >= 5 -> Amber60.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Stars,
                                    contentDescription = null,
                                    tint = if (moonDetails.stargazingScore >= 8) SuccessGreen else Amber60,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stargazing Index: ${moonDetails.stargazingRating}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (moonDetails.stargazingScore >= 8) SuccessGreen else Amber60
                            ) {
                                Text(
                                    text = "${moonDetails.stargazingScore}/10",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { moonDetails.stargazingScore / 10.0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (moonDetails.stargazingScore >= 8) SuccessGreen else Amber60,
                            trackColor = Color.LightGray.copy(alpha = 0.3f)
                        )

                        Text(
                            text = moonDetails.stargazingRecommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NightsStay,
                                contentDescription = null,
                                tint = Cobalt60,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Optimal Dark Window: ${moonDetails.optimalDarkSkyWindow}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Cobalt60
                            )
                        }
                    }
                }
            }

            // Next Major Lunar Milestones
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MilestoneCard(
                        title = "Next Full Moon 🌕",
                        dateStr = moonDetails.nextFullMoonDate.format(dateFormatter),
                        daysLeftStr = "${moonDetails.daysUntilFullMoon} days away",
                        accentColor = Amber60,
                        modifier = Modifier.weight(1f)
                    )

                    MilestoneCard(
                        title = "Next New Moon 🌑",
                        dateStr = moonDetails.nextNewMoonDate.format(dateFormatter),
                        daysLeftStr = "${moonDetails.daysUntilNewMoon} days away",
                        accentColor = Cyan60,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 7-Day Lunar Forecast Strip
            item {
                Text(
                    text = "7-DAY LUNAR FORECAST",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(forecast) { daySummary ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (daySummary.isMajorPhase) Cobalt60.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = daySummary.date.format(DateTimeFormatter.ofPattern("EEE")),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = daySummary.phaseType.emoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${daySummary.illuminationPercentage}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Cobalt60
                                )
                            }
                        }
                    }
                }
            }

            // Major Meteor Showers Celestial Guide
            item {
                Text(
                    text = "MAJOR ANNUAL METEOR SHOWERS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(LunarCelestialEngine.MAJOR_METEOR_SHOWERS) { shower ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Cyan60.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Cyan60,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = shower.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = shower.peakDate,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber60
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Peak Rate: ${shower.zhrRate} (${shower.constellation})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Cobalt60
                            )
                            Text(
                                text = shower.viewingAdvice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RealisticMoonCanvas(
    illumination: Float,
    isWaxing: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "moon_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.82f

        // Ambient Celestial Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Cyan60.copy(alpha = 0.25f), Color.Transparent),
                center = center,
                radius = radius * 1.35f * glowScale
            ),
            radius = radius * 1.35f * glowScale,
            center = center
        )

        // Moon Base Disc (Dark side of moon)
        drawCircle(
            color = Color(0xFF1E293B), // Slate dark
            radius = radius,
            center = center
        )

        // Subtle Surface Craters on Dark Side
        drawCircle(color = Color(0xFF0F172A), radius = radius * 0.18f, center = Offset(center.x - radius * 0.35f, center.y - radius * 0.25f))
        drawCircle(color = Color(0xFF0F172A), radius = radius * 0.12f, center = Offset(center.x + radius * 0.30f, center.y + radius * 0.30f))
        drawCircle(color = Color(0xFF0F172A), radius = radius * 0.08f, center = Offset(center.x - radius * 0.15f, center.y + radius * 0.40f))

        // Illuminated Moon Segment (Using clipping/drawing arc approximation)
        val illumFrac = (illumination / 100f).coerceIn(0f, 1f)
        val moonLitColor = Color(0xFFF8FAFC) // Bright pearl moonlight

        if (illumFrac > 0.05f) {
            // Draw illuminated disc
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(moonLitColor, Color(0xFFCBD5E1)),
                    center = Offset(center.x + if (isWaxing) radius * 0.3f else -radius * 0.3f, center.y),
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Shadow terminator mask
            if (illumFrac < 0.95f) {
                val shadowWidth = (1f - illumFrac) * (2 * radius)
                val shadowCenterX = if (isWaxing) center.x - (radius - shadowWidth / 2) else center.x + (radius - shadowWidth / 2)

                drawOval(
                    color = Color(0xFF1E293B).copy(alpha = 0.95f),
                    topLeft = Offset(shadowCenterX - shadowWidth / 2, center.y - radius),
                    size = Size(shadowWidth, 2 * radius)
                )
            }
        }

        // Outer Moon Rim
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = radius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun MilestoneCard(
    title: String,
    dateStr: String,
    daysLeftStr: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = daysLeftStr,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
        }
    }
}
