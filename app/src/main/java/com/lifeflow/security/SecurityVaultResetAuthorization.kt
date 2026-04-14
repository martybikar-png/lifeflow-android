package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.concurrent.atomic.AtomicLong

internal object SecurityVaultResetAuthorization {

    private const val AUTH_WINDOW_MS = 15_000L
    private val validUntilElapsedMs = AtomicLong(0L)

    private fun nowElapsedMs(): Long = System.nanoTime() / 1_000_000L

    fun grantFreshAuthorization() {
        validUntilElapsedMs.set(nowElapsedMs() + AUTH_WINDOW_MS)
        SecurityAuditLog.info(
            EventType.VAULT_RESET_AUTH_GRANTED,
            "Fresh vault reset authorization granted",
            metadata = mapOf("windowMs" to AUTH_WINDOW_MS.toString())
        )
    }

    fun clear() {
        val previous = validUntilElapsedMs.getAndSet(0L)
        if (previous != 0L) {
            SecurityAuditLog.info(
                EventType.VAULT_RESET_AUTH_CLEARED,
                "Fresh vault reset authorization cleared"
            )
        }
    }

    fun consumeFreshAuthorization(reason: String) {
        val now = nowElapsedMs()

        while (true) {
            val validUntil = validUntilElapsedMs.get()

            if (validUntil == 0L) {
                SecurityAuditLog.warning(
                    EventType.VAULT_RESET_AUTH_DENIED,
                    "Vault reset denied: missing fresh authorization",
                    metadata = mapOf("reason" to reason)
                )
                throw SecurityException("Vault reset denied: missing fresh authorization. $reason")
            }

            if (now > validUntil) {
                if (validUntilElapsedMs.compareAndSet(validUntil, 0L)) {
                    SecurityAuditLog.warning(
                        EventType.VAULT_RESET_AUTH_EXPIRED,
                        "Vault reset denied: fresh authorization expired",
                        metadata = mapOf("reason" to reason)
                    )
                }
                throw SecurityException("Vault reset denied: fresh authorization expired. $reason")
            }

            if (validUntilElapsedMs.compareAndSet(validUntil, 0L)) {
                SecurityAuditLog.info(
                    EventType.VAULT_RESET_AUTH_CONSUMED,
                    "Fresh vault reset authorization consumed"
                )
                return
            }
        }
    }
}
