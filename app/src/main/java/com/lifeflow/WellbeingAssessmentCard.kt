package com.lifeflow

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment

@Composable
internal fun WellbeingAssessmentCard(
    wellbeingAssessment: WellbeingAssessment?
) {
    val readinessColor = wellbeingAssessmentTitleColor(wellbeingAssessment)

    StatusCardShell(
        title = "Wellbeing assessment",
        titleColor = readinessColor,
        summary = wellbeingAssessmentSummary(wellbeingAssessment)
    ) {
        KeyValueStatusLine(
            label = "Overall readiness",
            value = overallReadinessLabel(wellbeingAssessment),
            valueColor = readinessColor
        )

        if (wellbeingAssessment == null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No wellbeing assessment is available yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@StatusCardShell
        }

        KeyValueStatusLine(
            label = "Activity level",
            value = activityLevelLabel(wellbeingAssessment.activityLevel),
            valueColor = activityLevelColor(wellbeingAssessment.activityLevel)
        )

        KeyValueStatusLine(
            label = "Heart-rate status",
            value = heartRateStatusLabel(wellbeingAssessment.heartRateStatus),
            valueColor = heartRateStatusColor(wellbeingAssessment.heartRateStatus)
        )

        KeyValueLine("Notes count", wellbeingAssessment.notes.size.toString())

        if (wellbeingAssessment.notes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            wellbeingAssessment.notes.forEach { note ->
                Text(
                    text = "• $note",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

private fun wellbeingAssessmentSummary(
    wellbeingAssessment: WellbeingAssessment?
): String {
    if (wellbeingAssessment == null) {
        return "No interpreted wellbeing state has been produced yet."
    }

    return when (wellbeingAssessment.overallReadiness) {
        OverallReadiness.BLOCKED ->
            "Wellbeing interpretation is blocked until protected inputs are available."

        OverallReadiness.NO_ACCESS ->
            "Wellbeing interpretation is limited because the required health access is missing."

        OverallReadiness.INSUFFICIENT_DATA ->
            "LifeFlow does not yet have enough usable signals for a fuller wellbeing assessment."

        OverallReadiness.ATTENTION_REQUIRED ->
            "LifeFlow detected a wellbeing state that may need closer review."

        OverallReadiness.LOW ->
            "LifeFlow assessed a low overall readiness state."

        OverallReadiness.FAIR ->
            "LifeFlow assessed a fair overall readiness state."

        OverallReadiness.GOOD ->
            "LifeFlow assessed a good overall readiness state."
    }
}

private fun overallReadinessLabel(
    wellbeingAssessment: WellbeingAssessment?
): String {
    if (wellbeingAssessment == null) {
        return "Not assessed yet"
    }

    return when (wellbeingAssessment.overallReadiness) {
        OverallReadiness.BLOCKED -> "Blocked"
        OverallReadiness.NO_ACCESS -> "No access"
        OverallReadiness.INSUFFICIENT_DATA -> "Insufficient data"
        OverallReadiness.ATTENTION_REQUIRED -> "Attention required"
        OverallReadiness.LOW -> "Low"
        OverallReadiness.FAIR -> "Fair"
        OverallReadiness.GOOD -> "Good"
    }
}

private fun activityLevelLabel(activityLevel: ActivityLevel): String {
    return when (activityLevel) {
        ActivityLevel.UNAVAILABLE -> "Unavailable"
        ActivityLevel.NO_ACCESS -> "No access"
        ActivityLevel.UNKNOWN -> "Unknown"
        ActivityLevel.NO_DATA -> "No data"
        ActivityLevel.SEDENTARY -> "Sedentary"
        ActivityLevel.LOW -> "Low"
        ActivityLevel.MODERATE -> "Moderate"
        ActivityLevel.ACTIVE -> "Active"
    }
}

private fun heartRateStatusLabel(heartRateStatus: HeartRateStatus): String {
    return when (heartRateStatus) {
        HeartRateStatus.UNAVAILABLE -> "Unavailable"
        HeartRateStatus.NO_ACCESS -> "No access"
        HeartRateStatus.UNKNOWN -> "Unknown"
        HeartRateStatus.NO_DATA -> "No data"
        HeartRateStatus.ABNORMAL_LOW -> "Abnormal low"
        HeartRateStatus.RESTING_LOW -> "Resting low"
        HeartRateStatus.NORMAL -> "Normal"
        HeartRateStatus.ELEVATED -> "Elevated"
        HeartRateStatus.ABNORMAL_HIGH -> "Abnormal high"
    }
}

@Composable
private fun wellbeingAssessmentTitleColor(
    wellbeingAssessment: WellbeingAssessment?
): Color {
    val colors = MaterialTheme.colorScheme

    if (wellbeingAssessment == null) {
        return colors.onSurfaceVariant
    }

    return when (wellbeingAssessment.overallReadiness) {
        OverallReadiness.GOOD,
        OverallReadiness.FAIR -> colors.primary

        OverallReadiness.ATTENTION_REQUIRED,
        OverallReadiness.LOW,
        OverallReadiness.NO_ACCESS,
        OverallReadiness.BLOCKED -> colors.error

        OverallReadiness.INSUFFICIENT_DATA -> colors.onSurfaceVariant
    }
}

@Composable
private fun activityLevelColor(activityLevel: ActivityLevel): Color {
    val colors = MaterialTheme.colorScheme
    return when (activityLevel) {
        ActivityLevel.ACTIVE,
        ActivityLevel.MODERATE -> colors.primary

        ActivityLevel.LOW,
        ActivityLevel.SEDENTARY,
        ActivityLevel.NO_ACCESS -> colors.error

        ActivityLevel.UNAVAILABLE,
        ActivityLevel.UNKNOWN,
        ActivityLevel.NO_DATA -> colors.onSurfaceVariant
    }
}

@Composable
private fun heartRateStatusColor(heartRateStatus: HeartRateStatus): Color {
    val colors = MaterialTheme.colorScheme
    return when (heartRateStatus) {
        HeartRateStatus.NORMAL,
        HeartRateStatus.RESTING_LOW -> colors.primary

        HeartRateStatus.ABNORMAL_LOW,
        HeartRateStatus.ABNORMAL_HIGH,
        HeartRateStatus.NO_ACCESS -> colors.error

        HeartRateStatus.ELEVATED,
        HeartRateStatus.UNAVAILABLE,
        HeartRateStatus.UNKNOWN,
        HeartRateStatus.NO_DATA -> colors.onSurfaceVariant
    }
}
