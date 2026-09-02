package com.harichselvamc.seetime.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ConfettiShape {
    RECTANGLE,
    CIRCLE,
    STRIP
}

private data class ConfettiParticle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val color: Color,
    val particleSize: Float,
    val shape: ConfettiShape,
    val rotationSpeed: Float,
    val initialRotation: Float
) {
    fun draw(drawScope: DrawScope, progress: Float, canvasWidth: Float, canvasHeight: Float) {
        val time = progress * 1.8f
        val gravity = 1200f
        val currentX = initialX + velocityX * time
        val currentY = initialY + velocityY * time + 0.5f * gravity * time * time
        val rotation = initialRotation + rotationSpeed * progress * 360f
        val alpha = if (progress > 0.7f) {
            ((1f - progress) / 0.3f).coerceIn(0f, 1f)
        } else {
            1f
        }

        if (currentY > canvasHeight + 50f || currentX < -50f || currentX > canvasWidth + 50f) return

        val particleColor = color.copy(alpha = alpha)

        drawScope.rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
            when (shape) {
                ConfettiShape.RECTANGLE -> {
                    drawRect(
                        color = particleColor,
                        topLeft = Offset(currentX - particleSize / 2f, currentY - particleSize / 2f),
                        size = Size(particleSize, particleSize * 0.6f)
                    )
                }
                ConfettiShape.CIRCLE -> {
                    drawCircle(
                        color = particleColor,
                        radius = particleSize / 2f,
                        center = Offset(currentX, currentY)
                    )
                }
                ConfettiShape.STRIP -> {
                    drawRect(
                        color = particleColor,
                        topLeft = Offset(currentX - particleSize / 4f, currentY - particleSize),
                        size = Size(particleSize * 0.5f, particleSize * 1.8f)
                    )
                }
            }
        }
    }

    companion object {
        private val CONFETTI_COLORS = listOf(
            Color(0xFF3861FB), // Cobalt
            Color(0xFF6366F1), // Indigo
            Color(0xFFF59E0B), // Amber / Gold
            Color(0xFF10B981), // Emerald Green
            Color(0xFFEF4444), // Coral Red
            Color(0xFFEC4899), // Pink
            Color(0xFF8B5CF6)  // Purple
        )

        fun createRandom(originX: Float, originY: Float): ConfettiParticle {
            val angle = Random.nextDouble(from = -0.85 * PI, until = -0.15 * PI).toFloat()
            val speed = Random.nextFloat() * 650f + 450f
            val vx = speed * cos(angle)
            val vy = speed * sin(angle)

            return ConfettiParticle(
                initialX = originX + (Random.nextFloat() - 0.5f) * 120f,
                initialY = originY + (Random.nextFloat() - 0.5f) * 40f,
                velocityX = vx,
                velocityY = vy,
                color = CONFETTI_COLORS.random(),
                particleSize = Random.nextFloat() * 14f + 10f,
                shape = ConfettiShape.values().random(),
                rotationSpeed = (Random.nextFloat() - 0.5f) * 4f,
                initialRotation = Random.nextFloat() * 360f
            )
        }
    }
}

/**
 * Delightful Jetpack Compose Canvas Confetti Explosion Engine
 * Creates a vibrant particle shower across the screen on milestone / streak completion.
 */
@Composable
fun ConfettiCelebration(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    onAnimationEnd: () -> Unit = {}
) {
    if (!trigger) return

    val progress = remember(trigger) { Animatable(0f) }

    LaunchedEffect(trigger) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val originX = width / 2
        val originY = height * 0.45f

        val particles = List(particleCount) {
            ConfettiParticle.createRandom(originX, originY)
        }

        particles.forEach { particle ->
            particle.draw(
                drawScope = this,
                progress = progress.value,
                canvasWidth = width,
                canvasHeight = height
            )
        }
    }
}
