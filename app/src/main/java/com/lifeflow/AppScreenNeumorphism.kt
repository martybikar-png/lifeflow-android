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

private val LifeFlowNeuLightShadow = Color(0xFFFFFFFF).copy(alpha = 0.50f)
private val LifeFlowNeuDarkShadow = Color(0xFFA3B1C6).copy(alpha = 0.60f)
private val LifeFlowNeuOutline = Color(0xFFFFFFFF).copy(alpha = 0.20f)
private val LifeFlowNeuInnerHighlight = Color(0xFFFFFFFF)
private val LifeFlowNeuInnerShade = Color(0xFF88A5BF).copy(alpha = 0.48f)

internal fun Modifier.lifeFlowRaisedPanelChrome(
    shape: RoundedCornerShape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 16.dp,
                spread = 0.dp,
                color = LifeFlowNeuLightShadow,
                offset = DpOffset(x = (-9).dp, y = (-9).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 16.dp,
                spread = 0.dp,
                color = LifeFlowNeuDarkShadow,
                offset = DpOffset(x = 9.dp, y = 9.dp)
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
                color = LifeFlowNeuInnerShade,
                offset = DpOffset(x = 3.dp, y = 3.dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerHighlight,
                offset = DpOffset(x = (-3).dp, y = (-3).dp)
            )
        )
        .border(
            width = 0.8.dp,
            color = LifeFlowNeuOutline,
            shape = shape
        )
}
