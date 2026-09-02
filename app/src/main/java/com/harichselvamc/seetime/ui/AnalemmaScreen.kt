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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
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
import com.harichselvamc.seetime.util.AnalemmaEngine
import com.harichselvamc.seetime.util.AnalemmaMilestone
import com.harichselvamc.seetime.util.AnalemmaPoint
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalemmaScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val today = remember { LocalDate.now() }

    val primaryZone = uiState.pairs.firstOrNull()?.toZone ?: "Asia/Tokyo"
    val zoneId = remember(primaryZone) {
        try { ZoneId.of(primaryZone) } catch (_: Exception) { ZoneId.systemDefault() }
    }

    val analemmaCurve = remember { AnalemmaEngine.generate365DayAnalemmaCurve(today.year) }
    val deviationDetails = remember(today, zoneId) {
        AnalemmaEngine.calculateDailyDeviation(today, 0.0, zoneId)
    }

    // Sundial Converter State
    var sundialInputHour by remember { mutableIntStateOf(12) }
    var sundialInputMinute by remember { mutableIntStateOf(0) }
    val convertedCivilTime = remember(sundialInputHour, sundialInputMinute, today, zoneId) {
        AnalemmaEngine.convertSundialToCivilTime(sundialInputHour, sundialInputMinute, 0.0, zoneId, today)
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)

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
                            text = "Solar Analemma & EoT",
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
            // Figure-8 Analemma Canvas Hero Card
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
                                text = "365-DAY SOLAR ANALEMMA FIGURE-8",
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
                                    text = "Day ${today.dayOfYear} / 365",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Amber60,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Figure-8 Analemma Canvas
                        AnalemmaFigure8Canvas(
                            curvePoints = analemmaCurve,
                            currentDay = today.dayOfYear,
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Equation of Time: ${deviationDetails.eotFormatted}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Solar Declination: ${String.format(Locale.US, "%+.1f°", deviationDetails.solarDeclinationDeg)} · ${if (deviationDetails.solarDeclinationDeg >= 0) "North" else "South"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Cyan60
                        )
                    }
                }
            }

            // Daily Solar Noon Deviation Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (deviationDetails.isSunFast) Amber60.copy(alpha = 0.12f) else Cobalt60.copy(alpha = 0.12f)
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
                            Text(
                                text = "TRUE SOLAR NOON TODAY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (deviationDetails.isSunFast) Amber60 else Cobalt60
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (deviationDetails.isSunFast) Amber60 else Cobalt60
                            ) {
                                Text(
                                    text = if (deviationDetails.isSunFast) "SUN AHEAD" else "SUN BEHIND",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = deviationDetails.solarNoonTimeLocal.format(timeFormatter),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = deviationDetails.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Interactive Sundial to Civil Watch Time Converter
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
                            Icon(Icons.Filled.Calculate, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sundial Shadow to Clock Converter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sundial Shadow: ${String.format(Locale.US, "%02d:%02d", sundialInputHour, sundialInputMinute)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Clock: ${convertedCivilTime.format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Amber60
                            )
                        }

                        Slider(
                            value = sundialInputHour.toFloat(),
                            onValueChange = { sundialInputHour = it.toInt() },
                            valueRange = 6f..18f,
                            colors = SliderDefaults.colors(thumbColor = Amber60, activeTrackColor = Amber60)
                        )
                    }
                }
            }

            // 8 Annual Analemma Milestones (Zero Drift & Extrema)
            item {
                Text(
                    text = "ANNUAL EQUATION OF TIME MILESTONES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(AnalemmaEngine.ANALEMMA_MILESTONES) { milestone ->
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
                            Text(milestone.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${milestone.approxDate} · ${milestone.significance}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (milestone.eotDrift.startsWith("+")) Amber60.copy(alpha = 0.15f) else if (milestone.eotDrift.startsWith("-")) Cobalt60.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = milestone.eotDrift,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (milestone.eotDrift.startsWith("+")) Amber60 else if (milestone.eotDrift.startsWith("-")) Cobalt60 else SuccessGreen,
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
private fun AnalemmaFigure8Canvas(
    curvePoints: List<AnalemmaPoint>,
    currentDay: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
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

        // Scale factors: X-axis (-18 min to +18 min), Y-axis (-25° to +25°)
        val scaleX = (w * 0.40f) / 18f
        val scaleY = (h * 0.42f) / 25f

        // Background
        drawRect(color = Color(0xFF030712))

        // Axis reference lines
        drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(center.x, 10.dp.toPx()), end = Offset(center.x, h - 10.dp.toPx()), strokeWidth = 1.dp.toPx())
        drawLine(color = Color.White.copy(alpha = 0.1f), start = Offset(10.dp.toPx(), center.y), end = Offset(w - 10.dp.toPx(), center.y), strokeWidth = 1.dp.toPx())

        // Draw Complete Figure-8 Curve
        val path = Path()
        var hasStarted = false
        var todayOffset: Offset? = null

        curvePoints.forEach { pt ->
            val screenX = center.x + (pt.eotMinutes.toFloat() * scaleX)
            val screenY = center.y - (pt.declinationDeg.toFloat() * scaleY) // Inverted Y

            if (!hasStarted) {
                path.moveTo(screenX, screenY)
                hasStarted = true
            } else {
                path.lineTo(screenX, screenY)
            }

            if (pt.dayOfYear == currentDay) {
                todayOffset = Offset(screenX, screenY)
            }
        }
        path.close()

        // Gradient Stroke for 4 seasons along loop
        drawPath(
            path = path,
            brush = Brush.sweepGradient(
                listOf(SuccessGreen, Amber60, Color(0xFFEA580C), Cyan60, SuccessGreen),
                center = center
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Glowing Sun Tracker for Today
        todayOffset?.let { sunPos ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Amber60, Amber60.copy(alpha = 0.2f), Color.Transparent),
                    center = sunPos,
                    radius = 18.dp.toPx() * sunPulse
                ),
                radius = 18.dp.toPx() * sunPulse,
                center = sunPos
            )
            drawCircle(color = Color(0xFFFBBF24), radius = 6.dp.toPx(), center = sunPos)
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = sunPos)
        }
    }
}
