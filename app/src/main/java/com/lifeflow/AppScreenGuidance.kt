package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GuidanceTitlePillShape = RoundedCornerShape(18.dp)
private val GuidanceTitlePillSurface = Color(0xFFF1F3F6)
private val GuidanceTitlePillBorder = Color(0xFFFFFFFF)

@Composable
internal fun GuidanceCard(
    title: String,
    message: String,
    leadingIconResId: Int? = null
) {
    LifeFlowCardShell(
        title = "",
        summary = null
    ) {
        AppScreenTitlePill(text = title)

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun AppScreenTitlePill(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
            fontSize = 11.sp,
            lineHeight = 14.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .background(
                color = GuidanceTitlePillSurface,
                shape = GuidanceTitlePillShape
            )
            .border(
                width = 1.25.dp,
                color = GuidanceTitlePillBorder,
                shape = GuidanceTitlePillShape
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}
