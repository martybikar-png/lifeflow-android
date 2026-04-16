package com.lifeflow

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment

private enum class WellbeingViewMode {
    Overall,
    Activity,
    Heart
}

private data class WellbeingMetric(
    val title: String,
    val value: String
)

private val WellbeingSelectorShape = RoundedCornerShape(16.dp)
private val WellbeingGraphPanelShape = RoundedCornerShape(24.dp)
private val WellbeingLegendPanelShape = RoundedCornerShape(18.dp)
private val WellbeingMetricShape = RoundedCornerShape(18.dp)
private val WellbeingLegendDotShape = RoundedCornerShape(999.dp)

private val WellbeingGraphCyan = Color(0xFF22CDF7)
private val WellbeingGraphLavender = Color(0xFF9EA7FF)
private val WellbeingGraphPeach = Color(0xFFF2B7A6)
private val WellbeingGraphGrid = Color(0xFFD9E1EA)
private val WellbeingGraphGlow = Color(0xFFFFFFFF).copy(alpha = 0.78f)

@Composable
internal fun WellbeingAssessmentCard(
    wellbeingAssessment: WellbeingAssessment?,
    onOpenWellbeing: () -> Unit = {},
    onOpenJournal: () -> Unit = {}
) {
    var selectedMode by remember { mutableStateOf(WellbeingViewMode.Overall) }

    val metrics = wellbeingMetrics(
        selectedMode = selectedMode,
        wellbeingAssessment = wellbeingAssessment
    )

    Column {
        StatusCardShell(
            title = "Wellbeing",
            titleColor = MaterialTheme.colorScheme.onSurface,
            summary = wellbeingAssessmentSummary(selectedMode)
        ) {}

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WellbeingSelectorChip(
                    label = "Overall",
                    selected = selectedMode == WellbeingViewMode.Overall,
                    accent = WellbeingGraphCyan,
                    onClick = { selectedMode = WellbeingViewMode.Overall }
                )
                WellbeingSelectorChip(
                    label = "Activity",
                    selected = selectedMode == WellbeingViewMode.Activity,
                    accent = WellbeingGraphLavender,
                    onClick = { selectedMode = WellbeingViewMode.Activity }
                )
                WellbeingSelectorChip(
                    label = "Heart",
                    selected = selectedMode == WellbeingViewMode.Heart,
                    accent = WellbeingGraphPeach,
                    onClick = { selectedMode = WellbeingViewMode.Heart }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp)
                .lifeFlowRaisedPanelChrome(WellbeingGraphPanelShape)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            WellbeingTrendChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp),
                selectedMode = selectedMode,
                wellbeingAssessment = wellbeingAssessment
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WellbeingMetricCard(
                metric = metrics[0],
                width = 84.dp
            )
            WellbeingMetricCard(
                metric = metrics[1],
                width = 84.dp
            )
            WellbeingMetricCard(
                metric = metrics[2],
                width = 84.dp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LifeFlowPrimaryActionButton(
                label = "Open wellbeing",
                onClick = onOpenWellbeing,
                modifier = Modifier.fillMaxWidth()
            )
            LifeFlowSecondaryActionButton(
                label = "Journal",
                onClick = onOpenJournal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WellbeingSelectorChip(
    label: String,
    selected: Boolean,
    accent: Color,
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
private fun WellbeingTrendChart(
    modifier: Modifier,
    selectedMode: WellbeingViewMode,
    wellbeingAssessment: WellbeingAssessment?
) {
    val score = wellbeingScore(selectedMode, wellbeingAssessment)

    val lineColor = when (selectedMode) {
        WellbeingViewMode.Overall -> WellbeingGraphCyan
        WellbeingViewMode.Activity -> WellbeingGraphLavender
        WellbeingViewMode.Heart -> WellbeingGraphPeach
    }

    val points = wellbeingTrendPoints(selectedMode, score)
    val axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

    Canvas(modifier = modifier) {
        val leftAxisWidth = 30.dp.toPx()
        val left = leftAxisWidth + 8.dp.toPx()
        val top = 10.dp.toPx()
        val right = size.width - 8.dp.toPx()
        val bottom = size.height - 12.dp.toPx()

        val chartWidth = right - left
        val chartHeight = bottom - top

        val axisPaint = Paint().apply {
            isAntiAlias = true
            color = axisLabelColor.toArgb()
            textSize = 21f
            textAlign = Paint.Align.LEFT
        }

        val axisLabels = listOf("75", "50", "25", "0")

        repeat(4) { index ->
            val y = top + (chartHeight * index / 3f)

            drawLine(
                color = WellbeingGraphGrid.copy(alpha = 0.42f),
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.1.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawContext.canvas.nativeCanvas.drawText(
                axisLabels[index],
                0f,
                y + 6.dp.toPx(),
                axisPaint
            )
        }

        repeat(7) { index ->
            val x = left + (chartWidth * index / 6f)
            drawCircle(
                color = WellbeingGraphGrid.copy(alpha = 0.58f),
                radius = 1.6.dp.toPx(),
                center = Offset(x, bottom + 2.dp.toPx())
            )
        }

        val chartPoints = points.mapIndexed { index, value ->
            Offset(
                x = left + chartWidth * index / (points.size - 1),
                y = bottom - chartHeight * value
            )
        }

        val linePath = Path().apply {
            moveTo(chartPoints.first().x, chartPoints.first().y)
            for (i in 0 until chartPoints.lastIndex) {
                val current = chartPoints[i]
                val next = chartPoints[i + 1]
                val dx = (next.x - current.x) * 0.38f
                cubicTo(
                    current.x + dx, current.y,
                    next.x - dx, next.y,
                    next.x, next.y
                )
            }
        }

        val areaPath = Path().apply {
            addPath(linePath)
            lineTo(chartPoints.last().x, bottom)
            lineTo(chartPoints.first().x, bottom)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.38f),
                    lineColor.copy(alpha = 0.20f),
                    lineColor.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = top,
                endY = bottom
            )
        )

        drawPath(
            path = linePath,
            color = WellbeingGraphGlow,
            style = Stroke(
                width = 7.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 3.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        val endPoint = chartPoints.last()
        drawCircle(
            color = lineColor.copy(alpha = 0.16f),
            radius = 10.dp.toPx(),
            center = endPoint
        )
        drawCircle(
            color = lineColor,
            radius = 4.2.dp.toPx(),
            center = endPoint
        )
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
            color = if (isActive) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WellbeingMetricCard(
    metric: WellbeingMetric,
    width: androidx.compose.ui.unit.Dp
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

private fun wellbeingAssessmentSummary(
    selectedMode: WellbeingViewMode
): String {
    return when (selectedMode) {
        WellbeingViewMode.Overall -> "Overall trend."
        WellbeingViewMode.Activity -> "Activity trend."
        WellbeingViewMode.Heart -> "Heart trend."
    }
}

private fun wellbeingMetrics(
    selectedMode: WellbeingViewMode,
    wellbeingAssessment: WellbeingAssessment?
): List<WellbeingMetric> {
    val notesCount = wellbeingAssessment?.notes?.size ?: 0

    return when (selectedMode) {
        WellbeingViewMode.Overall -> listOf(
            WellbeingMetric("Score", "${wellbeingReadinessScore(wellbeingAssessment)}%"),
            WellbeingMetric("State", overallStateShortLabel(wellbeingAssessment)),
            WellbeingMetric("Notes", notesCount.toString())
        )

        WellbeingViewMode.Activity -> listOf(
            WellbeingMetric("Score", "${wellbeingActivityScore(wellbeingAssessment)}%"),
            WellbeingMetric("State", activityStateShortLabel(wellbeingAssessment)),
            WellbeingMetric("Notes", notesCount.toString())
        )

        WellbeingViewMode.Heart -> listOf(
            WellbeingMetric("Score", "${wellbeingHeartScore(wellbeingAssessment)}%"),
            WellbeingMetric("State", heartStateShortLabel(wellbeingAssessment)),
            WellbeingMetric("Notes", notesCount.toString())
        )
    }
}

private fun wellbeingScore(
    selectedMode: WellbeingViewMode,
    wellbeingAssessment: WellbeingAssessment?
): Int {
    return when (selectedMode) {
        WellbeingViewMode.Overall -> wellbeingReadinessScore(wellbeingAssessment)
        WellbeingViewMode.Activity -> wellbeingActivityScore(wellbeingAssessment)
        WellbeingViewMode.Heart -> wellbeingHeartScore(wellbeingAssessment)
    }
}

private fun wellbeingTrendPoints(
    selectedMode: WellbeingViewMode,
    score: Int
): List<Float> {
    val endBoost = score / 100f * 0.10f

    return when (selectedMode) {
        WellbeingViewMode.Overall ->
            listOf(0.22f, 0.48f, 0.42f, 0.64f, 0.58f, 0.61f, 0.66f + endBoost)

        WellbeingViewMode.Activity ->
            listOf(0.18f, 0.36f, 0.31f, 0.52f, 0.46f, 0.55f, 0.58f + endBoost)

        WellbeingViewMode.Heart ->
            listOf(0.26f, 0.40f, 0.34f, 0.49f, 0.45f, 0.54f, 0.56f + endBoost)
    }
}

private fun wellbeingReadinessScore(
    wellbeingAssessment: WellbeingAssessment?
): Int {
    if (wellbeingAssessment == null) return 72

    return when (wellbeingAssessment.overallReadiness) {
        OverallReadiness.GOOD -> 84
        OverallReadiness.FAIR -> 68
        OverallReadiness.LOW -> 42
        OverallReadiness.ATTENTION_REQUIRED -> 36
        OverallReadiness.INSUFFICIENT_DATA -> 24
        OverallReadiness.NO_ACCESS -> 12
        OverallReadiness.BLOCKED -> 8
    }
}

private fun wellbeingActivityScore(
    wellbeingAssessment: WellbeingAssessment?
): Int {
    if (wellbeingAssessment == null) return 66

    return when (wellbeingAssessment.activityLevel) {
        ActivityLevel.ACTIVE -> 86
        ActivityLevel.MODERATE -> 71
        ActivityLevel.LOW -> 46
        ActivityLevel.SEDENTARY -> 28
        ActivityLevel.NO_DATA -> 18
        ActivityLevel.UNKNOWN -> 22
        ActivityLevel.NO_ACCESS -> 12
        ActivityLevel.UNAVAILABLE -> 14
    }
}

private fun wellbeingHeartScore(
    wellbeingAssessment: WellbeingAssessment?
): Int {
    if (wellbeingAssessment == null) return 74

    return when (wellbeingAssessment.heartRateStatus) {
        HeartRateStatus.NORMAL -> 82
        HeartRateStatus.RESTING_LOW -> 74
        HeartRateStatus.ELEVATED -> 56
        HeartRateStatus.ABNORMAL_HIGH -> 34
        HeartRateStatus.ABNORMAL_LOW -> 34
        HeartRateStatus.NO_DATA -> 18
        HeartRateStatus.UNKNOWN -> 22
        HeartRateStatus.NO_ACCESS -> 12
        HeartRateStatus.UNAVAILABLE -> 14
    }
}

private fun overallStateShortLabel(
    wellbeingAssessment: WellbeingAssessment?
): String {
    if (wellbeingAssessment == null) return "Good"

    return when (wellbeingAssessment.overallReadiness) {
        OverallReadiness.GOOD -> "Good"
        OverallReadiness.FAIR -> "Fair"
        OverallReadiness.LOW -> "Low"
        OverallReadiness.ATTENTION_REQUIRED -> "Alert"
        OverallReadiness.INSUFFICIENT_DATA -> "Low data"
        OverallReadiness.NO_ACCESS -> "No access"
        OverallReadiness.BLOCKED -> "Blocked"
    }
}

private fun activityStateShortLabel(
    wellbeingAssessment: WellbeingAssessment?
): String {
    if (wellbeingAssessment == null) return "Moderate"

    return when (wellbeingAssessment.activityLevel) {
        ActivityLevel.ACTIVE -> "Active"
        ActivityLevel.MODERATE -> "Moderate"
        ActivityLevel.LOW -> "Low"
        ActivityLevel.SEDENTARY -> "Sedentary"
        ActivityLevel.NO_DATA -> "No data"
        ActivityLevel.UNKNOWN -> "Unknown"
        ActivityLevel.NO_ACCESS -> "No access"
        ActivityLevel.UNAVAILABLE -> "Unavailable"
    }
}

private fun heartStateShortLabel(
    wellbeingAssessment: WellbeingAssessment?
): String {
    if (wellbeingAssessment == null) return "Normal"

    return when (wellbeingAssessment.heartRateStatus) {
        HeartRateStatus.NORMAL -> "Normal"
        HeartRateStatus.RESTING_LOW -> "Resting"
        HeartRateStatus.ELEVATED -> "Elevated"
        HeartRateStatus.ABNORMAL_HIGH -> "High"
        HeartRateStatus.ABNORMAL_LOW -> "Low"
        HeartRateStatus.NO_DATA -> "No data"
        HeartRateStatus.UNKNOWN -> "Unknown"
        HeartRateStatus.NO_ACCESS -> "No access"
        HeartRateStatus.UNAVAILABLE -> "Unavailable"
    }
}


