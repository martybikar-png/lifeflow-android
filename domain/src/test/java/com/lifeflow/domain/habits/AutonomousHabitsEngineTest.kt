package com.lifeflow.domain.habits

import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.timeline.FocusWindow
import com.lifeflow.domain.timeline.PrioritySignal
import com.lifeflow.domain.timeline.TimelineReadiness
import org.junit.Assert.assertEquals
import org.junit.Test

class AutonomousHabitsEngineTest {

    private val engine = AutonomousHabitsEngine()

    private fun timeline(
        readiness: TimelineReadiness = TimelineReadiness.READY,
        prioritySignal: PrioritySignal = PrioritySignal.FULL_CAPACITY,
        focusWindow: FocusWindow = FocusWindow.EXTENDED
    ) = AdaptiveTimelineState(
        readiness = readiness,
        prioritySignal = prioritySignal,
        focusWindow = focusWindow
    )

    @Test
    fun `blocked timeline returns BLOCKED and no slots`() {
        val result = engine.compute(timeline(readiness = TimelineReadiness.BLOCKED))
        assertEquals(HabitsReadiness.BLOCKED, result.readiness)
        assertEquals(0, result.suggestedSlots.size)
    }

    @Test
    fun `insufficient data returns INSUFFICIENT_DATA and no slots`() {
        val result = engine.compute(timeline(readiness = TimelineReadiness.INSUFFICIENT_DATA))
        assertEquals(HabitsReadiness.INSUFFICIENT_DATA, result.readiness)
        assertEquals(0, result.suggestedSlots.size)
    }

    @Test
    fun `full capacity returns DEEP_WORK slot`() {
        val result = engine.compute(timeline(
            readiness = TimelineReadiness.READY,
            prioritySignal = PrioritySignal.FULL_CAPACITY,
            focusWindow = FocusWindow.EXTENDED
        ))
        assertEquals(HabitsReadiness.READY, result.readiness)
        assert(result.suggestedSlots.any { it.type == HabitType.DEEP_WORK })
    }

    @Test
    fun `defer demanding returns only light movement and rest`() {
        val result = engine.compute(timeline(
            readiness = TimelineReadiness.READY,
            prioritySignal = PrioritySignal.DEFER_DEMANDING,
            focusWindow = FocusWindow.SHORT
        ))
        assertEquals(HabitsReadiness.READY, result.readiness)
        assert(result.suggestedSlots.none { it.type == HabitType.DEEP_WORK })
        assert(result.suggestedSlots.any { it.type == HabitType.LIGHT_MOVEMENT })
    }

    @Test
    fun `light tasks returns FOCUSED_WORK slot`() {
        val result = engine.compute(timeline(
            readiness = TimelineReadiness.READY,
            prioritySignal = PrioritySignal.LIGHT_TASKS,
            focusWindow = FocusWindow.MEDIUM
        ))
        assert(result.suggestedSlots.any { it.type == HabitType.FOCUSED_WORK })
    }
}
