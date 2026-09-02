package com.harichselvamc.seetime.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AirplaneTicket
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.harichselvamc.seetime.data.TravelPassRepository
import com.harichselvamc.seetime.util.BarcodeFormat
import com.harichselvamc.seetime.util.FlightCountdownStatus
import com.harichselvamc.seetime.util.PassType
import com.harichselvamc.seetime.util.TravelPass
import com.harichselvamc.seetime.util.TravelPassManager
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelVaultScreen(
    viewModel: TimeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { TravelPassRepository(context) }
    val passes by repository.passes.collectAsState()

    var selectedFilterType by remember { mutableStateOf<PassType?>(null) }
    var selectedPassForBarcode by remember { mutableStateOf<TravelPass?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val activeFlightPass = passes.firstOrNull { it.isPinned } ?: passes.firstOrNull { it.passType == PassType.FLIGHT } ?: passes.firstOrNull()

    val filteredPasses = passes.filter { pass ->
        selectedFilterType == null || pass.passType == selectedFilterType
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AirplaneTicket,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Travel Vault & Boarding Passes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { repository.resetToDefault() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restore Sample Passes")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Travel Pass")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Live Flight Gate & Dual Timezone HUD ────────────────────────
            if (activeFlightPass != null) {
                item {
                    FlightGateHudCard(
                        pass = activeFlightPass,
                        onScanBarcode = { selectedPassForBarcode = activeFlightPass }
                    )
                }
            }

            // ── Filter Chips ────────────────────────────────────────────────
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilterType == null,
                            onClick = { selectedFilterType = null },
                            label = { Text("All Passes (${passes.size})") }
                        )
                    }
                    items(PassType.values()) { type ->
                        val count = passes.count { it.passType == type }
                        FilterChip(
                            selected = selectedFilterType == type,
                            onClick = { selectedFilterType = if (selectedFilterType == type) null else type },
                            label = { Text("${type.displayName.substringBefore(' ')} ($count)") }
                        )
                    }
                }
            }

            // ── Pass Cards List ─────────────────────────────────────────────
            if (filteredPasses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No passes found in vault. Tap + to add one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredPasses, key = { it.id }) { pass ->
                    BoardingPassTicketCard(
                        pass = pass,
                        onOpenBarcode = { selectedPassForBarcode = pass },
                        onTogglePin = { repository.togglePin(pass.id) },
                        onDelete = { repository.deletePass(pass.id) },
                        onShare = {
                            val shareText = "✈️ ${pass.carrierName} ${pass.carrierCode}${pass.serviceNumber}\nRoute: ${pass.originCode} (${pass.originCity}) ➔ ${pass.destCode} (${pass.destCity})\nSeat: ${pass.seat} | Gate: ${pass.gate}\nPNR: ${pass.bookingReference}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Travel Pass", shareText))
                            Toast.makeText(context, "Pass details copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ── Full-Screen Brightness Barcode Dialog ──────────────────────────────
    if (selectedPassForBarcode != null) {
        BarcodeScannerDialog(
            pass = selectedPassForBarcode!!,
            onDismiss = { selectedPassForBarcode = null }
        )
    }

    // ── Add Pass Dialog ──────────────────────────────────────────────────
    if (showAddDialog) {
        AddTravelPassDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { newPass ->
                repository.addPass(newPass)
                showAddDialog = false
            }
        )
    }
}

// ── Flight Gate HUD Card ───────────────────────────────────────────────

@Composable
private fun FlightGateHudCard(
    pass: TravelPass,
    onScanBarcode: () -> Unit
) {
    val countdown = remember(pass) { TravelPassManager.calculateCountdown(pass) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FlightTakeoff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${pass.carrierCode}${pass.serviceNumber} · ${pass.carrierName}".uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = countdown.statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Airport Codes & Arrow Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = pass.originCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = pass.originCity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = countdown.departureFormattedOrigin,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = countdown.flightDurationFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "──────── ✈ ────────",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${if (countdown.timeDifferenceHours >= 0) "+${countdown.timeDifferenceHours}" else "${countdown.timeDifferenceHours}"}h timezone shift",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = pass.destCode,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = pass.destCity,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = countdown.arrivalFormattedDest,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gate / Seat / Barcode Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("GATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(pass.gate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("SEAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(pass.seat, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("PNR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(pass.bookingReference, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onScanBarcode,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show Code")
                }
            }
        }
    }
}

// ── Boarding Pass Ticket Card ──────────────────────────────────────────

