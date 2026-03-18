package com.lifeflow.domain.insights

import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.timeline.PrioritySignal
import com.lifeflow.domain.wellbeing.OverallReadiness

/**
 * QuantumInsightsGrid V1 — pattern recognition + anticipation layer.
 *
 * Input:  signals from Wellbeing, Timeline, Diary
 * Output: InsightsState with prioritized insights
 *
 * V1 = rule-based cross-signal pattern detection.
 * No ML, no prediction engine. That is LF Two territory.
 */
data class InsightsState(
    val insights: List<Insight>,
    val readiness: InsightsReadiness
)

data class Insight(
    val type: InsightType,
    val message: String,
    val priority: InsightPriority
)

enum class InsightType {
    RECOVERY_SIGNAL,
    PEAK_WINDOW,
    STRESS_PATTERN,
    FOCUS_OPPORTUNITY,
    SOCIAL_SIGNAL,
    CREATIVE_WINDOW,
    LOW_CAPACITY,
    HEALTH_ATTENTION
}

enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class InsightsReadiness {
    BLOCKED,
    INSUFFICIENT_DATA,
    READY
}
