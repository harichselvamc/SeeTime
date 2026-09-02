package com.harichselvamc.seetime.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60
import com.harichselvamc.seetime.ui.theme.SuccessGreen
import com.harichselvamc.seetime.util.BiologicalChronotype
import com.harichselvamc.seetime.util.ChronoScheduleBlock
import com.harichselvamc.seetime.util.ChronotypeOptimizerEngine
import java.time.LocalTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronotypeOptimizerScreen(
    viewModel: TimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    var selectedChronotype by remember { mutableStateOf(BiologicalChronotype.BEAR) }
    var showQuizDialog by remember { mutableStateOf(false) }

    val now = remember { LocalTime.now() }
    val currentHour = now.hour
    val currentMinute = now.minute

    val schedule = remember(selectedChronotype) {
        ChronotypeOptimizerEngine.getDailyRoutineSchedule(selectedChronotype)
    }

    val energyCurve = remember(selectedChronotype) {
        ChronotypeOptimizerEngine.generate24HourEnergyCurve(selectedChronotype)
    }

    val currentEnergyPercent = remember(energyCurve, currentHour) {
        energyCurve.getOrNull(currentHour) ?: 50f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = null,
                            tint = Color(selectedChronotype.themeColorHex),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Circadian Chronotype Optimizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(selectedChronotype.themeColorHex).copy(alpha = 0.2f),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { showQuizDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Quiz, contentDescription = null, tint = Color(selectedChronotype.themeColorHex), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Take Quiz",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color(selectedChronotype.themeColorHex)
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
            // Chronotype Selector Chips
            item {
                Text(
                    text = "BIOLOGICAL CHRONOTYPE PROFILE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BiologicalChronotype.values()) { chrono ->
                        val isSelected = selectedChronotype == chrono
                        val themeColor = Color(chrono.themeColorHex)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedChronotype = chrono },
                            label = { Text("${chrono.emoji} ${chrono.title}", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = themeColor.copy(alpha = 0.2f),
                                selectedLabelColor = themeColor
                            )
                        )
                    }
                }
            }

            // 24-Hour Circadian Energy Curve Hero Canvas Card
            item {
                val themeColor = Color(selectedChronotype.themeColorHex)
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
                                text = "24-HOUR CIRCADIAN ENERGY CURVE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = themeColor
                            )
                            Text(
                                text = "Now: ${currentEnergyPercent.toInt()}% Energy",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Circadian Curve Canvas
                        CircadianEnergyCurveCanvas(
                            energyLevels = energyCurve,
                            currentHour = currentHour,
                            currentMinute = currentMinute,
                            themeColor = themeColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM (Rest)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("08 AM (Cortisol)", style = MaterialTheme.typography.labelSmall, color = Amber60)
                            Text("12 PM (Peak)", style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                            Text("06 PM (Second Wind)", style = MaterialTheme.typography.labelSmall, color = Cyan60)
                            Text("11 PM (Sleep)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            // Active Chronotype Profile Summary Card
            item {
                val themeColor = Color(selectedChronotype.themeColorHex)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = themeColor.copy(alpha = 0.12f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedChronotype.emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${selectedChronotype.title} Chronotype",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = selectedChronotype.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = themeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = themeColor
                            ) {
                                Text(
                                    text = selectedChronotype.populationPercentage,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = selectedChronotype.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Ideal Wake", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(selectedChronotype.idealWakeTime, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = themeColor)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("Ideal Bedtime", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text(selectedChronotype.idealBedTime, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = themeColor)
                                }
                            }
                        }
                    }
                }
            }

            // Daily Routine Optimization Schedule Section Title
            item {
                Text(
                    text = "OPTIMAL DAILY BIOLOGICAL ROUTINE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(schedule) { block ->
                ChronoScheduleBlockCard(block = block)
            }
        }
    }

    // 5-Question Chronotype Self-Assessment Quiz Dialog
    if (showQuizDialog) {
        ChronoQuizDialog(
            onDismiss = { showQuizDialog = false },
            onQuizCompleted = { diagnosedChronotype ->
                selectedChronotype = diagnosedChronotype
                showQuizDialog = false
            }
        )
    }
}

@Composable
private fun ChronoScheduleBlockCard(block: ChronoScheduleBlock) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (block.isPeakFocus) SuccessGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
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
                color = if (block.isPeakFocus) SuccessGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = block.emoji, fontSize = 22.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = block.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = block.timeSpan,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (block.isPeakFocus) SuccessGreen else Cobalt60
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = block.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CircadianEnergyCurveCanvas(
    energyLevels: List<Float>,
    currentHour: Int,
    currentMinute: Int,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Background
        drawRect(color = Color(0xFF0F172A))

        if (energyLevels.isEmpty()) return@Canvas

        // Draw Energy Area Path
        val stepX = w / (energyLevels.size - 1)
        val path = Path()
        path.moveTo(0f, h - (energyLevels[0] / 100f * h))

        for (i in 1 until energyLevels.size) {
            val prevX = (i - 1) * stepX
            val prevY = h - (energyLevels[i - 1] / 100f * h)
            val currX = i * stepX
            val currY = h - (energyLevels[i] / 100f * h)

            val ctrlX1 = prevX + (stepX / 2f)
            val ctrlY1 = prevY
            val ctrlX2 = prevX + (stepX / 2f)
            val ctrlY2 = currY

            path.cubicTo(ctrlX1, ctrlY1, ctrlX2, ctrlY2, currX, currY)
        }

        // Fill under curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                listOf(themeColor.copy(alpha = 0.45f), Color.Transparent)
            )
        )

        // Stroke line
        drawPath(
            path = path,
            color = themeColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Current Time Needle
        val currentFraction = (currentHour + currentMinute / 60f) / 24f
        val needleX = (currentFraction * w).coerceIn(0f, w)

        drawLine(
            color = Color.White,
            start = Offset(needleX, 0f),
            end = Offset(needleX, h),
            strokeWidth = 2.dp.toPx()
        )

        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(needleX, 8.dp.toPx()))
    }
}

@Composable
private fun ChronoQuizDialog(
    onDismiss: () -> Unit,
    onQuizCompleted: (BiologicalChronotype) -> Unit
) {
    val selectedAnswers = remember { mutableStateListOf(3, 3, 3, 3, 3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Quiz, contentDescription = null, tint = Amber60, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chronotype Assessment Quiz", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(ChronotypeOptimizerEngine.CHRONO_QUIZ_QUESTIONS) { q ->
                    Column {
                        Text(
                            text = "${q.id}. ${q.question}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        q.options.forEach { (optionLabel, scoreVal) ->
                            val isChosen = selectedAnswers[q.id - 1] == scoreVal
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAnswers[q.id - 1] = scoreVal }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = isChosen, onClick = { selectedAnswers[q.id - 1] = scoreVal })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = optionLabel, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val total = selectedAnswers.sum()
                    val result = ChronotypeOptimizerEngine.evaluateQuizScore(total)
                    onQuizCompleted(result)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber60)
            ) {
                Text("Diagnose My Chronotype", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
