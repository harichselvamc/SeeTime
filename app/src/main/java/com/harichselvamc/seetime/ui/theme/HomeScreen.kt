@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.harichselvamc.seetime.util.CalendarHelper

@Composable
fun HomeScreen(
    viewModel: TimeViewModel,
    startWithAddDialog: Boolean = false,
    onOpenTimeTravelSheet: () -> Unit = {}
) {
    val state      by viewModel.state.collectAsState()
    val timeOffset by viewModel.timeOffsetMinutes.collectAsState()

    var showDialog        by remember { mutableStateOf(startWithAddDialog) }
    var editingPair       by remember { mutableStateOf<TimePairUi?>(null) }
    var pendingDeletePair by remember { mutableStateOf<TimePairUi?>(null) }
    var smartReminderPair by remember { mutableStateOf<TimePairUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.startTicker()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "See Time",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (timeOffset == 0) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.error
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (timeOffset == 0) "Live" else "Time Travel",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (timeOffset == 0) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    // Refresh
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh all times",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Time Travel button
                    IconButton(onClick = onOpenTimeTravelSheet) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = "Time Travel — preview past or future",
                                tint = if (timeOffset != 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPair = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary,
                shape          = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add time pair")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Loading with no data
                state.isLoading && state.pairs.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color       = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Loading your time pairs…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Empty state
                state.pairs.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                "No time pairs yet",
                                style      = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Tap + to add your first timezone comparison",
                                style     = MaterialTheme.typography.bodyMedium,
                                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Pair list
                else -> {
                    if (state.isLoading) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Refreshing…",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            WorldClockBar(state.pairs)
                        }

                        itemsIndexed(
                            items = state.pairs,
                            key = { _, item -> item.id }
                        ) { index, ui ->
                            DraggableTimePairCard(
                                ui            = ui,
                                index         = index,
                                onEdit        = { editingPair = ui; showDialog = true },
                                onDelete      = { pendingDeletePair = ui },
                                onMove        = { from, delta ->
                                    val to = (from + delta).coerceIn(0, state.pairs.lastIndex)
                                    if (from != to) viewModel.movePair(from, to)
                                },
                                onSetReminder = { smartReminderPair = ui },
                                modifier      = Modifier.animateItem(
                                    fadeInSpec    = null,
                                    fadeOutSpec   = null,
                                    placementSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness    = Spring.StiffnessLow
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    smartReminderPair?.let { pair ->
        SmartReminderDialog(
            fromZone  = pair.fromZone,
            toZone    = pair.toZone,
            onDismiss = { smartReminderPair = null }
        )
    }

    if (showDialog) {
        AddTimePairDialog(
            onDismiss    = { showDialog = false; editingPair = null },
            onSave       = { from, to, label ->
                if (editingPair == null) viewModel.addPair(from, to, label)
                else viewModel.editPair(editingPair!!.id, from, to, label)
            },
            initialFrom  = editingPair?.fromZone ?: "Asia/Kolkata",
            initialTo    = editingPair?.toZone   ?: "Europe/London",
            initialLabel = editingPair?.label    ?: "",
            title        = if (editingPair == null) "Add Time Pair" else "Edit Time Pair"
        )
    }

    pendingDeletePair?.let { pair ->
        AlertDialog(
            onDismissRequest = { pendingDeletePair = null },
            shape = MaterialTheme.shapes.large,
            title = {
                Text(
                    "Delete time pair?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    "${shortZoneName(pair.fromZone)} → ${shortZoneName(pair.toZone)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deletePair(pair.id); pendingDeletePair = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePair = null }) { Text("Cancel") }
            }
        )
    }
}

// ── World Clock Quick-Glance Grid ─────────────────────────────────────

@Composable
private fun WorldClockBar(pairs: List<TimePairUi>) {
    val uniqueZones = remember(pairs) {
        pairs.flatMap { listOf(it.fromZone to it.displayFromTime, it.toZone to it.displayToTime) }
            .distinctBy { it.first }
            .take(6)
    }

    if (uniqueZones.isEmpty()) return

    val rows = remember(uniqueZones) { uniqueZones.chunked(3) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "World Clock Overview",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            rows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (zone, displayTime) ->
                        val rawTime = displayTime.split(",", limit = 2).getOrNull(1)?.trim() ?: displayTime
                        val compactTime = remember(rawTime) {
                            rawTime.replace(Regex(":\\d{2}(\\s*[AP]M|\\b)", RegexOption.IGNORE_CASE), "$1")
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = shortZoneName(zone),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = compactTime,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ── Time Pair Card ────────────────────────────────────────────────────

@Composable
private fun DraggableTimePairCard(
    ui: TimePairUi,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onSetReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fromParts = remember(ui.displayFromTime) { ui.displayFromTime.split(",", limit = 2) }
    val toParts   = remember(ui.displayToTime)   { ui.displayToTime.split(",", limit = 2) }
    val fromDate  = fromParts.getOrNull(0)?.trim().orEmpty()
    val fromTime  = fromParts.getOrNull(1)?.trim().orEmpty()
    val toDate    = toParts.getOrNull(0)?.trim().orEmpty()
    val toTime    = toParts.getOrNull(1)?.trim().orEmpty()

    val dayDiffText = remember(fromDate, toDate) {
        if (fromDate.isNotEmpty() && toDate.isNotEmpty() && fromDate != toDate) "+1 day" else null
    }

    val context = LocalContext.current
    val dragOffsetY = remember { mutableStateOf(0f) }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation  = 3.dp,
                shape      = MaterialTheme.shapes.medium,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                spotColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            )
            .pointerInput(ui.id) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY.value += dragAmount.y
                        if (dragOffsetY.value > 56.dp.value) {
                            onMove(index, +1); dragOffsetY.value = 0f
                        } else if (dragOffsetY.value < -56.dp.value) {
                            onMove(index, -1); dragOffsetY.value = 0f
                        }
                    },
                    onDragEnd    = { dragOffsetY.value = 0f },
                    onDragCancel = { dragOffsetY.value = 0f }
                )
            },
        shape  = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // ── Gradient Header ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        if (ui.label.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text       = ui.label,
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = MaterialTheme.colorScheme.primary,
                                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                shortZoneName(ui.fromZone),
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Text(
                                "  →  ",
                                style      = MaterialTheme.typography.titleSmall,
                                color      = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                shortZoneName(ui.toZone),
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = "Options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded          = menuExpanded,
                            onDismissRequest  = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text        = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Outlined.EditNote, null) },
                                onClick     = { menuExpanded = false; onEdit() }
                            )
                            DropdownMenuItem(
                                text        = { Text("Add to Calendar") },
                                leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null) },
                                onClick     = {
                                    menuExpanded = false
                                    val intent = CalendarHelper.createCalendarEventIntent(
                                        title       = "SeeTime Meeting (${ui.fromZone} → ${ui.toZone})",
                                        description = "Time difference: ${ui.diffText}",
                                        startMillis = System.currentTimeMillis() + 3_600_000L,
                                        endMillis   = System.currentTimeMillis() + 7_200_000L,
                                        timeZone    = ui.toZone
                                    )
                                    try { context.startActivity(intent) } catch (_: Exception) {}
                                }
                            )
                            DropdownMenuItem(
                                text        = { Text("Set Smart Reminder") },
                                leadingIcon = { Icon(Icons.Outlined.NotificationsNone, null) },
                                onClick     = { menuExpanded = false; onSetReminder() }
                            )
                            DropdownMenuItem(
                                text        = { Text("Move Up") },
                                leadingIcon = { Icon(Icons.Outlined.KeyboardArrowUp, null) },
                                onClick     = { menuExpanded = false; onMove(index, -1) }
                            )
                            DropdownMenuItem(
                                text        = { Text("Move Down") },
                                leadingIcon = { Icon(Icons.Outlined.KeyboardArrowDown, null) },
                                onClick     = { menuExpanded = false; onMove(index, +1) }
                            )
                            DropdownMenuItem(
                                text        = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.DeleteOutline, null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { menuExpanded = false; onDelete() }
                            )
                        }
                    }
                }
            }

            // ── Time blocks ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.Top
            ) {
                TimeBlock(
                    label       = shortZoneName(ui.fromZone),
                    time        = fromTime.ifEmpty { ui.displayFromTime },
                    date        = fromDate,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier    = Modifier.weight(1f)
                )
                // Arrow + day badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text(
                        "→",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    dayDiffText?.let {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                it,
                                style      = MaterialTheme.typography.labelSmall,
                                color      = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                TimeBlock(
                    label       = shortZoneName(ui.toZone),
                    time        = toTime.ifEmpty { ui.displayToTime },
                    date        = toDate,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    modifier    = Modifier.weight(1f)
                )
            }

            // ── Diff / DST ──
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Column(
                modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InfoRow(
                    dot   = MaterialTheme.colorScheme.primary,
                    text  = ui.diffText
                )
                InfoRow(
                    dot   = MaterialTheme.colorScheme.tertiary,
                    text  = ui.dstText
                )
            }

            // ── Chart ──
            Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                TimeAuditChart(
                    fromZone               = shortZoneName(ui.fromZone),
                    toZone                 = shortZoneName(ui.toZone),
                    offsetDifferenceMinutes = ui.offsetDifferenceMinutes,
                    currentEpochMillis     = ui.currentEpochMillis
                )
            }
        }
    }
}

@Composable
private fun TimeBlock(
    label: String,
    time: String,
    date: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style    = MaterialTheme.typography.labelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            time,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            maxLines   = 2
        )
        if (date.isNotEmpty()) {
            Text(
                date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(dot: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(4.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shortZoneName(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')
