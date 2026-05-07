package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

private val RootCanvasMilkTop = Color(0xFFF8FAFB)
private val RootCanvasMilk = Color(0xFFEEF3F6)
private val RootCanvasMilkBottom = Color(0xFFE7EEF3)

private val RootCanvasSoftWhite = Color(0xFFFFFFFF)
private val RootCanvasSoftGrey = Color(0xFFF4F7F9)

private val DockWhite = Color(0xFFFFFFFF)

@Composable
internal fun LifeFlowRootBackground(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to RootCanvasMilkTop,
                        0.34f to RootCanvasMilk,
                        1.00f to RootCanvasMilkBottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMilkyAtmosphere()
            drawBottomDock()
        }
    }
}

private fun DrawScope.drawMilkyAtmosphere() {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                RootCanvasSoftWhite.copy(alpha = 0.72f),
                RootCanvasMilk.copy(alpha = 0.20f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.18f, size.height * 0.16f),
            radius = size.minDimension * 0.68f
        ),
        center = Offset(size.width * 0.18f, size.height * 0.16f),
        radius = size.minDimension * 0.68f
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                RootCanvasSoftWhite.copy(alpha = 0.48f),
                RootCanvasSoftGrey.copy(alpha = 0.22f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.84f, size.height * 0.44f),
            radius = size.minDimension * 0.76f
        ),
        center = Offset(size.width * 0.84f, size.height * 0.44f),
        radius = size.minDimension * 0.76f
    )
}

private fun DrawScope.drawBottomDock() {
    val dockTop = size.height - 88.dp.toPx()
    val sideCorner = 48.dp.toPx()

    val centerX = size.width * 0.5f
    val notchRadius = 46.dp.toPx()
    val shoulderWidth = 22.dp.toPx()
    val shoulderDrop = 14.dp.toPx()

    val arcLeft = centerX - notchRadius
    val arcRight = centerX + notchRadius
    val arcEntryY = dockTop + shoulderDrop

    val leftShoulderStart = arcLeft - shoulderWidth
    val rightShoulderEnd = arcRight + shoulderWidth

    val dockPath = Path().apply {
        moveTo(0f, size.height)
        lineTo(0f, dockTop + sideCorner)

        cubicTo(
            0f,
            dockTop + sideCorner * 0.48f,
            sideCorner * 0.42f,
            dockTop,
            sideCorner,
            dockTop
        )

        lineTo(leftShoulderStart, dockTop)

        cubicTo(
            leftShoulderStart + shoulderWidth * 0.62f,
            dockTop,
            arcLeft,
            arcEntryY - shoulderDrop * 0.42f,
            arcLeft,
            arcEntryY
        )

        arcTo(
            rect = Rect(
                left = centerX - notchRadius,
                top = arcEntryY - notchRadius,
                right = centerX + notchRadius,
                bottom = arcEntryY + notchRadius
            ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -180f,
            forceMoveTo = false
        )

        cubicTo(
            arcRight,
            arcEntryY - shoulderDrop * 0.42f,
            rightShoulderEnd - shoulderWidth * 0.62f,
            dockTop,
            rightShoulderEnd,
            dockTop
        )

        lineTo(size.width - sideCorner, dockTop)

        cubicTo(
            size.width - sideCorner * 0.42f,
            dockTop,
            size.width,
            dockTop + sideCorner * 0.48f,
            size.width,
            dockTop + sideCorner
        )

        lineTo(size.width, size.height)
        close()
    }

    drawPath(
        path = dockPath,
        color = DockWhite
    )
}
