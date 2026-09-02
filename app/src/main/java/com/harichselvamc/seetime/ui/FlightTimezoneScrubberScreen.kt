package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.DarkBg
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class FlightPreset(
    val title: String,
    val originCity: String,
    val originZoneId: String,
    val destCity: String,
    val destZoneId: String,
    val defaultFlightHours: Int
)

private val DEFAULT_PRESETS = listOf(
    FlightPreset("SFO ✈ LHR", "San Francisco", "America/Los_Angeles", "London", "Europe/London", 10),
    FlightPreset("NYC ✈ TYO", "New York", "America/New_York", "Tokyo", "Asia/Tokyo", 14),
    FlightPreset("SIN ✈ SYD", "Singapore", "Asia/Singapore", "Sydney", "Australia/Sydney", 8),
    FlightPreset("DXB ✈ CDG", "Dubai", "Asia/Dubai", "Paris", "Europe/Paris", 7),
    FlightPreset("BER ✈ JFK", "Berlin", "Europe/Berlin", "New York", "America/New_York", 9)
)

/**
 * Interactive 24h Visual Flight & Timezone Scrubber Screen
 * Provides intuitive dual-time scrubbing, daylight solar gradients, and mutual meeting/call sweet spots.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightTimezoneScrubberScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val currentPreset = DEFAULT_PRESETS[selectedPresetIndex % DEFAULT_PRESETS.size]

    var scrubbedHourFloat by remember { mutableFloatStateOf(9.0f) } // 0.0f to 23.99f
    var flightDurationHours by remember { mutableIntStateOf(currentPreset.defaultFlightHours) }

    val originZone = remember(currentPreset) {
        try { ZoneId.of(currentPreset.originZoneId) } catch (e: Exception) { ZoneId.systemDefault() }
    }
    val destZone = remember(currentPreset) {
        try { ZoneId.of(currentPreset.destZoneId) } catch (e: Exception) { ZoneId.of("UTC") }
    }

    // Compute base reference time
    val nowInOrigin = remember(originZone) { ZonedDateTime.now(originZone) }
    val scrubbedOriginTime = remember(scrubbedHourFloat, nowInOrigin) {
        val totalMinutes = (scrubbedHourFloat * 60).toInt()
        val hour = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60
        nowInOrigin.withHour(hour).withMinute(minute).withSecond(0)
    }

    val scrubbedDestTime = remember(scrubbedOriginTime, destZone) {
        scrubbedOriginTime.withZoneSameInstant(destZone)
    }

    val arrivalDestTime = remember(scrubbedDestTime, flightDurationHours) {
        scrubbedDestTime.plusHours(flightDurationHours.toLong())
    }

    // Overlap evaluation
    val originHour = scrubbedOriginTime.hour
    val destHour = scrubbedDestTime.hour
    val isOriginAwake = originHour in 7..22
    val isDestAwake = destHour in 7..22
    val isSweetSpot = (originHour in 9..18) && (destHour in 9..18)
    val isModerateOverlap = isOriginAwake && isDestAwake

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Flight,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Flight & Timezone Scrubber",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        androidx.compose.material3.IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back to Matrix"
                            )
                        }
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preset Flight Routes Chips
            item {
                Text(
                    text = "SELECT ROUTE OR TIME PAIR",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DEFAULT_PRESETS.indices.toList()) { index ->
                        val preset = DEFAULT_PRESETS[index]
                        val isSelected = selectedPresetIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPresetIndex = index
                                flightDurationHours = preset.defaultFlightHours
                            },
                            label = { Text(preset.title) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.FlightTakeoff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                selectedLabelColor = Cobalt60
                            )
                        )
                    }
                }
            }

            // Dual City Status Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CityScrubberCard(
                        modifier = Modifier.weight(1f),
                        cityName = currentPreset.originCity,
                        zonedDateTime = scrubbedOriginTime,
                        use24Hour = use24Hour,
                        badgeLabel = "DEPARTURE",
                        accentColor = Cobalt60,
                        icon = Icons.Filled.FlightTakeoff
                    )

                    CityScrubberCard(
                        modifier = Modifier.weight(1f),
                        cityName = currentPreset.destCity,
                        zonedDateTime = scrubbedDestTime,
                        use24Hour = use24Hour,
                        badgeLabel = "ARRIVAL / REMOTE",
                        accentColor = Amber60,
                        icon = Icons.Filled.FlightLand
                    )
                }
            }

            // 24-Hour Solar Gradient Visualizer Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "24-Hour Solar Gradient",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", (scrubbedHourFloat).toInt(), ((scrubbedHourFloat % 1f) * 60).toInt()),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Cobalt60
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Solar Gradient Canvas
                        SolarGradientBar(
                            currentHourFloat = scrubbedHourFloat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Slider Control
                        Slider(
                            value = scrubbedHourFloat,
                            onValueChange = { scrubbedHourFloat = it },
                            valueRange = 0f..23.99f,
                            colors = SliderDefaults.colors(
                                thumbColor = Cobalt60,
                                activeTrackColor = Cobalt60,
                                inactiveTrackColor = Cobalt60.copy(alpha = 0.2f)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("6 AM (Dawn)", style = MaterialTheme.typography.labelSmall, color = Amber60)
                            Text("12 PM (Noon)", style = MaterialTheme.typography.labelSmall, color = Cyan60)
                            Text("6 PM (Dusk)", style = MaterialTheme.typography.labelSmall, color = Amber60)
                            Text("11:59 PM", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // Overlap Sweet Spot Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSweetSpot -> SuccessGreen.copy(alpha = 0.12f)
                            isModerateOverlap -> Amber60.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isSweetSpot -> Icons.Rounded.CheckCircle
                                isModerateOverlap -> Icons.Filled.WbTwilight
                                else -> Icons.Filled.NightsStay
                            },
                            contentDescription = null,
                            tint = when {
                                isSweetSpot -> SuccessGreen
                                isModerateOverlap -> Amber60
                                else -> MaterialTheme.colorScheme.error
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = when {
                                    isSweetSpot -> "Optimal Meeting Sweet Spot! 🎯"
                                    isModerateOverlap -> "Moderate Overlap (Early/Late)"
                                    else -> "Asynchronous Window (Night / Sleep)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSweetSpot -> SuccessGreen
                                    isModerateOverlap -> Amber60
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                            Text(
                                text = when {
                                    isSweetSpot -> "Both locations are comfortably awake during working hours (9 AM - 6 PM)."
                                    isModerateOverlap -> "Both participants are awake, though one location is outside peak hours."
                                    else -> "One or both parties are likely sleeping. Best to schedule or send async messages."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Sweet Spot Action Button
            item {
                OutlinedButton(
                    onClick = {
                        // Find sweet spot: try 14:00 origin
                        scrubbedHourFloat = 14.0f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Amber60,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snap to Next Overlap Sweet Spot", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CityScrubberCard(
    cityName: String,
    zonedDateTime: ZonedDateTime,
    use24Hour: Boolean,
    badgeLabel: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val formatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }
    val hour = zonedDateTime.hour
    val isDay = hour in 6..18

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badgeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Icon(
                    imageVector = if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                    contentDescription = null,
                    tint = if (isDay) Amber60 else Cobalt60,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = cityName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = zonedDateTime.format(formatter),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = zonedDateTime.format(dateFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun SolarGradientBar(
    currentHourFloat: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 24h day/night gradient brush
        val gradient = Brush.horizontalGradient(
            0.0f to Color(0xFF0F172A), // Midnight (Navy)
            0.25f to Color(0xFFF59E0B), // 6 AM Dawn (Amber)
            0.50f to Color(0xFF38BDF8), // 12 PM Noon (Sky Blue)
            0.75f to Color(0xFFEA580C), // 6 PM Dusk (Orange)
            1.0f to Color(0xFF0F172A)  // Midnight
        )

        drawRect(
            brush = gradient,
            size = Size(w, h)
        )

        // Scrubber cursor line
        val cursorX = (currentHourFloat / 24.0f) * w
        drawLine(
            color = Color.White,
            start = Offset(cursorX, 0f),
            end = Offset(cursorX, h),
            strokeWidth = 3.dp.toPx()
        )

        drawCircle(
            color = Color.White,
            radius = 6.dp.toPx(),
            center = Offset(cursorX, h / 2)
        )
    }
}
