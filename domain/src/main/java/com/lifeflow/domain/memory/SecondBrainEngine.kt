package com.lifeflow.domain.memory

/**
 * SecondBrainEngine V1 — memory retrieval + significance scoring.
 *
 * Input:  List<MemoryEntry> + identityInitialized flag
 * Output: SecondBrainState with recent + pinned entries
 *
 * Fail-closed on missing identity. No ML. Rule-based only.
 */
class SecondBrainEngine {

    fun compute(
        entries: List<MemoryEntry>,
        identityInitialized: Boolean
    ): SecondBrainState {
        if (!identityInitialized) {
            return SecondBrainState(
                recentEntries = emptyList(),
                pinnedEntries = emptyList(),
                readiness = MemoryReadiness.BLOCKED
            )
        }

        if (entries.isEmpty()) {
            return SecondBrainState(
                recentEntries = emptyList(),
                pinnedEntries = emptyList(),
                readiness = MemoryReadiness.EMPTY
            )
        }

        val sorted = entries.sortedByDescending { it.timestampEpochMillis }

        val recent = sorted.take(MAX_RECENT_ENTRIES)

        val pinned = sorted
            .filter { it.significance == MemorySignificance.HIGH }
            .take(MAX_PINNED_ENTRIES)

        return SecondBrainState(
            recentEntries = recent,
            pinnedEntries = pinned,
            readiness = MemoryReadiness.READY
        )
    }

    companion object {
        private const val MAX_RECENT_ENTRIES = 10
        private const val MAX_PINNED_ENTRIES = 5
    }
}
