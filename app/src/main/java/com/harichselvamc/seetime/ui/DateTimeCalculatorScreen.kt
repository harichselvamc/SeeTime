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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.Purple60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.DateTimeCalculatorEngine
import com.harichselvamc.seetime.util.HolidayCalculationEngine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeCalculatorScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) } // 0: Date Difference, 1: Business Days, 2: Add/Subtract

    // State for Date Difference
    var startDateDiff by remember { mutableStateOf(LocalDate.now()) }
    var endDateDiff by remember { mutableStateOf(LocalDate.now().plusMonths(3).plusDays(15)) }

    // State for Business Days
    var startBusiness by remember { mutableStateOf(LocalDate.now()) }
    var endBusiness by remember { mutableStateOf(LocalDate.now().plusMonths(1)) }
    var selectedCountryCode by remember { mutableStateOf("US") }
    var includeEndDay by remember { mutableStateOf(true) }

    // State for Add / Subtract
    var startAddSub by remember { mutableStateOf(LocalDate.now()) }
    var addYears by remember { mutableIntStateOf(0) }
    var addMonths by remember { mutableIntStateOf(0) }
    var addWeeks by remember { mutableIntStateOf(0) }
    var addDays by remember { mutableIntStateOf(30) }
    var isSubtractMode by remember { mutableStateOf(false) }
    var isBusinessDaysOnly by remember { mutableStateOf(false) }

    // DatePicker dialog helper
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf("START_DIFF") }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DateTime & Workdays Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
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
                                text = "Offline Math",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = SuccessGreen
                            )
                        }
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
            // Segmented Tab Switcher Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Date Difference" to Icons.Filled.DateRange,
                        "Working Days" to Icons.Filled.BusinessCenter,
                        "Add / Subtract" to Icons.Filled.Schedule
                    ).forEachIndexed { index, (title, icon) ->
                        FilterChip(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(title, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                selectedLabelColor = Cobalt60
                            )
                        )
                    }
                }
            }

            // TAB 0: DATE DIFFERENCE
            if (activeTab == 0) {
                item {
                    val diffResult = DateTimeCalculatorEngine.calculateDifference(startDateDiff, endDateDiff)
                    DateDifferenceSection(
                        startDate = startDateDiff,
                        endDate = endDateDiff,
                        result = diffResult,
                        onOpenStartDatePicker = {
                            datePickerTarget = "START_DIFF"
                            isDatePickerOpen = true
                        },
                        onOpenEndDatePicker = {
                            datePickerTarget = "END_DIFF"
                            isDatePickerOpen = true
                        },
                        onSwapDates = {
                            val temp = startDateDiff
                            startDateDiff = endDateDiff
                            endDateDiff = temp
                        },
                        onCopyResult = { copyToClipboard("Date Difference", diffResult.formattedDetailedString) }
                    )
                }
            }

            // TAB 1: WORKING BUSINESS DAYS
            if (activeTab == 1) {
                item {
                    val businessResult = DateTimeCalculatorEngine.calculateBusinessDays(
                        start = startBusiness,
                        end = endBusiness,
                        countryCode = selectedCountryCode,
                        includeEndDay = includeEndDay
                    )
                    BusinessDaysSection(
                        startDate = startBusiness,
                        endDate = endBusiness,
                        selectedCountryCode = selectedCountryCode,
                        includeEndDay = includeEndDay,
                        result = businessResult,
                        onOpenStartDatePicker = {
                            datePickerTarget = "START_BUS"
                            isDatePickerOpen = true
                        },
                        onOpenEndDatePicker = {
                            datePickerTarget = "END_BUS"
                            isDatePickerOpen = true
                        },
                        onCountryChange = { selectedCountryCode = it },
                        onToggleIncludeEndDay = { includeEndDay = it },
                        onCopyResult = {
                            copyToClipboard(
                                "Business Days",
                                "${businessResult.businessDaysCount} working days (${businessResult.workingHoursEstimate}h) in $selectedCountryCode"
                            )
                        }
                    )
                }
            }

            // TAB 2: ADD / SUBTRACT TIME
            if (activeTab == 2) {
                item {
                    val addSubResult = if (isBusinessDaysOnly) {
                        val daysDelta = if (isSubtractMode) -addDays else addDays
                        DateTimeCalculatorEngine.addBusinessDays(startAddSub, daysDelta, selectedCountryCode)
                    } else {
                        val factor = if (isSubtractMode) -1 else 1
                        DateTimeCalculatorEngine.addTime(
                            start = startAddSub,
                            years = addYears * factor,
                            months = addMonths * factor,
                            weeks = addWeeks * factor,
                            days = addDays * factor,
                            countryCode = selectedCountryCode
                        )
                    }

                    AddSubtractTimeSection(
                        startDate = startAddSub,
                        years = addYears,
                        months = addMonths,
                        weeks = addWeeks,
                        days = addDays,
                        isSubtractMode = isSubtractMode,
                        isBusinessDaysOnly = isBusinessDaysOnly,
                        selectedCountryCode = selectedCountryCode,
                        result = addSubResult,
                        onOpenStartDatePicker = {
                            datePickerTarget = "START_ADDSUB"
                            isDatePickerOpen = true
                        },
                        onYearsChange = { addYears = it },
                        onMonthsChange = { addMonths = it },
                        onWeeksChange = { addWeeks = it },
                        onDaysChange = { addDays = it },
                        onToggleSubtractMode = { isSubtractMode = it },
                        onToggleBusinessDaysOnly = { isBusinessDaysOnly = it },
                        onCountryChange = { selectedCountryCode = it },
                        onCopyResult = {
                            val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.getDefault())
                            copyToClipboard("Calculated Date", addSubResult.calculatedDate.format(formatter))
                        }
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (isDatePickerOpen) {
        val initialDate = when (datePickerTarget) {
            "START_DIFF" -> startDateDiff
            "END_DIFF" -> endDateDiff
            "START_BUS" -> startBusiness
            "END_BUS" -> endBusiness
            else -> startAddSub
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { isDatePickerOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val selectedLocalDate = Instant.ofEpochMilli(selectedMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            when (datePickerTarget) {
                                "START_DIFF" -> startDateDiff = selectedLocalDate
                                "END_DIFF" -> endDateDiff = selectedLocalDate
                                "START_BUS" -> startBusiness = selectedLocalDate
                                "END_BUS" -> endBusiness = selectedLocalDate
                                "START_ADDSUB" -> startAddSub = selectedLocalDate
                            }
                        }
                        isDatePickerOpen = false
                    }
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerOpen = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// ── Section 0: Date Difference ────────────────────────────────────────

@Composable
private fun DateDifferenceSection(
    startDate: LocalDate,
    endDate: LocalDate,
    result: com.harichselvamc.seetime.util.DateDifferenceResult,
    onOpenStartDatePicker: () -> Unit,
    onOpenEndDatePicker: () -> Unit,
    onSwapDates: () -> Unit,
    onCopyResult: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy (EEE)", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Date Input Selector Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DateSelectionRow(
                    label = "START DATE",
                    dateText = startDate.format(formatter),
                    onDateClick = onOpenStartDatePicker
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Cobalt60.copy(alpha = 0.12f),
                        modifier = Modifier.clickable(onClick = onSwapDates)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Swap Dates",
                            tint = Cobalt60,
                            modifier = Modifier.padding(8.dp).size(22.dp)
                        )
                    }
                }

                DateSelectionRow(
                    label = "END DATE",
                    dateText = endDate.format(formatter),
                    onDateClick = onOpenEndDatePicker
                )
            }
        }

        // Primary Hero Result Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Cobalt60.copy(alpha = 0.10f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL DURATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Cobalt60
                    )
                    IconButton(onClick = onCopyResult, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy Result",
                            tint = Cobalt60,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "${result.totalDays} Days",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = result.formattedDetailedString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Cobalt60
                )
            }
        }

        // Comprehensive Breakdown Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "UNIT BREAKDOWN",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatUnitCard(
                    title = "Weeks & Days",
                    value = "${result.totalWeeks}w ${result.remainingDaysOfWeek}d",
                    modifier = Modifier.weight(1f)
                )
                StatUnitCard(
                    title = "% of Year",
                    value = String.format(Locale.getDefault(), "%.1f%%", result.percentageOfYear),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatUnitCard(
                    title = "Total Hours",
                    value = String.format(Locale.getDefault(), "%,d h", result.totalHours),
                    modifier = Modifier.weight(1f)
                )
                StatUnitCard(
                    title = "Total Minutes",
                    value = String.format(Locale.getDefault(), "%,d min", result.totalMinutes),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── Section 1: Business Working Days ──────────────────────────────────

@Composable
private fun BusinessDaysSection(
    startDate: LocalDate,
    endDate: LocalDate,
    selectedCountryCode: String,
    includeEndDay: Boolean,
    result: com.harichselvamc.seetime.util.BusinessDaysResult,
    onOpenStartDatePicker: () -> Unit,
    onOpenEndDatePicker: () -> Unit,
    onCountryChange: (String) -> Unit,
    onToggleIncludeEndDay: (Boolean) -> Unit,
    onCopyResult: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Date Inputs & Options Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        DateSelectionRow("FROM", startDate.format(formatter), onOpenStartDatePicker)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        DateSelectionRow("TO", endDate.format(formatter), onOpenEndDatePicker)
                    }
                }

                // Country Selector for Public Holidays
                Text(
                    text = "Statutory Public Holidays Region:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HolidayCalculationEngine.SUPPORTED_COUNTRIES) { country ->
                        FilterChip(
                            selected = selectedCountryCode == country.code,
                            onClick = { onCountryChange(country.code) },
                            label = { Text("${country.emoji} ${country.code}") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Include End Date in Count", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = includeEndDay, onCheckedChange = onToggleIncludeEndDay)
                }
            }
        }

        // Business Days Hero Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NET WORKING BUSINESS DAYS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = SuccessGreen
                    )
                    IconButton(onClick = onCopyResult, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = SuccessGreen, modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = "${result.businessDaysCount} Workdays",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "≈ ${result.workingHoursEstimate} billable business hours (8h/day)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
            }
        }

        // Days Distribution Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatUnitCard("Calendar Days", "${result.totalCalendarDays} days", modifier = Modifier.weight(1f))
            StatUnitCard("Weekend Days", "${result.weekendDaysCount} Sat/Sun", modifier = Modifier.weight(1f))
            StatUnitCard("Public Holidays", "${result.publicHolidaysCount} off", modifier = Modifier.weight(1f))
        }

        // Public Holidays List Encountered
        if (result.holidaysList.isNotEmpty()) {
            Text(
                text = "HOLIDAYS EXCLUDED (${result.holidaysList.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.primary
            )

            result.holidaysList.forEach { holiday ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(holiday.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(holiday.date.format(formatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = Amber60.copy(alpha = 0.15f)) {
                            Text("OFF WORK", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Amber60, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Section 2: Add / Subtract Time ────────────────────────────────────

@Composable
private fun AddSubtractTimeSection(
    startDate: LocalDate,
    years: Int,
    months: Int,
    weeks: Int,
    days: Int,
    isSubtractMode: Boolean,
    isBusinessDaysOnly: Boolean,
    selectedCountryCode: String,
    result: com.harichselvamc.seetime.util.AddSubtractDateResult,
    onOpenStartDatePicker: () -> Unit,
    onYearsChange: (Int) -> Unit,
    onMonthsChange: (Int) -> Unit,
    onWeeksChange: (Int) -> Unit,
    onDaysChange: (Int) -> Unit,
    onToggleSubtractMode: (Boolean) -> Unit,
    onToggleBusinessDaysOnly: (Boolean) -> Unit,
    onCountryChange: (String) -> Unit,
    onCopyResult: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault()) }
    val resultFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Controls Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                DateSelectionRow("BASE DATE", startDate.format(formatter), onOpenStartDatePicker)

                // Operation Switcher (Add vs Subtract)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Operation Mode", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = !isSubtractMode,
                            onClick = { onToggleSubtractMode(false) },
                            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Add (+)") }
                        )
                        FilterChip(
                            selected = isSubtractMode,
                            onClick = { onToggleSubtractMode(true) },
                            leadingIcon = { Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text("Subtract (-)") }
                        )
                    }
                }

                // Business days only toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Business Days Only", fontWeight = FontWeight.SemiBold)
                        Text("Skips Sat/Sun & Public Holidays", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                    Switch(checked = isBusinessDaysOnly, onCheckedChange = onToggleBusinessDaysOnly)
                }

                // Numeric Inputs
                if (isBusinessDaysOnly) {
                    NumericStepperField(
                        label = "Business Days",
                        value = days,
                        onValueChange = onDaysChange
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericStepperField("Years", years, onYearsChange, modifier = Modifier.weight(1f))
                        NumericStepperField("Months", months, onMonthsChange, modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NumericStepperField("Weeks", weeks, onWeeksChange, modifier = Modifier.weight(1f))
                        NumericStepperField("Days", days, onDaysChange, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Result Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Cobalt60.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSubtractMode) "CALCULATED PAST DATE" else "CALCULATED FUTURE DATE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Cobalt60
                    )
                    IconButton(onClick = onCopyResult, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", tint = Cobalt60, modifier = Modifier.size(18.dp))
                    }
                }

                Text(
                    text = result.calculatedDate.format(resultFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (result.isWeekend) Amber60.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (result.isWeekend) "WEEKEND (${result.dayOfWeek})" else "WEEKDAY (${result.dayOfWeek})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (result.isWeekend) Amber60 else SuccessGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (result.isHoliday) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "HOLIDAY: ${result.holidayName ?: "Official"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Reusable Micro-Components ─────────────────────────────────────────

@Composable
private fun DateSelectionRow(
    label: String,
    dateText: String,
    onDateClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onDateClick)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dateText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatUnitCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun NumericStepperField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { if (value > 0) onValueChange(value - 1) }
            ) {
                Text("-", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
            Text("$value", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onValueChange(value + 1) }
            ) {
                Text("+", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
            }
        }
    }
}
