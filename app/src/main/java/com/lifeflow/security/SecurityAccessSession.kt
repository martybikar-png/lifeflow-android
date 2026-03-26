package com.lifeflow.security

import android.content.Context
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * Single in-memory authority for "recent biometric auth" session.
 * 
 * Session is bound to:
 * - Time (TTL)
 * - Device (fingerprint)
 * - Trust state
 *
 * Fail-closed:
 * - No session => no access
 * - COMPROMISED => immediate deny
 * - Device mismatch => deny
 */
object SecurityAccessSession {

    private const val DEFAULT_SESSION_MS: Long = 30_000L   // 30s
    private const val MAX_SESSION_MS: Long = 5 * 60_000L   // 5min hard cap

    private val validUntilElapsedMs = AtomicLong(0L)
    private val boundDeviceFingerprint = AtomicReference<String?>(null)

    private fun nowElapsedMs(): Long = System.nanoTime() / 1_000_000L

    /**
     * Grants session with device binding.
     */
    fun grant(context: Context, durationMs: Long) {
        val safeDuration = durationMs
            .coerceAtLeast(0L)
            .coerceAtMost(MAX_SESSION_MS)

        if (safeDuration == 0L) {
            clear()
            return
        }

        val fingerprint = DeviceFingerprint.generate(context)
        boundDeviceFingerprint.set(fingerprint)

        val now = nowElapsedMs()
        validUntilElapsedMs.set(now + safeDuration)

        SecurityAuditLog.info(
            EventType.SESSION_CREATED,
            "Session granted with device binding"
        )
    }

    /**
     * Grants session without device binding (for testing/legacy).
     */
    fun grant(durationMs: Long) {
        val safeDuration = durationMs
            .coerceAtLeast(0L)
            .coerceAtMost(MAX_SESSION_MS)

        if (safeDuration == 0L) {
            clear()
            return
        }

        val now = nowElapsedMs()
        validUntilElapsedMs.set(now + safeDuration)
    }

    fun grantDefault() {
        grant(DEFAULT_SESSION_MS)
    }

    fun grantDefault(context: Context) {
        grant(context, DEFAULT_SESSION_MS)
    }

    fun clear() {
        validUntilElapsedMs.set(0L)
        boundDeviceFingerprint.set(null)
        SecurityAuditLog.info(
            EventType.SESSION_INVALIDATED,
            "Session cleared"
        )
    }

    fun isAuthorized(): Boolean {
        if (SecurityRuleEngine.getTrustState() == TrustState.COMPROMISED) return false

        val now = nowElapsedMs()
        val validUntil = validUntilElapsedMs.get()

        if (validUntil == 0L) return false

        if (now > validUntil) {
            SecurityAuditLog.info(
                EventType.SESSION_EXPIRED,
                "Session expired"
            )
            validUntilElapsedMs.compareAndSet(validUntil, 0L)
            boundDeviceFingerprint.set(null)
            return false
        }

        return true
    }

    /**
     * Validates session with device binding check.
     */
    fun isAuthorized(context: Context): Boolean {
        if (!isAuthorized()) return false

        val storedFingerprint = boundDeviceFingerprint.get()
        if (storedFingerprint != null) {
            val isValidDevice = DeviceFingerprint.validate(context, storedFingerprint)
            if (!isValidDevice) {
                SecurityAuditLog.critical(
                    EventType.POLICY_VIOLATION,
                    "Device fingerprint mismatch - possible session theft"
                )
                clear()
                return false
            }
        }

        return true
    }

    @Suppress("unused")
    fun requireAuthorized(reason: String) {
        if (!isAuthorized()) {
            throw SecurityException("SecurityAccessSession denied: $reason")
        }
    }

    @Suppress("unused")
    fun requireAuthorized(context: Context, reason: String) {
        if (!isAuthorized(context)) {
            throw SecurityException("SecurityAccessSession denied: $reason")
        }
    }
}
