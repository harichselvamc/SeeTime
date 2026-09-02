package com.harichselvamc.seetime.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ConnectingAirports
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.Purple60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.LayoverInfo
import com.harichselvamc.seetime.util.MultiLegTrip
import com.harichselvamc.seetime.util.TravelLeg
import com.harichselvamc.seetime.util.TripCircadianSleepWindow
import com.harichselvamc.seetime.util.TripSummary
import com.harichselvamc.seetime.util.TripTimelineEngine
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripTimelineScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = remember { TripTimelineEngine.getInstance(context) }
    val customTrips by engine.customTrips.collectAsState()

    val allTrips = remember(customTrips) { engine.getAllTrips() }
    var selectedTripId by remember { mutableStateOf(allTrips.firstOrNull()?.id ?: "") }

    val activeTrip = remember(allTrips, selectedTripId) {
        allTrips.find { it.id == selectedTripId } ?: allTrips.firstOrNull() ?: MultiLegTrip(tripTitle = "Trip", legs = emptyList())
    }

    val summary = remember(activeTrip) { engine.calculateTripSummary(activeTrip) }
    var showAddTripDialog by remember { mutableStateOf(false) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FlightTakeoff,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multi-Stop Trip Itinerary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Offline Brief",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = SuccessGreen
                            )
                        }
                    }
                    IconButton(onClick = { showAddTripDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Custom Trip",
                            tint = MaterialTheme.colorScheme.primary
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
            // Horizontal Trip Selection Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(allTrips) { trip ->
                        val isSelected = trip.id == selectedTripId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTripId = trip.id },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Flight,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(trip.tripTitle, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                selectedLabelColor = Cobalt60
                            )
                        )
                    }
                }
            }

            // Hero Journey Overview Card
            item {
                TripHeroSummaryCard(
                    summary = summary,
                    onCopyBrief = {
                        val brief = engine.generateMarkdownTripBrief(activeTrip)
                        copyToClipboard("Trip Brief", brief)
                    },
                    onDeleteTrip = {
                        engine.deleteCustomTrip(activeTrip.id)
                        selectedTripId = allTrips.firstOrNull()?.id ?: ""
                    }
                )
            }

            // Circadian In-Flight Sleep Strategy Section
            if (summary.sleepWindows.isNotEmpty()) {
                item {
                    Text(
                        text = "CIRCADIAN IN-FLIGHT SLEEP STRATEGY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(summary.sleepWindows) { sleepWindow ->
                    CircadianSleepWindowCard(sleepWindow = sleepWindow, originZone = summary.departureZdt.zone)
                }
            }

            // Vertical Timeline Journey Header
            item {
                Text(
                    text = "JOURNEY TIMELINE & AIRPORT TRANSIT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Vertical Timeline Steps
            items(summary.trip.legs) { leg ->
                val layover = summary.layovers.find { it.airportCode == leg.destAirportCode }
                TripTimelineLegBlock(
                    leg = leg,
                    layover = layover
                )
            }
        }
    }

    // Dialog: Add Custom Trip
    if (showAddTripDialog) {
        AddCustomTripDialog(
            onDismiss = { showAddTripDialog = false },
            onSave = { newTrip ->
                engine.saveCustomTrip(newTrip)
                selectedTripId = newTrip.id
                showAddTripDialog = false
            }
        )
    }
}

// ── Hero Summary Card ─────────────────────────────────────────────────

@Composable
private fun TripHeroSummaryCard(
    summary: TripSummary,
    onCopyBrief: () -> Unit,
    onDeleteTrip: () -> Unit
) {
    val durationHours = summary.totalJourneyDurationMinutes / 60
    val durationMins = summary.totalJourneyDurationMinutes % 60
    val shiftStr = if (summary.netTimezoneShiftHours > 0) "+${summary.netTimezoneShiftHours}h ahead" else "${summary.netTimezoneShiftHours}h behind"

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.trip.tripTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${summary.originCity} (${summary.originAirport}) ➔ ${summary.finalDestCity} (${summary.finalAirport})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Cobalt60
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onCopyBrief) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Trip Brief",
                            tint = Cobalt60
                        )
                    }
                    if (summary.trip.isCustom) {
                        IconButton(onClick = onDeleteTrip) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Custom Trip",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Metrics Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Cobalt60.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Transit Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${durationHours}h ${durationMins}m", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Purple60.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Net TZ Shift", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(shiftStr, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Purple60)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (summary.daysDelta != 0L) Amber60.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Calendar Shift", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (summary.daysDelta > 0) "+${summary.daysDelta}d Next" else if (summary.daysDelta < 0) "${summary.daysDelta}d Prev" else "Same Day",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.daysDelta != 0L) Amber60 else SuccessGreen
                        )
                    }
                }
            }
        }
    }
}

// ── Circadian Sleep Card ──────────────────────────────────────────────

