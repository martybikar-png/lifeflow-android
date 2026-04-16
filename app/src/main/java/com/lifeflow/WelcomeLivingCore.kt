package com.lifeflow

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate

private val WelcomeCoreCyan = Color(0xFF22CDF7)
private val WelcomeCoreLavender = Color(0xFF9EA7FF)
private val WelcomeCoreIce = Color(0xFFEAFBFF)

@Composable
internal fun WelcomeLivingCore(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "welcomeLivingCore")

    val outerRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 15000,
                easing = LinearEasing
            )
        ),
        label = "outerRotation"
    )

    val innerRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 12000,
                easing = LinearEasing
            )
        ),
        label = "innerRotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val drift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4400,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Canvas(modifier = modifier) {
        val minDim = size.minDimension
        val center = Offset(
            x = size.width * 0.50f + minDim * 0.040f * drift,
            y = size.height * 0.43f - minDim * 0.034f * drift
        )
        val outerAuraRadius = minDim * 0.78f
        val glowRadius = minDim * 0.62f
        val coreRadius = minDim * 0.30f * pulse

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    WelcomeCoreLavender.copy(alpha = 0.14f),
                    WelcomeCoreCyan.copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = center,
                radius = outerAuraRadius
            ),
            center = center,
            radius = outerAuraRadius
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.96f),
                    WelcomeCoreIce.copy(alpha = 0.88f),
                    WelcomeCoreCyan.copy(alpha = 0.34f),
                    WelcomeCoreLavender.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = center,
                radius = glowRadius
            ),
            center = center,
            radius = glowRadius
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.98f),
                    WelcomeCoreIce.copy(alpha = 0.92f),
                    WelcomeCoreCyan.copy(alpha = 0.56f),
                    WelcomeCoreLavender.copy(alpha = 0.26f),
                    Color.Transparent
                ),
                center = center,
                radius = minDim * 0.42f
            ),
            center = center,
            radius = minDim * 0.42f
        )

        rotate(
            degrees = outerRotation,
            pivot = center
        ) {
            val cyanOrbitCenter = Offset(
                x = center.x + coreRadius * 0.68f,
                y = center.y - coreRadius * 0.16f
            )
            val lavenderOrbitCenter = Offset(
                x = center.x - coreRadius * 0.56f,
                y = center.y + coreRadius * 0.28f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WelcomeCoreCyan.copy(alpha = 0.46f),
                        WelcomeCoreIce.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = cyanOrbitCenter,
                    radius = coreRadius * 1.16f
                ),
                center = cyanOrbitCenter,
                radius = coreRadius * 1.16f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WelcomeCoreLavender.copy(alpha = 0.34f),
                        Color.Transparent
                    ),
                    center = lavenderOrbitCenter,
                    radius = coreRadius * 0.98f
                ),
                center = lavenderOrbitCenter,
                radius = coreRadius * 0.98f
            )
        }

        rotate(
            degrees = innerRotation,
            pivot = center
        ) {
            val iceOrbitCenter = Offset(
                x = center.x + coreRadius * 0.16f,
                y = center.y + coreRadius * 0.62f
            )
            val cyanInnerCenter = Offset(
                x = center.x - coreRadius * 0.18f,
                y = center.y - coreRadius * 0.50f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WelcomeCoreIce.copy(alpha = 0.46f),
                        WelcomeCoreCyan.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = iceOrbitCenter,
                    radius = coreRadius * 0.96f
                ),
                center = iceOrbitCenter,
                radius = coreRadius * 0.96f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        WelcomeCoreCyan.copy(alpha = 0.38f),
                        Color.Transparent
                    ),
                    center = cyanInnerCenter,
                    radius = coreRadius * 0.82f
                ),
                center = cyanInnerCenter,
                radius = coreRadius * 0.82f
            )
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.76f),
            center = Offset(
                x = center.x - coreRadius * 0.26f,
                y = center.y - coreRadius * 0.28f
            ),
            radius = coreRadius * 0.18f
        )
    }
}

