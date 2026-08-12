package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TimeAuditChart(
    fromZone: String,
    toZone: String,
    offsetDifferenceMinutes: Int
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "24-Hour Working Overlap Audit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Business hours (9 AM – 6 PM)  ·  $fromZone vs $toZone",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val tertiaryColor = MaterialTheme.colorScheme.tertiary
            val inactiveColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

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
                    .height(56.dp)
            ) {
                val width = size.width
                val height = size.height
                val barWidth = width / 24f
                val cornerRadius = CornerRadius(4f, 4f)

                for (hour in 0 until 24) {
                    val isLocalWorking = hour in 9..17
                    val targetHour = (hour + (offsetDifferenceMinutes / 60) + 24) % 24
                    val isTargetWorking = targetHour in 9..17

                    val color = when {
                        isLocalWorking && isTargetWorking -> primaryColor
                        isLocalWorking || isTargetWorking -> tertiaryColor
                        else -> inactiveColor
                    }

                    // Stagger animation: bars appear left to right
                    val barProgress = (animProgress.value * 24f - hour).coerceIn(0f, 1f)
                    val barHeight = (height - 8f) * barProgress

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