@Composable
private fun CircadianSleepWindowCard(
    sleepWindow: TripCircadianSleepWindow,
    originZone: ZoneId
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val startStr = Instant.ofEpochMilli(sleepWindow.startEpochMillis).atZone(originZone).format(formatter)
    val endStr = Instant.ofEpochMilli(sleepWindow.endEpochMillis).atZone(originZone).format(formatter)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Purple60.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Purple60.copy(alpha = 0.20f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = null,
                        tint = Purple60,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sleep Window (${sleepWindow.flightNumber})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f Hours", sleepWindow.durationHours),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Purple60
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Recommended sleep: $startStr ➔ $endStr (Flight Time)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Purple60
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sleepWindow.rationale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Vertical Timeline Leg Block ───────────────────────────────────────

@Composable
private fun TripTimelineLegBlock(
    leg: TravelLeg,
    layover: LayoverInfo?
) {
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm (z)", Locale.getDefault())
    val dateFormat = DateTimeFormatter.ofPattern("EEE, MMM dd", Locale.getDefault())

    val depZdt = Instant.ofEpochMilli(leg.departureEpochMillis).atZone(ZoneId.of(leg.originTimezoneId))
    val arrZdt = Instant.ofEpochMilli(leg.arrivalEpochMillis).atZone(ZoneId.of(leg.destTimezoneId))

    val legMinutes = ChronoUnit.MINUTES.between(
        Instant.ofEpochMilli(leg.departureEpochMillis),
        Instant.ofEpochMilli(leg.arrivalEpochMillis)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Flight Segment Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Leg Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Cobalt60.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Flight,
                                    contentDescription = null,
                                    tint = Cobalt60,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${leg.airlineName} · ${leg.flightNumber}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${leg.originAirportCode} ➔ ${leg.destAirportCode} · ${legMinutes / 60}h ${legMinutes % 60}m Flight",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Cobalt60.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${legMinutes / 60}h ${legMinutes % 60}m",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Cobalt60,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Origin / Destination Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Departure Info
                    Column {
                        Text(
                            text = "DEPARTURE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = SuccessGreen
                        )
                        Text(
                            text = depZdt.format(timeFormat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${leg.originCity} (${leg.originAirportCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = depZdt.format(dateFormat),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    // Arrival Info
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ARRIVAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Cobalt60
                        )
                        Text(
                            text = arrZdt.format(timeFormat),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${leg.destCity} (${leg.destAirportCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = arrZdt.format(dateFormat),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                if (leg.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📌 ${leg.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Layover Connecting Node (if connecting)
        if (layover != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (layover.isTightConnection) Amber60.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (layover.isTightConnection) Icons.Filled.Warning else Icons.Filled.ConnectingAirports,
                        contentDescription = null,
                        tint = if (layover.isTightConnection) Amber60 else Cobalt60,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Layover in ${layover.cityName} (${layover.airportCode})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (layover.isTightConnection) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "· TIGHT (<90m)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Amber60
                                )
                            }
                        }
                        Text(
                            text = "Transit Duration: ${layover.durationMinutes / 60}h ${layover.durationMinutes % 60}m (Arrives ${layover.arrivalTimeStr} ➔ Departs ${layover.nextDepartureTimeStr})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// ── Add Custom Trip Dialog ────────────────────────────────────────────

@Composable
private fun AddCustomTripDialog(
    onDismiss: () -> Unit,
    onSave: (MultiLegTrip) -> Unit
) {
    var tripTitle by remember { mutableStateOf("") }
    var originCity by remember { mutableStateOf("New York") }
    var originAirport by remember { mutableStateOf("JFK") }
    var originTz by remember { mutableStateOf("America/New_York") }

    var destCity by remember { mutableStateOf("London") }
    var destAirport by remember { mutableStateOf("LHR") }
    var destTz by remember { mutableStateOf("Europe/London") }

    var flightNum by remember { mutableStateOf("BA 178") }
    var airline by remember { mutableStateOf("British Airways") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Multi-Leg Trip", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = tripTitle,
                    onValueChange = { tripTitle = it },
                    label = { Text("Trip Title") },
                    placeholder = { Text("e.g. European Summer Tour") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = originCity,
                        onValueChange = { originCity = it },
                        label = { Text("Origin City") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = originAirport,
                        onValueChange = { originAirport = it },
                        label = { Text("Airport Code") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = destCity,
                        onValueChange = { destCity = it },
                        label = { Text("Destination City") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = destAirport,
                        onValueChange = { destAirport = it },
                        label = { Text("Airport Code") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = airline,
                        onValueChange = { airline = it },
                        label = { Text("Airline") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = flightNum,
                        onValueChange = { flightNum = it },
                        label = { Text("Flight #") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tripTitle.isNotBlank()) {
                        val now = Instant.now()
                        val dep = now.plus(2, ChronoUnit.DAYS).toEpochMilli()
                        val arr = now.plus(2, ChronoUnit.DAYS).plus(7, ChronoUnit.HOURS).toEpochMilli()

                        val newTrip = MultiLegTrip(
                            tripTitle = tripTitle.trim(),
                            legs = listOf(
                                TravelLeg(
                                    originCity = originCity.trim(),
                                    originAirportCode = originAirport.trim().uppercase(),
                                    originTimezoneId = originTz,
                                    departureEpochMillis = dep,
                                    destCity = destCity.trim(),
                                    destAirportCode = destAirport.trim().uppercase(),
                                    destTimezoneId = destTz,
                                    arrivalEpochMillis = arr,
                                    flightNumber = flightNum.trim(),
                                    airlineName = airline.trim()
                                )
                            ),
                            isCustom = true
                        )
                        onSave(newTrip)
                    }
                },
                enabled = tripTitle.isNotBlank()
            ) {
                Text("Create Trip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
