package com.lifeflow.domain.habits

import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.timeline.TimelineReadiness

/**
 * AutonomousHabitsEngine V1 — rule-based habit suggestion.
 * No autonomy yet. Deterministic rules only.
 */
class AutonomousHabitsEngine {

    fun compute(timelineState: AdaptiveTimelineState): HabitsState {
        val notes = mutableListOf<String>()

        val readiness = resolveReadiness(timelineState, notes)

        val slots = if (readiness == HabitsReadiness.READY) {
            suggestSlots(
                prioritySignal = timelineState.prioritySignal,
                focusWindow = timelineState.focusWindow
            )
        } else {
            emptyList()
        }

        return HabitsState(
            suggestedSlots = slots,
            readiness = readiness,
            notes = notes
        )
    }

    private fun resolveReadiness(
        timelineState: AdaptiveTimelineState,
        notes: MutableList<String>
    ): HabitsReadiness {
        return when (timelineState.readiness) {
            TimelineReadiness.BLOCKED -> {
                notes += "Habits blocked: timeline is blocked."
                HabitsReadiness.BLOCKED
            }
            TimelineReadiness.INSUFFICIENT_DATA -> {
                notes += "Habits unavailable: insufficient timeline data."
                HabitsReadiness.INSUFFICIENT_DATA
            }
            TimelineReadiness.DEGRADED -> {
                notes += "Habits limited: timeline is degraded."
                HabitsReadiness.READY
            }
            TimelineReadiness.READY -> HabitsReadiness.READY
        }
    }
}
