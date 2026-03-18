package com.lifeflow.domain.memory

/**
 * SecondBrainNeuralNetwork V1 — personal memory + context continuity.
 *
 * Stores and retrieves meaningful context entries.
 * V1 = local tagged memory. No ML, no cloud sync.
 * Significance scoring is rule-based only.
 */
data class MemoryEntry(
    val id: String,
    val timestampEpochMillis: Long,
    val content: String,
    val tags: Set<MemoryTag>,
    val significance: MemorySignificance
)

enum class MemoryTag {
    HEALTH,
    FOCUS,
    DECISION,
    INSIGHT,
    GOAL,
    RELATIONSHIP,
    CREATIVE,
    STRESS,
    RECOVERY,
    MILESTONE
}

enum class MemorySignificance {
    LOW,
    MEDIUM,
    HIGH
}

data class SecondBrainState(
    val recentEntries: List<MemoryEntry>,
    val pinnedEntries: List<MemoryEntry>,
    val readiness: MemoryReadiness
)

enum class MemoryReadiness {
    BLOCKED,
    EMPTY,
    READY
}
