package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LifeFlowMarkerText = Color(0xFF6C788A)
private val LifeFlowMarkerDotColor = Color(0xFF22CDF7).copy(alpha = 0.74f)
private val LifeFlowMarkerSurfaceStart = Color(0xFFFFFFFF).copy(alpha = 0.72f)
private val LifeFlowMarkerSurfaceMid = Color(0xFFEFF9FD).copy(alpha = 0.80f)

@Composable
internal fun LifeFlowMarkerPill(
    markers: List<String>,
    modifier: Modifier = Modifier
) {
    if (markers.isEmpty()) return

    Row(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        LifeFlowMarkerSurfaceStart,
                        LifeFlowMarkerSurfaceMid,
                        LifeFlowMarkerSurfaceStart
                    )
                ),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        markers.forEachIndexed { index, marker ->
            LifeFlowMarkerLabel(marker)
            if (index < markers.lastIndex) {
                LifeFlowMarkerSeparator()
            }
        }
    }
}

@Composable
private fun LifeFlowMarkerLabel(
    text: String
) {
    Text(
        text = text,
        color = LifeFlowMarkerText,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Medium
        )
    )
}

@Composable
private fun LifeFlowMarkerSeparator() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                color = LifeFlowMarkerDotColor,
                shape = CircleShape
            )
    )
}
