package com.harichselvamc.seetime.ui

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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.HolidayCalculationEngine
import com.harichselvamc.seetime.util.HolidayCategory
import com.harichselvamc.seetime.util.PublicHoliday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayCalendarScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()
    val today = remember { LocalDate.now() }

    var selectedCountryIndex by remember { mutableIntStateOf(0) }
    val country = HolidayCalculationEngine.SUPPORTED_COUNTRIES[selectedCountryIndex % HolidayCalculationEngine.SUPPORTED_COUNTRIES.size]

    val holidays = remember(country, today.year) {
        HolidayCalculationEngine.getHolidaysForCountry(country.code, today.year)
    }

    val nextHoliday = remember(country, today) {
        HolidayCalculationEngine.getNextUpcomingHoliday(country.code, today)
    }

    // Meeting Conflict Checker Demo State
    var showConflictChecker by remember { mutableStateOf(false) }
    var conflictCheckDaysOffset by remember { mutableIntStateOf(0) }
    val conflictCheckDate = remember(today, conflictCheckDaysOffset) { today.plusDays(conflictCheckDaysOffset.toLong()) }
    val conflictHolidays = remember(conflictCheckDate) {
        HolidayCalculationEngine.checkMeetingHolidayConflicts(conflictCheckDate, listOf("US", "JP", "GB", "IN"))
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Celebration,
                            contentDescription = null,
                            tint = Amber60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Public Holiday Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
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
            // Country Selector Chips
            item {
                Text(
                    text = "SELECT COUNTRY / REGION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HolidayCalculationEngine.SUPPORTED_COUNTRIES.indices.toList()) { idx ->
                        val itemCountry = HolidayCalculationEngine.SUPPORTED_COUNTRIES[idx]
                        val isSelected = selectedCountryIndex == idx
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCountryIndex = idx },
                            label = { Text("${itemCountry.emoji} ${itemCountry.name}", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Amber60.copy(alpha = 0.15f),
                                selectedLabelColor = Amber60
                            )
                        )
                    }
                }
            }

            // Next Upcoming Holiday Hero Card
            nextHoliday?.let { holiday ->
                item {
                    val daysUntil = ChronoUnit.DAYS.between(today, holiday.date).toInt()
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Amber60.copy(alpha = 0.12f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Amber60
                                ) {
                                    Text(
                                        text = "UPCOMING STATUTORY HOLIDAY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = if (daysUntil == 0) "TODAY" else "in $daysUntil days",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Amber60
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = holiday.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = holiday.date.format(dateFormatter),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Amber60
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = holiday.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.BusinessCenter,
                                    contentDescription = null,
                                    tint = if (holiday.isOffWork) Color(0xFFEF4444) else SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (holiday.isOffWork) "Banks & Corporate Offices Closed" else "Regular Working Hours",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (holiday.isOffWork) Color(0xFFEF4444) else SuccessGreen
                                )
                            }
                        }
                    }
                }
            }

            // Cross-Timezone Meeting Conflict Checker Tool Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showConflictChecker = !showConflictChecker },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.EventBusy,
                                    contentDescription = null,
                                    tint = Cobalt60,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Meeting Holiday Conflict Detector",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Check if scheduled meetings coincide with remote partner holidays",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = showConflictChecker) {
                            Column(
                                modifier = Modifier.padding(top = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Simulated Meeting Date: ${conflictCheckDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(0, 1, 7, 14, 30).forEach { offset ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (conflictCheckDaysOffset == offset) Cobalt60 else MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.clickable { conflictCheckDaysOffset = offset }
                                        ) {
                                            Text(
                                                text = if (offset == 0) "Today" else "+${offset}d",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (conflictCheckDaysOffset == offset) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                if (conflictHolidays.isNotEmpty()) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.12f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Holiday Conflict Detected!", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 13.sp)
                                            }
                                            conflictHolidays.forEach { conflict ->
                                                Text(
                                                    text = "• ${conflict.countryName} (${conflict.countryCode}): ${conflict.name} (Offices Closed)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("No holiday conflicts across US, JP, UK & IN on this date.", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Complete Annual Holiday Calendar List
            item {
                Text(
                    text = "ALL ${country.name.uppercase()} PUBLIC HOLIDAYS (${today.year})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(holidays) { holiday ->
                HolidayRowCard(holiday = holiday)
            }
        }
    }
}

@Composable
private fun HolidayRowCard(holiday: PublicHoliday) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Amber60.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = Amber60,
                        modifier = Modifier.size(22.dp)
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
                        text = holiday.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (holiday.isOffWork) Color(0xFFEF4444).copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (holiday.isOffWork) "CLOSED" else "OPEN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = if (holiday.isOffWork) Color(0xFFEF4444) else SuccessGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${holiday.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))} · ${holiday.category.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Cobalt60
                )

                Text(
                    text = holiday.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