@Composable
private fun BoardingPassTicketCard(
    pass: TravelPass,
    onOpenBarcode: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Carrier & Pin/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (pass.passType) {
                            PassType.FLIGHT -> Icons.Filled.Flight
                            PassType.TRAIN -> Icons.Filled.Train
                            PassType.HOTEL -> Icons.Filled.Hotel
                            PassType.EVENT -> Icons.Filled.ConfirmationNumber
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${pass.carrierName} · ${pass.serviceNumber}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (pass.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin pass",
                            tint = if (pass.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share pass", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete pass", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Passenger Name & Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PASSENGER", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(pass.passengerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("CONFIRMATION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(pass.bookingReference, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Perforated line effect
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            ) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    strokeWidth = 2f
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gate / Seat / Mini Barcode Area
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text("SEAT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(pass.seat, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("GATE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(pass.gate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("GROUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(pass.boardingGroup, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    }
                }

                // Mini Barcode Thumbnail (Click to open full-screen scanner)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    modifier = Modifier
                        .clickable(onClick = onOpenBarcode)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        BarcodeMatrixCanvas(
                            payload = pass.barcodePayload,
                            format = pass.barcodeFormat,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            if (pass.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pass.notes,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Pure Algorithmic Barcode Canvas ────────────────────────────────────

@Composable
fun BarcodeMatrixCanvas(
    payload: String,
    format: BarcodeFormat,
    modifier: Modifier = Modifier,
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White
) {
    val matrix = remember(payload, format) {
        TravelPassManager.generateBarcodeMatrix(payload, format)
    }

    Canvas(modifier = modifier) {
        val rows = matrix.size
        val cols = matrix[0].size
        val cellW = size.width / cols
        val cellH = size.height / rows

        drawRect(color = backgroundColor)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (matrix[r][c]) {
                    drawRect(
                        color = barColor,
                        topLeft = Offset(c * cellW, r * cellH),
                        size = Size(cellW, cellH)
                    )
                }
            }
        }
    }
}

// ── Full-Screen Brightness Barcode Dialog ───────────────────────────────

@Composable
private fun BarcodeScannerDialog(
    pass: TravelPass,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${pass.carrierName} ${pass.carrierCode}${pass.serviceNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "${pass.originCode} ➔ ${pass.destCode} · Seat ${pass.seat}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Large High-Contrast Barcode / QR Code
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .size(240.dp)
                        .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    BarcodeMatrixCanvas(
                        payload = pass.barcodePayload,
                        format = pass.barcodeFormat,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PASSENGER: ${pass.passengerName}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "BOOKING REF (PNR): ${pass.bookingReference}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✨ Screen brightness optimized for optical gate scanners",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

// ── Add Travel Pass Dialog ─────────────────────────────────────────────

@Composable
private fun AddTravelPassDialog(
    onDismiss: () -> Unit,
    onConfirm: (TravelPass) -> Unit
) {
    var passType by remember { mutableStateOf(PassType.FLIGHT) }
    var carrierName by remember { mutableStateOf("") }
    var serviceNumber by remember { mutableStateOf("") }
    var passengerName by remember { mutableStateOf("HARICHSELVAM / C") }
    var originCode by remember { mutableStateOf("JFK") }
    var originCity by remember { mutableStateOf("New York") }
    var originZoneId by remember { mutableStateOf("America/New_York") }
    var destCode by remember { mutableStateOf("HND") }
    var destCity by remember { mutableStateOf("Tokyo") }
    var destZoneId by remember { mutableStateOf("Asia/Tokyo") }
    var seat by remember { mutableStateOf("14A") }
    var gate by remember { mutableStateOf("Gate 12") }
    var pnr by remember { mutableStateOf("8KTY9X") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Travel Pass / Ticket") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text("Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PassType.values()) { type ->
                            FilterChip(
                                selected = passType == type,
                                onClick = { passType = type },
                                label = { Text(type.displayName.substringBefore(' ')) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = carrierName,
                        onValueChange = { carrierName = it },
                        label = { Text("Carrier / Airline Name") },
                        placeholder = { Text("e.g. Singapore Airlines") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = serviceNumber,
                        onValueChange = { serviceNumber = it },
                        label = { Text("Flight / Service Number") },
                        placeholder = { Text("e.g. SQ026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = originCode,
                            onValueChange = { originCode = it },
                            label = { Text("Origin (Code)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = destCode,
                            onValueChange = { destCode = it },
                            label = { Text("Dest (Code)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = seat,
                            onValueChange = { seat = it },
                            label = { Text("Seat") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = gate,
                            onValueChange = { gate = it },
                            label = { Text("Gate") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = pnr,
                        onValueChange = { pnr = it },
                        label = { Text("Booking Reference (PNR)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = Instant.now()
                    val newPass = TravelPass(
                        passType = passType,
                        carrierName = if (carrierName.isNotBlank()) carrierName else "Airline Express",
                        carrierCode = if (serviceNumber.length >= 2) serviceNumber.take(2).uppercase() else "FL",
                        serviceNumber = if (serviceNumber.isNotBlank()) serviceNumber else "101",
                        passengerName = passengerName,
                        originCode = originCode.uppercase(),
                        originCity = originCity,
                        originZoneId = originZoneId,
                        destCode = destCode.uppercase(),
                        destCity = destCity,
                        destZoneId = destZoneId,
                        departureEpochMillis = now.plus(3, ChronoUnit.HOURS).toEpochMilli(),
                        arrivalEpochMillis = now.plus(16, ChronoUnit.HOURS).toEpochMilli(),
                        seat = seat,
                        gate = gate,
                        bookingReference = pnr.uppercase(),
                        barcodeFormat = BarcodeFormat.QR_CODE,
                        barcodePayload = "SEETIME-PASS:${carrierName}:${originCode}-${destCode}:${seat}:PNR-${pnr}"
                    )
                    onConfirm(newPass)
                }
            ) {
                Text("Add Pass")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
