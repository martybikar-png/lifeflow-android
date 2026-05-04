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

internal val LifeFlowNeuSurfaceColor = Color(0xFFF2F3F7)

private val LifeFlowNeuLightShadow = Color(0xFFFFFFFF)
private val LifeFlowNeuSoftDepth = Color(0xFFD2D6DF).copy(alpha = 0.68f)
private val LifeFlowNeuOutline = Color(0xFFE8D9AA).copy(alpha = 0.92f)
private val LifeFlowNeuInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.94f)
private val LifeFlowNeuInnerShade = Color(0xFFD2D6DF).copy(alpha = 0.18f)

internal fun Modifier.lifeFlowRaisedPanelChrome(
    shape: RoundedCornerShape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 15.dp,
                spread = 0.dp,
                color = LifeFlowNeuLightShadow,
                offset = DpOffset(x = (-6).dp, y = (-6).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 15.dp,
                spread = 0.dp,
                color = LifeFlowNeuSoftDepth,
                offset = DpOffset(x = 6.dp, y = 6.dp)
            )
        )
        .background(
            color = LifeFlowNeuSurfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerHighlight,
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.275.dp,
            color = LifeFlowNeuOutline,
            shape = shape
        )
}
