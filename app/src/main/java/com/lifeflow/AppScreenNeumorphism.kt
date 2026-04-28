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

internal val LifeFlowNeuSurfaceColor = Color(0xFFFFFFFF)

private val LifeFlowNeuLightShadow = Color(0xFFFFFFFF).copy(alpha = 0.92f)
private val LifeFlowNeuBlueGlow = Color(0xFF62C9F2).copy(alpha = 0.34f)
private val LifeFlowNeuBlueDepth = Color(0xFF5DBAE7).copy(alpha = 0.20f)
private val LifeFlowNeuOutline = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val LifeFlowNeuInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.88f)
private val LifeFlowNeuInnerBlueShade = Color(0xFFDDEAF5).copy(alpha = 0.30f)

internal fun Modifier.lifeFlowRaisedPanelChrome(
    shape: RoundedCornerShape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 28.dp,
                spread = 0.dp,
                color = LifeFlowNeuLightShadow,
                offset = DpOffset(x = (-8).dp, y = (-10).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 34.dp,
                spread = 2.dp,
                color = LifeFlowNeuBlueGlow,
                offset = DpOffset(x = 0.dp, y = 16.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 24.dp,
                spread = 0.dp,
                color = LifeFlowNeuBlueDepth,
                offset = DpOffset(x = 8.dp, y = 12.dp)
            )
        )
        .background(
            color = LifeFlowNeuSurfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerHighlight,
                offset = DpOffset(x = (-3).dp, y = (-4).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
                spread = 0.dp,
                color = LifeFlowNeuInnerBlueShade,
                offset = DpOffset(x = 4.dp, y = 5.dp)
            )
        )
        .border(
            width = 0.8.dp,
            color = LifeFlowNeuOutline,
            shape = shape
        )
}
