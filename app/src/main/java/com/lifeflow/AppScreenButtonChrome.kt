package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class LifeFlowButtonVariant {
    Primary,
    Secondary,
    OnBlue
}

internal val LifeFlowButtonShape = RoundedCornerShape(24.dp)
internal val LifeFlowButtonMinHeight = 42.dp
internal val LifeFlowButtonMaxWidth = 260.dp
internal val LifeFlowButtonOuterHorizontalPadding = 0.dp
internal val LifeFlowButtonOuterVerticalPadding = 4.dp
internal val LifeFlowButtonHorizontalPadding = 18.dp
internal val LifeFlowButtonVerticalPadding = 8.dp
internal val LifeFlowButtonIconSize = 14.dp

internal val LifeFlowButtonAccent = Color(0xFF22CDF7)

internal val LifeFlowButtonIdleSurface = Color(0xFFF2F3F7)
internal val LifeFlowButtonHoverSurface = Color(0xFFF2F3F7)
internal val LifeFlowButtonPressedSurface = Color(0xFFECEEF3)
internal val LifeFlowButtonDisabledSurface = Color(0xFFEAF4F3)

private val LifeFlowButtonPressedSurfaceBottom = Color(0xFFF2F3F7)

internal val LifeFlowButtonLiftLight = Color(0xFFFFFFFF)
internal val LifeFlowButtonLiftDark = Color(0xFFD2D6DF).copy(alpha = 0.72f)
internal val LifeFlowButtonHoverDark = Color(0xFFD2D6DF).copy(alpha = 0.82f)

private val LifeFlowButtonOnBlueLight = Color(0xFFFFFFFF).copy(alpha = 0.96f)
private val LifeFlowButtonOnBlueGlow = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val LifeFlowButtonOnBlueDepth = Color(0xFFD2D6DF).copy(alpha = 0.42f)
internal val LifeFlowButtonDisabledDark = Color(0xFFD2D6DF).copy(alpha = 0.28f)

private val LifeFlowButtonInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.94f)
private val LifeFlowButtonInnerShade = Color(0xFFD2D6DF).copy(alpha = 0.18f)

private val LifeFlowButtonPressedInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.82f)
private val LifeFlowButtonPressedInnerShade = Color(0xFFD2D6DF).copy(alpha = 0.34f)

internal val LifeFlowButtonIdleBorder = Color(0xFFE8D9AA).copy(alpha = 0.92f)
internal val LifeFlowButtonHoverBorder = Color(0xFFE1CD8D).copy(alpha = 0.96f)
internal val LifeFlowButtonPressedBorder = Color(0xFFD4B66A).copy(alpha = 0.98f)
internal val LifeFlowButtonDisabledBorder = Color(0xFFF2E8C9).copy(alpha = 0.82f)

internal val LifeFlowButtonPrimaryText = Color(0xFF35415C)
internal val LifeFlowButtonSecondaryText = Color(0xFF35415C)
internal val LifeFlowButtonInteractiveText = LifeFlowButtonAccent
internal val LifeFlowButtonDisabledText = Color(0xFF94A0AE)

@Composable
internal fun lifeFlowButtonTextStyle(): TextStyle {
    return MaterialTheme.typography.labelLarge.copy(
        fontSize = 12.sp,
        lineHeight = 15.sp
    )
}

internal fun lifeFlowButtonIconTint(
    enabled: Boolean
): Color {
    return if (enabled) {
        LifeFlowButtonAccent
    } else {
        LifeFlowButtonDisabledText
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
                radius = 15.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftLight,
                offset = DpOffset(x = (-6).dp, y = (-6).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 15.dp,
                spread = 0.dp,
                color = darkShadowColor,
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
                radius = 7.dp,
                spread = 0.dp,
                color = LifeFlowButtonInnerHighlight,
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = LifeFlowButtonInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.275.dp,
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
                radius = 8.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftLight,
                offset = DpOffset(x = (-3).dp, y = (-3).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftDark,
                offset = DpOffset(x = 3.dp, y = 3.dp)
            )
        )
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    surfaceColor,
                    LifeFlowButtonPressedSurfaceBottom
                )
            ),
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 9.dp,
                spread = 0.dp,
                color = LifeFlowButtonPressedInnerShade,
                offset = DpOffset(x = 3.dp, y = 3.dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 9.dp,
                spread = 0.dp,
                color = LifeFlowButtonPressedInnerHighlight,
                offset = DpOffset(x = (-3).dp, y = (-3).dp)
            )
        )
        .border(
            width = 0.275.dp,
            color = borderColor,
            shape = shape
        )
}

internal fun Modifier.lifeFlowRaisedOnBlueButtonChrome(
    shape: RoundedCornerShape,
    surfaceColor: Color,
    borderColor: Color
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 26.dp,
                spread = 0.dp,
                color = LifeFlowButtonOnBlueLight,
                offset = DpOffset(x = (-8).dp, y = (-10).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 24.dp,
                spread = 0.dp,
                color = LifeFlowButtonOnBlueGlow,
                offset = DpOffset(x = 9.dp, y = 12.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 14.dp,
                spread = 0.dp,
                color = LifeFlowButtonOnBlueDepth,
                offset = DpOffset(x = 13.dp, y = 16.dp)
            )
        )
        .background(
            color = surfaceColor,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.78f),
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .border(
            width = 0.275.dp,
            color = borderColor,
            shape = shape
        )
}
