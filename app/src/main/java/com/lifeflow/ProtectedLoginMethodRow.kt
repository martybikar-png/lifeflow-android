package com.lifeflow

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PremiumLoginMethodRow(
    title: String,
    subtitle: String,
    iconResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumLoginButtonCard(
        title = title,
        subtitle = subtitle,
        iconResId = iconResId,
        selected = selected,
        enabled = true,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
internal fun PremiumLoginEnterButton(
    label: String,
    subtitle: String,
    iconResId: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumLoginButtonCard(
        title = label,
        subtitle = subtitle,
        iconResId = iconResId,
        selected = false,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun PremiumLoginButtonCard(
    title: String,
    subtitle: String,
    iconResId: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActive = isPressed || selected

    val titleColor = when {
        !enabled -> PremiumLoginTextSecondary
        isActive -> PremiumLoginLink
        else -> PremiumLoginTextPrimary
    }

    val iconTint = when {
        !enabled -> PremiumLoginTextSecondary
        else -> PremiumLoginLink
    }

    val methodSurface = if (selected) {
        PremiumLoginMethodSelectedSurface
    } else {
        PremiumLoginWhite
    }

    val iconSurface = if (selected) {
        PremiumLoginMethodIconSelectedSurface
    } else {
        PremiumLoginWhite
    }

    val borderColor = if (isActive) {
        PremiumLoginMethodSelectedBorder.copy(alpha = 0.92f)
    } else {
        PremiumLoginMethodIdleBorder
    }

    Box(
        modifier = modifier
            .height(PremiumLoginMethodButtonHeight)
            .premiumLoginMethodButtonSurface(
                shape = PremiumLoginRowShape,
                surfaceColor = methodSurface,
                isPressed = isPressed
            )
            .border(
                width = if (isActive) 0.8.dp else 0.275.dp,
                color = borderColor,
                shape = PremiumLoginRowShape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(PremiumLoginRowShape)
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(iconSurface)
                    .premiumLoginMethodButtonSurface(
                        shape = CircleShape,
                        surfaceColor = iconSurface,
                        isPressed = isPressed
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    colorFilter = ColorFilter.tint(iconTint)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = titleColor,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = PremiumLoginTextSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 8.sp,
                        lineHeight = 10.sp
                    )
                )
            }
        }
    }
}
