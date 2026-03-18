package com.lifeflow.domain.connection

/**
 * IntimacyConnectionFlow V1 — relational layer.
 *
 * Tracks connection quality and relationship signals.
 * V1 = rule-based signal detection only.
 * Sensitive layer — fail-closed on missing identity.
 */
data class ConnectionEntry(
    val id: String,
    val timestampEpochMillis: Long,
    val personTag: String,
    val signal: ConnectionSignal,
    val depth: ConnectionDepth
)

enum class ConnectionSignal {
    MEANINGFUL_INTERACTION,
    SURFACE_INTERACTION,
    CONFLICT,
    SUPPORT_GIVEN,
    SUPPORT_RECEIVED,
    MISSED_CONNECTION,
    BOUNDARY_SET,
    BOUNDARY_CROSSED
}

enum class ConnectionDepth {
    SHALLOW,
    MODERATE,
    DEEP
}

data class ConnectionState(
    val recentEntries: List<ConnectionEntry>,
    val dominantSignal: ConnectionSignal?,
    val connectionNote: String?,
    val readiness: ConnectionReadiness
)

enum class ConnectionReadiness {
    BLOCKED,
    EMPTY,
    READY
}
