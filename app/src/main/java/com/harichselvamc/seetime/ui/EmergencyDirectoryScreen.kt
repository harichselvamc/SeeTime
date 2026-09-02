package com.harichselvamc.seetime.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.harichselvamc.seetime.data.EmergencyContactInfo
import com.harichselvamc.seetime.data.EmergencyDirectoryRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyDirectoryScreen(
    viewModel: TimeViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { EmergencyDirectoryRepository(context) }
    val allCountries by repository.countries.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }
    var callingDialogNumber by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showSosStrobe by remember { mutableStateOf(false) }

    val activeTimezone = viewModel?.state?.collectAsState()?.value?.pairs?.firstOrNull()?.toZone ?: "Asia/Tokyo"
    val recommendedCountry = remember(activeTimezone) {
        EmergencyDirectoryRepository.findCountryForTimezone(activeTimezone) ?: allCountries.firstOrNull { it.countryName == "Japan" }
    }

    val filteredCountries = remember(searchQuery, selectedRegion, allCountries) {
        repository.searchCountries(searchQuery, if (selectedRegion == "All") null else selectedRegion)
    }

    val regions = listOf("All", "Americas", "Europe", "Asia", "Oceania", "Middle East", "Africa")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Emergency,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Global Emergency Directory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSosStrobe = true }) {
                        Icon(
                            imageVector = Icons.Filled.FlashOn,
                            contentDescription = "SOS Strobe",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Active Destination Contextual Card ─────────────────────────
            if (recommendedCountry != null) {
                item {
                    ActiveDestinationEmergencyCard(
                        country = recommendedCountry,
                        activeZone = activeTimezone,
                        onCall = { service, num -> callingDialogNumber = Pair(service, num) },
                        onSendBeacon = {
                            val timeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            val smsText = "EMERGENCY SOS: I require immediate assistance. Current timezone: $activeTimezone (${recommendedCountry.countryName}). Local emergency number: ${recommendedCountry.universalEmergency}. Sent via SeeTime at $timeStr."
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).apply {
                                putExtra("sms_body", smsText)
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clip.setPrimaryClip(ClipData.newPlainText("SOS Beacon", smsText))
                                Toast.makeText(context, "SOS message copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // ── Search & Region Filter Bar ─────────────────────────────────
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search 195+ countries, codes (+81), or 911...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(regions) { region ->
                        FilterChip(
                            selected = selectedRegion == region,
                            onClick = { selectedRegion = region },
                            label = { Text(region) }
                        )
                    }
                }
            }

            // ── Country Cards Directory ────────────────────────────────────
            if (filteredCountries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No country found matching \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredCountries, key = { it.countryIsoCode }) { country ->
                    CountryEmergencyCard(
                        country = country,
                        onCall = { service, num -> callingDialogNumber = Pair(service, num) },
                        onCopyDialingCode = {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clip.setPrimaryClip(ClipData.newPlainText("Calling Code", country.callingCode))
                            Toast.makeText(context, "${country.callingCode} copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ── Call Confirmation Dialog ───────────────────────────────────────────
    if (callingDialogNumber != null) {
        val (service, number) = callingDialogNumber!!
        AlertDialog(
            onDismissRequest = { callingDialogNumber = null },
            icon = { Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Call $service ($number)?") },
            text = { Text("This will open your phone dialer to call emergency dispatch at $number.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.launchDialer(context, number)
                        callingDialogNumber = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Call $number")
                }
            },
            dismissButton = {
                TextButton(onClick = { callingDialogNumber = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Full-Screen SOS Strobe Screen ──────────────────────────────────────
    if (showSosStrobe) {
        SosStrobeDialog(onDismiss = { showSosStrobe = false })
    }
}

// ── Active Destination Emergency Card ──────────────────────────────────

@Composable
private fun ActiveDestinationEmergencyCard(
    country: EmergencyContactInfo,
    activeZone: String,
    onCall: (String, String) -> Unit,
    onSendBeacon: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
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
                    Text(text = country.flagEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ACTIVE DESTINATION HUD",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = country.countryName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(
                        text = "Dialing: ${country.callingCode}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Call Grid (Universal, Police, Ambulance, Fire)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmergencyButton(
                    label = "Universal",
                    number = country.universalEmergency,
                    icon = Icons.Filled.Emergency,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("Universal Emergency", country.universalEmergency) }
                )
                EmergencyButton(
                    label = "Police",
                    number = country.police,
                    icon = Icons.Outlined.LocalPolice,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("Police", country.police) }
                )
                EmergencyButton(
                    label = "Medical",
                    number = country.ambulance,
                    icon = Icons.Outlined.LocalHospital,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("Medical Ambulance", country.ambulance) }
                )
                EmergencyButton(
                    label = "Fire",
                    number = country.fire,
                    icon = Icons.Outlined.LocalFireDepartment,
                    color = Color(0xFFD84315),
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("Fire Dept", country.fire) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SMS Beacon Button
            Button(
                onClick = onSendBeacon,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Sms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Broadcast Emergency SMS Beacon (with Timezone & GPS)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Country Emergency Card ─────────────────────────────────────────────

@Composable
private fun CountryEmergencyCard(
    country: EmergencyContactInfo,
    onCall: (String, String) -> Unit,
    onCopyDialingCode: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = country.flagEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = country.countryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${country.region} · ISO: ${country.countryIsoCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.clickable(onClick = onCopyDialingCode)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = country.callingCode,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy code",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CompactServiceChip(
                    label = "Police",
                    number = country.police,
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("${country.countryName} Police", country.police) }
                )
                CompactServiceChip(
                    label = "Ambulance",
                    number = country.ambulance,
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("${country.countryName} Medical", country.ambulance) }
                )
                CompactServiceChip(
                    label = "Fire",
                    number = country.fire,
                    modifier = Modifier.weight(1f),
                    onClick = { onCall("${country.countryName} Fire", country.fire) }
                )
                if (country.touristPolice != null) {
                    CompactServiceChip(
                        label = "Tourist",
                        number = country.touristPolice.substringBefore(' '),
                        modifier = Modifier.weight(1f),
                        onClick = { onCall("${country.countryName} Tourist Police", country.touristPolice) }
                    )
                }
            }

            if (country.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = country.notes,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Reusable Buttons & Chips ───────────────────────────────────────────

@Composable
private fun EmergencyButton(
    label: String,
    number: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = color)
            Text(text = number.substringBefore(' '), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun CompactServiceChip(
    label: String,
    number: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
            Text(text = number, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

// ── SOS Strobe Screen Dialog ───────────────────────────────────────────

@Composable
private fun SosStrobeDialog(
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_strobe")
    val strobeColor by infiniteTransition.animateColor(
        initialValue = Color.White,
        targetValue = Color.Red,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_color"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(strobeColor)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SOS",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "••• ─── ••• (Morse Code)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "Tap anywhere to exit SOS strobe",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
