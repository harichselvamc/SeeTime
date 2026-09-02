package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.CountdownEvent
import com.harichselvamc.seetime.util.EventCountdownEngine
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCountdownScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val use24Hour by viewModel.use24HourFormat.collectAsState()

    val customEvents = remember { mutableStateListOf<CountdownEvent>() }
    val allEvents = remember(customEvents.toList()) {
        EventCountdownEngine.PRELOADED_EVENTS + customEvents
    }

    var selectedEventIndex by remember { mutableIntStateOf(0) }
    val currentEvent = allEvents.getOrNull(selectedEventIndex) ?: allEvents.first()

    var showCreateDialog by remember { mutableStateOf(false) }
    var triggerConfettiCelebration by remember { mutableStateOf(false) }

    var nowInstant by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live millisecond ticker loop (50 ms)
    LaunchedEffect(Unit) {
        while (true) {
            nowInstant = System.currentTimeMillis()
            kotlinx.coroutines.delay(50L)
        }
    }

    val currentZdt = remember(nowInstant) { ZonedDateTime.now() }
    val countdown = remember(currentEvent, currentZdt) {
        EventCountdownEngine.calculateCountdown(currentEvent.targetZdt, currentZdt)
    }

    // Trigger confetti when countdown hits zero
    LaunchedEffect(countdown.isPastEvent) {
        if (countdown.isPastEvent && kotlin.math.abs(countdown.totalMillisRemaining) < 3000L) {
            triggerConfettiCelebration = true
        }
    }

    val timeFormatter = remember(use24Hour) {
        if (use24Hour) DateTimeFormatter.ofPattern("HH:mm:ss") else DateTimeFormatter.ofPattern("hh:mm:ss a")
    }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.RocketLaunch,
                            contentDescription = null,
                            tint = Color(currentEvent.accentColorHex),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Event & Launch Countdown HUD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Custom Event", tint = Color(currentEvent.accentColorHex))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event Selector Chips
                item {
                    Text(
                        text = "SELECT EVENT / LAUNCH TARGET",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allEvents.indices.toList()) { idx ->
                            val itemEv = allEvents[idx]
                            val isSelected = selectedEventIndex == idx
                            val color = Color(itemEv.accentColorHex)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEventIndex = idx },
                                label = { Text("${itemEv.emoji} ${itemEv.title.substringBefore(" (")}", fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.2f),
                                    selectedLabelColor = color
                                )
                            )
                        }
                    }
                }

                // Sci-Fi Launch Control Hero Countdown Card
                item {
                    val accentColor = Color(currentEvent.accentColorHex)
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                    text = "LAUNCH CLOCK / T-MINUS HUD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = accentColor
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (countdown.isPastEvent) SuccessGreen.copy(alpha = 0.2f) else accentColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (countdown.isPastEvent) "T-PLUS (ELAPSED)" else "T-MINUS (COUNTDOWN)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                        color = if (countdown.isPastEvent) SuccessGreen else accentColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Glowing Sub-Second Radial Progress Canvas
                            CountdownSubSecondCanvas(
                                subSecondFraction = countdown.subSecondProgress,
                                accentColor = accentColor,
                                modifier = Modifier.size(150.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Big Monospace T-Minus Timer Display
                            Text(
                                text = countdown.tMinusFormatted,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "${currentEvent.emoji} ${currentEvent.title}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Text(
                                text = currentEvent.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dual Location Timezone Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("TARGET EVENT TIME", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = accentColor)
                                        Text(currentEvent.targetZdt.format(timeFormatter), style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(currentEvent.targetZdt.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("YOUR LOCAL TIME", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Cobalt60)
                                        val localEquivalent = currentEvent.targetZdt.withZoneSameInstant(ZoneId.systemDefault())
                                        Text(localEquivalent.format(timeFormatter), style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace), fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(localEquivalent.format(DateTimeFormatter.ofPattern("EEE, MMM d")), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // All Countdown Events List
                item {
                    Text(
                        text = "ALL MILESTONE LAUNCHES & COUNTDOWNS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(allEvents) { eventItem ->
                    val isCurrent = currentEvent.id == eventItem.id
                    val evCountdown = remember(eventItem, currentZdt) {
                        EventCountdownEngine.calculateCountdown(eventItem.targetZdt, currentZdt)
                    }
                    val accentColor = Color(eventItem.accentColorHex)

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) accentColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.clickable {
                            selectedEventIndex = allEvents.indexOf(eventItem)
                        }
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
                                    color = accentColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(eventItem.emoji, fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(eventItem.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = eventItem.targetZdt.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy · HH:mm zzz")),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (evCountdown.isPastEvent) SuccessGreen else accentColor
                            ) {
                                Text(
                                    text = "${evCountdown.days}d ${evCountdown.hours}h",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Confetti Celebration Overlay
            ConfettiCelebration(
                trigger = triggerConfettiCelebration,
                onAnimationEnd = { triggerConfettiCelebration = false }
            )
        }
    }

    // Custom Event Creation Dialog
    if (showCreateDialog) {
        CustomEventCreateDialog(
            onDismiss = { showCreateDialog = false },
            onEventCreated = { newEvent ->
                customEvents.add(newEvent)
                selectedEventIndex = allEvents.size - 1
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CountdownSubSecondCanvas(
    subSecondFraction: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w / 2, h / 2)
        val radius = (w / 2) * 0.85f

        // Outer Background Track
        drawCircle(color = Color(0xFF1E293B), radius = radius, center = center, style = Stroke(width = 8.dp.toPx()))

        // Rotating Sub-Second Arc (0 to 360 degrees)
        drawArc(
            brush = Brush.sweepGradient(
                listOf(accentColor, Cyan60, accentColor)
            ),
            startAngle = -90f,
            sweepAngle = (subSecondFraction * 360f).coerceIn(0f, 360f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Center Pulsing Core
        drawCircle(color = accentColor.copy(alpha = 0.2f), radius = radius * 0.45f, center = center)
        drawCircle(color = accentColor, radius = 5.dp.toPx(), center = center)
    }
}

@Composable
private fun CustomEventCreateDialog(
    onDismiss: () -> Unit,
    onEventCreated: (CountdownEvent) -> Unit
) {
    var titleText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var targetYear by remember { mutableIntStateOf(2027) }
    var targetMonth by remember { mutableIntStateOf(1) }
    var targetDay by remember { mutableIntStateOf(1) }
    var targetHour by remember { mutableIntStateOf(0) }
    var targetMinute by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Event, contentDescription = null, tint = Amber60, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Custom Countdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Event Title (e.g. Flight Departure / Launch)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Description") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Target Date & Time (UTC/Local)", style = MaterialTheme.typography.labelSmall, color = Cobalt60, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetYear.toString(),
                        onValueChange = { it.toIntOrNull()?.let { y -> targetYear = y } },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = targetMonth.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> targetMonth = m.coerceIn(1, 12) } },
                        label = { Text("Month (1-12)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = targetDay.toString(),
                        onValueChange = { it.toIntOrNull()?.let { d -> targetDay = d.coerceIn(1, 31) } },
                        label = { Text("Day (1-31)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetHour.toString(),
                        onValueChange = { it.toIntOrNull()?.let { h -> targetHour = h.coerceIn(0, 23) } },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = targetMinute.toString(),
                        onValueChange = { it.toIntOrNull()?.let { m -> targetMinute = m.coerceIn(0, 59) } },
                        label = { Text("Minute (0-59)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val date = LocalDate.of(targetYear, targetMonth, targetDay)
                    val time = LocalTime.of(targetHour, targetMinute)
                    val newEv = EventCountdownEngine.createCustomEvent(
                        title = titleText,
                        description = descriptionText,
                        date = date,
                        time = time,
                        zoneId = ZoneId.systemDefault()
                    )
                    onEventCreated(newEv)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber60)
            ) {
                Text("Create Countdown", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
