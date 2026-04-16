package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private enum class LifeFlowButtonVariant {
    Primary,
    Secondary
}

private val LifeFlowButtonShape = RoundedCornerShape(14.dp)
private val LifeFlowButtonMinHeight = 52.dp
private val LifeFlowButtonOuterHorizontalPadding = 4.dp
private val LifeFlowButtonOuterVerticalPadding = 4.dp
private val LifeFlowButtonHorizontalPadding = 18.dp
private val LifeFlowButtonVerticalPadding = 13.dp
private val LifeFlowButtonIconSize = 16.dp

private val LifeFlowButtonAccent = Color(0xFF22CDF7)

private val LifeFlowButtonIdleSurface = LifeFlowNeuSurfaceColor
private val LifeFlowButtonHoverSurface = Color(0xFFF7FBFF)
private val LifeFlowButtonPressedSurface = Color(0xFFEAF7FD)
private val LifeFlowButtonDisabledSurface = Color(0xFFF0F2F6)

private val LifeFlowButtonLiftLight = Color(0xFFFFFFFF).copy(alpha = 0.96f)
private val LifeFlowButtonLiftDark = Color(0xFF8994A3).copy(alpha = 0.38f)
private val LifeFlowButtonHoverDark = Color(0xFF7E8998).copy(alpha = 0.42f)
private val LifeFlowButtonDisabledDark = Color(0xFFBAC3CF).copy(alpha = 0.26f)

private val LifeFlowButtonInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.62f)
private val LifeFlowButtonInnerShade = Color(0xFFD8DEE7).copy(alpha = 0.40f)

private val LifeFlowButtonPressedInnerHighlight = Color(0xFFFFFFFF).copy(alpha = 0.78f)
private val LifeFlowButtonPressedInnerShade = Color(0xFFC9D5E1).copy(alpha = 0.78f)

private val LifeFlowButtonIdleBorder = Color(0xFF9EA8B5).copy(alpha = 0.20f)
private val LifeFlowButtonHoverBorder = LifeFlowButtonAccent.copy(alpha = 0.28f)
private val LifeFlowButtonPressedBorder = LifeFlowButtonAccent.copy(alpha = 0.34f)
private val LifeFlowButtonDisabledBorder = Color(0xFFB9C3CF).copy(alpha = 0.18f)

private val LifeFlowButtonPrimaryText = Color(0xFF171A20)
private val LifeFlowButtonSecondaryText = Color(0xFF2D3641)
private val LifeFlowButtonInteractiveText = LifeFlowButtonAccent
private val LifeFlowButtonDisabledText = Color(0xFF8B94A1)

@Composable
internal fun lifeFlowButtonTextStyle(): TextStyle {
    return MaterialTheme.typography.labelLarge.copy(
        fontSize = 15.sp,
        lineHeight = 18.sp
    )
}

private fun lifeFlowButtonIconTint(
    enabled: Boolean
): Color {
    return if (enabled) {
        LifeFlowButtonAccent
    } else {
        LifeFlowButtonAccent.copy(alpha = 0.58f)
    }
}

private fun Modifier.lifeFlowRaisedButtonChrome(
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

private fun Modifier.lifeFlowPressedButtonChrome(
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

@Composable
internal fun LifeFlowPrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconResId: Int? = null
) {
    LifeFlowSoftActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        iconResId = iconResId,
        variant = LifeFlowButtonVariant.Primary
    )
}

@Composable
internal fun LifeFlowSecondaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconResId: Int? = null
) {
    LifeFlowSoftActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        iconResId = iconResId,
        variant = LifeFlowButtonVariant.Secondary
    )
}

@Composable
private fun LifeFlowSoftActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    iconResId: Int?,
    variant: LifeFlowButtonVariant
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val surfaceColor = when {
        !enabled -> LifeFlowButtonDisabledSurface
        isPressed -> LifeFlowButtonPressedSurface
        isHovered -> LifeFlowButtonHoverSurface
        else -> LifeFlowButtonIdleSurface
    }

    val borderColor = when {
        !enabled -> LifeFlowButtonDisabledBorder
        isPressed -> LifeFlowButtonPressedBorder
        isHovered -> LifeFlowButtonHoverBorder
        else -> LifeFlowButtonIdleBorder
    }

    val contentColor = when {
        !enabled -> LifeFlowButtonDisabledText
        isPressed || isHovered -> LifeFlowButtonInteractiveText
        variant == LifeFlowButtonVariant.Primary -> LifeFlowButtonPrimaryText
        else -> LifeFlowButtonSecondaryText
    }

    val chromeModifier = when {
        !enabled -> Modifier.lifeFlowRaisedButtonChrome(
            shape = LifeFlowButtonShape,
            surfaceColor = surfaceColor,
            borderColor = borderColor,
            darkShadowColor = LifeFlowButtonDisabledDark
        )

        isPressed -> Modifier.lifeFlowPressedButtonChrome(
            shape = LifeFlowButtonShape,
            surfaceColor = surfaceColor,
            borderColor = borderColor
        )

        isHovered -> Modifier.lifeFlowRaisedButtonChrome(
            shape = LifeFlowButtonShape,
            surfaceColor = surfaceColor,
            borderColor = borderColor,
            darkShadowColor = LifeFlowButtonHoverDark
        )

        else -> Modifier.lifeFlowRaisedButtonChrome(
            shape = LifeFlowButtonShape,
            surfaceColor = surfaceColor,
            borderColor = borderColor,
            darkShadowColor = LifeFlowButtonLiftDark
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = LifeFlowButtonOuterHorizontalPadding,
                vertical = LifeFlowButtonOuterVerticalPadding
            )
            .hoverable(
                enabled = enabled,
                interactionSource = interactionSource
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = chromeModifier
                .fillMaxWidth()
                .heightIn(min = LifeFlowButtonMinHeight)
                .padding(
                    horizontal = LifeFlowButtonHorizontalPadding,
                    vertical = LifeFlowButtonVerticalPadding
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconResId != null) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(LifeFlowButtonIconSize),
                    colorFilter = ColorFilter.tint(lifeFlowButtonIconTint(enabled))
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = label,
                style = lifeFlowButtonTextStyle(),
                color = contentColor
            )
        }
    }
}
