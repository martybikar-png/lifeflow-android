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

    // --- Auto-degrade threshold (V1) ---
    private const val DENY_THRESHOLD_TO_DEGRADE = 3
    private var denyCount: Int = 0

    // --- Trust state (observable for UI) ---
    // Start fail-closed: until a successful biometric auth happens, we are in locked/limited mode.
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

    @Suppress("unused")
    @Synchronized
    fun setTrustState(state: TrustState, reason: String) {
        _trustState.value = state
        denyCount = 0

        record(
            decision = Decision.ALLOW,
            action = RuleAction.READ_ACTIVE,
            reason = "TrustState set to $state: $reason",
            state = state
        )

        if (state == TrustState.COMPROMISED) {
            // Immediate lockdown
            SecurityAccessSession.clear()
        }
    }

    /**
     * Phase VI hook: call when crypto/integrity fails.
     * This is treated as a COMPROMISED signal (fail-closed).
     */
    @Synchronized
    fun reportCryptoFailure(action: RuleAction, reason: String, throwable: Throwable) {
        _trustState.value = TrustState.COMPROMISED
        denyCount = 0

        record(
            decision = Decision.DENY,
            action = action,
            reason = "CRYPTO_FAILURE: $reason (${throwable::class.java.simpleName}: ${throwable.message})",
            state = _trustState.value
        )

        // Immediate lockdown
        SecurityAccessSession.clear()
    }

    fun requireAllowed(action: RuleAction, reason: String) {
        val state = _trustState.value

        // Hard rule: COMPROMISED => immediate lockdown + deny
        if (state == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
            record(Decision.DENY, action, "LOCKDOWN(COMPROMISED): $reason", state)
            throw SecurityException("RuleEngine denied (COMPROMISED): $action → $reason")
        }

        val sessionOk = SecurityAccessSession.isAuthorized()

        val allowed = when (state) {
            TrustState.VERIFIED -> sessionOk && allowInVerified(action)
            TrustState.DEGRADED -> sessionOk && allowInDegraded(action)
            TrustState.COMPROMISED -> false // unreachable due to guard above
        }

        if (allowed) {
            record(Decision.ALLOW, action, reason, state)
            return
        }

        // DENY path
        record(Decision.DENY, action, reason, state)

        // Auto-degrade only while VERIFIED
        if (state == TrustState.VERIFIED) {
            denyCount += 1
            if (denyCount >= DENY_THRESHOLD_TO_DEGRADE) {
                _trustState.value = TrustState.DEGRADED
                denyCount = 0
                record(
                    decision = Decision.DENY,
                    action = action,
                    reason = "AUTO_DEGRADE: deny threshold reached ($DENY_THRESHOLD_TO_DEGRADE)",
                    state = _trustState.value
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