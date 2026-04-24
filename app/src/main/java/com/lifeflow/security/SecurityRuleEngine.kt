package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.security.audit.SecurityAuditLog
import kotlinx.coroutines.flow.StateFlow

/**
 * Phase VI — Automatic Trust Transitions (V1)
 *
 * - Deny-by-default
 * - Trust state remains the authoritative security posture state
 * - Runtime action enforcement is delegated to SecurityRuntimeOperationGate
 * - Raw integrity verdict response normalization/audit is delegated to
 *   SecurityIntegrityTrustAuthority
 * - COMPROMISED: deny everything + clear session (immediate lockdown)
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

    private val stateStore = SecurityRuleEngineStateStore(TrustState.DEGRADED)
    private val auditStore = SecurityRuleEngineAuditStore(MAX_EVENTS)
    private val denyCounter = SecurityRuleEngineDenyCounter(DENY_THRESHOLD_TO_DEGRADE)

    private val transitionController = SecurityRuleEngineTransitionController(
        stateStore = stateStore,
        auditStore = auditStore,
        denyCounter = denyCounter
    )

    private val integrityReporter = SecurityRuleEngineIntegrityReporter(
        stateStore = stateStore,
        auditStore = auditStore,
        transitionController = transitionController
    )

    private val operationEnforcer = SecurityRuleEngineOperationEnforcer(
        stateStore = stateStore,
        auditStore = auditStore,
        denyCounter = denyCounter,
        transitionController = transitionController
    )

    val trustState: StateFlow<TrustState> = stateStore.trustState

    @Suppress("unused")
    @Synchronized
    fun getRecentEvents(): List<AuditEvent> =
        auditStore.getRecentEvents()

    @Suppress("unused")
    @Synchronized
    fun clearAudit() {
        auditStore.clear()
        denyCounter.clear()
        SecurityIntegrityTrustAuthority.clear()
        SecurityAuditLog.clear()
    }

    @Suppress("unused")
    fun getTrustState(): TrustState =
        stateStore.get()

    @Suppress("unused")
    @Synchronized
    fun setTrustState(state: TrustState, reason: String) {
        transitionController.setTrustState(
            state = state,
            reason = reason
        )
    }

    @Synchronized
    internal fun reportIntegrityTrustDecision(
        response: IntegrityTrustVerdictResponse
    ) {
        integrityReporter.reportIntegrityTrustDecision(response)
    }

    @Synchronized
    internal fun reportIntegrityTrustVerdict(
        verdict: SecurityIntegrityTrustVerdict,
        reason: String
    ) {
        integrityReporter.reportIntegrityTrustVerdict(
            verdict = verdict,
            reason = reason
        )
    }

    @Synchronized
    internal fun reportRuntimeCompromise(reason: String) {
        transitionController.reportRuntimeCompromise(reason)
    }

    @Synchronized
    internal fun forceResetForAdversarialSuite(
        state: TrustState,
        reason: String
    ) {
        transitionController.forceResetForAdversarialSuite(
            state = state,
            reason = reason
        )
    }

    @Synchronized
    internal fun recoverAfterVaultReset(reason: String) {
        transitionController.recoverAfterVaultReset(reason)
    }

    @Synchronized
    fun reportCryptoFailure(
        operation: DomainOperation,
        reason: String,
        throwable: Throwable
    ) {
        transitionController.reportCryptoFailure(
            operation = operation,
            reason = reason,
            throwable = throwable
        )
    }

    @Synchronized
    fun requireAllowed(
        operation: DomainOperation,
        reason: String
    ) {
        operationEnforcer.requireAllowed(
            operation = operation,
            reason = reason
        )
    }
}