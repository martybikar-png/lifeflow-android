package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityIntegrityAuditExplainability

/**
 * Central authority for integrity trust verdict responses.
 *
 * Scope:
 * - normalize server verdict metadata / replay / policyVersion rules
 * - enforce structured claim restrictions
 * - write audit-ready integrity evidence
 * - apply the final zero-trust decision into SecurityRuleEngine
 *
 * SecurityRuleEngine stays focused on trust-state transitions and runtime rule enforcement.
 */
internal object SecurityIntegrityTrustAuthority {

    private val serverVerdictPolicy = SecurityIntegrityServerVerdictPolicy()

    @Synchronized
    fun clear() {
        serverVerdictPolicy.clear()
    }

    @Synchronized
    fun reportVerdictResponse(
        response: IntegrityTrustVerdictResponse,
        nowEpochMs: Long = System.currentTimeMillis()
    ) {
        val normalized = serverVerdictPolicy.normalize(
            response = response,
            nowEpochMs = nowEpochMs
        )

        recordSecurityAudit(normalized)

        SecurityRuleEngine.reportIntegrityTrustDecision(
            response = normalized
        )
    }

    private fun recordSecurityAudit(
        response: IntegrityTrustVerdictResponse
    ) {
        val snapshot = SecurityIntegrityAuditExplainability.snapshot(response)

        when (response.verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED -> {
                SecurityAuditLog.info(
                    eventType = snapshot.eventType,
                    message = snapshot.message,
                    metadata = snapshot.metadata
                )
            }

            SecurityIntegrityTrustVerdict.DEGRADED -> {
                SecurityAuditLog.warning(
                    eventType = snapshot.eventType,
                    message = snapshot.message,
                    metadata = snapshot.metadata
                )
            }

            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                SecurityAuditLog.critical(
                    eventType = snapshot.eventType,
                    message = snapshot.message,
                    metadata = snapshot.metadata
                )
            }
        }
    }
}
