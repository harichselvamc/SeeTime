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
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.AsrJuristicMethod
import com.harichselvamc.seetime.util.CalculationMethod
import com.harichselvamc.seetime.util.DailyPrayerSchedule
import com.harichselvamc.seetime.util.GlobeMath
import com.harichselvamc.seetime.util.PrayerTimesEngine
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class PrayerRowItem(
    val name: String,
    val time: LocalTime,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedCityIndex by remember { mutableIntStateOf(0) }
    val city = GlobeMath.MAJOR_WORLD_CITIES[selectedCityIndex % GlobeMath.MAJOR_WORLD_CITIES.size]

    var selectedMethod by remember { mutableStateOf(CalculationMethod.MUSLIM_WORLD_LEAGUE) }
    var selectedAsrMethod by remember { mutableStateOf(AsrJuristicMethod.STANDARD_SHAFII) }

    val zoneId = remember(city) {
        try { ZoneId.of(city.zoneId) } catch (_: Exception) { ZoneId.systemDefault() }
    }
    val nowInCity = remember(zoneId) { ZonedDateTime.now(zoneId) }

    val schedule = remember(city, selectedMethod, selectedAsrMethod, nowInCity) {
        PrayerTimesEngine.calculatePrayerTimes(
            latitude = city.latitude,
            longitude = city.longitude,
            date = nowInCity.toLocalDate(),
            zoneId = zoneId,
            method = selectedMethod,
            asrJuristic = selectedAsrMethod,
            referenceTime = nowInCity.toLocalTime()
        )
    }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }

    val prayerRows = remember(schedule) {
        listOf(
            PrayerRowItem("Fajr (Dawn)", schedule.fajr, Icons.Filled.WbTwilight, Cobalt60),
            PrayerRowItem("Sunrise (Shuruq)", schedule.sunrise, Icons.Filled.WbSunny, Amber60),
            PrayerRowItem("Dhuhr (Noon)", schedule.dhuhr, Icons.Filled.Brightness5, Cyan60),
            PrayerRowItem("Asr (Afternoon)", schedule.asr, Icons.Filled.WbSunny, Amber60),
            PrayerRowItem("Maghrib (Sunset)", schedule.maghrib, Icons.Filled.WbTwilight, Color(0xFFEA580C)),
            PrayerRowItem("Isha (Night)", schedule.isha, Icons.Filled.NightsStay, Cobalt60)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Mosque,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Astronomical Prayer Times & Qibla",
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
            // City Location Chips
            item {
                Text(
                    text = "SELECT PRAYER LOCATION",
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
                                selectedContainerColor = SuccessGreen.copy(alpha = 0.15f),
                                selectedLabelColor = SuccessGreen
                            )
                        )
                    }
                }
            }

            // Qibla Direction Compass Dial Card
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
                                text = "GREAT-CIRCLE QIBLA HEADING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = SuccessGreen
                            )
                            Text(
                                text = "${String.format(Locale.US, "%,.0f", schedule.distanceToKaabaKm)} km to Makkah",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Qibla Compass Canvas
                        QiblaCompassCanvas(
                            qiblaHeadingDeg = schedule.qiblaAzimuthDeg.toFloat(),
                            modifier = Modifier.size(200.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Qibla Heading: ${String.format(Locale.US, "%.1f°", schedule.qiblaAzimuthDeg)} from North",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Kaaba Coordinates: 21.4225° N, 39.8262° E",
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessGreen
                        )
                    }
                }
            }

            // Active Prayer Hero Countdown Card
            item {
                val nextHrs = schedule.minutesUntilNextPrayer / 60
                val nextMins = schedule.minutesUntilNextPrayer % 60
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SuccessGreen
                            ) {
                                Text(
                                    text = "ACTIVE: ${schedule.currentActivePrayer.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "Next: ${schedule.nextPrayerName} in ${if (nextHrs > 0) "${nextHrs}h ${nextMins}m" else "${nextMins}m"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = SuccessGreen
                            )
                        }

                        Text(
                            text = "${schedule.nextPrayerName} at ${schedule.nextPrayerTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "${nowInCity.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))} · ${selectedMethod.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Daily 5 Prayer Schedule Timetable
            item {
                Text(
                    text = "DAILY PRAYER TIMETABLE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(prayerRows) { rowItem ->
                val isCurrent = schedule.currentActivePrayer.startsWith(rowItem.name.substringBefore(" "))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = rowItem.color.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(rowItem.icon, contentDescription = null, tint = rowItem.color, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = rowItem.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = rowItem.time.format(timeFormatter),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCurrent) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Method & Juristic Convention Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Calculation Convention & Asr School", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        // Method Selector Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CalculationMethod.values()) { method ->
                                val isSelected = selectedMethod == method
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMethod = method },
                                    label = { Text(method.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SuccessGreen.copy(alpha = 0.2f), selectedLabelColor = SuccessGreen)
                                )
                            }
                        }

                        // Asr Juristic Selector
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AsrJuristicMethod.values().forEach { asrMethod ->
                                val isSelected = selectedAsrMethod == asrMethod
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAsrMethod = asrMethod },
                                    label = { Text(asrMethod.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Amber60.copy(alpha = 0.2f), selectedLabelColor = Amber60)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QiblaCompassCanvas(
    qiblaHeadingDeg: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "qibla_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.85f

        // Outer Ring
        drawCircle(color = Color(0xFF1E293B), radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

        // Cardinal Direction Ticks
        listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f).forEach { (label, angle) ->
            val rad = Math.toRadians((angle - 90).toDouble())
            val startX = (center.x + (radius - 12.dp.toPx()) * cos(rad)).toFloat()
            val startY = (center.y + (radius - 12.dp.toPx()) * sin(rad)).toFloat()
            val endX = (center.x + radius * cos(rad)).toFloat()
            val endY = (center.y + radius * sin(rad)).toFloat()

            drawLine(
                color = if (label == "N") Color(0xFFEF4444) else Color.Gray,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = if (label == "N") 3.dp.toPx() else 1.5.dp.toPx()
            )
        }

        // Qibla Direction Vector
        val qiblaRad = Math.toRadians((qiblaHeadingDeg - 90).toDouble())
        val pointerR = radius * 0.78f
        val qiblaX = (center.x + pointerR * cos(qiblaRad)).toFloat()
        val qiblaY = (center.y + pointerR * sin(qiblaRad)).toFloat()

        // Direction Pointer Line
        drawLine(
            color = SuccessGreen,
            start = center,
            end = Offset(qiblaX, qiblaY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Glowing Kaaba Marker
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SuccessGreen.copy(alpha = 0.5f), Color.Transparent),
                center = Offset(qiblaX, qiblaY),
                radius = 16.dp.toPx() * pulseScale
            ),
            radius = 16.dp.toPx() * pulseScale,
            center = Offset(qiblaX, qiblaY)
        )
        drawCircle(color = SuccessGreen, radius = 6.dp.toPx(), center = Offset(qiblaX, qiblaY))
        drawCircle(color = Color(0xFFFBBF24), radius = 3.dp.toPx(), center = Offset(qiblaX, qiblaY))

        // Center Pivot
        drawCircle(color = Color.Gray, radius = 4.dp.toPx(), center = center)
    }
}
