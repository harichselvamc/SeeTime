@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.util.CalendarHelper
import com.harichselvamc.seetime.util.TimeMath
import com.harichselvamc.seetime.util.TimeMath.OverlapCategory
import java.time.LocalTime

import com.harichselvamc.seetime.ui.TimeViewModel
import com.harichselvamc.seetime.ui.TimePairUi
import com.harichselvamc.seetime.ui.SmartReminderDialog


private fun shortZone(tz: String): String =
    tz.substringAfterLast('/').replace('_', ' ')

@Composable
fun MeetingOverlapScreen(
    viewModel: TimeViewModel
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var selectedViewMode by remember { mutableIntStateOf(0) } // 0 = Matrix, 1 = Flight & Solar Scrubber
    var selectedPairIndex by remember { mutableIntStateOf(0) }
    var selectedHour by remember { mutableIntStateOf(LocalTime.now().hour) }
    var activeReminderPair by remember { mutableStateOf<TimePairUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.startTicker()
    }
    
    val ui = state.pairs.getOrNull(selectedPairIndex) ?: state.pairs.firstOrNull()

    if (selectedViewMode == 1) {
        FlightTimezoneScrubberScreen(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
            onBack = { selectedViewMode = 0 }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.GridOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Meeting Overlap Matrix",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Scrubber Switch Chip
                    FilterChip(
                        selected = false,
                        onClick = { selectedViewMode = 1 },
                        label = { Text("Flight Scrubber ✈") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "24-hour visual interactive overlap grid across working hours (9 AM – 5 PM)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state.pairs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(40.dp)
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
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "No Time Pairs Added",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add timezone comparison pairs on the Home tab to unlock the 24-hour interactive overlap matrix.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 120.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (ui != null) {
                        item { // Wrap all ui-dependent content in a single item
                            Column {
                                // Time pair filter selector chips
                                if (state.pairs.size > 1) {
                                    Text(
                                        "Select Time Pair",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(bottom = 6.dp) // Add padding to separate from next card
                                    ) {
                                        items(state.pairs.size) { idx ->
                                            val pair = state.pairs[idx]
                                            val isSelected = idx == selectedPairIndex
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = { selectedPairIndex = idx },
                                                label = {
                                                    Text(
                                                        if (pair.label.isNotBlank()) pair.label
                                                        else "${shortZone(pair.fromZone)} → ${shortZone(pair.toZone)}"
                                                    )
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }

                                val matrix = remember(ui.offsetDifferenceMinutes) {
                                    TimeMath.compute24HourOverlapMatrix(ui.offsetDifferenceMinutes)
                                }

                                val fullCount = remember(matrix) { matrix.count { it.category == OverlapCategory.FULL_WORKING } }
                                val extCount  = remember(matrix) { matrix.count { it.category == OverlapCategory.EXTENDED_WORKING } }
                                val offCount  = remember(matrix) { matrix.count { it.category == OverlapCategory.OFF_HOURS } }

                                // Overlap KPI Summary Card
                                PairSummaryCard(
                                    ui = ui,
                                    fullCount = fullCount,
                                    extCount = extCount,
                                    offCount = offCount
                                )

                                Spacer(Modifier.height(20.dp))

                                // 24-Hour Visual Matrix Grid
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "24-Hour Interactive Grid",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "Tap cell to inspect",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        // 24 Hour Grid (6 columns x 4 rows)
                                        Box(modifier = Modifier.height(280.dp)) {
                                            LazyVerticalGrid(
                                                columns = GridCells.Fixed(6),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxSize(),
                                                userScrollEnabled = false
                                            ) {
                                                items(matrix) { slot ->
                                                    val isSelected = slot.localHour == selectedHour
                                                    val isCurrentHour = slot.localHour == LocalTime.now().hour

                                                    GridCellItem(
                                                        slot = slot,
                                                        isSelected = isSelected,
                                                        isCurrentHour = isCurrentHour,
                                                        onClick = { selectedHour = slot.localHour }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        // Legend
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            LegendChip(
                                                color = Color(0xFF2E7D32),
                                                label = "Full (9-5)"
                                            )
                                            LegendChip(
                                                color = Color(0xFFF57F17),
                                                label = "Extended"
                                            )
                                            LegendChip(
                                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                label = "Off Hours"
                                            )
                                            LegendChip(
                                                color = MaterialTheme.colorScheme.error,
                                                label = "Current Hour"
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                // Selected Slot Detail & Actions
                                val selectedSlot = matrix.find { it.localHour == selectedHour } ?: matrix[0]
                                SelectedSlotCard(
                                    ui = ui,
                                    slot = selectedSlot,
                                    context = context,
                                    onSetReminder = { activeReminderPair = ui }
                                )
                            }
                        }
                    } else {
                        item { // Fallback for when ui is null
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No time pair selected for overlap matrix.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(20.dp)
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

// ── Summary KPI Card ──────────────────────────────────────────────────
@Composable
private fun PairSummaryCard(
    ui: TimePairUi,
    fullCount: Int,
    extCount: Int,
    offCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${shortZone(ui.fromZone)} ↔ ${shortZone(ui.toZone)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = ui.diffText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiStatBox(
                    label = "Full Overlap",
                    value = "${fullCount}h",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                KpiStatBox(
                    label = "Extended",
                    value = "${extCount}h",
                    containerColor = Color(0xFFFFF8E1),
                    contentColor = Color(0xFFF57F17),
                    modifier = Modifier.weight(1f)
                )
                KpiStatBox(
                    label = "Off Hours",
                    value = "${offCount}h",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun KpiStatBox(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

// ── Grid Cell Item ────────────────────────────────────────────────────
@Composable
private fun GridCellItem(
    slot: TimeMath.HourlyOverlapSlot,
    isSelected: Boolean,
    isCurrentHour: Boolean,
    onClick: () -> Unit
) {
    val (bgColor, textColor) = when (slot.category) {
        OverlapCategory.FULL_WORKING -> Pair(Color(0xFFE8F5E9), Color(0xFF1B5E20))
        OverlapCategory.EXTENDED_WORKING -> Pair(Color(0xFFFFF8E1), Color(0xFFE65100))
        OverlapCategory.OFF_HOURS -> Pair(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isCurrentHour -> MaterialTheme.colorScheme.error
        else -> Color.Transparent
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .border(
                width = if (isSelected || isCurrentHour) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = slot.localDisplay,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontSize = 10.sp,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = slot.targetDisplay,
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.8f),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}

// ── Selected Slot Action Card ─────────────────────────────────────────
@Composable
private fun SelectedSlotCard(
    ui: TimePairUi,
    slot: TimeMath.HourlyOverlapSlot,
    context: Context,
    onSetReminder: () -> Unit
) {
    val (catLabel, catColor, catBg) = when (slot.category) {
        OverlapCategory.FULL_WORKING -> Triple("Prime Overlap Window", Color(0xFF2E7D32), Color(0xFFE8F5E9))
        OverlapCategory.EXTENDED_WORKING -> Triple("Extended Window", Color(0xFFF57F17), Color(0xFFFFF8E1))
        OverlapCategory.OFF_HOURS -> Triple("Off-Hours", Color(0xFFC62828), Color(0xFFFFEBEE))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Selected Time Slot",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = catBg
                ) {
                    Text(
                        text = catLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = catColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${shortZone(ui.fromZone)} Local",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = slot.localDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "↔",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${shortZone(ui.toZone)} Remote",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = slot.targetDisplay,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = remember(ui, slot, context) {
                        {
                            val intent = CalendarHelper.createCalendarEventIntent(
                                title = "Meeting (${shortZone(ui.fromZone)} & ${shortZone(ui.toZone)})",
                                description = "Scheduled slot: ${slot.localDisplay} local / ${slot.targetDisplay} remote",
                                startMillis = System.currentTimeMillis() + 3_600_000L,
                                endMillis = System.currentTimeMillis() + 7_200_000L,
                                timeZone = ui.toZone
                            )
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.CalendarMonth, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Calendar", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = remember(onSetReminder) { { onSetReminder() } },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.AddAlarm, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reminder", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = remember(ui, slot, context) {
                        {
                            val text = "Meeting Window: ${slot.localDisplay} (${shortZone(ui.fromZone)}) / ${slot.targetDisplay} (${shortZone(ui.toZone)})"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SeeTime Meeting Window", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied slot to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LegendChip(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}
