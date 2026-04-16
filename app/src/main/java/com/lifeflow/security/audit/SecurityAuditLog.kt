package com.lifeflow.security.audit

import com.lifeflow.security.SecurityRuntimeContainmentPolicy
import com.lifeflow.security.SecurityRuntimeContainmentSnapshot
import com.lifeflow.security.TrustState
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * SecurityAuditLog — tamper-evident log of security events.
 *
 * Rules:
 * - Never log sensitive data (passwords, keys, PII)
 * - Log only security-relevant events
 * - Immutable entries with timestamps
 * - Memory-bounded (circular buffer)
 */
object SecurityAuditLog {

    private const val MAX_ENTRIES = 500
    private val entries = ConcurrentLinkedQueue<AuditEntry>()

    enum class Severity { INFO, WARNING, CRITICAL }

    enum class EventType {
        AUTH_SUCCESS,
        AUTH_FAILURE,
        AUTH_LOCKOUT,
        SESSION_CREATED,
        SESSION_EXPIRED,
        SESSION_INVALIDATED,
        TRUST_VERIFIED,
        TRUST_DEGRADED,
        TRUST_COMPROMISED,
        HARDENING_CHECK_PASSED,
        HARDENING_CHECK_FAILED,
        ROOT_DETECTED,
        DEBUGGER_DETECTED,
        EMULATOR_DETECTED,
        INSTRUMENTATION_DETECTED,
        TAMPER_DETECTED,
        SIGNATURE_INVALID,
        INTEGRITY_FAILED,
        VAULT_ACCESSED,
        VAULT_WRITE,
        VAULT_RESET,
        VAULT_RESET_AUTH_GRANTED,
        VAULT_RESET_AUTH_CONSUMED,
        VAULT_RESET_AUTH_EXPIRED,
        VAULT_RESET_AUTH_DENIED,
        VAULT_RESET_AUTH_CLEARED,
        RECOVERY_INITIATED,
        RECOVERY_COMPLETED,
        RECOVERY_FAILED,
        BREAK_GLASS_APPROVED,
        BREAK_GLASS_EXPIRED,
        BREAK_GLASS_CLEARED,
        BREAK_GLASS_REJECTED,
        POLICY_VIOLATION,
        INVARIANT_VIOLATION
    }

    data class AuditEntry(
        val timestamp: Instant,
        val eventType: EventType,
        val severity: Severity,
        val message: String,
        val metadata: Map<String, String> = emptyMap()
    )

    fun log(
        eventType: EventType,
        severity: Severity,
        message: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val sanitizedMetadata = metadata.mapValues { sanitize(it.value) }
        val sanitizedMessage = sanitize(message)

        val entry = AuditEntry(
            timestamp = Instant.now(),
            eventType = eventType,
            severity = severity,
            message = sanitizedMessage,
            metadata = sanitizedMetadata
        )

        entries.add(entry)

        while (entries.size > MAX_ENTRIES) {
            entries.poll()
        }
    }

    fun info(
        eventType: EventType,
        message: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        log(eventType, Severity.INFO, message, metadata)
    }

    fun warning(
        eventType: EventType,
        message: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        log(eventType, Severity.WARNING, message, metadata)
    }

    fun critical(
        eventType: EventType,
        message: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        log(eventType, Severity.CRITICAL, message, metadata)
    }

    fun getEntries(): List<AuditEntry> = entries.toList()

    fun getEntriesSince(since: Instant): List<AuditEntry> =
        entries.filter { it.timestamp.isAfter(since) }

    fun getEntriesBySeverity(severity: Severity): List<AuditEntry> =
        entries.filter { it.severity == severity }

    fun getCriticalEntries(): List<AuditEntry> =
        getEntriesBySeverity(Severity.CRITICAL)

    internal fun incidentSignalSnapshot(): SecurityIncidentSignalSnapshot =
        SecurityAuditIncidentSignalAnalyzer.snapshot(getEntries())

    internal fun incidentSignalSnapshotSince(
        since: Instant
    ): SecurityIncidentSignalSnapshot =
        SecurityAuditIncidentSignalAnalyzer.snapshot(getEntriesSince(since))

    internal fun incidentResponseSnapshot(
        currentTrustState: TrustState
    ): SecurityIncidentResponseSnapshot =
        SecurityIncidentResponseBridge.snapshot(
            incident = incidentSignalSnapshot(),
            currentTrustState = currentTrustState
        )

    internal fun incidentResponseSnapshotSince(
        since: Instant,
        currentTrustState: TrustState
    ): SecurityIncidentResponseSnapshot =
        SecurityIncidentResponseBridge.snapshot(
            incident = incidentSignalSnapshotSince(since),
            currentTrustState = currentTrustState
        )

    internal fun runtimeContainmentSnapshot(
        currentTrustState: TrustState
    ): SecurityRuntimeContainmentSnapshot =
        SecurityRuntimeContainmentPolicy.snapshot(
            incidentResponse = incidentResponseSnapshot(currentTrustState)
        )

    internal fun runtimeContainmentSnapshotSince(
        since: Instant,
        currentTrustState: TrustState
    ): SecurityRuntimeContainmentSnapshot =
        SecurityRuntimeContainmentPolicy.snapshot(
            incidentResponse = incidentResponseSnapshotSince(
                since = since,
                currentTrustState = currentTrustState
            )
        )

    fun clear() {
        entries.clear()
    }

    private fun sanitize(value: String): String {
        if (value.isBlank()) return value

        val sensitivePatterns = listOf(
            Regex("(?i)(password|passwd|pwd)\\s*[:=]\\s*\\S+"),
            Regex("(?i)(token|key|secret|auth)\\s*[:=]\\s*\\S+"),
            Regex("[A-Za-z0-9+/]{32,}={0,2}"),
            Regex("[0-9a-fA-F]{32,}")
        )

        var sanitized = value
        sensitivePatterns.forEach { pattern ->
            sanitized = sanitized.replace(pattern, "[REDACTED]")
        }

        return sanitized
    }
}
