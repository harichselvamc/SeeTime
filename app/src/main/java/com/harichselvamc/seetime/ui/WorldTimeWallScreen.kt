package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.CityLiveState
import com.harichselvamc.seetime.util.MarketStatus
import com.harichselvamc.seetime.util.WorldGlanceEngine
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldTimeWallScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val engine = remember { WorldGlanceEngine.getInstance(context) }
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var searchQuery by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("All") }
    var showPinnedOnly by remember { mutableStateOf(false) }
    var selectedCityForDetail by remember { mutableStateOf<CityLiveState?>(null) }

    // Live clock ticker
    var currentInstant by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentInstant = Instant.now()
            delay(1000L)
        }
    }

    val pinnedCityIds by engine.pinnedCityIds.collectAsState()

    val filteredCityStates = remember(searchQuery, selectedRegion, showPinnedOnly, currentInstant, pinnedCityIds) {
        engine.filterCities(
            query = searchQuery,
            regionFilter = selectedRegion,
            pinnedOnly = showPinnedOnly,
            instant = currentInstant,
            userZoneId = ZoneId.systemDefault()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.GridView,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "World Time Glance Wall",
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
                                text = "16 Global Hubs",
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isTablet) 4 else 2),
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Region Filter Bar (Full width span)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search city, country, or exchange (NYSE, LSE, TSE)...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Region Filter Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val regions = listOf("All", "Americas", "Europe", "Asia-Pacific", "Middle East & Africa")
                        items(regions) { region ->
                            FilterChip(
                                selected = selectedRegion == region && !showPinnedOnly,
                                onClick = {
                                    selectedRegion = region
                                    showPinnedOnly = false
                                },
                                label = { Text(region) }
                            )
                        }

                        item {
                            FilterChip(
                                selected = showPinnedOnly,
                                onClick = { showPinnedOnly = !showPinnedOnly },
                                leadingIcon = {
                                    Icon(Icons.Filled.PushPin, contentDescription = null, modifier = Modifier.size(14.dp))
                                },
                                label = { Text("Pinned (${pinnedCityIds.size})") }
                            )
                        }
                    }
                }
            }

            // City Glance Tiles
            items(filteredCityStates, key = { it.city.id }) { cityState ->
                WorldCityGlanceCard(
                    cityState = cityState,
                    onTogglePin = { engine.togglePin(cityState.city.id) },
                    onClick = { selectedCityForDetail = cityState }
                )
            }
        }
    }

    // Detail Dialog
    selectedCityForDetail?.let { state ->
        CityDetailDialog(
            cityState = state,
            onDismiss = { selectedCityForDetail = null },
            onTogglePin = {
                engine.togglePin(state.city.id)
                selectedCityForDetail = null
            }
        )
    }
}

// ── Composable City Card ──────────────────────────────────────────────

