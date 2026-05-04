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

private val PremiumLoginShadowLight = Color(0xFFFFFFFF)
private val PremiumLoginShadowDark = Color(0xFFD2D6DF).copy(alpha = 0.72f)
private val PremiumLoginGoldBorder = Color(0xFFE8D9AA).copy(alpha = 0.92f)
private val PremiumLoginInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.94f)
private val PremiumLoginInnerShade = Color(0xFFD2D6DF).copy(alpha = 0.18f)

internal fun Modifier.premiumLoginRaisedSurface(
    shape: Shape,
    surfaceColor: Color = PremiumLoginWhite,
    darkAlpha: Float = 0.72f
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
                spread = 0.dp,
                color = PremiumLoginShadowLight,
                offset = DpOffset(x = (-6).dp, y = (-6).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
                spread = 0.dp,
                color = PremiumLoginShadowDark.copy(alpha = darkAlpha),
                offset = DpOffset(x = 6.dp, y = 6.dp)
            )
        )
        .background(
            color = surfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 4.dp,
                spread = 0.dp,
                color = PremiumLoginInnerHighlight,
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 4.dp,
                spread = 0.dp,
                color = PremiumLoginInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.275.dp,
            color = PremiumLoginGoldBorder,
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
                radius = 18.dp,
                spread = 0.dp,
                color = PremiumLoginShadowLight,
                offset = DpOffset(x = (-8).dp, y = (-8).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 16.dp,
                spread = 0.dp,
                color = PremiumLoginShadowDark,
                offset = DpOffset(x = 8.dp, y = 8.dp)
            )
        )
        .background(
            color = PremiumLoginWhite,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 4.dp,
                spread = 0.dp,
                color = PremiumLoginInnerHighlight,
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 4.dp,
                spread = 0.dp,
                color = PremiumLoginInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.275.dp,
            color = PremiumLoginGoldBorder,
            shape = shape
        )
}

internal fun Modifier.premiumLoginMethodButtonSurface(
    shape: Shape,
    surfaceColor: Color = PremiumLoginWhite,
    isPressed: Boolean = false
): Modifier {
    val lightRadius = if (isPressed) 8.dp else 15.dp
    val darkRadius = if (isPressed) 8.dp else 15.dp
    val lightOffset = if (isPressed) (-3).dp else (-6).dp
    val darkOffset = if (isPressed) 3.dp else 6.dp
    val surface = if (isPressed) PremiumLoginRowPressedSurface else surfaceColor

    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = lightRadius,
                spread = 0.dp,
                color = PremiumLoginShadowLight,
                offset = DpOffset(x = lightOffset, y = lightOffset)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = darkRadius,
                spread = 0.dp,
                color = PremiumLoginShadowDark,
                offset = DpOffset(x = darkOffset, y = darkOffset)
            )
        )
        .background(
            color = surface,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = PremiumLoginInnerHighlight,
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = PremiumLoginInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.275.dp,
            color = PremiumLoginGoldBorder,
            shape = shape
        )
}
