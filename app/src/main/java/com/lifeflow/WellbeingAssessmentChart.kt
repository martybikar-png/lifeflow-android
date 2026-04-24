package com.lifeflow

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.lifeflow.domain.wellbeing.WellbeingAssessment

@Composable
internal fun WellbeingTrendChart(
    modifier: Modifier,
    selectedMode: WellbeingViewMode,
    wellbeingAssessment: WellbeingAssessment?
) {
    val score = wellbeingScore(selectedMode, wellbeingAssessment)

    val lineColor = when (selectedMode) {
        WellbeingViewMode.Overall -> WellbeingGraphCyan
        WellbeingViewMode.Activity -> WellbeingGraphLavender
        WellbeingViewMode.Heart -> WellbeingGraphPeach
    }

    val points = wellbeingTrendPoints(selectedMode, score)
    val axisLabelColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(
        alpha = 0.72f
    )

    Canvas(modifier = modifier) {
        val leftAxisWidth = 30.dp.toPx()
        val left = leftAxisWidth + 8.dp.toPx()
        val top = 10.dp.toPx()
        val right = size.width - 8.dp.toPx()
        val bottom = size.height - 12.dp.toPx()

        val chartWidth = right - left
        val chartHeight = bottom - top

        val axisPaint = Paint().apply {
            isAntiAlias = true
            color = axisLabelColor.toArgb()
            textSize = 21f
            textAlign = Paint.Align.LEFT
        }

        val axisLabels = listOf("75", "50", "25", "0")

        repeat(4) { index ->
            val y = top + (chartHeight * index / 3f)

            drawLine(
                color = WellbeingGraphGrid.copy(alpha = 0.42f),
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.1.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawContext.canvas.nativeCanvas.drawText(
                axisLabels[index],
                0f,
                y + 6.dp.toPx(),
                axisPaint
            )
        }

        repeat(7) { index ->
            val x = left + (chartWidth * index / 6f)
            drawCircle(
                color = WellbeingGraphGrid.copy(alpha = 0.58f),
                radius = 1.6.dp.toPx(),
                center = Offset(x, bottom + 2.dp.toPx())
            )
        }

        val chartPoints = points.mapIndexed { index, value ->
            Offset(
                x = left + chartWidth * index / (points.size - 1),
                y = bottom - chartHeight * value
            )
        }

        val linePath = Path().apply {
            moveTo(chartPoints.first().x, chartPoints.first().y)
            for (i in 0 until chartPoints.lastIndex) {
                val current = chartPoints[i]
                val next = chartPoints[i + 1]
                val dx = (next.x - current.x) * 0.38f
                cubicTo(
                    current.x + dx,
                    current.y,
                    next.x - dx,
                    next.y,
                    next.x,
                    next.y
                )
            }
        }

        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(chartPoints.last().x, bottom)
            lineTo(chartPoints.first().x, bottom)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.38f),
                    lineColor.copy(alpha = 0.20f),
                    lineColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = top,
                endY = bottom
            )
        )

        drawPath(
            path = linePath,
            color = WellbeingGraphGlow,
            style = Stroke(
                width = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 3.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        val endPoint = chartPoints.last()
        drawCircle(
            color = lineColor.copy(alpha = 0.16f),
            radius = 10.dp.toPx(),
            center = endPoint
        )
        drawCircle(
            color = lineColor,
            radius = 4.2.dp.toPx(),
            center = endPoint
        )
    }
}