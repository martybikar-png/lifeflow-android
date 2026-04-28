package com.lifeflow

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val PremiumLoginTopShape = RoundedCornerShape(0.dp)

internal val PremiumLoginBodyShape = RoundedCornerShape(
    topStart = 44.dp,
    topEnd = 44.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

internal val PremiumLoginRowShape = RoundedCornerShape(22.dp)

internal val PremiumLoginScreenBackground = Color(0xFFFFFFFF)
internal val PremiumLoginBlueTop = Color(0xFF22CDF7)
internal val PremiumLoginBlueBottom = Color(0xFF2F8FFF)
internal val PremiumLoginWhite = Color(0xFFFFFFFF)

internal const val PremiumLoginWhiteStartRatio = 0.25f
internal val PremiumLoginCenterCircleLift = 72.dp
internal val PremiumLoginRowPressedSurface = Color(0xFFFFFFFF)

internal val PremiumLoginTextPrimary = Color(0xFF35415C)
internal val PremiumLoginTextSecondary = Color(0xFF8792AA)
internal val PremiumLoginLink = Color(0xFF22CDF7)

internal val PremiumLoginGoldDividerBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFE4B94D),
        Color(0xFFFFE9A8),
        Color(0xFFFFF6C8),
        Color(0xFFD49A2E),
        Color(0xFFE4B94D)
    )
)

internal val PremiumLoginGoldCircleBrush = Brush.sweepGradient(
    colors = listOf(
        Color(0xFFFFF0A8),
        Color(0xFFD49A2E),
        Color(0xFFFFF6C8),
        Color(0xFFE4B94D),
        Color(0xFFFFF0A8)
    )
)

internal enum class LoginMethod {
    FACE_ID,
    IRIS_ID,
    FINGERPRINT,
    DEVICE_BOUND
}