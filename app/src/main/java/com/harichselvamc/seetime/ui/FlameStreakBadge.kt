package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.harichselvamc.seetime.ui.theme.Amber60
import com.harichselvamc.seetime.ui.theme.Cobalt60
import com.harichselvamc.seetime.ui.theme.Cyan60

/**
 * Pulsing Flame Streak Component
 * Features continuous pulsing glow animation and dynamic color gradations based on streak tiers.
 */
@Composable
fun FlameStreakBadge(
    streakCount: Int,
    streakFreezes: Int = 0,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame_pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_glow"
    )

    val (flameColors, flameTitle) = when {
        streakCount >= 100 -> listOf(Color(0xFFE0E7FF), Color(0xFF6366F1), Color(0xFFEC4899)) to "Supernova"
        streakCount >= 30  -> listOf(Color(0xFF8B5CF6), Color(0xFFEC4899), Amber60) to "Inferno"
        streakCount >= 7   -> listOf(Amber60, Color(0xFFF97316), Color(0xFFEF4444)) to "Blaze"
        else               -> listOf(Amber60, Color(0xFFF97316)) to "Spark"
    }

    Surface(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(20.dp))
            .bouncyClick(scaleDown = 0.92f, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(24.dp)
            ) {
                // Background animated glow ring
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    flameColors.first().copy(alpha = glowAlpha),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )

                // Blazing flame icon
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "$streakCount day streak ($flameTitle)",
                    tint = flameColors.first(),
                    modifier = Modifier
                        .size(20.dp)
                        .scale(pulseScale)
                )
            }

            // Streak Counter text
            Text(
                text = "$streakCount",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Optional Ice Shield / Freeze indicator
            if (streakFreezes > 0) {
                Icon(
                    imageVector = Icons.Rounded.AcUnit,
                    contentDescription = "$streakFreezes Streak Freeze available",
                    tint = Cyan60,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
