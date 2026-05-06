package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val RootCanvasMilk = Color(0xFFEEF3F6)
private val RootCanvasMilkTop = Color(0xFFF8FAFB)
private val RootCanvasMilkBottom = Color(0xFFE8F0F4)
private val RootCanvasSoftWhite = Color(0xFFFFFFFF)

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
                        0.28f to RootCanvasMilk,
                        1.00f to RootCanvasMilkBottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RootCanvasSoftWhite.copy(alpha = 0.76f),
                        RootCanvasMilk.copy(alpha = 0.52f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.14f),
                    radius = size.minDimension * 0.74f
                ),
                center = Offset(size.width * 0.18f, size.height * 0.14f),
                radius = size.minDimension * 0.74f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RootCanvasSoftWhite.copy(alpha = 0.44f),
                        RootCanvasMilk.copy(alpha = 0.36f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.58f),
                    radius = size.minDimension * 0.88f
                ),
                center = Offset(size.width * 0.88f, size.height * 0.58f),
                radius = size.minDimension * 0.88f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        RootCanvasSoftWhite.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.50f, size.height * 0.96f),
                    radius = size.minDimension * 0.68f
                ),
                center = Offset(size.width * 0.50f, size.height * 0.96f),
                radius = size.minDimension * 0.68f
            )
        }
    }
}