package com.harichselvamc.seetime.ui

import com.harichselvamc.seetime.data.TimeRepository
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.data.local.Activity
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.Purple60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.AnalyticsPeriod
import com.harichselvamc.seetime.util.CategoryAnalytics
import com.harichselvamc.seetime.util.TimeAnalyticsEngine
import com.harichselvamc.seetime.util.TimeAnalyticsReport
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeAnalyticsScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf(AnalyticsPeriod.TODAY) }
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    var showAddActivityDialog by remember { mutableStateOf(false) }

    var roomActivities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    LaunchedEffect(state.activities) {
        try {
            val dbActivities = TimeRepository.getInstance(context).database.dao().getActivities()
            roomActivities = dbActivities
        } catch (_: Exception) {}
    }

    val report = remember(roomActivities, selectedPeriod) {
        TimeAnalyticsEngine.generateReport(roomActivities, selectedPeriod)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = null,
                            tint = Cobalt60,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Time Analytics & Focus Charts",
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
                                text = "100% Offline",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddActivityDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log Activity")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnalyticsPeriod.values().forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = {
                                selectedPeriod = period
                                selectedCategoryName = null
                            },
                            label = { Text(period.displayName, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Cobalt60.copy(alpha = 0.15f),
                                selectedLabelColor = Cobalt60
                            )
                        )
                    }
                }
            }

            // Hero Productivity & Metrics Overview Card
            item {
                ProductivityScoreHeroCard(report = report)
            }

            // Interactive Donut Chart Visualizer Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "TIME ALLOCATION DONUT CHART",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Donut Chart Canvas with Inner Detail
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            InteractiveDonutChart(
                                categories = report.categories,
                                selectedCategoryName = selectedCategoryName,
                                onSelectCategory = { selectedCategoryName = it },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Donut Center Hole Telemetry
                            val activeCat = report.categories.find { it.categoryName == selectedCategoryName }
                                ?: report.categories.firstOrNull()

                            if (activeCat != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(activeCat.percentage * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.ExtraBold
                                        ),
                                        color = Color(activeCat.colorHex)
                                    )
                                    Text(
                                        text = activeCat.categoryName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1fh logged", activeCat.durationHours),
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Tap on any chart arc to inspect specific category time share",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Category Breakdown Cards Header
            item {
                Text(
                    text = "CATEGORY TIME DISTRIBUTION (${report.categories.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Category Breakdown Items
            items(report.categories, key = { it.categoryName }) { cat ->
                CategoryAnalyticsCard(
                    category = cat,
                    isSelected = cat.categoryName == selectedCategoryName,
                    onClick = {
                        selectedCategoryName = if (selectedCategoryName == cat.categoryName) null else cat.categoryName
                    }
                )
            }
        }
    }

    if (showAddActivityDialog) {
        AddActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onSave = { start, end, label ->
                viewModel.addActivity(label, start, end)
                showAddActivityDialog = false
            }
        )
    }
}

// ── Hero Score Card ───────────────────────────────────────────────────

@Composable
private fun ProductivityScoreHeroCard(report: TimeAnalyticsReport) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Cobalt60.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                Column {
                    Text(
                        text = "PRODUCTIVITY SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Cobalt60
                    )
                    Text(
                        text = "${report.productivityScorePercent}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (report.productivityScorePercent >= 75) SuccessGreen.copy(alpha = 0.15f) else Amber60.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (report.productivityScorePercent >= 75) "OPTIMAL FLOW" else "BALANCED",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (report.productivityScorePercent >= 75) SuccessGreen else Amber60,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // 3-Metric Summary Tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Total Logged", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(String.format(Locale.getDefault(), "%.1f hrs", report.totalTrackedHours), fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Peak Focus Slot", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(report.peakProductiveHour.substringBefore("–").trim(), fontWeight = FontWeight.Bold, color = Cobalt60)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Activities", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${report.activitiesCount} Sessions", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ── Interactive Donut Chart Canvas ────────────────────────────────────

@Composable
private fun InteractiveDonutChart(
    categories: List<CategoryAnalytics>,
    selectedCategoryName: String?,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val animSweep = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animSweep.snapTo(0f)
        animSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = modifier.pointerInput(categories) {
            detectTapGestures { tapOffset ->
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = tapOffset.x - center.x
                val dy = tapOffset.y - center.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDim = minOf(size.width, size.height).toFloat()
                val outerRadius = minDim / 2f
                val innerRadius = outerRadius * 0.62f

                if (dist in innerRadius..outerRadius) {
                    var angleDeg = (atan2(dy, dx) * 180.0 / Math.PI + 90.0)
                    if (angleDeg < 0) angleDeg += 360.0

                    var cumulativeAngle = 0.0
                    for (cat in categories) {
                        val sweep = cat.percentage * 360.0
                        if (angleDeg in cumulativeAngle..(cumulativeAngle + sweep)) {
                            onSelectCategory(cat.categoryName)
                            break
                        }
                        cumulativeAngle += sweep
                    }
                }
            }
        }
    ) {
        val outerRadius = size.minDimension / 2f * 0.90f
        val innerRadius = outerRadius * 0.65f
        val strokeWidth = outerRadius - innerRadius
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        for (cat in categories) {
            val sweep = (cat.percentage * 360f * animSweep.value)
            val isSelected = cat.categoryName == selectedCategoryName
            val catColor = Color(cat.colorHex)

            val effectiveRadius = if (isSelected) outerRadius * 1.05f else outerRadius
            val effectiveStroke = if (isSelected) strokeWidth * 1.15f else strokeWidth

            drawArc(
                color = catColor,
                startAngle = startAngle,
                sweepAngle = sweep.coerceAtLeast(1.5f),
                useCenter = false,
                topLeft = Offset(center.x - effectiveRadius + effectiveStroke / 2f, center.y - effectiveRadius + effectiveStroke / 2f),
                size = Size(effectiveRadius * 2f - effectiveStroke, effectiveRadius * 2f - effectiveStroke),
                style = Stroke(width = effectiveStroke, cap = StrokeCap.Butt)
            )

            startAngle += sweep
        }
    }
}

// ── Category Card ─────────────────────────────────────────────────────

@Composable
private fun CategoryAnalyticsCard(
    category: CategoryAnalytics,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val catColor = Color(category.colorHex)
    val catIcon = getCategoryVector(category.categoryName)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) catColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = catColor.copy(alpha = 0.20f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = category.categoryName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${category.sessionCount} Sessions · ${if (category.isProductive) "Productive" else "Personal"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f Hours", category.durationHours),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${(category.percentage * 100).toInt()}% share",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = catColor
                    )
                }
            }

            // Linear Progress Bar
            LinearProgressIndicator(
                progress = { category.percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

// ── Category Icon Helper ──────────────────────────────────────────────

private fun getCategoryVector(category: String): ImageVector {
    val l = category.lowercase()
    return when {
        l.contains("code") || l.contains("dev") -> Icons.Filled.Code
        l.contains("deep") || l.contains("focus") -> Icons.Filled.Psychology
        l.contains("meet") || l.contains("sync") -> Icons.Filled.Groups
        l.contains("study") || l.contains("learn") -> Icons.Filled.School
        l.contains("exercise") || l.contains("health") -> Icons.Filled.DirectionsWalk
        l.contains("rest") || l.contains("sleep") -> Icons.Filled.Hotel
        else -> Icons.Filled.Timeline
    }
}
