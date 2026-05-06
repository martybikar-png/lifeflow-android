package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

internal val LifeFlowNeuSurfaceColor = Color(0xFFF5F7FC)

private val LifeFlowNeuLightShadow = Color(0xFFFFFFFF).copy(alpha = 0.98f)
private val LifeFlowNeuAmbientLift = Color(0xFFE8F7FF).copy(alpha = 0.82f)
private val LifeFlowNeuSoftDepth = Color(0xFFBFC8D8).copy(alpha = 0.56f)
private val LifeFlowNeuBlueDepth = Color(0xFF2F8FFF).copy(alpha = 0.13f)
private val LifeFlowNeuOutline = Color(0xFFFFFFFF).copy(alpha = 0.78f)
private val LifeFlowNeuInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.96f)
private val LifeFlowNeuInnerShade = Color(0xFFB8C3D4).copy(alpha = 0.16f)

internal fun Modifier.lifeFlowRaisedPanelChrome(
    shape: RoundedCornerShape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 24.dp,
                spread = 0.dp,
                color = LifeFlowNeuAmbientLift,
                offset = DpOffset(x = (-10).dp, y = (-10).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 18.dp,
                spread = 0.dp,
                color = LifeFlowNeuLightShadow,
                offset = DpOffset(x = (-5).dp, y = (-5).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 22.dp,
                spread = 0.dp,
                color = LifeFlowNeuSoftDepth,
                offset = DpOffset(x = 8.dp, y = 9.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 30.dp,
                spread = 0.dp,
                color = LifeFlowNeuBlueDepth,
                offset = DpOffset(x = 0.dp, y = 14.dp)
            )
        )
        .background(
            color = LifeFlowNeuSurfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerHighlight,
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 9.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.35.dp,
            color = LifeFlowNeuOutline,
            shape = shape
        )
}
