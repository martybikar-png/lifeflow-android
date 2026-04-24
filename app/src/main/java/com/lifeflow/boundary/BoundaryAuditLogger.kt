package com.lifeflow.boundary

import com.lifeflow.domain.core.boundary.BoundaryAccessOutcome
import com.lifeflow.domain.core.boundary.BoundaryAuditExpectation
import com.lifeflow.domain.core.boundary.BoundaryDecision
import com.lifeflow.security.audit.SecurityAuditLog
import java.util.concurrent.ConcurrentHashMap

internal object BoundaryAuditLogger {

    private val emittedFingerprints = ConcurrentHashMap.newKeySet<String>()

    fun log(decision: BoundaryDecision) {
        val record = decision.auditRecord ?: return
        if (record.expectation == BoundaryAuditExpectation.NONE) return

        val fingerprint = listOf(
            record.boundaryKey,
            record.outcome.name,
            record.entitlementTier.name,
            record.entitlementStatus.name,
            record.reasonCode,
            record.expectation.name
        ).joinToString("|")

        if (!emittedFingerprints.add(fingerprint)) {
            return
        }

        val message =
            "Boundary audit: ${record.boundaryKey} -> ${record.outcome.name} " +
                "[${record.reasonCode}] expectation=${record.expectation.name} " +
                "tier=${record.entitlementTier.name} status=${record.entitlementStatus.name}"

        when {
            record.outcome == BoundaryAccessOutcome.ALLOW -> {
                SecurityAuditLog.info(
                    SecurityAuditLog.EventType.POLICY_VIOLATION,
                    message
                )
            }
            record.expectation == BoundaryAuditExpectation.LOG_ON_BLOCKED_ACTION -> {
                SecurityAuditLog.warning(
                    SecurityAuditLog.EventType.POLICY_VIOLATION,
                    message
                )
            }
            else -> {
                SecurityAuditLog.info(
                    SecurityAuditLog.EventType.POLICY_VIOLATION,
                    message
                )
            }
        }
    }

    fun clear() {
        emittedFingerprints.clear()
    }
}
