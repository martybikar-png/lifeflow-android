package com.lifeflow.security

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong

/**
 * Single in-memory authority for "recent biometric auth" session.
 * Goal: explicitly gate access to encryption/decryption operations
 * (in addition to Android Keystore auth requirements).
 *
 * Fail-closed:
 * - No session => no access
 * - COMPROMISED => immediate deny
 */
object SecurityAccessSession {

    // Default and max TTL to prevent accidental "forever sessions"
    private const val DEFAULT_SESSION_MS: Long = 30_000L   // 30s
    private const val MAX_SESSION_MS: Long = 5 * 60_000L   // 5min hard cap

    private val validUntilElapsedMs = AtomicLong(0L)

    fun grant(durationMs: Long) {
        val now = SystemClock.elapsedRealtime()

        val safeDuration = durationMs
            .coerceAtLeast(0L)
            .coerceAtMost(MAX_SESSION_MS)

        validUntilElapsedMs.set(now + safeDuration)
    }

    fun grantDefault() {
        grant(DEFAULT_SESSION_MS)
    }

    fun clear() {
        validUntilElapsedMs.set(0L)
    }

    fun isAuthorized(): Boolean {
        if (SecurityRuleEngine.getTrustState() == TrustState.COMPROMISED) return false
        val now = SystemClock.elapsedRealtime()
        return now <= validUntilElapsedMs.get()
    }

    @Suppress("unused")
    fun requireAuthorized(reason: String) {
        if (!isAuthorized()) {
            throw SecurityException("SecurityAccessSession denied: $reason")
        }
    }
}