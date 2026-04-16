package com.lifeflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppScreenPillShape = RoundedCornerShape(18.dp)
private val AppScreenPillText = androidx.compose.ui.graphics.Color(0xFF22CDF7)

@Composable
internal fun appScreenPillTextStyle(): TextStyle {
    return MaterialTheme.typography.labelLarge.copy(
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
}

@Composable
internal fun LifeFlowSignalPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .lifeFlowRaisedPanelChrome(AppScreenPillShape)
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Text(
                text = text,
                style = appScreenPillTextStyle(),
                color = AppScreenPillText
            )
        }
    }
}
