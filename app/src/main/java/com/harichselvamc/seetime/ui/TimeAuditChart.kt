package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime

@Composable
fun TimeAuditChart(
    fromZone: String,
    toZone: String,
    offsetDifferenceMinutes: Int,
    currentEpochMillis: Long = 0L,
    currentHour: Int = -1,
    currentMinute: Int = 0,
    showNowNeedle: Boolean = true
) {
    // Stagger animation
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(fromZone, toZone, offsetDifferenceMinutes) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )
    }

    // Local 1-second ticker to ensure chart needle updates live continuously
    var localTick by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000L)
            localTick = System.currentTimeMillis()
        }
    }

    // Determine current hour and minute for the needle
    val (effectiveHour, effectiveMin) = remember(currentEpochMillis, localTick, currentHour, currentMinute) {
        if (currentEpochMillis > 0L) {
            val zdt = java.time.ZonedDateTime.ofInstant(java.time.Instant.ofEpochMilli(currentEpochMillis), java.time.ZoneId.systemDefault())
            Pair(zdt.hour, zdt.minute)
        } else if (currentHour in 0..23) {
            Pair(currentHour, currentMinute)
        } else {
            val now = LocalTime.now()
            Pair(now.hour, now.minute)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "24-Hour Working Overlap Audit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Business hours (9 AM – 6 PM)  ·  $fromZone vs $toZone",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (showNowNeedle) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("NOW %02d:%02d", effectiveHour, effectiveMin),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 10.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            val needleColor = MaterialTheme.colorScheme.error

            // Calculate overlap hours for summary
            var bothWorkingCount = 0
            var oneWorkingCount = 0
            for (hour in 0 until 24) {
                val isLocalWorking = hour in 9..17
                val targetHour = (hour + (offsetDifferenceMinutes / 60) + 24) % 24
                val isTargetWorking = targetHour in 9..17
                if (isLocalWorking && isTargetWorking) bothWorkingCount++
                else if (isLocalWorking || isTargetWorking) oneWorkingCount++
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                val width = size.width
                val height = size.height
                val barWidth = width / 24f
                val cornerRadius = CornerRadius(4f, 4f)

                // Draw 24-hour bars
                for (hour in 0 until 24) {
                    val isLocalWorking = hour in 9..17
                    val targetHour = (hour + (offsetDifferenceMinutes / 60) + 24) % 24
                    val isTargetWorking = targetHour in 9..17

                    val color = when {
                        isLocalWorking && isTargetWorking -> primaryColor
                        isLocalWorking || isTargetWorking -> tertiaryColor
                        else -> inactiveColor
                    }

                    val barProgress = (animProgress.value * 24f - hour).coerceIn(0f, 1f)
                    val barHeight = (height - 12f) * barProgress

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(
                            hour * barWidth + 1.5f,
                            height - 4f - barHeight
                        ),
                        size = Size(barWidth - 3f, barHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // Draw Live Current Time Needle
                if (showNowNeedle) {
                    val nowFraction = (effectiveHour + effectiveMin / 60f) / 24f
                    val needleX = (nowFraction * width).coerceIn(4f, width - 4f)

                    // Draw vertical glowing line
                    drawLine(
                        color = needleColor,
                        start = Offset(needleX, 0f),
                        end = Offset(needleX, height),
                        strokeWidth = 3f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                    )

                    // Draw top indicator pulse dot
                    drawCircle(
                        color = needleColor,
                        radius = 5f,
                        center = Offset(needleX, 4f)
                    )

                    // Draw bottom indicator dot
                    drawCircle(
                        color = needleColor,
                        radius = 4f,
                        center = Offset(needleX, height - 4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hour labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("12AM", "6AM", "12PM", "6PM", "12AM").forEach { label ->
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(
                    color = primaryColor,
                    label = "Both ($bothWorkingCount h)"
                )
                LegendItem(
                    color = tertiaryColor,
                    label = "One ($oneWorkingCount h)"
                )
                LegendItem(
                    color = inactiveColor,
                    label = "Neither"
                )
                if (showNowNeedle) {
                    LegendItem(
                        color = needleColor,
                        label = "Live Needle"
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = RoundedCornerShape(3.dp),
            color = color
        ) {}
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
