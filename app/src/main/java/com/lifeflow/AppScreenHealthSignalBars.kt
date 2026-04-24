package com.lifeflow

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.core.HealthConnectUiState

private data class HealthBarMetric(
    val label: String,
    val value: Float,
    val brush: Brush
)

private val HealthBarShape = RoundedCornerShape(20.dp)
private val HealthBarFillShape = RoundedCornerShape(14.dp)

private val HealthCyan = Color(0xFF22CDF7)
private val HealthLavender = Color(0xFF9EA7FF)
private val HealthPeach = Color(0xFFF2B7A6)
private val HealthSlot = Color(0xFFF4F6FA)

@Composable
internal fun HealthSignalBars(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        healthBarMetrics(
            healthState = healthState,
            requiredCount = requiredCount,
            grantedCount = grantedCount,
            stepsGranted = stepsGranted,
            hrGranted = hrGranted
        ).forEach { metric ->
            HealthSignalBar(metric = metric)
        }
    }
}

@Composable
private fun HealthSignalBar(
    metric: HealthBarMetric
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(132.dp)
                .lifeFlowRaisedPanelChrome(HealthBarShape)
                .padding(horizontal = 6.dp, vertical = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = HealthSlot,
                        shape = HealthBarFillShape
                    )
            )

            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(96.dp * metric.value.coerceIn(0.12f, 1f))
                    .background(
                        brush = metric.brush,
                        shape = HealthBarFillShape
                    )
            )
        }

        Text(
            text = metric.label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.sp,
                lineHeight = 13.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun healthBarMetrics(
    healthState: HealthConnectUiState,
    requiredCount: Int,
    grantedCount: Int,
    stepsGranted: Boolean,
    hrGranted: Boolean
): List<HealthBarMetric> {
    return listOf(
        HealthBarMetric(
            label = "State",
            value = healthStateRatio(healthState),
            brush = Brush.verticalGradient(
                colors = listOf(HealthLavender, HealthCyan)
            )
        ),
        HealthBarMetric(
            label = "Access",
            value = permissionRatio(requiredCount, grantedCount),
            brush = Brush.verticalGradient(
                colors = listOf(HealthCyan, HealthLavender)
            )
        ),
        HealthBarMetric(
            label = "Signals",
            value = signalRatio(stepsGranted, hrGranted),
            brush = Brush.verticalGradient(
                colors = listOf(HealthPeach, HealthCyan)
            )
        )
    )
}

private fun healthStateRatio(
    healthState: HealthConnectUiState
): Float {
    return when (healthState) {
        HealthConnectUiState.Available -> 0.92f
        HealthConnectUiState.UpdateRequired -> 0.42f
        HealthConnectUiState.NotInstalled -> 0.18f
        else -> 0.26f
    }
}

private fun permissionRatio(
    requiredCount: Int,
    grantedCount: Int
): Float {
    if (requiredCount <= 0) return 0.18f
    return (grantedCount.toFloat() / requiredCount.toFloat()).coerceIn(0f, 1f)
}

private fun signalRatio(
    stepsGranted: Boolean,
    hrGranted: Boolean
): Float {
    val count = listOf(stepsGranted, hrGranted).count { it }
    return when (count) {
        2 -> 0.92f
        1 -> 0.52f
        else -> 0.18f
    }
}