package com.lifeflow.security

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

    @Synchronized
    private fun record(
        decision: Decision,
        action: RuleAction,
        reason: String,
        state: TrustState
    ) {
        if (auditEvents.size >= MAX_EVENTS) {
            auditEvents.removeFirst()
        }

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
    }

    @Volatile
    private var currentState: TrustState = TrustState.VERIFIED

    @Suppress("unused")
    @Synchronized
    fun getTrustState(): TrustState = currentState

    @Suppress("unused")
    @Synchronized
    fun setTrustState(state: TrustState, reason: String) {
        currentState = state

        record(
            decision = Decision.ALLOW,
            action = RuleAction.READ_ACTIVE,
            reason = "TrustState set to $state: $reason",
            state = state
        )

        if (state == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
        }
    }

    @Suppress("unused")
    fun isAllowed(@Suppress("UNUSED_PARAMETER") action: RuleAction): Boolean {
        return SecurityAccessSession.isAuthorized()
    }

    fun requireAllowed(action: RuleAction, reason: String) {
        val state = currentState
        val sessionOk = SecurityAccessSession.isAuthorized()

        val allowed = when (state) {
            TrustState.VERIFIED ->
                sessionOk && allowInVerified(action)

            TrustState.DEGRADED ->
                sessionOk && allowInDegraded(action)

            TrustState.COMPROMISED ->
                false
        }

        if (allowed) {
            record(Decision.ALLOW, action, reason, state)
            return
        }

        record(Decision.DENY, action, reason, state)

        if (state == TrustState.COMPROMISED) {
            SecurityAccessSession.clear()
        }

        throw SecurityException("RuleEngine denied ($state): $action → $reason")
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