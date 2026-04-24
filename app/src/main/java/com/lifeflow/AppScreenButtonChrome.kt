package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class LifeFlowButtonVariant {
    Primary,
    Secondary
}

internal val LifeFlowButtonShape = RoundedCornerShape(14.dp)
internal val LifeFlowButtonMinHeight = 52.dp
internal val LifeFlowButtonOuterHorizontalPadding = 4.dp
internal val LifeFlowButtonOuterVerticalPadding = 4.dp
internal val LifeFlowButtonHorizontalPadding = 18.dp
internal val LifeFlowButtonVerticalPadding = 13.dp
internal val LifeFlowButtonIconSize = 16.dp

internal val LifeFlowButtonAccent = Color(0xFF22CDF7)

internal val LifeFlowButtonIdleSurface = LifeFlowNeuSurfaceColor
internal val LifeFlowButtonHoverSurface = Color(0xFFF7FBFF)
internal val LifeFlowButtonPressedSurface = Color(0xFFEAF7FD)
internal val LifeFlowButtonDisabledSurface = Color(0xFFF0F2F6)

internal val LifeFlowButtonLiftLight = Color(0xFFFFFFFF).copy(alpha = 0.96f)
internal val LifeFlowButtonLiftDark = Color(0xFF8994A3).copy(alpha = 0.38f)
internal val LifeFlowButtonHoverDark = Color(0xFF7E8998).copy(alpha = 0.42f)
internal val LifeFlowButtonDisabledDark = Color(0xFFBAC3CF).copy(alpha = 0.26f)

private val LifeFlowButtonInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.62f)
private val LifeFlowButtonInnerShade = Color(0xFFD8DEE7).copy(alpha = 0.40f)

private val LifeFlowButtonPressedInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.78f)
private val LifeFlowButtonPressedInnerShade = Color(0xFFC9D5E1).copy(alpha = 0.78f)

internal val LifeFlowButtonIdleBorder = Color(0xFF9EA8B5).copy(alpha = 0.20f)
internal val LifeFlowButtonHoverBorder = LifeFlowButtonAccent.copy(alpha = 0.28f)
internal val LifeFlowButtonPressedBorder = LifeFlowButtonAccent.copy(alpha = 0.34f)
internal val LifeFlowButtonDisabledBorder = Color(0xFFB9C3CF).copy(alpha = 0.18f)

internal val LifeFlowButtonPrimaryText = Color(0xFF171A20)
internal val LifeFlowButtonSecondaryText = Color(0xFF2D3641)
internal val LifeFlowButtonInteractiveText = LifeFlowButtonAccent
internal val LifeFlowButtonDisabledText = Color(0xFF8B94A1)

@Composable
internal fun lifeFlowButtonTextStyle(): TextStyle {
    return MaterialTheme.typography.labelLarge.copy(
        fontSize = 15.sp,
        lineHeight = 18.sp
    )
}

internal fun lifeFlowButtonIconTint(
    enabled: Boolean
): Color {
    return if (enabled) {
        LifeFlowButtonAccent
    } else {
        LifeFlowButtonAccent.copy(alpha = 0.58f)
    }
}

internal fun Modifier.lifeFlowRaisedButtonChrome(
    shape: RoundedCornerShape,
    surfaceColor: Color,
    borderColor: Color,
    darkShadowColor: Color
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 18.dp,
                spread = 1.dp,
                color = LifeFlowButtonLiftLight,
                offset = DpOffset(x = (-7).dp, y = (-7).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 18.dp,
                spread = 1.dp,
                color = darkShadowColor,
                offset = DpOffset(x = 7.dp, y = 7.dp)
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
                color = LifeFlowButtonInnerHighlight,
                offset = DpOffset(x = (-1).dp, y = (-1).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = LifeFlowButtonInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.7.dp,
            color = borderColor,
            shape = shape
        )
}

internal fun Modifier.lifeFlowPressedButtonChrome(
    shape: RoundedCornerShape,
    surfaceColor: Color,
    borderColor: Color
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftLight,
                offset = DpOffset(x = (-3).dp, y = (-3).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftDark,
                offset = DpOffset(x = 3.dp, y = 3.dp)
            )
        )
        .background(
            color = surfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
                spread = 0.dp,
                color = LifeFlowButtonPressedInnerShade,
                offset = DpOffset(x = 5.dp, y = 5.dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                spread = 0.dp,
                color = LifeFlowButtonPressedInnerHighlight,
                offset = DpOffset(x = (-4).dp, y = (-4).dp)
            )
        )
        .border(
            width = 0.8.dp,
            color = borderColor,
            shape = shape
        )
}