package com.lifeflow.domain.connection

/**
 * IntimacyConnectionEngine V1 — relational signal detection.
 *
 * Input:  List<ConnectionEntry> + identityInitialized flag
 * Output: ConnectionState with dominant signal + note
 *
 * Fail-closed on missing identity. Sensitive layer.
 */
class IntimacyConnectionEngine {

    fun compute(
        entries: List<ConnectionEntry>,
        identityInitialized: Boolean
    ): ConnectionState {
        if (!identityInitialized) {
            return ConnectionState(
                recentEntries = emptyList(),
                dominantSignal = null,
                connectionNote = "Connection layer blocked: identity not initialized.",
                readiness = ConnectionReadiness.BLOCKED
            )
        }

        val recent = entries
            .sortedByDescending { it.timestampEpochMillis }
            .take(MAX_RECENT_ENTRIES)

        if (recent.isEmpty()) {
            return ConnectionState(
                recentEntries = emptyList(),
                dominantSignal = null,
                connectionNote = "No connection entries yet.",
                readiness = ConnectionReadiness.EMPTY
            )
        }

        val dominantSignal = resolveDominantSignal(recent)
        val note = resolveConnectionNote(dominantSignal, recent)

        return ConnectionState(
            recentEntries = recent,
            dominantSignal = dominantSignal,
            connectionNote = note,
            readiness = ConnectionReadiness.READY
        )
    }

    private fun resolveDominantSignal(
        entries: List<ConnectionEntry>
    ): ConnectionSignal? {
        return entries
            .groupBy { it.signal }
            .mapValues { (_, group) ->
                group.sumOf { depthWeight(it.depth) }
            }
            .maxByOrNull { it.value }
            ?.key
    }

    private fun depthWeight(depth: ConnectionDepth): Int {
        return when (depth) {
            ConnectionDepth.SHALLOW -> 1
            ConnectionDepth.MODERATE -> 2
            ConnectionDepth.DEEP -> 3
        }
    }

    private fun resolveConnectionNote(
        dominantSignal: ConnectionSignal?,
        entries: List<ConnectionEntry>
    ): String {
        if (dominantSignal == null) return "No dominant connection pattern detected."

        val deepCount = entries.count {
            it.signal == dominantSignal && it.depth == ConnectionDepth.DEEP
        }

        return when (dominantSignal) {
            ConnectionSignal.MEANINGFUL_INTERACTION ->
                if (deepCount >= 2) "Strong meaningful connection pattern. Nurture these relationships."
                else "Meaningful interactions present. Good relational state."
            ConnectionSignal.SURFACE_INTERACTION ->
                "Surface interaction pattern. Consider deepening key connections."
            ConnectionSignal.CONFLICT ->
                if (deepCount >= 2) "Persistent conflict pattern detected. Consider resolution or boundary setting."
                else "Conflict signal present. Monitor and address if recurring."
            ConnectionSignal.SUPPORT_GIVEN ->
                "Support-giving pattern. Ensure your own needs are also met."
            ConnectionSignal.SUPPORT_RECEIVED ->
                "Support-receiving pattern. Acknowledge and reciprocate when ready."
            ConnectionSignal.MISSED_CONNECTION ->
                "Missed connection pattern. Reach out to maintain important relationships."
            ConnectionSignal.BOUNDARY_SET ->
                "Boundary-setting pattern. Healthy self-protection in progress."
            ConnectionSignal.BOUNDARY_CROSSED ->
                if (deepCount >= 2) "Persistent boundary crossing detected. Address directly or seek support."
                else "Boundary crossed signal present. Monitor closely."
        }
    }

    companion object {
        private const val MAX_RECENT_ENTRIES = 10
    }
}
