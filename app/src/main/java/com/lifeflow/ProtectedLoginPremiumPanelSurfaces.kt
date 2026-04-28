package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

internal fun Modifier.premiumLoginRaisedSurface(
    shape: Shape,
    surfaceColor: Color = PremiumLoginWhite,
    darkAlpha: Float = 0.22f
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 24.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.86f),
                offset = DpOffset(x = (-8).dp, y = (-10).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 24.dp,
                spread = 0.dp,
                color = Color(0xFF8FA0B8).copy(alpha = darkAlpha),
                offset = DpOffset(x = 8.dp, y = 12.dp)
            )
        )
        .background(
            color = surfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.82f),
                offset = DpOffset(x = 0.dp, y = (-7).dp)
            )
        )
        .border(
            width = 0.7.dp,
            color = Color.White.copy(alpha = 0.48f),
            shape = shape
        )
}

internal fun Modifier.premiumLoginBodyCardSurface(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 112.dp,
                spread = 0.dp,
                color = Color(0xFF0A75E8).copy(alpha = 0.18f),
                offset = DpOffset(x = 0.dp, y = (-40).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 84.dp,
                spread = 0.dp,
                color = Color(0xFF075FE0).copy(alpha = 0.24f),
                offset = DpOffset(x = 0.dp, y = (-28).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 58.dp,
                spread = 0.dp,
                color = Color(0xFF0646B9).copy(alpha = 0.22f),
                offset = DpOffset(x = 0.dp, y = (-18).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 32.dp,
                spread = 0.dp,
                color = Color(0xFF22CDF7).copy(alpha = 0.10f),
                offset = DpOffset(x = 0.dp, y = (-8).dp)
            )
        )
        .background(
            color = PremiumLoginWhite,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.84f),
                offset = DpOffset(x = 0.dp, y = (-8).dp)
            )
        )
        .border(
            width = 0.8.dp,
            color = Color.White.copy(alpha = 0.58f),
            shape = shape
        )
}

internal fun Modifier.premiumLoginMethodButtonSurface(
    shape: Shape,
    surfaceColor: Color = PremiumLoginWhite,
    isPressed: Boolean = false
): Modifier {
    val blueShadowAlpha = if (isPressed) 0.18f else 0.34f
    val darkShadowAlpha = if (isPressed) 0.10f else 0.18f

    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 26.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.90f),
                offset = DpOffset(x = (-7).dp, y = (-9).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 30.dp,
                spread = 1.dp,
                color = Color(0xFF62C9F2).copy(alpha = blueShadowAlpha),
                offset = DpOffset(x = 0.dp, y = 13.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 18.dp,
                spread = 0.dp,
                color = Color(0xFF5D6E86).copy(alpha = darkShadowAlpha),
                offset = DpOffset(x = 7.dp, y = 9.dp)
            )
        )
        .background(
            color = surfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 6.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.72f),
                offset = DpOffset(x = (-1).dp, y = (-2).dp)
            )
        )
        .border(
            width = 0.7.dp,
            color = Color.White.copy(alpha = 0.78f),
            shape = shape
        )
}