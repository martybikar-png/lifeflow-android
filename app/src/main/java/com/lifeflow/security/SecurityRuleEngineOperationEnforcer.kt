package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal class SecurityRuleEngineOperationEnforcer(
    private val stateStore: SecurityRuleEngineStateStore,
    private val auditStore: SecurityRuleEngineAuditStore,
    private val denyCounter: SecurityRuleEngineDenyCounter,
    private val transitionController: SecurityRuleEngineTransitionController
) {
    fun requireAllowed(
        operation: DomainOperation,
        reason: String
    ) {
        val state = stateStore.get()
        val decision = SecurityRuntimeOperationGate.evaluate(operation)

        when (decision.outcome) {
            SecurityRuntimeOperationOutcome.ALLOWED -> {
                if (state == TrustState.VERIFIED) {
                    denyCounter.clear()
                }
                auditStore.record(SecurityRuleEngine.Decision.ALLOW, operation, reason, state)
                return
            }

            SecurityRuntimeOperationOutcome.DENIED -> {
                val denialCode =
                    SecurityRuntimeOperationGate.denialCodeName(decision)
                auditStore.record(
                    SecurityRuleEngine.Decision.DENY,
                    operation,
                    "$denialCode: $reason",
                    state
                )

                if (state == TrustState.VERIFIED &&
                    denyCounter.incrementAndReachedThreshold()
                ) {
                    transitionController.transitionTo(
                        newState = TrustState.DEGRADED,
                        decision = SecurityRuleEngine.Decision.DENY,
                        operation = operation,
                        reason = "AUTO_DEGRADE: deny threshold reached (${denyCounter.threshold})"
                    )
                }

                throw SecurityRuntimeOperationGate.deniedException(
                    operation = operation,
                    decision = decision,
                    reason = reason
                )
            }

            SecurityRuntimeOperationOutcome.LOCKED -> {
                val denialCode =
                    SecurityRuntimeOperationGate.denialCodeName(decision)

                if (SecurityRuntimeOperationGate.isCompromisedLockdown(decision)) {
                    SecurityAccessSession.clear()
                    auditStore.record(
                        SecurityRuleEngine.Decision.DENY,
                        operation,
                        "LOCKDOWN(COMPROMISED): $reason",
                        state
                    )
                    throw SecurityRuntimeOperationGate.deniedException(
                        operation = operation,
                        decision = decision,
                        reason = reason
                    )
                }

                auditStore.record(
                    SecurityRuleEngine.Decision.DENY,
                    operation,
                    "LOCKDOWN($denialCode): $reason",
                    state
                )
                throw SecurityRuntimeOperationGate.deniedException(
                    operation = operation,
                    decision = decision,
                    reason = reason
                )
            }
        }
    }
}