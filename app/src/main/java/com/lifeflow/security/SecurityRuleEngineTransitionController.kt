package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation

internal class SecurityRuleEngineTransitionController(
    private val stateStore: SecurityRuleEngineStateStore,
    private val auditStore: SecurityRuleEngineAuditStore,
    private val denyCounter: SecurityRuleEngineDenyCounter
) {
    fun transitionTo(
        newState: TrustState,
        decision: SecurityRuleEngine.Decision,
        operation: DomainOperation?,
        reason: String
    ) {
        stateStore.set(newState)
        denyCounter.clear()

        auditStore.record(
            decision = decision,
            operation = operation,
            reason = reason,
            state = newState
        )

        if (newState != TrustState.VERIFIED) {
            SecurityAccessSession.clear()
        }
    }

    fun setTrustState(state: TrustState, reason: String) {
        val current = stateStore.get()

        if (current == TrustState.COMPROMISED && state != TrustState.COMPROMISED) {
            auditStore.record(
                decision = SecurityRuleEngine.Decision.DENY,
                operation = null,
                reason = "DENY_TRUST_OVERRIDE: COMPROMISED -> $state blocked. $reason",
                state = current
            )
            SecurityAccessSession.clear()
            return
        }

        transitionTo(
            newState = state,
            decision = SecurityRuleEngine.Decision.ALLOW,
            operation = null,
            reason = "TrustState set to $state: $reason"
        )
    }

    fun reportRuntimeCompromise(reason: String) {
        if (stateStore.get() == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
            return
        }

        transitionTo(
            newState = TrustState.COMPROMISED,
            decision = SecurityRuleEngine.Decision.DENY,
            operation = null,
            reason = "RUNTIME_COMPROMISE: $reason"
        )
    }

    fun forceResetForAdversarialSuite(
        state: TrustState,
        reason: String
    ) {
        auditStore.clear()
        denyCounter.clear()
        SecurityIntegrityTrustAuthority.clear()
        stateStore.set(state)

        if (state != TrustState.VERIFIED) {
            SecurityAccessSession.clear()
        }

        auditStore.record(
            decision = SecurityRuleEngine.Decision.ALLOW,
            operation = null,
            reason = "ADVERSARIAL_SUITE_FORCE_RESET: $reason -> $state",
            state = state
        )
    }

    fun recoverAfterVaultReset(reason: String) {
        auditStore.clear()
        denyCounter.clear()
        SecurityIntegrityTrustAuthority.clear()
        stateStore.set(TrustState.DEGRADED)
        SecurityAccessSession.clear()

        auditStore.record(
            decision = SecurityRuleEngine.Decision.ALLOW,
            operation = null,
            reason = "VAULT_RESET_RECOVERY: $reason -> DEGRADED",
            state = TrustState.DEGRADED
        )
    }

    fun reportCryptoFailure(
        operation: DomainOperation,
        reason: String,
        throwable: Throwable
    ) {
        transitionTo(
            newState = TrustState.COMPROMISED,
            decision = SecurityRuleEngine.Decision.DENY,
            operation = operation,
            reason = "CRYPTO_FAILURE: $reason (${throwable::class.java.simpleName}: ${throwable.message})"
        )
    }
}