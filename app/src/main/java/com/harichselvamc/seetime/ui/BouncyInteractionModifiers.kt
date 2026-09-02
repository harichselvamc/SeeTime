package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

/**
 * Bouncy and Spring Interaction Modifiers Suite for Jetpack Compose.
 * Delivers delightful physical feedback on clicks, presses, and micro-interactions.
 */

/**
 * Applies a bouncy spring scale reduction when pressed, with optional haptic feedback.
 */
fun Modifier.bouncyClick(
    scaleDown: Float = 0.93f,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessLow,
    enableHaptics: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val haptics = LocalHapticFeedback.current
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    this
        .scale(scale.value)
        .pointerInput(Unit) {
            while (true) {
                awaitPointerEventScope {
                    awaitFirstDown(requireUnconsumed = false)
                    if (enableHaptics) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    scope.launch {
                        scale.animateTo(
                            targetValue = scaleDown,
                            animationSpec = spring(
                                dampingRatio = dampingRatio,
                                stiffness = stiffness
                            )
                        )
                    }
                    val up = waitForUpOrCancellation()
                    scope.launch {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = dampingRatio,
                                stiffness = stiffness
                            )
                        )
                    }
                    if (up != null) {
                        onClick()
                    }
                }
            }
        }
}

/**
 * Observes isPressed state and smoothly springs to target scale.
 */
fun Modifier.springScale(
    isPressed: Boolean,
    targetScale: Float = 0.94f,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessMediumLow
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    androidx.compose.runtime.LaunchedEffect(isPressed) {
        scale.animateTo(
            targetValue = if (isPressed) targetScale else 1f,
            animationSpec = spring(
                dampingRatio = dampingRatio,
                stiffness = stiffness
            )
        )
    }

    this.scale(scale.value)
}

/**
 * Continuous subtle pulsing breathing effect for important actionable badges or indicators.
 */
fun Modifier.pulsingScale(
    minScale: Float = 0.97f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_scale")
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_anim"
    )

    this.scale(scale)
}
