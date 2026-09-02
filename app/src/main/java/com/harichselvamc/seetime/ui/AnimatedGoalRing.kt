package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Purple60
import com.harichselvamc.seetime.ui.theme.SuccessGreen

data class GoalRingData(
    val progress: Float, // 0.0f to 1.0f+
    val color: Color,
    val label: String
)

/**
 * Animated Concentric Goal Rings (Duolingo / Apple Fitness style)
 * Smoothly animates arc sweeps with rounded caps and center summary.
 */
@Composable
fun AnimatedGoalRing(
    outerRingProgress: Float,
    middleRingProgress: Float = 0f,
    innerRingProgress: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 10.dp
) {
    val animatedOuter by animateFloatAsState(
        targetValue = outerRingProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "outer_ring_anim"
    )

    val animatedMiddle by animateFloatAsState(
        targetValue = middleRingProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "middle_ring_anim"
    )

    val animatedInner by animateFloatAsState(
        targetValue = innerRingProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
        label = "inner_ring_anim"
    )

    val overallPercent = ((animatedOuter + animatedMiddle + animatedInner) / 3f * 100).toInt()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val spacingPx = strokePx + 4.dp.toPx()

            // 1. Outer Ring (Daily Actions / Check-in)
            val outerRadius = (this.size.minDimension - strokePx) / 2
            val outerCenter = Offset(this.size.width / 2, this.size.height / 2)
            val outerTopLeft = Offset(outerCenter.x - outerRadius, outerCenter.y - outerRadius)
            val outerArcSize = Size(outerRadius * 2, outerRadius * 2)

            // Background Track
            drawArc(
                color = Amber60.copy(alpha = 0.18f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            // Foreground Sweep
            drawArc(
                color = Amber60,
                startAngle = -90f,
                sweepAngle = animatedOuter * 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Middle Ring (Tracked Focus Hours)
            if (middleRingProgress > 0f || animatedMiddle > 0f) {
                val middleRadius = outerRadius - spacingPx
                val middleTopLeft = Offset(outerCenter.x - middleRadius, outerCenter.y - middleRadius)
                val middleArcSize = Size(middleRadius * 2, middleRadius * 2)

                drawArc(
                    color = Cobalt60.copy(alpha = 0.18f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = middleTopLeft,
                    size = middleArcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Cobalt60,
                    startAngle = -90f,
                    sweepAngle = animatedMiddle * 360f,
                    useCenter = false,
                    topLeft = middleTopLeft,
                    size = middleArcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // 3. Inner Ring (Timezone Overlap / Sync)
            if (innerRingProgress > 0f || animatedInner > 0f) {
                val innerRadius = outerRadius - (spacingPx * 2)
                val innerTopLeft = Offset(outerCenter.x - innerRadius, outerCenter.y - innerRadius)
                val innerArcSize = Size(innerRadius * 2, innerRadius * 2)

                drawArc(
                    color = Purple60.copy(alpha = 0.18f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerArcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                drawArc(
                    color = Purple60,
                    startAngle = -90f,
                    sweepAngle = animatedInner * 360f,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerArcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedOuter * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
