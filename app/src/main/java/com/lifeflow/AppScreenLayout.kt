package com.lifeflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

private val ScreenOuterHorizontalPadding = 20.dp
private val ScreenOuterVerticalPadding = 12.dp
private val SectionSpacing = 20.dp
private val ScreenContentMaxWidth = 580.dp
private val ScreenHeaderSpacing = 14.dp

private val ScreenSurfaceTone = Color(0xFFF2F3F7)
private val ScreenAuraBlueCore = Color(0xFF22CDF7).copy(alpha = 0.22f)
private val ScreenAuraBlueSoft = Color(0xFF22CDF7).copy(alpha = 0.11f)
private val ScreenAuraWhite = Color(0xFFFFFFFF).copy(alpha = 0.34f)
private val ScreenWaveBlueSoft = Color(0xFF22CDF7).copy(alpha = 0.09f)
private val ScreenWaveBlueLine = Color(0xFF22CDF7).copy(alpha = 0.13f)
private val ScreenWaveWhiteSoft = Color(0xFFFFFFFF).copy(alpha = 0.14f)

private val ScreenBottomAuraBase = Color(0xFF22CDF7)
private val ScreenBottomAuraStrong = Color(0xFF22CDF7).copy(alpha = 0.72f)
private val ScreenBottomAuraMid = Color(0xFF22CDF7).copy(alpha = 0.30f)
private val ScreenBottomAuraSoft = Color(0xFF22CDF7).copy(alpha = 0.08f)

private fun Modifier.lifeFlowScreenBackdrop(): Modifier {
    return drawWithCache {
        val minSide = min(size.width, size.height)

        val topAuraCenter = Offset(
            x = size.width * 0.20f,
            y = size.height * 0.16f
        )
        val middleAuraCenter = Offset(
            x = size.width * 0.16f,
            y = size.height * 0.42f
        )
        val lowerAuraCenter = Offset(
            x = size.width * 0.12f,
            y = size.height * 0.72f
        )

        val topAuraBrush = Brush.radialGradient(
            colors = listOf(
                ScreenAuraWhite,
                ScreenAuraBlueCore,
                ScreenAuraBlueSoft,
                Color.Transparent
            ),
            center = topAuraCenter,
            radius = minSide * 0.78f
        )

        val middleAuraBrush = Brush.radialGradient(
            colors = listOf(
                ScreenAuraBlueCore,
                ScreenAuraBlueSoft,
                Color.Transparent
            ),
            center = middleAuraCenter,
            radius = minSide * 0.58f
        )

        val lowerAuraBrush = Brush.radialGradient(
            colors = listOf(
                ScreenAuraBlueSoft,
                Color.Transparent
            ),
            center = lowerAuraCenter,
            radius = minSide * 0.48f
        )

        fun contourWave(
            startX: Float,
            startY: Float,
            control1X: Float,
            control1Y: Float,
            control2X: Float,
            control2Y: Float,
            endX: Float,
            endY: Float
        ): Path {
            return Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    control1X, control1Y,
                    control2X, control2Y,
                    endX, endY
                )
            }
        }

        val upperWave = contourWave(
            startX = -size.width * 0.18f,
            startY = size.height * 0.16f,
            control1X = size.width * 0.00f,
            control1Y = size.height * 0.04f,
            control2X = size.width * 0.32f,
            control2Y = size.height * 0.24f,
            endX = size.width * 0.76f,
            endY = size.height * 0.18f
        )

        val middleWave = contourWave(
            startX = -size.width * 0.20f,
            startY = size.height * 0.42f,
            control1X = size.width * 0.02f,
            control1Y = size.height * 0.30f,
            control2X = size.width * 0.34f,
            control2Y = size.height * 0.56f,
            endX = size.width * 0.78f,
            endY = size.height * 0.44f
        )

        val lowerWave = contourWave(
            startX = -size.width * 0.16f,
            startY = size.height * 0.70f,
            control1X = size.width * 0.05f,
            control1Y = size.height * 0.58f,
            control2X = size.width * 0.36f,
            control2Y = size.height * 0.84f,
            endX = size.width * 0.74f,
            endY = size.height * 0.74f
        )

        val accentWave = contourWave(
            startX = -size.width * 0.04f,
            startY = size.height * 0.56f,
            control1X = size.width * 0.14f,
            control1Y = size.height * 0.46f,
            control2X = size.width * 0.40f,
            control2Y = size.height * 0.68f,
            endX = size.width * 0.70f,
            endY = size.height * 0.58f
        )

        val waveBlurStroke = 12.dp.toPx()
        val waveSoftStroke = 4.dp.toPx()
        val waveFineStroke = 1.25.dp.toPx()

        onDrawBehind {
            drawRect(color = ScreenSurfaceTone)
            drawRect(brush = topAuraBrush)
            drawRect(brush = middleAuraBrush)
            drawRect(brush = lowerAuraBrush)

            fun drawWave(path: Path) {
                drawPath(
                    path = path,
                    color = ScreenWaveBlueSoft,
                    style = Stroke(width = waveBlurStroke)
                )
                drawPath(
                    path = path,
                    color = ScreenWaveWhiteSoft,
                    style = Stroke(width = waveSoftStroke)
                )
                drawPath(
                    path = path,
                    color = ScreenWaveBlueLine,
                    style = Stroke(width = waveFineStroke)
                )
            }

            drawWave(upperWave)
            drawWave(middleWave)
            drawWave(lowerWave)
            drawWave(accentWave)
        }
    }
}

@Composable
private fun LifeFlowBottomAura(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawRect(
            color = ScreenSurfaceTone
        )
    }
}

@Composable
internal fun ScreenContainer(
    title: String,
    showBackButton: Boolean = false,
    onBack: (() -> Unit)? = null,
    useBackdrop: Boolean = true,
    showBottomAura: Boolean = false,
    content: @Composable () -> Unit
) {
    val showHeader = (showBackButton && onBack != null) || title.isNotBlank()
    val screenModifier = if (useBackdrop) {
        Modifier
            .fillMaxSize()
            .lifeFlowScreenBackdrop()
    } else {
        Modifier
            .fillMaxSize()
            .background(ScreenSurfaceTone)
    }

    Box(modifier = screenModifier) {
        if (showBottomAura) {
            LifeFlowBottomAura(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .align(Alignment.BottomCenter)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = ScreenOuterHorizontalPadding,
                    vertical = ScreenOuterVerticalPadding
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ScreenContentMaxWidth)
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.Top
            ) {
                if (showHeader) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ScreenHeaderSpacing)
                    ) {
                        if (showBackButton && onBack != null) {
                            TextButton(onClick = onBack) {
                                Text("Back")
                            }
                        }

                        if (title.isNotBlank()) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(SectionSpacing))
                }

                content()
            }
        }
    }
}

@Composable
internal fun KeyValueLine(
    label: String,
    value: String
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label: ",
            style = lifeFlowCardRowLabelStyle(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = lifeFlowCardRowValueStyle(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

