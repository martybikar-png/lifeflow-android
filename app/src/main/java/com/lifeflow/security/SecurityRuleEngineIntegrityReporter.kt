package com.lifeflow.security

internal class SecurityRuleEngineIntegrityReporter(
    private val stateStore: SecurityRuleEngineStateStore,
    private val auditStore: SecurityRuleEngineAuditStore,
    private val transitionController: SecurityRuleEngineTransitionController
) {
    fun reportIntegrityTrustDecision(
        response: IntegrityTrustVerdictResponse
    ) {
        val current = stateStore.get()

        if (current == TrustState.COMPROMISED &&
            response.verdict != SecurityIntegrityTrustVerdict.COMPROMISED &&
            response.decision != IntegrityTrustDecision.LOCK
        ) {
            auditStore.record(
                decision = SecurityRuleEngine.Decision.DENY,
                operation = null,
                reason =
                    "DENY_ZERO_TRUST_OVERRIDE: COMPROMISED -> ${response.decision}/${response.verdict} blocked. " +
                        response.reason,
                state = current
            )
            SecurityAccessSession.clear()
            return
        }

        when (response.decision) {
            IntegrityTrustDecision.ALLOW -> {
                reportIntegrityTrustVerdict(
                    verdict = response.verdict,
                    reason = "ZERO_TRUST_ALLOW: ${response.reason}"
                )
            }

            IntegrityTrustDecision.STEP_UP -> {
                applyIntegrityStepUp(response)
            }

            IntegrityTrustDecision.DEGRADED -> {
                reportIntegrityTrustVerdict(
                    verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                    reason = "ZERO_TRUST_DEGRADED: ${response.reason}"
                )
            }

            IntegrityTrustDecision.DENY -> {
                applyIntegrityDeny(response)
            }

            IntegrityTrustDecision.LOCK -> {
                transitionController.transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_LOCK: ${response.reason}"
                )
            }
        }
    }

    fun reportIntegrityTrustVerdict(
        verdict: SecurityIntegrityTrustVerdict,
        reason: String
    ) {
        val current = stateStore.get()

        if (current == TrustState.COMPROMISED &&
            verdict != SecurityIntegrityTrustVerdict.COMPROMISED
        ) {
            auditStore.record(
                decision = SecurityRuleEngine.Decision.DENY,
                operation = null,
                reason = "DENY_INTEGRITY_OVERRIDE: COMPROMISED -> $verdict blocked. $reason",
                state = current
            )
            SecurityAccessSession.clear()
            return
        }

        when (verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED -> {
                transitionController.transitionTo(
                    newState = TrustState.VERIFIED,
                    decision = SecurityRuleEngine.Decision.ALLOW,
                    operation = null,
                    reason = "INTEGRITY_TRUST_VERIFIED: $reason"
                )
            }

            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionController.transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "INTEGRITY_TRUST_DEGRADED: $reason"
                )
            }

            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionController.transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "INTEGRITY_TRUST_COMPROMISED: $reason"
                )
            }
        }
    }

    private fun applyIntegrityStepUp(
        response: IntegrityTrustVerdictResponse
    ) {
        when (response.verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED -> {
                transitionController.transitionTo(
                    newState = TrustState.VERIFIED,
                    decision = SecurityRuleEngine.Decision.ALLOW,
                    operation = null,
                    reason = "ZERO_TRUST_STEP_UP: ${response.reason}"
                )
                SecurityAccessSession.clear()
            }

            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionController.transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_STEP_UP: ${response.reason}"
                )
            }

            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionController.transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_STEP_UP_INVALID: ${response.reason}"
                )
            }
        }
    }

    private fun applyIntegrityDeny(
        response: IntegrityTrustVerdictResponse
    ) {
        when (response.verdict) {
            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionController.transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_DENY: ${response.reason}"
                )
            }

            SecurityIntegrityTrustVerdict.VERIFIED,
            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionController.transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = SecurityRuleEngine.Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_DENY: ${response.reason}"
                )
            }
        }
    }
}