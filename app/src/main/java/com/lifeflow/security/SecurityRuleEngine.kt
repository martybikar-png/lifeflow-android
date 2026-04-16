package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase VI — Automatic Trust Transitions (V1)
 *
 * - Deny-by-default
 * - Trust state remains the authoritative security posture state
 * - Runtime action enforcement is delegated to SecurityRuntimeAccessPolicy
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
 */
object SecurityRuleEngine {

    enum class Decision { ALLOW, DENY }

    data class AuditEvent(
        val timestampEpochMs: Long,
        val decision: Decision,
        val action: RuleAction,
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
        action: RuleAction,
        reason: String,
        state: TrustState
    ) {
        if (auditEvents.size >= MAX_EVENTS) auditEvents.removeFirst()
        auditEvents.addLast(
            AuditEvent(
                timestampEpochMs = System.currentTimeMillis(),
                decision = decision,
                action = action,
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
        action: RuleAction,
        reason: String
    ) {
        _trustState.value = newState
        denyCount = 0

        record(
            decision = decision,
            action = action,
            reason = reason,
            state = newState
        )

        if (newState != TrustState.VERIFIED) {
            SecurityAccessSession.clear()
        }
    }

    /**
     * Explicit trust change.
     *
     * SECURITY RULE (B2):
     * If we're COMPROMISED, you cannot set VERIFIED/DEGRADED/EMERGENCY_LIMITED
     * via normal flows.
     * A dedicated recovery/reset flow must handle that later.
     */
    @Suppress("unused")
    @Synchronized
    fun setTrustState(state: TrustState, reason: String) {
        val current = _trustState.value

        if (current == TrustState.COMPROMISED && state != TrustState.COMPROMISED) {
            record(
                decision = Decision.DENY,
                action = RuleAction.READ_ACTIVE,
                reason = "DENY_TRUST_OVERRIDE: COMPROMISED -> $state blocked. $reason",
                state = current
            )
            SecurityAccessSession.clear()
            return
        }

        transitionTo(
            newState = state,
            decision = Decision.ALLOW,
            action = RuleAction.READ_ACTIVE,
            reason = "TrustState set to $state: $reason"
        )
    }

    /**
     * Applies a final integrity trust verdict.
     *
     * NOTE:
     * - raw integrity response handling lives in SecurityIntegrityTrustAuthority
     * - this method only applies the already normalized verdict to trust state
     */
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
                action = RuleAction.READ_ACTIVE,
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
                    action = RuleAction.READ_ACTIVE,
                    reason = "INTEGRITY_TRUST_VERIFIED: $reason"
                )
            }

            SecurityIntegrityTrustVerdict.DEGRADED -> {
                transitionTo(
                    newState = TrustState.DEGRADED,
                    decision = Decision.DENY,
                    action = RuleAction.READ_ACTIVE,
                    reason = "INTEGRITY_TRUST_DEGRADED: $reason"
                )
            }

            SecurityIntegrityTrustVerdict.COMPROMISED -> {
                transitionTo(
                    newState = TrustState.COMPROMISED,
                    decision = Decision.DENY,
                    action = RuleAction.READ_ACTIVE,
                    reason = "INTEGRITY_TRUST_COMPROMISED: $reason"
                )
            }
        }
    }

    /**
     * Dedicated manual-suite reset hook.
     *
     * IMPORTANT:
     * - This bypasses the normal COMPROMISED -> VERIFIED/DEGRADED/EMERGENCY_LIMITED block on purpose.
     * - It exists only so the adversarial manual suite can deterministically reset baseline
     *   between destructive test cases.
     * - Normal production/auth flows must NOT use this.
     */
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
            action = RuleAction.READ_ACTIVE,
            reason = "ADVERSARIAL_SUITE_FORCE_RESET: $reason -> $state",
            state = state
        )
    }

    /**
     * Dedicated production recovery hook after a successful vault reset.
     *
     * IMPORTANT:
     * - This is the ONLY normal recovery path allowed to move from COMPROMISED
     *   back to a safe baseline.
     * - It must be used only after destructive vault reset + blob wipe succeeds.
     * - Resulting state is DEGRADED (fail-closed baseline, auth required again).
     */
    @Synchronized
    internal fun recoverAfterVaultReset(reason: String) {
        auditEvents.clear()
        denyCount = 0
        SecurityIntegrityTrustAuthority.clear()
        _trustState.value = TrustState.DEGRADED
        SecurityAccessSession.clear()

        record(
            decision = Decision.ALLOW,
            action = RuleAction.READ_ACTIVE,
            reason = "VAULT_RESET_RECOVERY: $reason -> DEGRADED",
            state = TrustState.DEGRADED
        )
    }

    /**
     * Phase VI hook: call when crypto/integrity fails.
     * This is treated as a COMPROMISED signal (fail-closed).
     */
    @Synchronized
    fun reportCryptoFailure(action: RuleAction, reason: String, throwable: Throwable) {
        transitionTo(
            newState = TrustState.COMPROMISED,
            decision = Decision.DENY,
            action = action,
            reason = "CRYPTO_FAILURE: $reason (${throwable::class.java.simpleName}: ${throwable.message})"
        )
    }

    @Synchronized
    fun requireAllowed(action: RuleAction, reason: String) {
        val state = _trustState.value
        val decision = SecurityRuntimeAccessPolicy.decideRuleAction(action)

        when (decision.outcome) {
            SecurityRuntimeRuleActionOutcome.ALLOWED -> {
                if (state == TrustState.VERIFIED) {
                    denyCount = 0
                }
                record(Decision.ALLOW, action, reason, state)
                return
            }

            SecurityRuntimeRuleActionOutcome.DENIED -> {
                val denialCode = decision.code ?: "DENIED"
                record(Decision.DENY, action, "$denialCode: $reason", state)

                if (state == TrustState.VERIFIED) {
                    denyCount += 1
                    if (denyCount >= DENY_THRESHOLD_TO_DEGRADE) {
                        transitionTo(
                            newState = TrustState.DEGRADED,
                            decision = Decision.DENY,
                            action = action,
                            reason = "AUTO_DEGRADE: deny threshold reached ($DENY_THRESHOLD_TO_DEGRADE)"
                        )
                    }
                }

                throw SecurityException("RuleEngine denied ($denialCode): $action → $reason")
            }

            SecurityRuntimeRuleActionOutcome.LOCKED -> {
                val denialCode = decision.code ?: "LOCKED"

                if (denialCode == "COMPROMISED") {
                    SecurityAccessSession.clear()
                    record(Decision.DENY, action, "LOCKDOWN(COMPROMISED): $reason", state)
                    throw SecurityException("RuleEngine denied (COMPROMISED): $action → $reason")
                }

                record(Decision.DENY, action, "LOCKDOWN($denialCode): $reason", state)
                throw SecurityException("RuleEngine denied ($denialCode): $action → $reason")
            }
        }
    }
}

