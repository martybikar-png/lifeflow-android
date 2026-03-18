package com.lifeflow.domain.insights

import com.lifeflow.domain.diary.DiaryReadiness
import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.timeline.FocusWindow
import com.lifeflow.domain.timeline.PrioritySignal
import com.lifeflow.domain.timeline.TimelineReadiness
import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import org.junit.Assert.assertEquals
import org.junit.Test

class QuantumInsightsEngineTest {

    private val engine = QuantumInsightsEngine()

    private fun wellbeing(
        overallReadiness: OverallReadiness = OverallReadiness.GOOD,
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
        heartRateStatus: HeartRateStatus = HeartRateStatus.NORMAL
    ) = WellbeingAssessment(
        activityLevel = activityLevel,
        heartRateStatus = heartRateStatus,
        overallReadiness = overallReadiness
    )

    private fun timeline(
        readiness: TimelineReadiness = TimelineReadiness.READY,
        prioritySignal: PrioritySignal = PrioritySignal.FULL_CAPACITY,
        focusWindow: FocusWindow = FocusWindow.EXTENDED
    ) = AdaptiveTimelineState(
        readiness = readiness,
        prioritySignal = prioritySignal,
        focusWindow = focusWindow
    )

    private fun diary(
        dominantSignal: DiarySignal? = null
    ) = ShadowDiaryState(
        recentEntries = emptyList(),
        dominantSignal = dominantSignal,
        patternNote = null,
        readiness = DiaryReadiness.READY
    )

    @Test
    fun `blocked wellbeing returns BLOCKED`() {
        val result = engine.compute(
            wellbeing = wellbeing(overallReadiness = OverallReadiness.BLOCKED),
            timeline = timeline(),
            diary = diary()
        )
        assertEquals(InsightsReadiness.BLOCKED, result.readiness)
        assertEquals(0, result.insights.size)
    }

    @Test
    fun `peak window produces PEAK_WINDOW insight`() {
        val result = engine.compute(
            wellbeing = wellbeing(overallReadiness = OverallReadiness.GOOD),
            timeline = timeline(prioritySignal = PrioritySignal.FULL_CAPACITY),
            diary = diary()
        )
        assertEquals(InsightsReadiness.READY, result.readiness)
        assert(result.insights.any { it.type == InsightType.PEAK_WINDOW })
    }

    @Test
    fun `health attention produces HEALTH_ATTENTION insight`() {
        val result = engine.compute(
            wellbeing = wellbeing(overallReadiness = OverallReadiness.ATTENTION_REQUIRED),
            timeline = timeline(),
            diary = diary()
        )
        assert(result.insights.any { it.type == InsightType.HEALTH_ATTENTION })
    }

    @Test
    fun `creative peak diary signal produces CREATIVE_WINDOW insight`() {
        val result = engine.compute(
            wellbeing = wellbeing(),
            timeline = timeline(),
            diary = diary(dominantSignal = DiarySignal.CREATIVE_PEAK)
        )
        assert(result.insights.any { it.type == InsightType.CREATIVE_WINDOW })
    }

    @Test
    fun `stress high diary signal produces STRESS_PATTERN insight`() {
        val result = engine.compute(
            wellbeing = wellbeing(),
            timeline = timeline(),
            diary = diary(dominantSignal = DiarySignal.STRESS_HIGH)
        )
        assert(result.insights.any { it.type == InsightType.STRESS_PATTERN })
    }

    @Test
    fun `insights are sorted by priority descending`() {
        val result = engine.compute(
            wellbeing = wellbeing(overallReadiness = OverallReadiness.ATTENTION_REQUIRED),
            timeline = timeline(prioritySignal = PrioritySignal.FULL_CAPACITY),
            diary = diary(dominantSignal = DiarySignal.SOCIAL_RECHARGED)
        )
        val priorities = result.insights.map { it.priority.ordinal }
        assertEquals(priorities.sortedDescending(), priorities)
    }
}
