package com.lifeflow.domain.timeline

data class AdaptiveTimelineState(
    val readiness: TimelineReadiness,
    val prioritySignal: PrioritySignal,
    val focusWindow: FocusWindow,
    val notes: List<String> = emptyList()
)

enum class TimelineReadiness {
    BLOCKED,
    INSUFFICIENT_DATA,
    DEGRADED,
    READY
}

enum class PrioritySignal {
    UNKNOWN,
    DEFER_DEMANDING,
    LIGHT_TASKS,
    FULL_CAPACITY
}

enum class FocusWindow {
    UNKNOWN,
    SHORT,
    MEDIUM,
    EXTENDED
}