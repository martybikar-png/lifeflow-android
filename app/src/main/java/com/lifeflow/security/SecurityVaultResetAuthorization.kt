package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.concurrent.atomic.AtomicReference

internal object SecurityVaultResetAuthorization {

    private const val AUTH_WINDOW_MS = 15_000L

    private enum class GrantSource {
        VAULT_RESET_BIOMETRIC,
        VAULT_RESET_AUTH_PER_USE_CRYPTO
    }

    private data class GrantSnapshot(
        val validUntilElapsedMs: Long,
        val grantedTrustState: TrustState,
        val grantSource: GrantSource
    )

    private val activeGrant = AtomicReference<GrantSnapshot?>(null)

    private fun nowElapsedMs(): Long = System.nanoTime() / 1_000_000L

    fun grantFromVaultResetBiometricSuccess() {
        grantFreshAuthorization(
            grantSource = GrantSource.VAULT_RESET_BIOMETRIC
        )
    }

    fun grantFromVaultResetAuthPerUseSuccess() {
        grantFreshAuthorization(
            grantSource = GrantSource.VAULT_RESET_AUTH_PER_USE_CRYPTO
        )
    }

    fun clear() {
        val previous = activeGrant.getAndSet(null)
        if (previous != null) {
            SecurityAuditLog.info(
                EventType.VAULT_RESET_AUTH_CLEARED,
                "Fresh vault reset authorization cleared",
                metadata = mapOf(
                    "grantedTrustState" to previous.grantedTrustState.name,
                    "grantSource" to previous.grantSource.name
                )
            )
        }
    }

    fun consumeFreshAuthorization(reason: String) {
        while (true) {
            val grant = activeGrant.get()

            if (grant == null) {
                SecurityAuditLog.warning(
                    EventType.VAULT_RESET_AUTH_DENIED,
                    "Vault reset denied: missing fresh authorization",
                    metadata = mapOf("reason" to reason)
                )
                throw SecurityException("Vault reset denied: missing fresh authorization. $reason")
            }

            val now = nowElapsedMs()
            if (now > grant.validUntilElapsedMs) {
                if (activeGrant.compareAndSet(grant, null)) {
                    SecurityAuditLog.warning(
                        EventType.VAULT_RESET_AUTH_EXPIRED,
                        "Vault reset denied: fresh authorization expired",
                        metadata = mapOf(
                            "reason" to reason,
                            "grantedTrustState" to grant.grantedTrustState.name,
                            "grantSource" to grant.grantSource.name
                        )
                    )
                }
                throw SecurityException("Vault reset denied: fresh authorization expired. $reason")
            }

            val currentTrustState = SecurityRuleEngine.getTrustState()
            if (currentTrustState == TrustState.EMERGENCY_LIMITED) {
                if (activeGrant.compareAndSet(grant, null)) {
                    SecurityAuditLog.warning(
                        EventType.VAULT_RESET_AUTH_DENIED,
                        "Vault reset denied: emergency limited mode blocks consume",
                        metadata = mapOf(
                            "reason" to reason,
                            "grantedTrustState" to grant.grantedTrustState.name,
                            "currentTrustState" to currentTrustState.name,
                            "grantSource" to grant.grantSource.name
                        )
                    )
                }
                throw SecurityException(
                    "Vault reset denied: emergency limited mode must be cleared before vault reset."
                )
            }

            if (!SecurityAccessSession.isAuthorized()) {
                if (activeGrant.compareAndSet(grant, null)) {
                    SecurityAuditLog.warning(
                        EventType.VAULT_RESET_AUTH_DENIED,
                        "Vault reset denied: active auth session expired before consume",
                        metadata = mapOf(
                            "reason" to reason,
                            "grantedTrustState" to grant.grantedTrustState.name,
                            "currentTrustState" to currentTrustState.name,
                            "grantSource" to grant.grantSource.name
                        )
                    )
                }
                throw SecurityException("Vault reset denied: active auth session is required. $reason")
            }

            if (activeGrant.compareAndSet(grant, null)) {
                SecurityAuditLog.info(
                    EventType.VAULT_RESET_AUTH_CONSUMED,
                    "Fresh vault reset authorization consumed",
                    metadata = mapOf(
                        "grantedTrustState" to grant.grantedTrustState.name,
                        "currentTrustState" to currentTrustState.name,
                        "grantSource" to grant.grantSource.name
                    )
                )
                return
            }
        }
    }

    private fun grantFreshAuthorization(
        grantSource: GrantSource
    ) {
        val trustState = SecurityRuleEngine.getTrustState()

        if (trustState == TrustState.EMERGENCY_LIMITED) {
            SecurityAuditLog.warning(
                EventType.VAULT_RESET_AUTH_DENIED,
                "Vault reset authorization denied in emergency limited mode",
                metadata = mapOf(
                    "trustState" to trustState.name,
                    "grantSource" to grantSource.name
                )
            )
            throw SecurityException(
                "Vault reset denied: emergency limited mode must be cleared before reset authorization can be granted."
            )
        }

        if (!SecurityAccessSession.isAuthorized()) {
            SecurityAuditLog.warning(
                EventType.VAULT_RESET_AUTH_DENIED,
                "Vault reset authorization denied: active auth session is required",
                metadata = mapOf(
                    "trustState" to trustState.name,
                    "grantSource" to grantSource.name
                )
            )
            throw SecurityException(
                "Vault reset denied: active auth session is required before reset authorization can be granted."
            )
        }

        activeGrant.set(
            GrantSnapshot(
                validUntilElapsedMs = nowElapsedMs() + AUTH_WINDOW_MS,
                grantedTrustState = trustState,
                grantSource = grantSource
            )
        )

        SecurityAuditLog.info(
            EventType.VAULT_RESET_AUTH_GRANTED,
            "Fresh vault reset authorization granted",
            metadata = mapOf(
                "windowMs" to AUTH_WINDOW_MS.toString(),
                "trustState" to trustState.name,
                "grantSource" to grantSource.name
            )
        )
    }
}
