package com.lifeflow

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment

internal enum class WellbeingViewMode {
    Overall,
    Activity,
    Heart
}

internal data class WellbeingMetric(
    val title: String,
    val value: String
)

internal val WellbeingSelectorShape = RoundedCornerShape(16.dp)
internal val WellbeingGraphPanelShape = RoundedCornerShape(24.dp)
internal val WellbeingLegendPanelShape = RoundedCornerShape(18.dp)
internal val WellbeingMetricShape = RoundedCornerShape(18.dp)
internal val WellbeingLegendDotShape = RoundedCornerShape(999.dp)

internal val WellbeingGraphCyan = Color(0xFF22CDF7)
internal val WellbeingGraphLavender = Color(0xFF9EA7FF)
internal val WellbeingGraphPeach = Color(0xFFF2B7A6)
internal val WellbeingGraphGrid = Color(0xFFD9E1EA)
internal val WellbeingGraphGlow = Color(0xFFFFFFFF).copy(alpha = 0.78f)

internal fun wellbeingAssessmentSummary(
    selectedMode: WellbeingViewMode
): String {
    return when (selectedMode) {
        WellbeingViewMode.Overall -> "Overall trend."
        WellbeingViewMode.Activity -> "Activity trend."
        WellbeingViewMode.Heart -> "Heart trend."
    }
}

internal fun wellbeingMetrics(
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

internal fun wellbeingScore(
    selectedMode: WellbeingViewMode,
    wellbeingAssessment: WellbeingAssessment?
): Int {
    return when (selectedMode) {
        WellbeingViewMode.Overall -> wellbeingReadinessScore(wellbeingAssessment)
        WellbeingViewMode.Activity -> wellbeingActivityScore(wellbeingAssessment)
        WellbeingViewMode.Heart -> wellbeingHeartScore(wellbeingAssessment)
    }
}

internal fun wellbeingTrendPoints(
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