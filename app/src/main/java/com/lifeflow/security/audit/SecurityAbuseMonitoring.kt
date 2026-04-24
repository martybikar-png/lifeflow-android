package com.lifeflow.security.audit

import com.lifeflow.security.TrustState
import java.time.Instant

internal enum class SecurityAbuseMonitoringLevel {
    NORMAL,
    OBSERVE,
    THROTTLE,
    ESCALATE
}

internal enum class SecurityAbuseMonitoringCode {
    WARNING_ACTIVITY,
    AUTH_FAILURE_THROTTLE,
    POLICY_REVIEW,
    COMPROMISE_ESCALATION,
    TRUST_DEGRADED_GUARD,
    TRUST_COMPROMISED_ESCALATION,
    MONITORING_NOTIFICATION
}

internal data class SecurityAbuseMonitoringSnapshot(
    val generatedAt: Instant,
    val monitoringLevel: SecurityAbuseMonitoringLevel,
    val incidentLevel: SecurityIncidentLevel,
    val currentTrustState: TrustState,
    val shouldThrottleAuthentication: Boolean,
    val shouldRequireOperatorReview: Boolean,
    val shouldThrottleSensitiveOperations: Boolean,
    val notifyMonitoring: Boolean,
    val monitoringCodes: Set<SecurityAbuseMonitoringCode>
)

internal object SecurityAbuseMonitoringAnalyzer {

    fun snapshot(
        incident: SecurityIncidentSignalSnapshot,
        currentTrustState: TrustState,
        generatedAt: Instant = incident.generatedAt
    ): SecurityAbuseMonitoringSnapshot {
        val shouldThrottleAuthentication =
            incident.repeatedAuthFailureSignal ||
                currentTrustState == TrustState.DEGRADED

        val shouldRequireOperatorReview =
            incident.activeCompromiseSignal ||
                incident.repeatedPolicyViolationSignal ||
                currentTrustState == TrustState.COMPROMISED

        val shouldThrottleSensitiveOperations =
            incident.incidentLevel == SecurityIncidentLevel.ELEVATED ||
                incident.incidentLevel == SecurityIncidentLevel.CRITICAL ||
                currentTrustState != TrustState.VERIFIED

        val monitoringLevel = when {
            shouldRequireOperatorReview -> SecurityAbuseMonitoringLevel.ESCALATE
            shouldThrottleAuthentication || shouldThrottleSensitiveOperations ->
                SecurityAbuseMonitoringLevel.THROTTLE
            incident.incidentLevel == SecurityIncidentLevel.OBSERVE ->
                SecurityAbuseMonitoringLevel.OBSERVE
            else ->
                SecurityAbuseMonitoringLevel.NORMAL
        }

        val monitoringCodes = linkedSetOf<SecurityAbuseMonitoringCode>().apply {
            if (SecurityIncidentTriggerCode.WARNING_ACTIVITY in incident.triggerCodes) {
                add(SecurityAbuseMonitoringCode.WARNING_ACTIVITY)
            }
            if (incident.repeatedAuthFailureSignal) {
                add(SecurityAbuseMonitoringCode.AUTH_FAILURE_THROTTLE)
            }
            if (incident.repeatedPolicyViolationSignal) {
                add(SecurityAbuseMonitoringCode.POLICY_REVIEW)
            }
            if (incident.activeCompromiseSignal) {
                add(SecurityAbuseMonitoringCode.COMPROMISE_ESCALATION)
            }
            if (currentTrustState == TrustState.DEGRADED) {
                add(SecurityAbuseMonitoringCode.TRUST_DEGRADED_GUARD)
            }
            if (currentTrustState == TrustState.COMPROMISED) {
                add(SecurityAbuseMonitoringCode.TRUST_COMPROMISED_ESCALATION)
            }
        }

        val notifyMonitoring =
            monitoringLevel == SecurityAbuseMonitoringLevel.THROTTLE ||
                monitoringLevel == SecurityAbuseMonitoringLevel.ESCALATE

        if (notifyMonitoring) {
            monitoringCodes += SecurityAbuseMonitoringCode.MONITORING_NOTIFICATION
        }

        return SecurityAbuseMonitoringSnapshot(
            generatedAt = generatedAt,
            monitoringLevel = monitoringLevel,
            incidentLevel = incident.incidentLevel,
            currentTrustState = currentTrustState,
            shouldThrottleAuthentication = shouldThrottleAuthentication,
            shouldRequireOperatorReview = shouldRequireOperatorReview,
            shouldThrottleSensitiveOperations = shouldThrottleSensitiveOperations,
            notifyMonitoring = notifyMonitoring,
            monitoringCodes = monitoringCodes
        )
    }
}
