package com.lifeflow.domain.timeline

import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment

class AdaptiveTimelineEngine {

    fun compute(assessment: WellbeingAssessment): AdaptiveTimelineState {
        val notes = mutableListOf<String>()
        val readiness = resolveReadiness(assessment, notes)
        val prioritySignal = resolvePrioritySignal(assessment, readiness)
        val focusWindow = resolveFocusWindow(assessment, readiness)
        return AdaptiveTimelineState(
            readiness = readiness,
            prioritySignal = prioritySignal,
            focusWindow = focusWindow,
            notes = notes
        )
    }

    private fun resolveReadiness(
        assessment: WellbeingAssessment,
        notes: MutableList<String>
    ): TimelineReadiness {
        return when (assessment.overallReadiness) {
            OverallReadiness.BLOCKED -> {
                notes += "Timeline blocked: identity not initialized."
                TimelineReadiness.BLOCKED
            }
            OverallReadiness.NO_ACCESS -> {
                notes += "Timeline degraded: no health access granted."
                TimelineReadiness.DEGRADED
            }
            OverallReadiness.INSUFFICIENT_DATA -> {
                notes += "Timeline degraded: insufficient wellbeing data."
                TimelineReadiness.INSUFFICIENT_DATA
            }
            OverallReadiness.ATTENTION_REQUIRED -> {
                notes += "Timeline degraded: heart rate signal requires attention."
                TimelineReadiness.DEGRADED
            }
            OverallReadiness.LOW -> {
                notes += "Timeline ready but capacity is low."
                TimelineReadiness.READY
            }
            OverallReadiness.FAIR,
            OverallReadiness.GOOD -> TimelineReadiness.READY
        }
    }

    private fun resolvePrioritySignal(
        assessment: WellbeingAssessment,
        readiness: TimelineReadiness
    ): PrioritySignal {
        if (readiness == TimelineReadiness.BLOCKED ||
            readiness == TimelineReadiness.INSUFFICIENT_DATA
        ) return PrioritySignal.UNKNOWN

        if (assessment.heartRateStatus == HeartRateStatus.ABNORMAL_HIGH ||
            assessment.heartRateStatus == HeartRateStatus.ABNORMAL_LOW
        ) return PrioritySignal.DEFER_DEMANDING

        return when (assessment.activityLevel) {
            ActivityLevel.UNAVAILABLE,
            ActivityLevel.NO_ACCESS,
            ActivityLevel.UNKNOWN,
            ActivityLevel.NO_DATA -> PrioritySignal.UNKNOWN
            ActivityLevel.SEDENTARY,
            ActivityLevel.LOW -> PrioritySignal.DEFER_DEMANDING
            ActivityLevel.MODERATE -> PrioritySignal.LIGHT_TASKS
            ActivityLevel.ACTIVE -> when (assessment.overallReadiness) {
                OverallReadiness.GOOD -> PrioritySignal.FULL_CAPACITY
                else -> PrioritySignal.LIGHT_TASKS
            }
        }
    }

    private fun resolveFocusWindow(
        assessment: WellbeingAssessment,
        readiness: TimelineReadiness
    ): FocusWindow {
        if (readiness == TimelineReadiness.BLOCKED ||
            readiness == TimelineReadiness.INSUFFICIENT_DATA
        ) return FocusWindow.UNKNOWN

        return when (assessment.overallReadiness) {
            OverallReadiness.BLOCKED,
            OverallReadiness.NO_ACCESS,
            OverallReadiness.INSUFFICIENT_DATA -> FocusWindow.UNKNOWN
            OverallReadiness.ATTENTION_REQUIRED,
            OverallReadiness.LOW -> FocusWindow.SHORT
            OverallReadiness.FAIR -> FocusWindow.MEDIUM
            OverallReadiness.GOOD -> FocusWindow.EXTENDED
        }
    }
}