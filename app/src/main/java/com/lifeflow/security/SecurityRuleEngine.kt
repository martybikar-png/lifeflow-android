package com.lifeflow.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase VI — Automatic Trust Transitions (V1)
 *
 * - Deny-by-default
 * - VERIFIED: session required, allow reads+writes
 * - DEGRADED: session required, allow reads only
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
    private val auditEvents: ArrayDeque<AuditEvent> = ArrayDeque(MAX_EVENTS)

    private const val DENY_THRESHOLD_TO_DEGRADE = 3
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

        if (newState == TrustState.DEGRADED || newState == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
        }
    }

    /**
     * Explicit trust change.
     *
     * SECURITY RULE (B2):
     * If we're COMPROMISED, you cannot set VERIFIED/DEGRADED via normal flows (including biometric).
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
     * Dedicated manual-suite reset hook.
     *
     * IMPORTANT:
     * - This bypasses the normal COMPROMISED -> VERIFIED/DEGRADED block on purpose.
     * - It exists only so the adversarial manual suite can deterministically reset baseline
     *   between destructive test cases.
     * - Normal production/auth flows must NOT use this.
     *
     * Test baseline nuance:
     * - VERIFIED baseline may intentionally preserve an already granted session so auth-path
     *   tests can model "session exists + trust verified".
     * - DEGRADED / COMPROMISED always clear session fail-closed.
     */
    @Synchronized
    internal fun forceResetForAdversarialSuite(
        state: TrustState,
        reason: String
    ) {
        auditEvents.clear()
        denyCount = 0
        _trustState.value = state

        if (state == TrustState.DEGRADED || state == TrustState.COMPROMISED) {
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

        if (state == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
            record(Decision.DENY, action, "LOCKDOWN(COMPROMISED): $reason", state)
            throw SecurityException("RuleEngine denied (COMPROMISED): $action → $reason")
        }

        val sessionOk = SecurityAccessSession.isAuthorized()

        val allowed = when (state) {
            TrustState.VERIFIED -> sessionOk && allowInVerified(action)
            TrustState.DEGRADED -> sessionOk && allowInDegraded(action)
            TrustState.COMPROMISED -> false
        }

        if (allowed) {
            if (state == TrustState.VERIFIED) {
                denyCount = 0
            }
            record(Decision.ALLOW, action, reason, state)
            return
        }

        record(Decision.DENY, action, reason, state)

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

        throw SecurityException("RuleEngine denied (${_trustState.value}): $action → $reason")
    }

    private fun allowInVerified(action: RuleAction): Boolean =
        when (action) {
            RuleAction.READ_BY_ID,
            RuleAction.READ_ACTIVE,
            RuleAction.WRITE_SAVE,
            RuleAction.WRITE_DELETE -> true
        }

    private fun allowInDegraded(action: RuleAction): Boolean =
        when (action) {
            RuleAction.READ_BY_ID,
            RuleAction.READ_ACTIVE -> true
            RuleAction.WRITE_SAVE,
            RuleAction.WRITE_DELETE -> false
        }
}