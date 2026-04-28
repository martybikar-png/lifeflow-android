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

internal fun Modifier.premiumLoginFloatingPhotoSurface(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 12.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.28f),
                offset = DpOffset(x = 0.dp, y = (-5).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.14f),
                offset = DpOffset(x = 0.dp, y = (-2).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 14.dp,
                spread = 0.dp,
                color = Color(0xFF0F75E8).copy(alpha = 0.08f),
                offset = DpOffset(x = 0.dp, y = 6.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = Color(0xFF22CDF7).copy(alpha = 0.05f),
                offset = DpOffset(x = 0.dp, y = 2.dp)
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
                color = Color.White.copy(alpha = 0.88f),
                offset = DpOffset(x = (-3).dp, y = (-5).dp)
            )
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 14.dp,
                spread = 0.dp,
                color = Color(0xFFC7D4E7).copy(alpha = 0.24f),
                offset = DpOffset(x = 4.dp, y = 6.dp)
            )
        )
        .border(
            width = 0.8.dp,
            color = Color.White.copy(alpha = 0.62f),
            shape = shape
        )
}

internal fun Modifier.premiumLoginAddButtonSurface(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 7.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.24f),
                offset = DpOffset(x = 0.dp, y = (-3).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 5.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.12f),
                offset = DpOffset(x = 0.dp, y = (-1).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 9.dp,
                spread = 0.dp,
                color = Color(0xFF0F75E8).copy(alpha = 0.07f),
                offset = DpOffset(x = 0.dp, y = 4.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 5.dp,
                spread = 0.dp,
                color = Color(0xFF22CDF7).copy(alpha = 0.04f),
                offset = DpOffset(x = 0.dp, y = 1.dp)
            )
        )
        .background(
            color = PremiumLoginWhite,
            shape = shape
        )
        .innerShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.78f),
                offset = DpOffset(x = (-2).dp, y = (-3).dp)
            )
        )
        .border(
            width = 0.7.dp,
            color = Color.White.copy(alpha = 0.54f),
            shape = shape
        )
}

internal fun Modifier.premiumLoginPhotoBlueLevitationShadow(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 14.dp,
                spread = 0.dp,
                color = Color(0xFF0F75E8).copy(alpha = 0.08f),
                offset = DpOffset(x = 0.dp, y = 6.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 8.dp,
                spread = 0.dp,
                color = Color(0xFF22CDF7).copy(alpha = 0.05f),
                offset = DpOffset(x = 0.dp, y = 2.dp)
            )
        )
}

internal fun Modifier.premiumLoginPlusBlueLevitationShadow(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 9.dp,
                spread = 0.dp,
                color = Color(0xFF0F75E8).copy(alpha = 0.07f),
                offset = DpOffset(x = 0.dp, y = 4.dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 5.dp,
                spread = 0.dp,
                color = Color(0xFF22CDF7).copy(alpha = 0.04f),
                offset = DpOffset(x = 0.dp, y = 1.dp)
            )
        )
}

internal fun Modifier.premiumLoginPhotoWhiteTopLevitationShadow(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 28.dp,
                spread = 1.dp,
                color = Color.White.copy(alpha = 0.34f),
                offset = DpOffset(x = 0.dp, y = (-13).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 16.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.20f),
                offset = DpOffset(x = 0.dp, y = (-6).dp)
            )
        )
}

internal fun Modifier.premiumLoginPlusWhiteTopLevitationShadow(
    shape: Shape
): Modifier {
    return this
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 17.dp,
                spread = 1.dp,
                color = Color.White.copy(alpha = 0.30f),
                offset = DpOffset(x = 0.dp, y = (-8).dp)
            )
        )
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 9.dp,
                spread = 0.dp,
                color = Color.White.copy(alpha = 0.16f),
                offset = DpOffset(x = 0.dp, y = (-3).dp)
            )
        )
}