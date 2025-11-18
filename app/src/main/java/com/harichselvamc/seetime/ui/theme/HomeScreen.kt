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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: TimeViewModel) {
    val state by viewModel.state.collectAsState()

    // Dialog state: null = add, non-null = edit
    var showDialog by remember { mutableStateOf(false) }
    var editingPair by remember { mutableStateOf<TimePairUi?>(null) }

    // Initial load + start ticking seconds
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
                    // First load: spinner in center
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.pairs.isEmpty() -> {
                    // Empty state
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
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (state.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(18.dp)
                                )
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text("Refreshing times…")
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

                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { value ->
                                        when (value) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                // Right swipe -> Delete
                                                viewModel.deletePair(ui.id)
                                                true
                                            }

                                            SwipeToDismissBoxValue.EndToStart -> {
                                                // Left swipe -> Edit
                                                editingPair = ui
                                                showDialog = true
                                                // Don't remove item
                                                false
                                            }

                                            else -> false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = true,
                                    enableDismissFromEndToStart = true,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Swipe → delete | ← edit",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    },
                                    content = {
                                        DraggableTimePairCard(
                                            ui = ui,
                                            index = index,
                                            onMove = { from, delta ->
                                                val to = (from + delta)
                                                    .coerceIn(0, state.pairs.lastIndex)
                                                if (from != to) {
                                                    viewModel.movePair(from, to)
                                                }
                                            }
                                        )
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
}

/**
 * Simple pill-style chip for small pieces of info.
 */
@Composable
private fun InfoChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * One card that can be long-pressed and dragged up/down to reorder.
 * Shows a small "≡" handle on the right as visual cue.
 */
@Composable
private fun DraggableTimePairCard(
    ui: TimePairUi,
    index: Int,
    onMove: (fromIndex: Int, delta: Int) -> Unit
) {
    // Split the date + time so UI looks cleaner
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

    // Track drag distance just to know when to move up/down by 1
    val dragOffsetY = remember { mutableStateOf(0f) }
    val moveThresholdPx = 56.dp.value // approx one row height in dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(ui.id) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, dragAmount ->
                        change.consumePositionChange()
                        dragOffsetY.value += dragAmount.y

                        // Move down
                        if (dragOffsetY.value > moveThresholdPx) {
                            onMove(index, +1)
                            dragOffsetY.value = 0f
                        }
                        // Move up
                        else if (dragOffsetY.value < -moveThresholdPx) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Top row: zones + drag handle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = ui.fromZone,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = fromTime.ifEmpty { ui.displayFromTime },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (fromDate.isNotEmpty()) {
                        Text(
                            text = fromDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = ui.toZone,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = toTime.ifEmpty { ui.displayToTime },
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (toDate.isNotEmpty()) {
                        Text(
                            text = toDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Drag handle visual (≡)
                Text(
                    text = "≡",
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom: chips for diff and DST
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(
                    text = ui.diffText
                )
                InfoChip(
                    text = ui.dstText
                )
            }
        }
    }
}
