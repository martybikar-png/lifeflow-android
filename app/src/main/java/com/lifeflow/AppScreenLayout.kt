package com.lifeflow

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val ScreenOuterHorizontalPadding = 20.dp
private val ScreenOuterVerticalPadding = 12.dp
private val SectionSpacing = 20.dp
private val ScreenContentMaxWidth = 580.dp
private val ScreenHeaderSpacing = 14.dp

private val ScreenSurfaceTone = Color(0xFFF2F3F7)

private val ScreenAuraCore = Color(0xFF22CDF7).copy(alpha = 0.18f)
private val ScreenAuraSoft = Color(0xFF22CDF7).copy(alpha = 0.10f)
private val ScreenAuraWhite = Color(0xFFFFFFFF).copy(alpha = 0.28f)
private val ScreenAuraLavender = Color(0xFFC9D4FF).copy(alpha = 0.10f)

private val ScreenOrbitLine = Color(0xFF22CDF7).copy(alpha = 0.14f)
private val ScreenOrbitSoft = Color(0xFFFFFFFF).copy(alpha = 0.16f)

private val ScreenLowerAuraCore = Color(0xFF22CDF7).copy(alpha = 0.08f)
private val ScreenLowerAuraSoft = Color(0xFF22CDF7).copy(alpha = 0.04f)

@Composable
private fun LifeFlowScreenBackdropLayer(
    modifier: Modifier = Modifier,
    includeLowerAura: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "lifeFlowScreenBackdrop")

    val driftXFactor = transition.animateFloat(
        initialValue = -0.018f,
        targetValue = 0.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 16000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lifeFlowBackdropDriftX"
    )

    val driftYFactor = transition.animateFloat(
        initialValue = -0.016f,
        targetValue = 0.020f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 18000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lifeFlowBackdropDriftY"
    )

    val radiusPulse = transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 14000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lifeFlowBackdropRadiusPulse"
    )

    Canvas(modifier = modifier) {
        val minDim = minOf(size.width, size.height)

        val auraCenter = Offset(
            x = size.width * (0.14f + driftXFactor.value),
            y = size.height * (0.11f + driftYFactor.value)
        )

        val outerRadius = minDim * 0.42f * radiusPulse.value
        val midRadius = minDim * 0.28f * radiusPulse.value
        val innerRadius = minDim * 0.15f * radiusPulse.value

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ScreenAuraWhite,
                    ScreenAuraCore,
                    ScreenAuraSoft,
                    Color.Transparent
                ),
                center = auraCenter,
                radius = outerRadius
            ),
            radius = outerRadius,
            center = auraCenter
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ScreenAuraLavender,
                    ScreenAuraSoft,
                    Color.Transparent
                ),
                center = Offset(
                    x = auraCenter.x + minDim * 0.03f,
                    y = auraCenter.y + minDim * 0.02f
                ),
                radius = midRadius
            ),
            radius = midRadius,
            center = Offset(
                x = auraCenter.x + minDim * 0.03f,
                y = auraCenter.y + minDim * 0.02f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ScreenAuraWhite,
                    Color.Transparent
                ),
                center = Offset(
                    x = auraCenter.x - minDim * 0.02f,
                    y = auraCenter.y - minDim * 0.015f
                ),
                radius = innerRadius
            ),
            radius = innerRadius,
            center = Offset(
                x = auraCenter.x - minDim * 0.02f,
                y = auraCenter.y - minDim * 0.015f
            )
        )

        drawCircle(
            color = ScreenOrbitSoft,
            radius = minDim * 0.19f * radiusPulse.value,
            center = auraCenter,
            style = Stroke(width = 1.2.dp.toPx())
        )

        drawCircle(
            color = ScreenOrbitLine,
            radius = minDim * 0.27f * radiusPulse.value,
            center = auraCenter,
            style = Stroke(width = 1.0.dp.toPx())
        )

        if (includeLowerAura) {
            val lowerCenter = Offset(
                x = size.width * 0.50f,
                y = size.height * 0.94f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ScreenLowerAuraCore,
                        ScreenLowerAuraSoft,
                        Color.Transparent
                    ),
                    center = lowerCenter,
                    radius = size.width * 0.62f
                ),
                radius = size.width * 0.62f,
                center = lowerCenter
            )
        }
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenSurfaceTone)
    ) {
        if (useBackdrop) {
            LifeFlowScreenBackdropLayer(
                modifier = Modifier.fillMaxSize(),
                includeLowerAura = showBottomAura
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
                            LifeFlowPrimaryActionButton(
                                label = "Back",
                                onClick = onBack
                            )
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
