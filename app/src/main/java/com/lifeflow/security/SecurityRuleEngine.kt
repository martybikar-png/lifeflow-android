package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.security.audit.SecurityAuditLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase VI — Automatic Trust Transitions (V1)
 *
 * - Deny-by-default
 * - Trust state remains the authoritative security posture state
 * - Runtime action enforcement is delegated to SecurityRuntimeOperationGate
 * - Raw integrity verdict response normalization/audit is delegated to
 *   SecurityIntegrityTrustAuthority
 * - COMPROMISED: deny everything + clear session (immediate lockdown)
 *
 * Automatic transitions:
 * - Repeated DENY (while VERIFIED) -> DEGRADED
 * - Crypto/integrity failure -> COMPROMISED (fail-closed)
 *
 * Phase VI.B:
 * - Exposes trustState as StateFlow so UI can fail-closed immediately.
 * - Applies server zero-trust decisions (ALLOW / STEP_UP / DEGRADED / DENY / LOCK)
 *   on top of normalized integrity verdicts.
 */
object SecurityRuleEngine {

    enum class Decision { ALLOW, DENY }

    data class AuditEvent(
        val timestampEpochMs: Long,
        val decision: Decision,
        val operation: DomainOperation?,
        val reason: String,
        val trustState: TrustState
    )

    private const val MAX_EVENTS = 200
    private const val DENY_THRESHOLD_TO_DEGRADE = 3

    private val auditEvents: ArrayDeque<AuditEvent> = ArrayDeque(MAX_EVENTS)

    private var denyCount: Int = 0

    private val _trustState = MutableStateFlow(TrustState.DEGRADED)
    val trustState: StateFlow<TrustState> = _trustState.asStateFlow()

    @Synchronized
    private fun record(
        decision: Decision,
        operation: DomainOperation?,
        reason: String,
        state: TrustState
    ) {
        if (auditEvents.size >= MAX_EVENTS) auditEvents.removeFirst()
        auditEvents.addLast(
            AuditEvent(
                timestampEpochMs = System.currentTimeMillis(),
                decision = decision,
                operation = operation,
                reason = reason,
                trustState = state
            )
        )
    }

    @Suppress("unused")
    @Synchronized
    fun getRecentEvents(): List<AuditEvent> = auditEvents.toList()

    @Suppress("unused")
    @Synchronized
    fun clearAudit() {
        auditEvents.clear()
        denyCount = 0
        SecurityIntegrityTrustAuthority.clear()
        SecurityAuditLog.clear()
    }

    @Suppress("unused")
    fun getTrustState(): TrustState = _trustState.value

    @Synchronized
    private fun transitionTo(
        newState: TrustState,
        decision: Decision,
        operation: DomainOperation?,
        reason: String
    ) {
        _trustState.value = newState
        denyCount = 0

        record(
            decision = decision,
            operation = operation,
            reason = reason,
            state = newState
        )

        if (newState != TrustState.VERIFIED) {
            SecurityAccessSession.clear()
        }
    }

    @Suppress("unused")
    @Synchronized
    fun setTrustState(state: TrustState, reason: String) {
        val current = _trustState.value

        if (current == TrustState.COMPROMISED && state != TrustState.COMPROMISED) {
            record(
                decision = Decision.DENY,
                operation = null,
                reason = "DENY_TRUST_OVERRIDE: COMPROMISED -> $state blocked. $reason",
                state = current
            )
            SecurityAccessSession.clear()
            return
        }

        transitionTo(
            newState = state,
            decision = Decision.ALLOW,
            operation = null,
            reason = "TrustState set to $state: $reason"
        )
    }

