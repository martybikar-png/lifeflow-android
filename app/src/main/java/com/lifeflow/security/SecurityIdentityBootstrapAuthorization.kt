package com.lifeflow.security

import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.concurrent.atomic.AtomicReference

internal object SecurityIdentityBootstrapAuthorization {

    private const val AUTH_WINDOW_MS = 10_000L

    private data class GrantSnapshot(
        val validUntilElapsedMs: Long,
        val reason: String
    )

    private val activeGrant = AtomicReference<GrantSnapshot?>(null)

    private fun nowElapsedMs(): Long = System.nanoTime() / 1_000_000L

    suspend fun <T> withFreshBootstrapAuthorization(
        reason: String,
        block: suspend () -> T
    ): T {
        val grant = GrantSnapshot(
            validUntilElapsedMs = nowElapsedMs() + AUTH_WINDOW_MS,
            reason = reason
        )

        activeGrant.set(grant)

        SecurityAuditLog.info(
            EventType.HARDENING_CHECK_PASSED,
            "Fresh identity bootstrap authorization granted",
            metadata = mapOf(
                "windowMs" to AUTH_WINDOW_MS.toString(),
                "reason" to reason
            )
        )

        return try {
            block()
        } finally {
            activeGrant.compareAndSet(grant, null)
        }
    }

    fun allowsOperation(operation: DomainOperation): Boolean {
        val grant = activeGrant.get() ?: return false

        if (nowElapsedMs() > grant.validUntilElapsedMs) {
            activeGrant.compareAndSet(grant, null)
            return false
        }

        if (!SecurityAccessSession.isAuthorized()) {
            activeGrant.compareAndSet(grant, null)
            return false
        }

        val decision = SecurityRuntimeAccessPolicy
            .decideBiometricBootstrapOperation(operation)

        if (!decision.allowed) {
            return false
        }

        SecurityAuditLog.info(
            EventType.HARDENING_CHECK_PASSED,
            "Identity bootstrap operation authorized by fresh biometric scope",
            metadata = mapOf(
                "operation" to operation.name,
                "effectiveTrustState" to decision.effectiveTrustState.name,
                "reason" to grant.reason
            )
        )

        return true
    }
}
