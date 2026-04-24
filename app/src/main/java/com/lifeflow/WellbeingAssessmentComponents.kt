package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun WellbeingSelectorChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(78.dp)
            .height(34.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .lifeFlowRaisedPanelChrome(WellbeingSelectorShape)
                .background(
                    color = if (selected) Color.White.copy(alpha = 0.62f) else Color.Transparent,
                    shape = WellbeingSelectorShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                ),
                color = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
internal fun WellbeingLegendPanel(
    selectedMode: WellbeingViewMode
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .lifeFlowRaisedPanelChrome(WellbeingLegendPanelShape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WellbeingLegendItem(
                label = "Overall",
                color = WellbeingGraphCyan,
                isActive = selectedMode == WellbeingViewMode.Overall
            )
            WellbeingLegendItem(
                label = "Activity",
                color = WellbeingGraphLavender,
                isActive = selectedMode == WellbeingViewMode.Activity
            )
            WellbeingLegendItem(
                label = "Heart",
                color = WellbeingGraphPeach,
                isActive = selectedMode == WellbeingViewMode.Heart
            )
        }
    }
}

@Composable
private fun WellbeingLegendItem(
    label: String,
    color: Color,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 6.dp)
                .background(
                    color = color.copy(alpha = if (isActive) 1f else 0.55f),
                    shape = WellbeingLegendDotShape
                )
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.sp,
                lineHeight = 13.sp
            ),
            color = if (isActive) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
internal fun WellbeingMetricCard(
    metric: WellbeingMetric,
    width: Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(62.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .lifeFlowRaisedPanelChrome(WellbeingMetricShape)
                .padding(horizontal = 10.dp, vertical = 10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = metric.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}