    @Synchronized
    internal fun reportIntegrityTrustDecision(
        response: IntegrityTrustVerdictResponse
    ) {
        val current = _trustState.value

        if (current == TrustState.COMPROMISED &&
            response.verdict != SecurityIntegrityTrustVerdict.COMPROMISED &&
            response.decision != IntegrityTrustDecision.LOCK
        ) {
            record(
                decision = Decision.DENY,
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
                transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_LOCK: ${response.reason}"
                )
            }
        }
    }

    @Synchronized
    internal fun reportIntegrityTrustVerdict(
        verdict: SecurityIntegrityTrustVerdict,
        reason: String
    ) {
        val current = _trustState.value

        if (current == TrustState.COMPROMISED &&
            verdict != SecurityIntegrityTrustVerdict.COMPROMISED
        ) {
            record(
                decision = Decision.DENY,
                operation = null,
                reason = "DENY_INTEGRITY_OVERRIDE: COMPROMISED -> $verdict blocked. $reason",
                state = current
            )
            SecurityAccessSession.clear()
            return
        }

        when (verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED -> {
                transitionTo(
                    newState = TrustState.VERIFIED,
                    decision = Decision.ALLOW,
                    operation = null,
                    reason = "INTEGRITY_TRUST_VERIFIED: $reason"
                )
            }

            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "INTEGRITY_TRUST_DEGRADED: $reason"
                )
            }

            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "INTEGRITY_TRUST_COMPROMISED: $reason"
                )
            }
        }
    }

    @Synchronized
    private fun applyIntegrityStepUp(
        response: IntegrityTrustVerdictResponse
    ) {
        when (response.verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED -> {
                transitionTo(
                    newState = TrustState.VERIFIED,
                    decision = Decision.ALLOW,
                    operation = null,
                    reason = "ZERO_TRUST_STEP_UP: ${response.reason}"
                )
                SecurityAccessSession.clear()
            }

            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_STEP_UP: ${response.reason}"
                )
            }

            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_STEP_UP_INVALID: ${response.reason}"
                )
            }
        }
    }

    @Synchronized
    private fun applyIntegrityDeny(
        response: IntegrityTrustVerdictResponse
    ) {
        when (response.verdict) {
            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_DENY: ${response.reason}"
                )
            }

            SecurityIntegrityTrustVerdict.VERIFIED,
            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = Decision.DENY,
                    operation = null,
                    reason = "ZERO_TRUST_DENY: ${response.reason}"
                )
            }
        }
    }

    @Synchronized
    internal fun reportRuntimeCompromise(reason: String) {
        if (_trustState.value == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
            return
        }

        transitionTo(
            newState = TrustState.COMPROMISED,
            decision = Decision.DENY,
            operation = null,
            reason = "RUNTIME_COMPROMISE: $reason"
        )
    }

    @Synchronized
    internal fun forceResetForAdversarialSuite(
        state: TrustState,
        reason: String
    ) {
        auditEvents.clear()
        denyCount = 0
        SecurityIntegrityTrustAuthority.clear()
        _trustState.value = state

        if (state != TrustState.VERIFIED) {
            SecurityAccessSession.clear()
        }

        record(
            decision = Decision.ALLOW,
            operation = null,
            reason = "ADVERSARIAL_SUITE_FORCE_RESET: $reason -> $state",
            state = state
        )
    }

    @Synchronized
    internal fun recoverAfterVaultReset(reason: String) {
        auditEvents.clear()
        denyCount = 0
        SecurityIntegrityTrustAuthority.clear()
        _trustState.value = TrustState.DEGRADED
        SecurityAccessSession.clear()

        record(
            decision = Decision.ALLOW,
            operation = null,
            reason = "VAULT_RESET_RECOVERY: $reason -> DEGRADED",
            state = TrustState.DEGRADED
        )
    }

    @Synchronized
    fun reportCryptoFailure(
        operation: DomainOperation,
        reason: String,
        throwable: Throwable
    ) {
        transitionTo(
            newState = TrustState.COMPROMISED,
            decision = Decision.DENY,
            operation = operation,
            reason = "CRYPTO_FAILURE: $reason (${throwable::class.java.simpleName}: ${throwable.message})"
        )
    }

    @Synchronized
    fun requireAllowed(
        operation: DomainOperation,
        reason: String
    ) {
        val state = _trustState.value
        val decision = SecurityRuntimeOperationGate.evaluate(operation)

        when (decision.outcome) {
            SecurityRuntimeOperationOutcome.ALLOWED -> {
                if (state == TrustState.VERIFIED) {
                    denyCount = 0
                }
                record(Decision.ALLOW, operation, reason, state)
                return
            }

            SecurityRuntimeOperationOutcome.DENIED -> {
                val denialCode =
                    SecurityRuntimeOperationGate.denialCodeName(decision)
                record(Decision.DENY, operation, "$denialCode: $reason", state)

                if (state == TrustState.VERIFIED) {
                    denyCount += 1
                    if (denyCount >= DENY_THRESHOLD_TO_DEGRADE) {
                        transitionTo(
                            newState = TrustState.DEGRADED,
                            decision = Decision.DENY,
                            operation = operation,
                            reason = "AUTO_DEGRADE: deny threshold reached ($DENY_THRESHOLD_TO_DEGRADE)"
                        )
                    }
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
                    record(
                        Decision.DENY,
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

                record(Decision.DENY, operation, "LOCKDOWN($denialCode): $reason", state)
                throw SecurityRuntimeOperationGate.deniedException(
                    operation = operation,
                    decision = decision,
                    reason = reason
                )
            }
        }
    }
}
