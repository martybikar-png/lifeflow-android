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
    Secondary
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

internal val LifeFlowButtonIdleSurface = Color(0xFFFFFFFF)
internal val LifeFlowButtonHoverSurface = Color(0xFFFFFFFF)
internal val LifeFlowButtonPressedSurface = Color(0xFFF8FCFF)
internal val LifeFlowButtonDisabledSurface = Color(0xFFF7F9FC)

private val LifeFlowButtonPressedSurfaceBottom = Color(0xFFFFFFFF)

internal val LifeFlowButtonLiftLight = Color(0xFFFFFFFF).copy(alpha = 0.98f)
internal val LifeFlowButtonLiftDark = Color(0xFF5DBAE7).copy(alpha = 0.34f)
internal val LifeFlowButtonHoverDark = Color(0xFF4FAFE2).copy(alpha = 0.38f)
internal val LifeFlowButtonDisabledDark = Color(0xFFB8C7D6).copy(alpha = 0.18f)

private val LifeFlowButtonInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.84f)
private val LifeFlowButtonInnerShade = Color(0xFFDDEAF5).copy(alpha = 0.24f)

private val LifeFlowButtonPressedInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.82f)
private val LifeFlowButtonPressedInnerShade = Color(0xFF9DDEEF).copy(alpha = 0.36f)

internal val LifeFlowButtonIdleBorder = Color(0xFFFFFFFF).copy(alpha = 0.72f)
internal val LifeFlowButtonHoverBorder = LifeFlowButtonAccent.copy(alpha = 0.22f)
internal val LifeFlowButtonPressedBorder = LifeFlowButtonAccent.copy(alpha = 0.26f)
internal val LifeFlowButtonDisabledBorder = Color(0xFFFFFFFF).copy(alpha = 0.48f)

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
                radius = 22.dp,
                spread = 1.dp,
                color = LifeFlowButtonLiftLight,
                offset = DpOffset(x = (-7).dp, y = (-8).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 22.dp,
                spread = 1.dp,
                color = darkShadowColor,
                offset = DpOffset(x = 8.dp, y = 10.dp)
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
                radius = 10.dp,
                spread = 0.dp,
                color = LifeFlowButtonInnerShade,
                offset = DpOffset(x = 2.dp, y = 3.dp)
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
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftDark,
                offset = DpOffset(x = 3.dp, y = 4.dp)
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
                radius = 14.dp,
                spread = 0.dp,
                color = LifeFlowButtonPressedInnerShade,
                offset = DpOffset(x = 4.dp, y = 5.dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
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
