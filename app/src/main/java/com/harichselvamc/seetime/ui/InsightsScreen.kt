@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

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
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightbulbCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

@Composable
fun InsightsScreen(
    viewModel: TimeViewModel
) {
    val state by viewModel.state.collectAsState()

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
                "Insights",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Plain-English analysis of your timezone pairs",
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
                        "No Data Yet",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add timezone pairs from the Home tab to see collaboration insights here.",
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
                    top    = 12.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Section A: Who's working right now? ──
                item { SectionLabel("Who's working right now?", Icons.Outlined.WbSunny) }

                item {
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.medium,
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val workingStatuses = remember(state.pairs) {
                                state.pairs.flatMap { listOf(it.fromZone, it.toZone) }
                                    .distinct()
                                    .map { zone ->
                                        val hour = try {
                                            ZonedDateTime.now(ZoneId.of(zone)).hour
                                        } catch (_: Exception) { -1 }
                                        val working = hour in 9..17
                                        Triple(zone, hour, working)
                                    }
                            }
                            workingStatuses.forEachIndexed { idx, (zone, hour, working) ->
                                if (idx > 0) Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status dot
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (working) MaterialTheme.colorScheme.tertiary
                                                else MaterialTheme.colorScheme.outline
                                            )
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            zone.substringAfterLast('/').replace('_', ' '),
                                            style      = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        val timeLabel = when {
                                            hour < 0   -> "Unknown"
                                            hour < 6   -> "Late night ($hour:00)"
                                            hour < 9   -> "Morning ($hour:00)"
                                            hour <= 17 -> "Working hours ($hour:00)"
                                            hour <= 21 -> "Evening ($hour:00)"
                                            else       -> "Night ($hour:00)"
                                        }
                                        Text(
                                            timeLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (working)
                                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text     = if (working) "Working" else "Offline",
                                            style    = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color    = if (working)
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

                // ── Section B: Best Time to Meet ──
                item { SectionLabel("Best time to meet", Icons.Outlined.Groups) }

                items(state.pairs) { ui ->
                    val bestWindow = remember(ui.offsetDifferenceMinutes) {
                        computeBestWindow(ui.offsetDifferenceMinutes)
                    }
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.medium,
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Pair label
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (ui.label.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            ui.label,
                                            style      = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color      = MaterialTheme.colorScheme.primary,
                                            modifier   = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(
                                    "${ui.fromZone.substringAfterLast('/').replace('_',' ')} → ${ui.toZone.substringAfterLast('/').replace('_',' ')}",
                                    style      = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // Plain-English gap
                            val absMin = abs(ui.offsetDifferenceMinutes)
                            val h = absMin / 60
                            val m = absMin % 60
                            val dir = if (ui.offsetDifferenceMinutes < 0) "ahead of" else "behind"
                            val gapText = when {
                                h > 0 && m > 0 -> "${h}h ${m}m"
                                h > 0          -> "${h} hour${if (h > 1) "s" else ""}"
                                else           -> "${m} minutes"
                            }

                            InfoTile(
                                icon  = Icons.Outlined.Schedule,
                                color = MaterialTheme.colorScheme.primary,
                                text  = "${ui.fromZone.substringAfterLast('/').replace('_',' ')} is $gapText $dir ${ui.toZone.substringAfterLast('/').replace('_',' ')}"
                            )

                            Spacer(Modifier.height(10.dp))

                            // Best window
                            if (bestWindow != null) {
                                InfoTile(
                                    icon  = Icons.Outlined.Groups,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    text  = "Best time to call: ${bestWindow.first} – ${bestWindow.second} (your local time)"
                                )
                                Spacer(Modifier.height(10.dp))
                                InfoTile(
                                    icon  = Icons.Outlined.LightbulbCircle,
                                    color = MaterialTheme.colorScheme.secondary,
                                    text  = "You share ${bestWindow.overlapHours} hour${if (bestWindow.overlapHours > 1) "s" else ""} of business time each day"
                                )
                            } else {
                                InfoTile(
                                    icon  = Icons.Outlined.Info,
                                    color = MaterialTheme.colorScheme.error,
                                    text  = "No overlapping business hours — consider async collaboration"
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            // Chart stays for visual context
                            TimeAuditChart(
                                fromZone               = ui.fromZone.substringAfterLast('/').replace('_',' '),
                                toZone                 = ui.toZone.substringAfterLast('/').replace('_',' '),
                                offsetDifferenceMinutes = ui.offsetDifferenceMinutes
                            )
                        }
                    }
                }

                // ── Section C: DST Status ──
                item { SectionLabel("Daylight Saving Status", Icons.Outlined.WbSunny) }

                items(state.pairs) { ui ->
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = MaterialTheme.shapes.medium,
                        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "${ui.fromZone.substringAfterLast('/').replace('_',' ')} & ${ui.toZone.substringAfterLast('/').replace('_',' ')}",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
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
}

// ── Helpers ────────────────────────────────────────────────────────────

private data class BestWindow(
    val first: String,
    val second: String,
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

    fun fmt(h: Int): String {
        val ampm = if (h < 12) "AM" else "PM"
        val h12  = when {
            h == 0  -> 12
            h <= 12 -> h
            else    -> h - 12
        }
        return "$h12:00 $ampm"
    }
    return BestWindow(
        first        = fmt(firstOverlapHour),
        second       = fmt(lastOverlapHour + 1),
        overlapHours = count
    )
}

@Composable
private fun SectionLabel(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text       = text,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    color: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(16.dp).padding(top = 1.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
