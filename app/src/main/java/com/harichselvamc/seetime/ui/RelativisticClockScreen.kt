package com.harichselvamc.seetime.ui

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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.RelativityTimeEngine
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelativisticClockScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    var selectedAltitudeKm by remember { mutableDoubleStateOf(20200.0) } // Default GPS orbit
    var selectedOrbitName by remember { mutableStateOf("GPS Satellite Constellation") }

    var velocityBeta by remember { mutableFloatStateOf(0.70f) } // 70% speed of light

    // Live microsecond ticker
    var liveMicrosecondsToday by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(Unit) {
        while (true) {
            val millisOfDay = (System.currentTimeMillis() % 86400000L).toDouble()
            val dayFraction = millisOfDay / 86400000.0
            liveMicrosecondsToday = dayFraction * 38.74
            kotlinx.coroutines.delay(100L)
        }
    }

    val orbitResult = remember(selectedAltitudeKm, selectedOrbitName) {
        RelativityTimeEngine.calculateOrbitalDilation(selectedAltitudeKm, selectedOrbitName)
    }

    val lorentzGamma = remember(velocityBeta) {
        RelativityTimeEngine.calculateLorentzFactor(velocityBeta.toDouble())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.SatelliteAlt,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Einstein Relativistic Clocks",
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
            // Live GPS Atomic Clock Divergence Hero Card
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
                                text = "GPS ORBITAL TIME DILATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Cyan60.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "+38.74 µs / day",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Cyan60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Divergence Ticker
                        Text(
                            text = String.format(Locale.getDefault(), "+%.4f µs", liveMicrosecondsToday),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )

                        Text(
                            text = "Cumulative GPS Clock Lead Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Physics Breakdown Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("General Relativity", style = MaterialTheme.typography.labelSmall, color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("+45.9 µs/day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Weaker gravity at 20,200 km speeds clock up", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Special Relativity", style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("-7.2 µs/day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Orbital speed (3.87 km/s) slows clock down", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Daily Drift Warning Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Amber60.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Public, contentDescription = null, tint = Amber60, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Without Einstein's correction, Google Maps GPS would drift by 11.6 km every day!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Amber60
                                )
                            }
                        }
                    }
                }
            }

            // Animated Photon Light-Clock Canvas Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Einstein Light-Clock Thought Experiment",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "v = ${(velocityBeta * 100).toInt()}% c",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Amber60
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Light Clock Canvas
                        PhotonLightClockCanvas(
                            beta = velocityBeta,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Light travels further along diagonal hypotenuse for moving observer. Since speed of light c is constant, moving time t' ticks slower by Lorentz factor γ = ${String.format(Locale.getDefault(), "%.3f", lorentzGamma)}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // Interactive Orbital Altitude Simulator
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Interactive Orbital Altitude Simulator",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Preset Orbit Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(RelativityTimeEngine.KNOWN_ORBITS) { (alt, name) ->
                                val isSelected = selectedAltitudeKm == alt
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedAltitudeKm = alt
                                        selectedOrbitName = name
                                    },
                                    label = { Text(name, fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Cyan60.copy(alpha = 0.15f),
                                        selectedLabelColor = Cyan60
                                    )
                                )
                            }
                        }

                        // Altitude Slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Altitude: ${selectedAltitudeKm.toInt()} km", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text("Orbital Speed: ${String.format(Locale.getDefault(), "%.2f", orbitResult.orbitalVelocityKmS)} km/s", style = MaterialTheme.typography.bodySmall, color = Cobalt60)
                            }
                            Slider(
                                value = selectedAltitudeKm.toFloat(),
                                onValueChange = {
                                    selectedAltitudeKm = it.toDouble()
                                    selectedOrbitName = "Custom Altitude Orbit"
                                },
                                valueRange = 200f..40000f,
                                colors = SliderDefaults.colors(thumbColor = Cyan60, activeTrackColor = Cyan60)
                            )
                        }

                        // Altitude Dilation Result Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Net Relativistic Dilation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = "${if (orbitResult.netDilationMicrosecondsPerDay >= 0) "+" else ""}${String.format(Locale.getDefault(), "%.2f", orbitResult.netDilationMicrosecondsPerDay)} µs/day",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (orbitResult.netDilationMicrosecondsPerDay >= 0) SuccessGreen else Color(0xFFEF4444)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Uncorrected Daily Drift", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.2f", orbitResult.dailyPositioningErrorKm)} km/day",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Amber60
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interstellar Lorentz Time-Warp Calculator
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.RocketLaunch, contentDescription = null, tint = Amber60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Interstellar Travel & Lorentz Time Warp", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Spacecraft Speed (v / c)", style = MaterialTheme.typography.bodySmall)
                                Text("${(velocityBeta * 100).toInt()}% c", fontWeight = FontWeight.Bold, color = Amber60)
                            }
                            Slider(
                                value = velocityBeta,
                                onValueChange = { velocityBeta = it },
                                valueRange = 0.10f..0.99f,
                                colors = SliderDefaults.colors(thumbColor = Amber60, activeTrackColor = Amber60)
                            )
                        }

                        val shipTimeYears = RelativityTimeEngine.calculateShipTravelTime(10.0, velocityBeta.toDouble())
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("If 10.0 Years Pass on Earth:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Spacecraft Crew Ages Only ${String.format(Locale.getDefault(), "%.2f", shipTimeYears)} Years! 🚀",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Amber60
                                )
                            }
                        }
                    }
                }
            }

            // Deep Space Planetary Light Delay
            item {
                Text(
                    text = "SPEED OF LIGHT (c) COMMUNICATION LAG",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(RelativityTimeEngine.CELESTIAL_TARGETS) { target ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(target.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(target.targetName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Distance: ${String.format(Locale.getDefault(), "%,.0f", target.distanceKm)} km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Cobalt60.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = target.formattedDelay,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Cobalt60,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotonLightClockCanvas(
    beta: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "photon_bounce")
    val bounceFrac by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val topMirrorY = 20.dp.toPx()
        val bottomMirrorY = h - 20.dp.toPx()

        // Background
        drawRect(color = Color(0xFF0F172A))

        // Stationary Clock (Left)
        val leftX = w * 0.25f
        drawLine(color = Color.LightGray, start = Offset(leftX - 30.dp.toPx(), topMirrorY), end = Offset(leftX + 30.dp.toPx(), topMirrorY), strokeWidth = 3.dp.toPx())
        drawLine(color = Color.LightGray, start = Offset(leftX - 30.dp.toPx(), bottomMirrorY), end = Offset(leftX + 30.dp.toPx(), bottomMirrorY), strokeWidth = 3.dp.toPx())

        // Stationary vertical photon
        val statPhotonY = topMirrorY + (bottomMirrorY - topMirrorY) * bounceFrac
        drawLine(color = Cyan60.copy(alpha = 0.3f), start = Offset(leftX, topMirrorY), end = Offset(leftX, bottomMirrorY), strokeWidth = 1.5.dp.toPx())
        drawCircle(color = Cyan60, radius = 5.dp.toPx(), center = Offset(leftX, statPhotonY))

        // Moving Clock (Right - Diagonal path)
        val rightX = w * 0.65f
        val deltaX = 40.dp.toPx() * beta
        drawLine(color = Color.LightGray, start = Offset(rightX - 30.dp.toPx(), topMirrorY), end = Offset(rightX + 30.dp.toPx(), topMirrorY), strokeWidth = 3.dp.toPx())
        drawLine(color = Color.LightGray, start = Offset(rightX - 30.dp.toPx(), bottomMirrorY), end = Offset(rightX + 30.dp.toPx(), bottomMirrorY), strokeWidth = 3.dp.toPx())

        // Moving diagonal photon
        val movPhotonX = rightX + (deltaX * (bounceFrac - 0.5f))
        val movPhotonY = topMirrorY + (bottomMirrorY - topMirrorY) * bounceFrac
        drawLine(color = Amber60.copy(alpha = 0.3f), start = Offset(rightX - deltaX * 0.5f, topMirrorY), end = Offset(rightX + deltaX * 0.5f, bottomMirrorY), strokeWidth = 1.5.dp.toPx())
        drawCircle(color = Amber60, radius = 5.dp.toPx(), center = Offset(movPhotonX, movPhotonY))
    }
}
