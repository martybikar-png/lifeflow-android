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
internal val LifeFlowButtonMinHeight = 46.dp
internal val LifeFlowButtonOuterHorizontalPadding = 52.dp
internal val LifeFlowButtonOuterVerticalPadding = 4.dp
internal val LifeFlowButtonHorizontalPadding = 18.dp
internal val LifeFlowButtonVerticalPadding = 10.dp
internal val LifeFlowButtonIconSize = 16.dp

internal val LifeFlowButtonAccent = Color(0xFF22CDF7)

internal val LifeFlowButtonIdleSurface = Color(0xFFE8E8E8)
internal val LifeFlowButtonHoverSurface = Color(0xFFF5FBFE)
internal val LifeFlowButtonPressedSurface = Color(0xFFA6EAFF)
internal val LifeFlowButtonDisabledSurface = Color(0xFFF0F2F6)

private val LifeFlowButtonPressedSurfaceBottom = Color(0xFFB8F0FF)

internal val LifeFlowButtonLiftLight = Color(0xFFFFFFFF).copy(alpha = 0.96f)
internal val LifeFlowButtonLiftDark = Color(0xFF737B86).copy(alpha = 0.42f)
internal val LifeFlowButtonHoverDark = Color(0xFF687482).copy(alpha = 0.44f)
internal val LifeFlowButtonDisabledDark = Color(0xFFBAC3CF).copy(alpha = 0.24f)

private val LifeFlowButtonInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.70f)
private val LifeFlowButtonInnerShade = Color(0xFFC9CDD3).copy(alpha = 0.42f)

private val LifeFlowButtonPressedInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val LifeFlowButtonPressedInnerShade = Color(0xFF77CBE4).copy(alpha = 0.58f)

internal val LifeFlowButtonIdleBorder = Color(0xFF7F8792).copy(alpha = 0.30f)
internal val LifeFlowButtonHoverBorder = LifeFlowButtonAccent.copy(alpha = 0.32f)
internal val LifeFlowButtonPressedBorder = LifeFlowButtonAccent.copy(alpha = 0.46f)
internal val LifeFlowButtonDisabledBorder = Color(0xFFB9C3CF).copy(alpha = 0.18f)

internal val LifeFlowButtonPrimaryText = Color(0xFF090909)
internal val LifeFlowButtonSecondaryText = Color(0xFF20242A)
internal val LifeFlowButtonInteractiveText = Color(0xFF090909)
internal val LifeFlowButtonDisabledText = Color(0xFF7F8792)

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
        LifeFlowButtonPrimaryText
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
                radius = 20.dp,
                spread = 1.dp,
                color = LifeFlowButtonLiftLight,
                offset = DpOffset(x = (-8).dp, y = (-8).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 20.dp,
                spread = 1.dp,
                color = darkShadowColor,
                offset = DpOffset(x = 8.dp, y = 8.dp)
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
                radius = 9.dp,
                spread = 0.dp,
                color = LifeFlowButtonInnerShade,
                offset = DpOffset(x = 2.dp, y = 2.dp)
            )
        )
        .border(
            width = 0.8.dp,
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
                offset = DpOffset(x = (-2).dp, y = (-2).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = LifeFlowButtonLiftDark,
                offset = DpOffset(x = 2.dp, y = 2.dp)
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
                offset = DpOffset(x = 5.dp, y = 5.dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 11.dp,
                spread = 0.dp,
                color = LifeFlowButtonPressedInnerHighlight,
                offset = DpOffset(x = (-4).dp, y = (-4).dp)
            )
        )
        .border(
            width = 0.9.dp,
            color = borderColor,
            shape = shape
        )
}
