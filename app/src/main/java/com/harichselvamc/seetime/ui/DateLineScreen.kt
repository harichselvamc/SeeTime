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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.DateLineEngine
import com.harichselvamc.seetime.util.IdlFlightRoute
import com.harichselvamc.seetime.util.TransPacificFlightStatus
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateLineScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedRouteIndex by remember { mutableIntStateOf(0) }
    val route = DateLineEngine.POPULAR_IDL_ROUTES[selectedRouteIndex % DateLineEngine.POPULAR_IDL_ROUTES.size]

    var flightProgress by remember { mutableFloatStateOf(0.52f) } // Mid-flight past IDL

    val departureTime = remember(route) {
        ZonedDateTime.now(java.time.ZoneId.of(route.originZoneId))
    }

    val flightStatus = remember(route, flightProgress, departureTime) {
        DateLineEngine.calculateFlightStatus(route, flightProgress, departureTime)
    }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("hh:mm a")
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Flight,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Date Line & Time-Warp Flight",
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
            // Route Selector Chips
            item {
                Text(
                    text = "SELECT TRANS-PACIFIC ROUTE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DateLineEngine.POPULAR_IDL_ROUTES.indices.toList()) { idx ->
                        val itemRoute = DateLineEngine.POPULAR_IDL_ROUTES[idx]
                        val isSelected = selectedRouteIndex == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedRouteIndex = idx },
                            label = { Text(itemRoute.routeCode, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cyan60.copy(alpha = 0.15f),
                                selectedLabelColor = Cyan60
                            )
                        )
                    }
                }
            }

            // Pacific Flight Corridor Canvas Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
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
                                text = "180° PACIFIC DATE LINE CORRIDOR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Text(
                                text = "${(flightProgress * 100).toInt()}% Flight Progress",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pacific Corridor Canvas
                        PacificFlightCorridorCanvas(
                            route = route,
                            flightProgress = flightProgress,
                            hasCrossed = flightStatus.hasCrossedDateLine,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Flight Progress Slider
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Takeoff (${route.originAirportCode})", style = MaterialTheme.typography.labelSmall, color = Cobalt60)
                                Text(
                                    text = if (flightStatus.hasCrossedDateLine) "Crossed Date Line! ⚡" else "West of Date Line",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (flightStatus.hasCrossedDateLine) Amber60 else Color.LightGray
                                )
                                Text("Landing (${route.destAirportCode})", style = MaterialTheme.typography.labelSmall, color = Amber60)
                            }
                            Slider(
                                value = flightProgress,
                                onValueChange = { flightProgress = it },
                                valueRange = 0f..1f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Cyan60,
                                    activeTrackColor = Cyan60
                                )
                            )
                        }
                    }
                }
            }

            // Time Warp Paradox Hero Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (flightStatus.calendarDayDelta < 0) Amber60.copy(alpha = 0.15f) else Cyan60.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (flightStatus.calendarDayDelta < 0) Amber60 else Cyan60
                            ) {
                                Text(
                                    text = flightStatus.timeTravelBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "${route.flightDurationHours}h Flight Duration",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = route.paradoxTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = route.paradoxDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Departure vs Arrival Dual-Date Breakdown Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Departure Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FlightTakeoff, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DEPARTURE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Cobalt60)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(route.originCity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(flightStatus.departureZdt.format(timeFormatter), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Text(flightStatus.departureZdt.format(dateFormatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    // Arrival Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.FlightLand, contentDescription = null, tint = Amber60, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ARRIVAL", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Amber60)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(route.destCity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(flightStatus.arrivalZdt.format(timeFormatter), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                            Text(flightStatus.arrivalZdt.format(dateFormatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            // Historical Date Line Trivia Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("The Pacific Date Line Marvel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "• Kiribati (UTC+14): In 1995, the island nation of Kiribati shifted the Date Line eastward so the entire country shares the same calendar day, becoming the first inhabited place to greet every new sunrise.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Samoa's Missing Day: On December 29, 2011, Samoa skipped December 30 completely to jump across the Date Line to align trading days with Australia and New Zealand!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PacificFlightCorridorCanvas(
    route: IdlFlightRoute,
    flightProgress: Float,
    hasCrossed: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "beacon_glow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Deep Pacific Ocean background
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF075985))
            )
        )

        // 180° Date Line vertical dividing line
        val idlX = w * 0.50f
        drawLine(
            color = Amber60.copy(alpha = 0.6f),
            start = Offset(idlX, 0f),
            end = Offset(idlX, h),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
        )

        // Flight Origin (Left) & Destination (Right)
        val originPos = Offset(w * 0.15f, h * 0.50f)
        val destPos = Offset(w * 0.85f, h * 0.50f)

        // Great Circle Arc trajectory curve
        val path = Path().apply {
            moveTo(originPos.x, originPos.y)
            quadraticTo(w * 0.50f, h * 0.15f, destPos.x, destPos.y)
        }

        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.4f),
            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        )

        // Airport Waypoints
        drawCircle(color = Cobalt60, radius = 6.dp.toPx(), center = originPos)
        drawCircle(color = Amber60, radius = 6.dp.toPx(), center = destPos)

        // Interpolate Plane Position along quadratic curve
        val t = flightProgress.coerceIn(0f, 1f)
        val planeX = (1 - t) * (1 - t) * originPos.x + 2 * (1 - t) * t * (w * 0.50f) + t * t * destPos.x
        val planeY = (1 - t) * (1 - t) * originPos.y + 2 * (1 - t) * t * (h * 0.15f) + t * t * destPos.y

        // Glowing Airplane Icon Marker
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Cyan60.copy(alpha = pulseAlpha), Color.Transparent),
                center = Offset(planeX, planeY),
                radius = 18.dp.toPx()
            ),
            radius = 18.dp.toPx(),
            center = Offset(planeX, planeY)
        )
        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(planeX, planeY))
        drawCircle(color = Cyan60, radius = 2.5.dp.toPx(), center = Offset(planeX, planeY))
    }
}
