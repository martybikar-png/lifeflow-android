package com.lifeflow.security.audit

import java.time.Instant

internal enum class SecurityIncidentLevel {
    NORMAL,
    OBSERVE,
    ELEVATED,
    CRITICAL
}

internal data class SecurityIncidentSignalSnapshot(
    val generatedAt: Instant,
    val incidentLevel: SecurityIncidentLevel,
    val totalEvents: Int,
    val criticalCount: Int,
    val warningCount: Int,
    val infoCount: Int,
    val activeCompromiseSignal: Boolean,
    val repeatedAuthFailureSignal: Boolean,
    val repeatedPolicyViolationSignal: Boolean,
    val latestEventType: SecurityAuditLog.EventType?,
    val latestCriticalEventType: SecurityAuditLog.EventType?,
    val triggerCodes: Set<String>
)

internal object SecurityAuditIncidentSignalAnalyzer {

    private const val AUTH_FAILURE_BURST_THRESHOLD = 3
    private const val POLICY_VIOLATION_BURST_THRESHOLD = 2

    private val compromiseEventTypes = setOf(
        SecurityAuditLog.EventType.TRUST_COMPROMISED,
        SecurityAuditLog.EventType.HARDENING_CHECK_FAILED,
        SecurityAuditLog.EventType.ROOT_DETECTED,
        SecurityAuditLog.EventType.DEBUGGER_DETECTED,
        SecurityAuditLog.EventType.EMULATOR_DETECTED,
        SecurityAuditLog.EventType.INSTRUMENTATION_DETECTED,
        SecurityAuditLog.EventType.TAMPER_DETECTED,
        SecurityAuditLog.EventType.SIGNATURE_INVALID,
        SecurityAuditLog.EventType.INTEGRITY_FAILED,
        SecurityAuditLog.EventType.POLICY_VIOLATION,
        SecurityAuditLog.EventType.INVARIANT_VIOLATION
    )

    fun snapshot(
        entries: List<SecurityAuditLog.AuditEntry>,
        generatedAt: Instant = Instant.now()
    ): SecurityIncidentSignalSnapshot {
        val criticalCount = entries.count { it.severity == SecurityAuditLog.Severity.CRITICAL }
        val warningCount = entries.count { it.severity == SecurityAuditLog.Severity.WARNING }
        val infoCount = entries.count { it.severity == SecurityAuditLog.Severity.INFO }

        val activeCompromiseSignal = entries.any { it.eventType in compromiseEventTypes }

        val authFailureCount = entries.count {
            it.eventType == SecurityAuditLog.EventType.AUTH_FAILURE ||
                it.eventType == SecurityAuditLog.EventType.AUTH_LOCKOUT
        }
        val repeatedAuthFailureSignal = authFailureCount >= AUTH_FAILURE_BURST_THRESHOLD

        val policyViolationCount = entries.count {
            it.eventType == SecurityAuditLog.EventType.POLICY_VIOLATION ||
                it.eventType == SecurityAuditLog.EventType.INVARIANT_VIOLATION
        }
        val repeatedPolicyViolationSignal =
            policyViolationCount >= POLICY_VIOLATION_BURST_THRESHOLD

        val triggerCodes = linkedSetOf<String>().apply {
            if (activeCompromiseSignal) add("ACTIVE_COMPROMISE_SIGNAL")
            if (repeatedAuthFailureSignal) add("AUTH_FAILURE_BURST")
            if (repeatedPolicyViolationSignal) add("POLICY_VIOLATION_BURST")
            if (!activeCompromiseSignal && !repeatedAuthFailureSignal &&
                !repeatedPolicyViolationSignal && warningCount > 0
            ) {
                add("WARNING_ACTIVITY")
            }
        }

        val incidentLevel = when {
            activeCompromiseSignal -> SecurityIncidentLevel.CRITICAL
            repeatedAuthFailureSignal || repeatedPolicyViolationSignal || criticalCount > 0 ->
                SecurityIncidentLevel.ELEVATED
            warningCount > 0 -> SecurityIncidentLevel.OBSERVE
            else -> SecurityIncidentLevel.NORMAL
        }

        return SecurityIncidentSignalSnapshot(
            generatedAt = generatedAt,
            incidentLevel = incidentLevel,
            totalEvents = entries.size,
            criticalCount = criticalCount,
            warningCount = warningCount,
            infoCount = infoCount,
            activeCompromiseSignal = activeCompromiseSignal,
            repeatedAuthFailureSignal = repeatedAuthFailureSignal,
            repeatedPolicyViolationSignal = repeatedPolicyViolationSignal,
            latestEventType = entries.lastOrNull()?.eventType,
            latestCriticalEventType = entries.lastOrNull {
                it.severity == SecurityAuditLog.Severity.CRITICAL
            }?.eventType,
            triggerCodes = triggerCodes
        )
    }
}
