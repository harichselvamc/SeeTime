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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.data.OfflineCurrencyRepository
import com.harichselvamc.seetime.data.local.CurrencyRateEntity
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { OfflineCurrencyRepository.getInstance(context) }
    val rates by repository.ratesState.collectAsState()
    val scope = rememberCoroutineScope()

    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("EUR") }
    var inputAmountText by remember { mutableStateOf("100") }
    var convertedResult by remember { mutableDoubleStateOf(92.5) }

    // Tip Calculator State
    var showTipCalculator by remember { mutableStateOf(false) }
    var tipPercent by remember { mutableFloatStateOf(15.0f) }
    var splitCount by remember { mutableIntStateOf(1) }

    // Currency Picker Sheet State
    var isPickerOpen by remember { mutableStateOf(false) }
    var pickerTargetMode by remember { mutableStateOf("FROM") } // "FROM" or "TO"
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    // Trigger Conversion
    fun triggerConversion() {
        val amount = inputAmountText.toDoubleOrNull() ?: 0.0
        scope.launch {
            convertedResult = repository.convert(amount, fromCurrency, toCurrency)
        }
    }

    LaunchedEffect(fromCurrency, toCurrency, inputAmountText, rates) {
        triggerConversion()
    }

    val fromEntity = rates.firstOrNull { it.currencyCode == fromCurrency }
    val toEntity = rates.firstOrNull { it.currencyCode == toCurrency }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CurrencyExchange,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Offline Currency Converter",
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
                                text = "Offline Ready",
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
            // Main Converter Interactive Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // From Currency Row
                        CurrencySelectRow(
                            label = "YOU PAY / FROM",
                            currencyCode = fromCurrency,
                            symbol = fromEntity?.symbol ?: "$",
                            countryHint = fromEntity?.countryOrZoneHint ?: "Base",
                            amountText = inputAmountText,
                            isEditable = true,
                            onAmountChange = {
                                inputAmountText = it
                            },
                            onCurrencyClick = {
                                pickerTargetMode = "FROM"
                                isPickerOpen = true
                            }
                        )

                        // Swap Button Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.clickable {
                                    val temp = fromCurrency
                                    fromCurrency = toCurrency
                                    toCurrency = temp
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapVert,
                                    contentDescription = "Swap Currencies",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(24.dp)
                                )
                            }
                        }

                        // To Currency Row (Result)
                        val formattedResult = String.format(Locale.getDefault(), "%,.2f", convertedResult)
                        CurrencySelectRow(
                            label = "YOU RECEIVE / TO",
                            currencyCode = toCurrency,
                            symbol = toEntity?.symbol ?: "€",
                            countryHint = toEntity?.countryOrZoneHint ?: "Target",
                            amountText = formattedResult,
                            isEditable = false,
                            onAmountChange = {},
                            onCurrencyClick = {
                                pickerTargetMode = "TO"
                                isPickerOpen = true
                            }
                        )
                    }
                }
            }

            // Quick Preset Currencies Chips
            item {
                Text(
                    text = "FREQUENT TRAVEL CURRENCIES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USD", "EUR", "GBP", "JPY", "INR").forEach { code ->
                        FilterChip(
                            selected = toCurrency == code,
                            onClick = { toCurrency = code },
                            label = { Text(code, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                selectedLabelColor = Cobalt60
                            )
                        )
                    }
                }
            }

            // Tip & Bill Splitter Toggle Tool
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
                                .clickable { showTipCalculator = !showTipCalculator },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Calculate,
                                    contentDescription = null,
                                    tint = Cobalt60,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Travel Tip & Bill Splitter",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Calculate local gratuity and split among travel companions",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showTipCalculator) Icons.Filled.ArrowDownward else Icons.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = Cobalt60
                            )
                        }

                        AnimatedVisibility(visible = showTipCalculator) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val amount = inputAmountText.toDoubleOrNull() ?: 0.0
                                val tipVal = repository.calculateTip(amount, tipPercent.toDouble())
                                val totalVal = amount + tipVal
                                val perPerson = repository.calculateSplit(totalVal, splitCount)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tip Rate: ${tipPercent.toInt()}%", fontWeight = FontWeight.SemiBold)
                                    Text("Tip: ${fromEntity?.symbol ?: ""}${String.format(Locale.getDefault(), "%.2f", tipVal)}", color = Amber60, fontWeight = FontWeight.Bold)
                                }

                                Slider(
                                    value = tipPercent,
                                    onValueChange = { tipPercent = it },
                                    valueRange = 0f..30f,
                                    steps = 5,
                                    colors = SliderDefaults.colors(thumbColor = Amber60, activeTrackColor = Amber60)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Groups, contentDescription = null, tint = Cobalt60, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Split: $splitCount people", fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.clickable { if (splitCount > 1) splitCount-- }
                                        ) {
                                            Text("-", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.clickable { splitCount++ }
                                        ) {
                                            Text("+", fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                        }
                                    }
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total per Person:", fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "${fromEntity?.symbol ?: ""}${String.format(Locale.getDefault(), "%.2f", perPerson)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SuccessGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Currency Bottom Sheet Picker
    if (isPickerOpen) {
        ModalBottomSheet(
            onDismissRequest = { isPickerOpen = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Select Currency (${if (pickerTargetMode == "FROM") "Source" else "Destination"})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search country, currency, or code...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                val filteredRates = rates.filter {
                    searchQuery.isBlank() ||
                    it.currencyCode.contains(searchQuery, ignoreCase = true) ||
                    it.currencyName.contains(searchQuery, ignoreCase = true) ||
                    it.countryOrZoneHint.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRates) { entity ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (pickerTargetMode == "FROM") {
                                        fromCurrency = entity.currencyCode
                                    } else {
                                        toCurrency = entity.currencyCode
                                    }
                                    isPickerOpen = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${entity.currencyCode} · ${entity.currencyName}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = entity.countryOrZoneHint,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = entity.symbol,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Cobalt60
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencySelectRow(
    label: String,
    currencyCode: String,
    symbol: String,
    countryHint: String,
    amountText: String,
    isEditable: Boolean,
    onAmountChange: (String) -> Unit,
    onCurrencyClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Currency Picker Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Cobalt60.copy(alpha = 0.12f),
                modifier = Modifier.clickable(onClick = onCurrencyClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currencyCode ($symbol)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Cobalt60
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = Cobalt60,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Input / Output Display
            if (isEditable) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.width(170.dp)
                )
            } else {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
