package com.lifeflow.domain.timeline

import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveTimelineEngineTest {

    private val engine = AdaptiveTimelineEngine()

    private fun assessment(
        activityLevel: ActivityLevel = ActivityLevel.ACTIVE,
        heartRateStatus: HeartRateStatus = HeartRateStatus.NORMAL,
        overallReadiness: OverallReadiness = OverallReadiness.GOOD
    ) = WellbeingAssessment(
        activityLevel = activityLevel,
        heartRateStatus = heartRateStatus,
        overallReadiness = overallReadiness
    )

    @Test
    fun `blocked readiness returns BLOCKED and UNKNOWN signals`() {
        val result = engine.compute(assessment(overallReadiness = OverallReadiness.BLOCKED))
        assertEquals(TimelineReadiness.BLOCKED, result.readiness)
        assertEquals(PrioritySignal.UNKNOWN, result.prioritySignal)
        assertEquals(FocusWindow.UNKNOWN, result.focusWindow)
    }

    @Test
    fun `good readiness returns READY FULL_CAPACITY EXTENDED`() {
        val result = engine.compute(assessment(
            activityLevel = ActivityLevel.ACTIVE,
            heartRateStatus = HeartRateStatus.NORMAL,
            overallReadiness = OverallReadiness.GOOD
        ))
        assertEquals(TimelineReadiness.READY, result.readiness)
        assertEquals(PrioritySignal.FULL_CAPACITY, result.prioritySignal)
        assertEquals(FocusWindow.EXTENDED, result.focusWindow)
    }

    @Test
    fun `fair readiness returns READY LIGHT_TASKS MEDIUM`() {
        val result = engine.compute(assessment(
            activityLevel = ActivityLevel.MODERATE,
            heartRateStatus = HeartRateStatus.NORMAL,
            overallReadiness = OverallReadiness.FAIR
        ))
        assertEquals(TimelineReadiness.READY, result.readiness)
        assertEquals(PrioritySignal.LIGHT_TASKS, result.prioritySignal)
        assertEquals(FocusWindow.MEDIUM, result.focusWindow)
    }

    @Test
    fun `low readiness returns READY DEFER_DEMANDING SHORT`() {
        val result = engine.compute(assessment(
            activityLevel = ActivityLevel.SEDENTARY,
            heartRateStatus = HeartRateStatus.NORMAL,
            overallReadiness = OverallReadiness.LOW
        ))
        assertEquals(TimelineReadiness.READY, result.readiness)
        assertEquals(PrioritySignal.DEFER_DEMANDING, result.prioritySignal)
        assertEquals(FocusWindow.SHORT, result.focusWindow)
    }

    @Test
    fun `abnormal high heart rate returns DEFER_DEMANDING`() {
        val result = engine.compute(assessment(
            activityLevel = ActivityLevel.ACTIVE,
            heartRateStatus = HeartRateStatus.ABNORMAL_HIGH,
            overallReadiness = OverallReadiness.ATTENTION_REQUIRED
        ))
        assertEquals(PrioritySignal.DEFER_DEMANDING, result.prioritySignal)
        assertEquals(FocusWindow.SHORT, result.focusWindow)
    }

    @Test
    fun `insufficient data returns INSUFFICIENT_DATA and UNKNOWN`() {
        val result = engine.compute(assessment(
            activityLevel = ActivityLevel.NO_DATA,
            heartRateStatus = HeartRateStatus.NO_DATA,
            overallReadiness = OverallReadiness.INSUFFICIENT_DATA
        ))
        assertEquals(TimelineReadiness.INSUFFICIENT_DATA, result.readiness)
        assertEquals(PrioritySignal.UNKNOWN, result.prioritySignal)
        assertEquals(FocusWindow.UNKNOWN, result.focusWindow)
    }
}