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
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.EpochConverterEngine
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpochConverterScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    var liveEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Live ticking loop
    LaunchedEffect(Unit) {
        while (true) {
            liveEpochMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(100L)
        }
    }

    var inputText by remember { mutableStateOf((liveEpochMillis / 1000L).toString()) }
    var evaluatedEpochMillis by remember { mutableLongStateOf(liveEpochMillis) }

    // Re-evaluate on input change
    LaunchedEffect(inputText) {
        val parsed = EpochConverterEngine.parseInputToEpochMillis(inputText)
        if (parsed != null) {
            evaluatedEpochMillis = parsed
        }
    }

    val formats = remember(evaluatedEpochMillis) {
        EpochConverterEngine.convertEpochMillis(evaluatedEpochMillis)
    }

    val milestones = remember(liveEpochMillis) {
        EpochConverterEngine.calculateMilestoneCountdowns(liveEpochMillis / 1000L)
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AvTimer,
                            contentDescription = null,
                            tint = Cyan60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multi-Epoch Converter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Cyan60.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = "LIVE TICKER",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = Cyan60,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
            // Live Real-Time Unix Timestamp Counter Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT UNIX EPOCH TIMESTAMP",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Cyan60
                            )
                            IconButton(onClick = { copyToClipboard("Unix Timestamp", (liveEpochMillis / 1000L).toString()) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Cyan60, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Monospace Unix Seconds Display
                        Text(
                            text = (liveEpochMillis / 1000L).toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Milliseconds: $liveEpochMillis",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    val nowSec = (System.currentTimeMillis() / 1000L).toString()
                                    inputText = nowSec
                                    evaluatedEpochMillis = System.currentTimeMillis()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Cobalt60)
                            ) {
                                Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset to Now", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Input Timestamp Evaluator Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Enter Epoch Timestamp or Date",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Enter seconds, ms, 0xHex, 0bBin, or ISO date...") },
                            trailingIcon = {
                                IconButton(onClick = { inputText = "" }) {
                                    Icon(Icons.Filled.RestartAlt, contentDescription = "Clear")
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Auto-detects Unix Seconds, Milliseconds, Hexadecimal (0x...), Binary (0b...), or ISO (yyyy-MM-dd).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Formats Breakdown Section Title
            item {
                Text(
                    text = "ASTRONOMICAL & COMPUTATIONAL EPOCHS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Formats Grid Cards
            item {
                EpochFormatRowCard("UTC Gregorian Date", formats.gregorianUtc, Icons.Filled.DateRange, Cyan60) { copyToClipboard("UTC Date", formats.gregorianUtc) }
            }
            item {
                EpochFormatRowCard("Local Gregorian Date", formats.gregorianLocal, Icons.Filled.Schedule, Amber60) { copyToClipboard("Local Date", formats.gregorianLocal) }
            }
            item {
                EpochFormatRowCard("Unix Timestamp (Seconds)", formats.unixSeconds.toString(), Icons.Filled.Numbers, Cobalt60) { copyToClipboard("Unix Seconds", formats.unixSeconds.toString()) }
            }
            item {
                EpochFormatRowCard("Unix Milliseconds", formats.unixMilliseconds.toString(), Icons.Filled.Numbers, Cobalt60) { copyToClipboard("Unix Milliseconds", formats.unixMilliseconds.toString()) }
            }
            item {
                EpochFormatRowCard("Unix Hexadecimal (32/64-bit)", formats.unixHexadecimal, Icons.Filled.Code, SuccessGreen) { copyToClipboard("Unix Hex", formats.unixHexadecimal) }
            }
            item {
                EpochFormatRowCard("Julian Date (JDN)", String.format(Locale.getDefault(), "%.5f", formats.julianDate), Icons.Filled.History, Amber60) { copyToClipboard("Julian Date", formats.julianDate.toString()) }
            }
            item {
                EpochFormatRowCard("Modified Julian Date (MJD)", String.format(Locale.getDefault(), "%.5f", formats.modifiedJulianDate), Icons.Filled.History, Amber60) { copyToClipboard("MJD", formats.modifiedJulianDate.toString()) }
            }
            item {
                EpochFormatRowCard("GPS Epoch (Week / Sec)", "Week ${formats.gpsWeek}, Sec ${formats.gpsSecondOfWeek}", Icons.Filled.DataArray, Cyan60) { copyToClipboard("GPS Week", "${formats.gpsWeek}:${formats.gpsSecondOfWeek}") }
            }
            item {
                EpochFormatRowCard("Microsoft Excel Serial (1900)", String.format(Locale.getDefault(), "%.5f", formats.excelSerial), Icons.Filled.Numbers, SuccessGreen) { copyToClipboard("Excel Serial", formats.excelSerial.toString()) }
            }
            item {
                EpochFormatRowCard("ISO 8601 Ordinal & Week", "${formats.isoOrdinalDate} (${formats.isoWeekDate})", Icons.Filled.DateRange, Cobalt60) { copyToClipboard("ISO Ordinal", formats.isoOrdinalDate) }
            }

            // Year 2038 Bug (Y2K38) & Milestone Countdown Cards
            item {
                Text(
                    text = "EPOCH MILESTONES & OVERFLOW COUNTDOWN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(milestones) { milestone ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (milestone.title.contains("2038")) Color(0xFFEF4444).copy(alpha = 0.12f) else Cobalt60.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (milestone.title.contains("2038")) Color(0xFFEF4444) else Cobalt60
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (milestone.title.contains("2038")) Color(0xFFEF4444) else Cobalt60
                            ) {
                                Text(
                                    text = "${milestone.daysRemaining} days left",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Target: ${milestone.targetUtc} (${milestone.targetTimestampSeconds}s)",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = milestone.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpochFormatRowCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    onCopy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(onClick = onCopy) {
                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy $label", tint = accentColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}
