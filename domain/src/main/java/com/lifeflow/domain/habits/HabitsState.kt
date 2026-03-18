package com.lifeflow.domain.habits

import com.lifeflow.domain.timeline.FocusWindow
import com.lifeflow.domain.timeline.PrioritySignal

data class HabitsState(
    val suggestedSlots: List<HabitSlot>,
    val readiness: HabitsReadiness,
    val notes: List<String> = emptyList()
)

data class HabitSlot(
    val type: HabitType,
    val durationMinutes: Int,
    val priority: HabitPriority
)

enum class HabitsReadiness { BLOCKED, INSUFFICIENT_DATA, READY }

enum class HabitType {
    LIGHT_MOVEMENT, FOCUSED_WORK, REST, DEEP_WORK, SOCIAL, CREATIVE
}

enum class HabitPriority { LOW, MEDIUM, HIGH }

internal fun defaultFocusDuration(focusWindow: FocusWindow): Int {
    return when (focusWindow) {
        FocusWindow.SHORT -> 20
        FocusWindow.MEDIUM -> 40
        FocusWindow.EXTENDED -> 55
        FocusWindow.UNKNOWN -> 20
    }
}

internal fun suggestSlots(
    prioritySignal: PrioritySignal,
    focusWindow: FocusWindow
): List<HabitSlot> {
    val duration = defaultFocusDuration(focusWindow)
    return when (prioritySignal) {
        PrioritySignal.UNKNOWN,
        PrioritySignal.DEFER_DEMANDING -> listOf(
            HabitSlot(HabitType.LIGHT_MOVEMENT, 10, HabitPriority.HIGH),
            HabitSlot(HabitType.REST, 15, HabitPriority.MEDIUM)
        )
        PrioritySignal.LIGHT_TASKS -> listOf(
            HabitSlot(HabitType.LIGHT_MOVEMENT, 15, HabitPriority.HIGH),
            HabitSlot(HabitType.FOCUSED_WORK, duration, HabitPriority.MEDIUM),
            HabitSlot(HabitType.REST, 10, HabitPriority.LOW)
        )
        PrioritySignal.FULL_CAPACITY -> listOf(
            HabitSlot(HabitType.LIGHT_MOVEMENT, 15, HabitPriority.MEDIUM),
            HabitSlot(HabitType.DEEP_WORK, duration, HabitPriority.HIGH),
            HabitSlot(HabitType.CREATIVE, 20, HabitPriority.MEDIUM),
            HabitSlot(HabitType.REST, 10, HabitPriority.LOW)
        )
    }
}
