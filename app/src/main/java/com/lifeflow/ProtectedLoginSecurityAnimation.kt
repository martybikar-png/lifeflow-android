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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun PremiumLoginSecurityAnimation(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "premiumLoginSecurityAnimation")

    val outerRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "securityOuterRotation"
    )

    val innerRotation by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "securityInnerRotation"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "securityPulse"
    )

    Canvas(modifier = modifier) {
        val minDim = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)
        val accent = PremiumLoginLink

        drawCircle(
            brush = Brush.radialGradient(
                colorStops = arrayOf(
                    0.0f to accent.copy(alpha = 0.16f),
                    0.42f to accent.copy(alpha = 0.08f),
                    0.82f to Color.White.copy(alpha = 0.18f),
                    1.0f to Color.Transparent
                ),
                center = center,
                radius = minDim * 0.50f
            ),
            radius = minDim * 0.50f,
            center = center
        )

        rotate(degrees = outerRotation, pivot = center) {
            drawSecurityOrbit(
                center = center,
                radius = minDim * 0.39f,
                nodeCount = 6,
                accent = accent,
                alpha = 0.34f
            )
        }

        rotate(degrees = innerRotation, pivot = center) {
            drawSecurityOrbit(
                center = center,
                radius = minDim * 0.28f,
                nodeCount = 4,
                accent = accent,
                alpha = 0.24f
            )
        }

        drawSecurityShieldBadge(
            center = center,
            radius = minDim * 0.20f * pulse,
            accent = accent
        )
    }
}

private fun DrawScope.drawSecurityOrbit(
    center: Offset,
    radius: Float,
    nodeCount: Int,
    accent: Color,
    alpha: Float
) {
    drawCircle(
        color = accent.copy(alpha = alpha * 0.34f),
        radius = radius,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )

    repeat(nodeCount) { index ->
        val angle = (Math.PI * 2.0 * index.toDouble()) / nodeCount.toDouble()
        val node = Offset(
            x = center.x + cos(angle).toFloat() * radius,
            y = center.y + sin(angle).toFloat() * radius
        )

        drawLine(
            color = accent.copy(alpha = alpha * 0.18f),
            start = center,
            end = node,
            strokeWidth = 0.75.dp.toPx(),
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.88f),
            radius = 3.6.dp.toPx(),
            center = node
        )

        drawCircle(
            color = accent.copy(alpha = 0.96f),
            radius = 1.85.dp.toPx(),
            center = node
        )
    }
}

private fun DrawScope.drawSecurityShieldBadge(
    center: Offset,
    radius: Float,
    accent: Color
) {
    val shield = classicShieldPath(center = center, radius = radius)

    drawPath(
        path = shield,
        color = accent.copy(alpha = 0.98f),
        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
    )

    val badgeCenter = Offset(
        x = center.x - radius * 0.66f,
        y = center.y + radius * 0.46f
    )
    val badgeRadius = radius * 0.36f

    drawCircle(
        color = Color.White.copy(alpha = 0.96f),
        radius = badgeRadius,
        center = badgeCenter
    )

    drawCircle(
        color = accent.copy(alpha = 0.98f),
        radius = badgeRadius,
        center = badgeCenter,
        style = Stroke(width = 1.45.dp.toPx())
    )

    drawKeyIcon(
        center = badgeCenter,
        radius = badgeRadius * 0.56f,
        color = accent.copy(alpha = 0.98f)
    )
}

private fun classicShieldPath(
    center: Offset,
    radius: Float
): Path {
    return Path().apply {
        moveTo(center.x, center.y - radius * 1.06f)

        cubicTo(
            center.x + radius * 0.34f, center.y - radius * 0.88f,
            center.x + radius * 0.74f, center.y - radius * 0.84f,
            center.x + radius * 0.88f, center.y - radius * 0.58f
        )

        cubicTo(
            center.x + radius * 0.90f, center.y - radius * 0.10f,
            center.x + radius * 0.72f, center.y + radius * 0.52f,
            center.x, center.y + radius * 1.12f
        )

        cubicTo(
            center.x - radius * 0.72f, center.y + radius * 0.52f,
            center.x - radius * 0.90f, center.y - radius * 0.10f,
            center.x - radius * 0.88f, center.y - radius * 0.58f
        )

        cubicTo(
            center.x - radius * 0.74f, center.y - radius * 0.84f,
            center.x - radius * 0.34f, center.y - radius * 0.88f,
            center.x, center.y - radius * 1.06f
        )

        close()
    }
}

private fun DrawScope.drawKeyIcon(
    center: Offset,
    radius: Float,
    color: Color
) {
    val stroke = 1.3.dp.toPx()
    val headRadius = radius * 0.34f
    val headCenter = Offset(
        x = center.x - radius * 0.24f,
        y = center.y
    )

    drawCircle(
        color = color,
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = stroke)
    )

    drawLine(
        color = color,
        start = Offset(headCenter.x + headRadius * 0.92f, headCenter.y),
        end = Offset(center.x + radius * 0.76f, center.y),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.26f, center.y),
        end = Offset(center.x + radius * 0.26f, center.y + radius * 0.24f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )

    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.56f, center.y),
        end = Offset(center.x + radius * 0.56f, center.y + radius * 0.16f),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}
