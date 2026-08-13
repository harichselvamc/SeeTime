@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.util.CalendarHelper
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun InsightsScreen(
    viewModel: TimeViewModel
) {
    val state by viewModel.state.collectAsState()
    val timeOffset by viewModel.timeOffsetMinutes.collectAsState()
    val context = LocalContext.current

    var activeReminderPair by remember { mutableStateOf<TimePairUi?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.startTicker()
    }

    androidx.compose.material3.Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Page Header ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text(
                    "Insights & Overlap Audit",
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Real-time collaboration windows, call slots & timezone audit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.pairs.isEmpty()) {
                Box(
                    modifier            = Modifier.fillMaxSize(),
                    contentAlignment    = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint     = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No Time Pairs Yet",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add timezone comparison pairs on the Home tab to unlock live overlap audits and best meeting recommendations.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start  = 16.dp,
                        end    = 16.dp,
                        top    = 16.dp,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ── Section 1: Who's Working Right Now? ──
                    item { SectionLabel("Who's Working Right Now?", Icons.Outlined.WbSunny) }

                    item {
                        WorkingNowCard(pairs = state.pairs)
                    }

                    // ── Section 2: Overlap Audit & Call Recommendations ──
                    item { SectionLabel("Overlap Audit & Recommended Call Windows", Icons.Outlined.Groups) }

                    items(state.pairs, key = { it.id }) { ui ->
                        PairInsightCard(
                            ui = ui,
                            timeOffsetMinutes = timeOffset,
                            context = context,
                            onSetReminder = { activeReminderPair = ui }
                        )
                    }

                    // ── Section 3: Daylight Saving Status ──
                    item { SectionLabel("Daylight Saving Status", Icons.Outlined.Analytics) }

                    items(state.pairs, key = { "dst_${it.id}" }) { ui ->
                        Card(
                            modifier  = Modifier.fillMaxWidth(),
                            shape     = MaterialTheme.shapes.medium,
                            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "${shortZone(ui.fromZone)} & ${shortZone(ui.toZone)}",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text  = ui.dstText.replace("|", "\n"),
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

    activeReminderPair?.let { pair ->
        SmartReminderDialog(
            fromZone = pair.fromZone,
            toZone = pair.toZone,
            onDismiss = { activeReminderPair = null }
        )
    }
}

// ── Section 1: Working Now Card ───────────────────────────────────────

@Composable
private fun WorkingNowCard(pairs: List<TimePairUi>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val uniqueZones = remember(pairs) {
                pairs.flatMap { listOf(it.fromZone, it.toZone) }.distinct()
            }

            uniqueZones.forEachIndexed { idx, zone ->
                if (idx > 0) Spacer(Modifier.height(12.dp))
                val hour = try {
                    ZonedDateTime.now(ZoneId.of(zone)).hour
                } catch (_: Exception) { -1 }

                val isWorking = hour in 9..17
                val timeLabel = when {
                    hour < 0   -> "Unknown"
                    hour < 6   -> "Late night ($hour:00)"
                    hour < 9   -> "Early morning ($hour:00)"
                    hour <= 17 -> "Working hours ($hour:00)"
                    hour <= 21 -> "Evening ($hour:00)"
                    else       -> "Night ($hour:00)"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isWorking) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.outline
                            )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            shortZone(zone),
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            timeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isWorking)
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text     = if (isWorking) "Active" else "Offline",
                            style    = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color    = if (isWorking)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Section 2: Pair Insight Card ──────────────────────────────────────

@Composable
private fun PairInsightCard(
    ui: TimePairUi,
    timeOffsetMinutes: Int,
    context: Context,
    onSetReminder: () -> Unit
) {
    val bestWindow = remember(ui.offsetDifferenceMinutes) {
        computeBestWindow(ui.offsetDifferenceMinutes)
    }

    val liveNow = remember(ui.currentEpochMillis, timeOffsetMinutes) {
        val zdt = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(ui.currentEpochMillis), java.time.ZoneId.systemDefault())
        Pair(zdt.hour, zdt.minute)
    }

    val liveStatus = remember(ui.offsetDifferenceMinutes, liveNow) {
        computeLiveOverlapStatus(liveNow.first, liveNow.second, ui.offsetDifferenceMinutes)
    }

    val scorecard = remember(ui.offsetDifferenceMinutes) {
        computeScorecard(ui.offsetDifferenceMinutes)
    }

    var sliderHour by remember { mutableFloatStateOf(liveNow.first.toFloat()) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Pair title + Scorecard badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (ui.label.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                ui.label,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary,
                                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        "${shortZone(ui.fromZone)} → ${shortZone(ui.toZone)}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = scorecard.color.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Star,
                            contentDescription = null,
                            tint = scorecard.color,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${scorecard.score}/100",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = scorecard.color
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Clean Live Active / Upcoming Overlap Status Banner (No emojis!)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = liveStatus.containerColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(liveStatus.accentColor)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = liveStatus.headline,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = liveStatus.textColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = liveStatus.subtext,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = liveStatus.textColor.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Dual Timezone Window Bounds
            if (bestWindow != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Dual-Timezone Overlap Bounds",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${shortZone(ui.fromZone)}: ${bestWindow.first} – ${bestWindow.second}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${shortZone(ui.toZone)}: ${bestWindow.targetFirst} – ${bestWindow.targetSecond}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
            }

            // Top 3 Recommended Meeting Slots with Professional Material Icons
            Text(
                "Recommended Meeting Slots",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))

            val slots = remember(ui.offsetDifferenceMinutes) {
                computeRecommendedSlots(ui.offsetDifferenceMinutes, shortZone(ui.fromZone), shortZone(ui.toZone))
            }

            slots.forEach { slot ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = slot.iconTint.copy(alpha = 0.12f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = slot.icon,
                                    contentDescription = null,
                                    tint = slot.iconTint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = slot.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${slot.localTime} local (${slot.targetTime} remote)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = {
                            val intent = CalendarHelper.createCalendarEventIntent(
                                title = "${slot.title} (${shortZone(ui.fromZone)} & ${shortZone(ui.toZone)})",
                                description = "Slot: ${slot.localTime} local / ${slot.targetTime} remote",
                                startMillis = System.currentTimeMillis() + 3_600_000L,
                                endMillis = System.currentTimeMillis() + 7_200_000L,
                                timeZone = ui.toZone
                            )
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(18.dp))
                        }
                        TextButton(onClick = onSetReminder) {
                            Icon(Icons.Outlined.AddAlarm, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Interactive Hour Scrubber Card (Clean professional styling)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Interactive Hour Scrubber",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    val selectedH = sliderHour.toInt().coerceIn(0, 23)
                    val targetH = (selectedH + (ui.offsetDifferenceMinutes / 60) + 24) % 24
                    val localWorking = selectedH in 9..17
                    val targetWorking = targetH in 9..17

                    val (statusLabel, statusColor) = when {
                        localWorking && targetWorking -> Pair("Both Working", MaterialTheme.colorScheme.tertiary)
                        localWorking -> Pair("${shortZone(ui.fromZone)} Working", MaterialTheme.colorScheme.primary)
                        targetWorking -> Pair("${shortZone(ui.toZone)} Working", MaterialTheme.colorScheme.secondary)
                        else -> Pair("Both Off-Hours", MaterialTheme.colorScheme.error)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                val selectedH = sliderHour.toInt().coerceIn(0, 23)
                val targetH = (selectedH + (ui.offsetDifferenceMinutes / 60) + 24) % 24

                Text(
                    text = "${formatHour12(selectedH)} (${shortZone(ui.fromZone)}) ↔ ${formatHour12(targetH)} (${shortZone(ui.toZone)})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Slider(
                    value = sliderHour,
                    onValueChange = { sliderHour = it },
                    valueRange = 0f..23f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Spacer(Modifier.height(16.dp))

            // 24-Hour Overlap Chart with Live NOW Needle
            TimeAuditChart(
                fromZone = shortZone(ui.fromZone),
                toZone = shortZone(ui.toZone),
                offsetDifferenceMinutes = ui.offsetDifferenceMinutes,
                currentEpochMillis = ui.currentEpochMillis,
                currentHour = liveNow.first,
                currentMinute = liveNow.second,
                showNowNeedle = true
            )
        }
    }
}

// ── Helpers & Calculation Models ──────────────────────────────────────

private data class BestWindow(
    val first: String,
    val second: String,
    val targetFirst: String,
    val targetSecond: String,
    val overlapHours: Int
)

private fun computeBestWindow(offsetDiffMinutes: Int): BestWindow? {
    var firstOverlapHour = -1
    var lastOverlapHour  = -1
    var count = 0
    for (hour in 0 until 24) {
        val isLocalWorking  = hour in 9..17
        val targetHour      = (hour + (offsetDiffMinutes / 60) + 24) % 24
        val isTargetWorking = targetHour in 9..17
        if (isLocalWorking && isTargetWorking) {
            if (firstOverlapHour < 0) firstOverlapHour = hour
            lastOverlapHour = hour
            count++
        }
    }
    if (count == 0) return null

    val targetFirstH = (firstOverlapHour + (offsetDiffMinutes / 60) + 24) % 24
    val targetLastH = ((lastOverlapHour + 1) + (offsetDiffMinutes / 60) + 24) % 24

    return BestWindow(
        first        = formatHour12(firstOverlapHour),
        second       = formatHour12(lastOverlapHour + 1),
        targetFirst  = formatHour12(targetFirstH),
        targetSecond = formatHour12(targetLastH),
        overlapHours = count
    )
}

private data class LiveStatus(
    val headline: String,
    val subtext: String,
    val containerColor: Color,
    val accentColor: Color,
    val textColor: Color
)

private fun computeLiveOverlapStatus(nowH: Int, nowM: Int, offsetDiffMinutes: Int): LiveStatus {
    val targetH = (nowH + (offsetDiffMinutes / 60) + 24) % 24
    val localWorking = nowH in 9..17
    val targetWorking = targetH in 9..17

    return when {
        localWorking && targetWorking -> {
            val endDiffMin = (18 - nowH) * 60 - nowM
            val endH = endDiffMin / 60
            val endM = endDiffMin % 60
            LiveStatus(
                headline = "Active Overlap Window",
                subtext = "Ends in ${endH}h ${endM}m",
                containerColor = Color(0xFFE8F5E9),
                accentColor = Color(0xFF2E7D32),
                textColor = Color(0xFF1B5E20)
            )
        }
        nowH < 9 -> {
            val startMin = (9 - nowH) * 60 - nowM
            val h = startMin / 60
            val m = startMin % 60
            LiveStatus(
                headline = "Next Overlap Upcoming Today",
                subtext = "Starts in ${h}h ${m}m (at 9:00 AM)",
                containerColor = Color(0xFFFFF8E1),
                accentColor = Color(0xFFF57F17),
                textColor = Color(0xFFF57F17)
            )
        }
        else -> {
            val startMin = (24 - nowH + 9) * 60 - nowM
            val h = startMin / 60
            val m = startMin % 60
            LiveStatus(
                headline = "Outside Working Overlap",
                subtext = "Next overlap in ${h}h ${m}m",
                containerColor = Color(0xFFFFEBEE),
                accentColor = Color(0xFFC62828),
                textColor = Color(0xFFB71C1C)
            )
        }
    }
}

private data class Scorecard(
    val score: Int,
    val color: Color
)

private fun computeScorecard(offsetDiffMinutes: Int): Scorecard {
    var overlapHours = 0
    for (hour in 0 until 24) {
        val localWorking = hour in 9..17
        val targetHour = (hour + (offsetDiffMinutes / 60) + 24) % 24
        val targetWorking = targetHour in 9..17
        if (localWorking && targetWorking) overlapHours++
    }

    val score = (overlapHours * 11 + 12).coerceIn(15, 98)
    val color = when {
        score >= 70 -> Color(0xFF2E7D32)
        score >= 45 -> Color(0xFFF57F17)
        else        -> Color(0xFFC62828)
    }
    return Scorecard(score, color)
}

private data class MeetingSlot(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val localTime: String,
    val targetTime: String
)

private fun computeRecommendedSlots(offsetDiffMinutes: Int, localZone: String, targetZone: String): List<MeetingSlot> {
    val targetOffsetH = offsetDiffMinutes / 60

    val primeH = 10
    val targetPrimeH = (primeH + targetOffsetH + 24) % 24

    val earlyH = 9
    val targetEarlyH = (earlyH + targetOffsetH + 24) % 24

    val eveningH = 16
    val targetEveningH = (eveningH + targetOffsetH + 24) % 24

    return listOf(
        MeetingSlot(Icons.Outlined.Star, Color(0xFFD84315), "Golden Sync Window", formatHour12(primeH), formatHour12(targetPrimeH)),
        MeetingSlot(Icons.Outlined.WbSunny, Color(0xFFF57F17), "Early Morning Slot", formatHour12(earlyH), formatHour12(targetEarlyH)),
        MeetingSlot(Icons.Outlined.NightsStay, Color(0xFF1565C0), "Evening Standup Slot", formatHour12(eveningH), formatHour12(targetEveningH))
    )
}

private fun formatHour12(h: Int): String {
    val hour = (h + 24) % 24
    val ampm = if (hour < 12) "AM" else "PM"
    val h12 = when {
        hour == 0 -> 12
        hour <= 12 -> hour
        else -> hour - 12
    }
    return "$h12:00 $ampm"
}

@Composable
private fun SectionLabel(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(top = 6.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun shortZone(tz: String): String =
    tz.substringAfterLast('/').replace('_', ' ')
