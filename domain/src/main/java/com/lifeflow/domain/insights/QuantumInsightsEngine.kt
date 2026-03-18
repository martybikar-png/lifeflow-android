package com.lifeflow.domain.insights

import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.timeline.PrioritySignal
import com.lifeflow.domain.timeline.TimelineReadiness
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment

/**
 * QuantumInsightsGrid V1 — cross-signal pattern detection.
 *
 * Input:  WellbeingAssessment + AdaptiveTimelineState + ShadowDiaryState
 * Output: InsightsState with prioritized insights
 *
 * Rule-based only. Fail-closed on blocked inputs.
 */
class QuantumInsightsEngine {

    fun compute(
        wellbeing: WellbeingAssessment,
        timeline: AdaptiveTimelineState,
        diary: ShadowDiaryState
    ): InsightsState {
        if (wellbeing.overallReadiness == OverallReadiness.BLOCKED ||
            timeline.readiness == TimelineReadiness.BLOCKED
        ) {
            return InsightsState(
                insights = emptyList(),
                readiness = InsightsReadiness.BLOCKED
            )
        }

        if (wellbeing.overallReadiness == OverallReadiness.INSUFFICIENT_DATA &&
            timeline.readiness == TimelineReadiness.INSUFFICIENT_DATA
        ) {
            return InsightsState(
                insights = emptyList(),
                readiness = InsightsReadiness.INSUFFICIENT_DATA
            )
        }

        val insights = mutableListOf<Insight>()

        resolveHealthInsight(wellbeing)?.let { insights += it }
        resolveCapacityInsight(wellbeing, timeline)?.let { insights += it }
        resolvePeakWindowInsight(timeline)?.let { insights += it }
        resolveDiaryInsight(diary)?.let { insights += it }

        return InsightsState(
            insights = insights.sortedByDescending { it.priority.ordinal },
            readiness = InsightsReadiness.READY
        )
    }

    private fun resolveHealthInsight(wellbeing: WellbeingAssessment): Insight? {
        return when (wellbeing.overallReadiness) {
            OverallReadiness.ATTENTION_REQUIRED -> Insight(
                type = InsightType.HEALTH_ATTENTION,
                message = "Heart rate signal requires attention. Review before demanding tasks.",
                priority = InsightPriority.HIGH
            )
            OverallReadiness.LOW -> Insight(
                type = InsightType.LOW_CAPACITY,
                message = "Low overall capacity detected. Prioritize light tasks and recovery.",
                priority = InsightPriority.MEDIUM
            )
            else -> null
        }
    }

    private fun resolveCapacityInsight(
        wellbeing: WellbeingAssessment,
        timeline: AdaptiveTimelineState
    ): Insight? {
        val isLow = wellbeing.overallReadiness == OverallReadiness.LOW ||
                timeline.prioritySignal == PrioritySignal.DEFER_DEMANDING
        if (isLow) {
            return Insight(
                type = InsightType.RECOVERY_SIGNAL,
                message = "Recovery window detected. Defer demanding tasks.",
                priority = InsightPriority.MEDIUM
            )
        }
        return null
    }

    private fun resolvePeakWindowInsight(timeline: AdaptiveTimelineState): Insight? {
        return when (timeline.prioritySignal) {
            PrioritySignal.FULL_CAPACITY -> Insight(
                type = InsightType.PEAK_WINDOW,
                message = "Peak capacity window. Good time for deep or creative work.",
                priority = InsightPriority.HIGH
            )
            PrioritySignal.LIGHT_TASKS -> Insight(
                type = InsightType.FOCUS_OPPORTUNITY,
                message = "Moderate capacity. Focused light tasks are suitable now.",
                priority = InsightPriority.MEDIUM
            )
            else -> null
        }
    }

    private fun resolveDiaryInsight(diary: ShadowDiaryState): Insight? {
        val signal = diary.dominantSignal ?: return null
        return when (signal) {
            DiarySignal.CREATIVE_PEAK -> Insight(
                type = InsightType.CREATIVE_WINDOW,
                message = "Creative peak signal from diary. Prioritize creative work.",
                priority = InsightPriority.HIGH
            )
            DiarySignal.STRESS_HIGH -> Insight(
                type = InsightType.STRESS_PATTERN,
                message = "High stress pattern in diary. Consider recovery or lighter load.",
                priority = InsightPriority.HIGH
            )
            DiarySignal.SOCIAL_DRAINED -> Insight(
                type = InsightType.SOCIAL_SIGNAL,
                message = "Social drain pattern detected. Solo recovery time recommended.",
                priority = InsightPriority.MEDIUM
            )
            DiarySignal.SOCIAL_RECHARGED -> Insight(
                type = InsightType.SOCIAL_SIGNAL,
                message = "Social recharge pattern. Collaborative work suitable.",
                priority = InsightPriority.LOW
            )
            else -> null
        }
    }
}
