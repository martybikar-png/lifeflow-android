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
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState

private data class DigitalTwinBarMetric(
    val label: String,
    val value: Float,
    val brush: Brush
)

private val DigitalTwinBarShape = RoundedCornerShape(20.dp)
private val DigitalTwinBarFillShape = RoundedCornerShape(14.dp)

private val DigitalTwinCyan = Color(0xFF22CDF7)
private val DigitalTwinLavender = Color(0xFF9EA7FF)
private val DigitalTwinPeach = Color(0xFFF2B7A6)
private val DigitalTwinSlot = Color(0xFFF4F6FA)

@Composable
internal fun DigitalTwinCard(
    digitalTwinState: DigitalTwinState?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LifeFlowSignalPill(text = "Digital twin view")
        }

        LifeFlowSectionPanel(title = "Digital Twin") {
            Text(
                text = digitalTwinShortSummary(digitalTwinState),
                style = lifeFlowCardSummaryStyle(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LifeFlowSectionPanel(title = "Twin state") {
            KeyValueStatusLine(
                label = "Snapshot",
                value = digitalTwinReadinessLabel(digitalTwinState),
                valueColor = digitalTwinReadinessColor(digitalTwinState)
            )
            KeyValueStatusLine(
                label = "Signals",
                value = digitalTwinSignalCoverageLabel(digitalTwinState),
                valueColor = digitalTwinSignalCoverageColor(digitalTwinState)
            )
            KeyValueStatusLine(
                label = "Next",
                value = digitalTwinNextMoveLabel(digitalTwinState),
                valueColor = digitalTwinNextMoveColor(digitalTwinState)
            )
        }

        LifeFlowSectionPanel(title = "Signal bars") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                digitalTwinBarMetrics(digitalTwinState).forEach { metric ->
                    DigitalTwinSignalBar(metric = metric)
                }
            }
        }

        LifeFlowSectionPanel(title = "Twin details") {
            if (digitalTwinState == null) {
                Text(
                    text = "No snapshot yet.",
                    style = lifeFlowCardSummaryStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                KeyValueLine(
                    "Updated",
                    formatLastUpdated(digitalTwinState.lastUpdatedEpochMillis)
                )
                KeyValueLine(
                    "Notes",
                    digitalTwinState.notes.size.toString()
                )
            }
        }
    }
}

@Composable
private fun DigitalTwinSignalBar(
    metric: DigitalTwinBarMetric
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(30.dp)
                .height(132.dp)
                .lifeFlowRaisedPanelChrome(DigitalTwinBarShape)
                .padding(horizontal = 9.dp, vertical = 10.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        color = DigitalTwinSlot,
                        shape = DigitalTwinBarFillShape
                    )
            )

            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height((96.dp * metric.value.coerceIn(0.12f, 1f)))
                    .background(
                        brush = metric.brush,
                        shape = DigitalTwinBarFillShape
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

private fun digitalTwinBarMetrics(
    digitalTwinState: DigitalTwinState?
): List<DigitalTwinBarMetric> {
    return listOf(
        DigitalTwinBarMetric(
            label = "Snapshot",
            value = digitalTwinSnapshotRatio(digitalTwinState),
            brush = Brush.verticalGradient(
                colors = listOf(DigitalTwinLavender, DigitalTwinCyan)
            )
        ),
        DigitalTwinBarMetric(
            label = "Signals",
            value = digitalTwinSignalRatio(digitalTwinState),
            brush = Brush.verticalGradient(
                colors = listOf(DigitalTwinCyan, DigitalTwinLavender)
            )
        ),
        DigitalTwinBarMetric(
            label = "Freshness",
            value = digitalTwinFreshnessRatio(digitalTwinState),
            brush = Brush.verticalGradient(
                colors = listOf(DigitalTwinPeach, DigitalTwinCyan)
            )
        )
    )
}

private fun digitalTwinShortSummary(
    digitalTwinState: DigitalTwinState?
): String {
    return if (digitalTwinState == null) {
        "No snapshot yet."
    } else {
        "Twin snapshot is available."
    }
}

private fun digitalTwinSnapshotRatio(
    digitalTwinState: DigitalTwinState?
): Float {
    if (digitalTwinState == null) return 0.18f

    val label = digitalTwinReadinessLabel(digitalTwinState).lowercase()

    return when {
        "loaded" in label -> 0.90f
        "ready" in label -> 0.88f
        "not loaded" in label -> 0.18f
        else -> 0.54f
    }
}

private fun digitalTwinSignalRatio(
    digitalTwinState: DigitalTwinState?
): Float {
    if (digitalTwinState == null) return 0.12f

    val label = digitalTwinSignalCoverageLabel(digitalTwinState)
    val match = Regex("""(\d+)\s*/\s*(\d+)""").find(label) ?: return 0.42f

    val current = match.groupValues[1].toFloatOrNull() ?: return 0.42f
    val total = match.groupValues[2].toFloatOrNull() ?: return 0.42f

    return if (total <= 0f) 0.12f else (current / total).coerceIn(0f, 1f)
}

private fun digitalTwinFreshnessRatio(
    digitalTwinState: DigitalTwinState?
): Float {
    if (digitalTwinState == null) return 0.16f

    val ageMillis = (System.currentTimeMillis() - digitalTwinState.lastUpdatedEpochMillis)
        .coerceAtLeast(0L)

    val ageHours = ageMillis / 3_600_000f

    return when {
        ageHours <= 1f -> 0.92f
        ageHours <= 6f -> 0.78f
        ageHours <= 24f -> 0.58f
        ageHours <= 72f -> 0.34f
        else -> 0.18f
    }
}