@Composable
private fun WorldCityGlanceCard(
    cityState: CityLiveState,
    onTogglePin: () -> Unit,
    onClick: () -> Unit
) {
    val isDaytime = cityState.isDaytime
    val marketStatus = cityState.marketStatus
    val isPinned = cityState.city.isPinned

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPinned) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPinned) 2.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Flag + City + Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cityState.city.flagEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cityState.city.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin City",
                        tint = if (isPinned) Cobalt60 else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Offset & Sun/Moon Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Cobalt60.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = cityState.formattedOffset,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = Cobalt60,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDaytime) Icons.Filled.WbSunny else Icons.Filled.DarkMode,
                        contentDescription = null,
                        tint = if (isDaytime) Amber60 else Color(0xFF7E57C2),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDaytime) "Day" else "Night",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Analog Clock & Digital Time Center
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mini Analog Clock Canvas
                MiniAnalogClock(
                    hourAngle = cityState.hourAngle,
                    minuteAngle = cityState.minuteAngle,
                    secondAngle = cityState.secondAngle,
                    isDaytime = isDaytime,
                    modifier = Modifier.size(46.dp)
                )

                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = cityState.formattedTime24,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = ":${cityState.formattedSeconds}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                color = Cobalt60,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                    Text(
                        text = cityState.formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Market Trading Status LED Strip
            if (cityState.city.marketInfo != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (marketStatus) {
                        MarketStatus.OPEN -> SuccessGreen.copy(alpha = 0.15f)
                        MarketStatus.CLOSING_SOON -> Amber60.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (marketStatus) {
                                            MarketStatus.OPEN -> SuccessGreen
                                            MarketStatus.CLOSING_SOON -> Amber60
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cityState.city.marketInfo.exchangeCode,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = when (marketStatus) {
                                MarketStatus.OPEN -> "OPEN"
                                MarketStatus.CLOSING_SOON -> "CLOSING"
                                MarketStatus.WEEKEND -> "WEEKEND"
                                else -> "CLOSED"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = when (marketStatus) {
                                MarketStatus.OPEN -> SuccessGreen
                                MarketStatus.CLOSING_SOON -> Amber60
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniAnalogClock(
    hourAngle: Float,
    minuteAngle: Float,
    secondAngle: Float,
    isDaytime: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Clock Face Dial
        drawCircle(
            color = if (isDaytime) Color(0xFFF3F4F6) else Color(0xFF1E293B),
            radius = radius
        )
        drawCircle(
            color = if (isDaytime) Color(0xFFCBD5E1) else Color(0xFF334155),
            radius = radius,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 12-hour tick marks
        for (i in 0 until 12) {
            val angleRad = (i * 30 - 90) * (Math.PI / 180.0)
            val outer = Offset(
                (center.x + (radius - 2.dp.toPx()) * cos(angleRad)).toFloat(),
                (center.y + (radius - 2.dp.toPx()) * sin(angleRad)).toFloat()
            )
            val inner = Offset(
                (center.x + (radius - (if (i % 3 == 0) 5.dp.toPx() else 3.dp.toPx())) * cos(angleRad)).toFloat(),
                (center.y + (radius - (if (i % 3 == 0) 5.dp.toPx() else 3.dp.toPx())) * sin(angleRad)).toFloat()
            )
            drawLine(
                color = if (isDaytime) Color(0xFF64748B) else Color(0xFF94A3B8),
                start = inner,
                end = outer,
                strokeWidth = if (i % 3 == 0) 1.5.dp.toPx() else 1.dp.toPx()
            )
        }

        // Hour Hand
        val hourRad = (hourAngle - 90) * (Math.PI / 180.0)
        val hourEnd = Offset(
            (center.x + radius * 0.50f * cos(hourRad)).toFloat(),
            (center.y + radius * 0.50f * sin(hourRad)).toFloat()
        )
        drawLine(
            color = if (isDaytime) Color(0xFF0F172A) else Color(0xFFFFFFFF),
            start = center,
            end = hourEnd,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minRad = (minuteAngle - 90) * (Math.PI / 180.0)
        val minEnd = Offset(
            (center.x + radius * 0.75f * cos(minRad)).toFloat(),
            (center.y + radius * 0.75f * sin(minRad)).toFloat()
        )
        drawLine(
            color = Cobalt60,
            start = center,
            end = minEnd,
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second Hand
        val secRad = (secondAngle - 90) * (Math.PI / 180.0)
        val secEnd = Offset(
            (center.x + radius * 0.85f * cos(secRad)).toFloat(),
            (center.y + radius * 0.85f * sin(secRad)).toFloat()
        )
        drawLine(
            color = Amber60,
            start = center,
            end = secEnd,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Pin Cap
        drawCircle(color = Amber60, radius = 2.5.dp.toPx())
    }
}

// ── Detail Modal Dialog ───────────────────────────────────────────────

@Composable
private fun CityDetailDialog(
    cityState: CityLiveState,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit
) {
    val city = cityState.city
    val market = city.marketInfo

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(city.flagEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(city.cityName, fontWeight = FontWeight.Bold)
                    Text(city.countryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Big Time HUD
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Cobalt60.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${cityState.formattedTime24}:${cityState.formattedSeconds}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Cobalt60
                        )
                        Text(
                            text = "${cityState.formattedTime12} ${cityState.amPm} · ${cityState.formattedDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Timezone: ${city.timezoneId} (${cityState.formattedOffset})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Financial Market Details
                if (market != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ShowChart, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(market.exchangeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "Exchange Code: ${market.exchangeCode}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Trading Hours: ${market.openLocalTime} – ${market.closeLocalTime} (Local Time)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Status: ${cityState.marketStatusDescription}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (cityState.marketStatus) {
                                    MarketStatus.OPEN -> SuccessGreen
                                    MarketStatus.CLOSING_SOON -> Amber60
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onTogglePin) {
                Text(if (city.isPinned) "Unpin City" else "Pin to Top")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
