package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal class SecurityRuleEngineAuditStore(
    private val maxEvents: Int
) {
    private val auditEvents: ArrayDeque<SecurityRuleEngine.AuditEvent> =
        ArrayDeque(maxEvents)

    fun record(
        decision: SecurityRuleEngine.Decision,
        operation: DomainOperation?,
        reason: String,
        state: TrustState
    ) {
        if (auditEvents.size >= maxEvents) {
            auditEvents.removeFirst()
        }

        auditEvents.addLast(
            SecurityRuleEngine.AuditEvent(
                timestampEpochMs = System.currentTimeMillis(),
                decision = decision,
                operation = operation,
                reason = reason,
                trustState = state
            )
        )
    }

    fun getRecentEvents(): List<SecurityRuleEngine.AuditEvent> =
        auditEvents.toList()

    fun clear() {
        auditEvents.clear()
    }
}