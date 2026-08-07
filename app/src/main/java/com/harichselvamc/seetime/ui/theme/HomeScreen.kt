@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.harichselvamc.seetime.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    viewModel: TimeViewModel,
    startWithAddDialog: Boolean = false,
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    var showDialog by remember { mutableStateOf(startWithAddDialog) }
    var editingPair by remember { mutableStateOf<TimePairUi?>(null) }
    var pendingDeletePair by remember { mutableStateOf<TimePairUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load()
        viewModel.startTicker()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("See Time") },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPair = null
                    showDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Pair")
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                state.isLoading && state.pairs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.pairs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No time pairs yet",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add your first timezone comparison.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (state.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.height(18.dp))
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("Refreshing times...")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(
                                items = state.pairs,
                                key = { _, item -> item.id }
                            ) { index, ui ->
                                DraggableTimePairCard(
                                    ui = ui,
                                    index = index,
                                    onEdit = {
                                        editingPair = ui
                                        showDialog = true
                                    },
                                    onDelete = {
                                        pendingDeletePair = ui
                                    },
                                    onMove = { from, delta ->
                                        val to = (from + delta)
                                            .coerceIn(0, state.pairs.lastIndex)
                                        if (from != to) {
                                            viewModel.movePair(from, to)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddTimePairDialog(
            onDismiss = {
                showDialog = false
                editingPair = null
            },
            onSave = { from, to ->
                if (editingPair == null) {
                    viewModel.addPair(from, to)
                } else {
                    viewModel.editPair(editingPair!!.id, from, to)
                }
            },
            initialFrom = editingPair?.fromZone ?: "Asia/Kolkata",
            initialTo = editingPair?.toZone ?: "Europe/London",
            title = if (editingPair == null) "Add Time Pair" else "Edit Time Pair"
        )
    }

    pendingDeletePair?.let { pair ->
        AlertDialog(
            onDismissRequest = { pendingDeletePair = null },
            title = { Text("Delete time pair?") },
            text = { Text("${pair.fromZone} to ${pair.toZone}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePair(pair.id)
                        pendingDeletePair = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeletePair = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DraggableTimePairCard(
    ui: TimePairUi,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: (fromIndex: Int, delta: Int) -> Unit
) {
    val fromParts = remember(ui.displayFromTime) {
        ui.displayFromTime.split(",", limit = 2)
    }
    val toParts = remember(ui.displayToTime) {
        ui.displayToTime.split(",", limit = 2)
    }

    val fromDate = fromParts.getOrNull(0)?.trim().orEmpty()
    val fromTime = fromParts.getOrNull(1)?.trim().orEmpty()

    val toDate = toParts.getOrNull(0)?.trim().orEmpty()
    val toTime = toParts.getOrNull(1)?.trim().orEmpty()

    val dragOffsetY = remember { mutableStateOf(0f) }
    val moveThresholdPx = 56.dp.value
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(ui.id) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY.value += dragAmount.y

                        if (dragOffsetY.value > moveThresholdPx) {
                            onMove(index, +1)
                            dragOffsetY.value = 0f
                        } else if (dragOffsetY.value < -moveThresholdPx) {
                            onMove(index, -1)
                            dragOffsetY.value = 0f
                        }
                    },
                    onDragEnd = {
                        dragOffsetY.value = 0f
                    },
                    onDragCancel = {
                        dragOffsetY.value = 0f
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${shortZoneName(ui.fromZone)} -> ${shortZoneName(ui.toZone)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Time pair options"
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move up") },
                            leadingIcon = {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onMove(index, -1)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move down") },
                            leadingIcon = {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onMove(index, +1)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.Top
            ) {
                TimeBlock(
                    label = shortZoneName(ui.fromZone),
                    time = fromTime.ifEmpty { ui.displayFromTime },
                    date = fromDate,
                    modifier = Modifier.weight(1f)
                )
                TimeBlock(
                    label = shortZoneName(ui.toZone),
                    time = toTime.ifEmpty { ui.displayToTime },
                    date = toDate,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = ui.diffText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = ui.dstText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = time,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2
        )
        if (date.isNotEmpty()) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun shortZoneName(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')
