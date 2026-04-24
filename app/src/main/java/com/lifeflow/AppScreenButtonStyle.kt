package com.lifeflow

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

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