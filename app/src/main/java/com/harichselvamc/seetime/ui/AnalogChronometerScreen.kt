package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Watch
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.ChronometerEngine
import com.harichselvamc.seetime.util.SubDialState
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class WatchTheme(
    val name: String,
    val dialColor: Color,
    val bezelPrimary: Color,
    val bezelSecondary: Color,
    val accentColor: Color,
    val handColor: Color
)

private val WATCH_THEMES = listOf(
    WatchTheme("Obsidian Gold", Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155), Amber60, Color(0xFFFBBF24)),
    WatchTheme("Pepsi GMT", Color(0xFF0B1329), Color(0xFF1E3A8A), Color(0xFF991B1B), Color(0xFFEF4444), Color.White),
    WatchTheme("Stealth Titanium", Color(0xFF030712), Color(0xFF111827), Color(0xFF1F2937), Cyan60, Cyan60)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalogChronometerScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    var selectedThemeIndex by remember { mutableIntStateOf(0) }
    val currentTheme = WATCH_THEMES[selectedThemeIndex % WATCH_THEMES.size]

    var bezelRotationDeg by remember { mutableFloatStateOf(0f) }

    var currentTime by remember { mutableStateOf(ZonedDateTime.now()) }
    var currentMillis by remember { mutableIntStateOf(0) }

    // 60 FPS sweeping ticker loop
    LaunchedEffect(Unit) {
        while (true) {
            val now = ZonedDateTime.now()
            val millis = (System.currentTimeMillis() % 1000).toInt()
            currentTime = now
            currentMillis = millis
            kotlinx.coroutines.delay(20L)
        }
    }

    val mainAngles = remember(currentTime, currentMillis) {
        ChronometerEngine.calculateMainDialAngles(currentTime, currentMillis)
    }

    val subDial1 = remember(currentTime) {
        ChronometerEngine.calculateSubDialState("London", "Europe/London", currentTime)
    }
    val subDial2 = remember(currentTime) {
        ChronometerEngine.calculateSubDialState("Tokyo", "Asia/Tokyo", currentTime)
    }
    val subDial3 = remember(currentTime) {
        ChronometerEngine.calculateSubDialState("GMT 24H", "UTC", currentTime)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Watch,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "World Chronometer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { bezelRotationDeg = (bezelRotationDeg + 15f) % 360f }) {
                        Icon(
                            imageVector = Icons.Filled.RotateRight,
                            contentDescription = "Rotate Bezel",
                            tint = Amber60
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
            // Main Chronometer Watch Face Canvas
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                text = "SWISS HOROLOGY COMPLICATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Amber60
                            )
                            Text(
                                text = "Drag Bezel to Align",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Analog Watch Canvas
                        LuxuryWatchCanvas(
                            mainAngles = mainAngles,
                            subDial1 = subDial1,
                            subDial2 = subDial2,
                            subDial3 = subDial3,
                            bezelRotationDeg = bezelRotationDeg,
                            theme = currentTheme,
                            dateText = currentTime.format(DateTimeFormatter.ofPattern("EEE d")),
                            onBezelDrag = { delta ->
                                bezelRotationDeg = (bezelRotationDeg + delta) % 360f
                            },
                            modifier = Modifier
                                .size(290.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Theme Selection Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WATCH_THEMES.forEachIndexed { idx, theme ->
                                val isSelected = selectedThemeIndex == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Amber60.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    modifier = Modifier.clickable { selectedThemeIndex = idx }
                                ) {
                                    Text(
                                        text = theme.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (isSelected) Amber60 else Color.LightGray,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Sub-Dial Live Information Cards
            item {
                Text(
                    text = "ACTIVE TIMEZONE COMPLICATIONS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubDialStatusCard(subDial = subDial1, modifier = Modifier.weight(1f))
                    SubDialStatusCard(subDial = subDial2, modifier = Modifier.weight(1f))
                    SubDialStatusCard(subDial = subDial3, modifier = Modifier.weight(1f))
                }
            }

            // 24-Hour World Bezel Reference List
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "24-Hour World Cities Bezel Key",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ChronometerEngine.BEZEL_CITIES) { city ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = city.code,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Amber60
                                        )
                                        Text(
                                            text = city.cityName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubDialStatusCard(subDial: SubDialState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subDial.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subDial.timeFormatted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (subDial.isDaylight) Amber60 else Cobalt60
            )
            Text(
                text = if (subDial.isDaylight) "Day" else "Night",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = if (subDial.isDaylight) Amber60 else Cobalt60
            )
        }
    }
}

@Composable
private fun LuxuryWatchCanvas(
    mainAngles: com.harichselvamc.seetime.util.DialHandAngles,
    subDial1: SubDialState,
    subDial2: SubDialState,
    subDial3: SubDialState,
    bezelRotationDeg: Float,
    theme: WatchTheme,
    dateText: String,
    onBezelDrag: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consume()
                onBezelDrag(dragAmount.x * 0.3f)
            }
        }
    ) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val outerRadius = (w / 2) * 0.95f
        val bezelInnerRadius = outerRadius * 0.82f
        val mainDialRadius = bezelInnerRadius * 0.96f

        // Outer Bezel Ring (Split Day/Night)
        rotate(bezelRotationDeg, pivot = center) {
            // Day Arc (0 to 180)
            drawArc(
                color = theme.bezelPrimary,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = (outerRadius - bezelInnerRadius))
            )
            // Night Arc (180 to 360)
            drawArc(
                color = theme.bezelSecondary,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2),
                style = Stroke(width = (outerRadius - bezelInnerRadius))
            )

            // Bezel 24h Ticks
            for (hour24 in 0 until 24 step 2) {
                val tickAngle = (hour24 * 15f) - 90f
                val rad = Math.toRadians(tickAngle.toDouble())
                val tickR = (outerRadius + bezelInnerRadius) / 2
                val tickPos = Offset((center.x + tickR * cos(rad)).toFloat(), (center.y + tickR * sin(rad)).toFloat())
                drawCircle(color = Color.White, radius = 2.dp.toPx(), center = tickPos)
            }
        }

        // Main Dial Base
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(theme.dialColor, Color(0xFF020617)),
                center = center,
                radius = mainDialRadius
            ),
            radius = mainDialRadius,
            center = center
        )

        // 12 Applied Hour Markers & Minute Track
        for (i in 0 until 60) {
            val isHour = (i % 5 == 0)
            val tickAngle = (i * 6f) - 90f
            val rad = Math.toRadians(tickAngle.toDouble())
            val innerR = if (isHour) mainDialRadius * 0.85f else mainDialRadius * 0.92f
            val outerR = mainDialRadius * 0.96f

            val start = Offset((center.x + innerR * cos(rad)).toFloat(), (center.y + innerR * sin(rad)).toFloat())
            val end = Offset((center.x + outerR * cos(rad)).toFloat(), (center.y + outerR * sin(rad)).toFloat())

            drawLine(
                color = if (isHour) theme.handColor else Color.Gray.copy(alpha = 0.4f),
                start = start,
                end = end,
                strokeWidth = if (isHour) 2.5.dp.toPx() else 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 3 Mini Sub-Dials (9h, 3h, 6h)
        val subRadius = mainDialRadius * 0.26f
        drawSubDial(center = Offset(center.x - mainDialRadius * 0.45f, center.y), radius = subRadius, subDial = subDial1, accentColor = theme.accentColor)
        drawSubDial(center = Offset(center.x + mainDialRadius * 0.45f, center.y), radius = subRadius, subDial = subDial2, accentColor = theme.accentColor)
        drawSubDial(center = Offset(center.x, center.y + mainDialRadius * 0.45f), radius = subRadius, subDial = subDial3, accentColor = theme.accentColor)

        // 24H Red Arrow GMT Hand
        rotate(mainAngles.gmt24Angle, pivot = center) {
            drawLine(
                color = Color(0xFFEF4444),
                start = center,
                end = Offset(center.x, center.y - mainDialRadius * 0.88f),
                strokeWidth = 2.dp.toPx()
            )
            // Arrow Head
            val tip = Offset(center.x, center.y - mainDialRadius * 0.88f)
            val arrowPath = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(tip.x - 4.dp.toPx(), tip.y + 8.dp.toPx())
                lineTo(tip.x + 4.dp.toPx(), tip.y + 8.dp.toPx())
                close()
            }
            drawPath(path = arrowPath, color = Color(0xFFEF4444))
        }

        // Main Hour Hand
        rotate(mainAngles.hourAngle, pivot = center) {
            drawLine(
                color = theme.handColor,
                start = Offset(center.x, center.y + 10.dp.toPx()),
                end = Offset(center.x, center.y - mainDialRadius * 0.52f),
                strokeWidth = 4.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Main Minute Hand
        rotate(mainAngles.minuteAngle, pivot = center) {
            drawLine(
                color = theme.handColor,
                start = Offset(center.x, center.y + 12.dp.toPx()),
                end = Offset(center.x, center.y - mainDialRadius * 0.78f),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Sweeping Chronometer Second Needle
        rotate(mainAngles.secondAngle, pivot = center) {
            drawLine(
                color = Color(0xFFF43F5E), // Rose red
                start = Offset(center.x, center.y + 18.dp.toPx()),
                end = Offset(center.x, center.y - mainDialRadius * 0.90f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = Color(0xFFF43F5E), radius = 3.5.dp.toPx(), center = Offset(center.x, center.y + 18.dp.toPx()))
        }

        // Center Pin
        drawCircle(color = theme.handColor, radius = 4.dp.toPx(), center = center)
        drawCircle(color = Color.Black, radius = 2.dp.toPx(), center = center)
    }
}

private fun DrawScope.drawSubDial(
    center: Offset,
    radius: Float,
    subDial: SubDialState,
    accentColor: Color
) {
    // Sub-dial Background
    drawCircle(
        color = Color(0xFF0F172A).copy(alpha = 0.85f),
        radius = radius,
        center = center
    )
    drawCircle(
        color = Color.LightGray.copy(alpha = 0.3f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    // Sub-dial Hand
    rotate(subDial.hourAngle, pivot = center) {
        drawLine(
            color = accentColor,
            start = center,
            end = Offset(center.x, center.y - radius * 0.75f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    drawCircle(color = accentColor, radius = 2.5.dp.toPx(), center = center)
}
