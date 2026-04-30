package com.lifeflow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun PremiumLoginGoldDivider(
    modifier: Modifier = Modifier
) {
    val shine = rememberInfiniteTransition(label = "dividerShine").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9750, easing = LinearEasing), RepeatMode.Restart),
        label = "dividerShineProgress"
    ).value

    Canvas(modifier = modifier.height(48.dp)) {
        val strokeWidth = 3.dp.toPx()
        val radius = 44.dp.toPx()
        val curve = 0.55228475f
        val cornerControl = radius * curve

        val path = Path().apply {
            moveTo(-2f, radius)
            cubicTo(-2f, radius - cornerControl, radius - cornerControl - 2f, 0f, radius - 2f, 0f)
            lineTo(size.width - radius - 2f, 0f)
            cubicTo(
                size.width - radius + cornerControl - 2f,
                0f,
                size.width - 2f,
                radius - cornerControl,
                size.width - 2f,
                radius
            )
        }

        drawPath(
            path = path,
            brush = PremiumLoginGoldDividerBrush,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val measure = PathMeasure()
        measure.setPath(path, false)
        val total = measure.length
        val start = total * shine
        val length = total * 0.14f
        val alphas = listOf(0.03f, 0.06f, 0.11f, 0.18f, 0.30f, 0.48f, 0.86f, 0.48f, 0.30f, 0.18f, 0.11f, 0.06f, 0.03f)

        alphas.forEachIndexed { index, alpha ->
            val from = start + length * index / alphas.size
            val to = start + length * (index + 1) / alphas.size
            val segment = Path()
            if (measure.getSegment(from.coerceAtMost(total), to.coerceAtMost(total), segment, true)) {
                drawPath(
                    path = segment,
                    color = Color(0xFFFFF6D8).copy(alpha = alpha * 0.24f),
                    style = Stroke(width = strokeWidth + 6.4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = segment,
                    color = Color(0xFFFFF1B0).copy(alpha = alpha * 0.55f),
                    style = Stroke(width = strokeWidth + 3.0.dp.toPx(), cap = StrokeCap.Round)
                )
                drawPath(
                    path = segment,
                    color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.78f),
                    style = Stroke(width = strokeWidth + 0.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}
