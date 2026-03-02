package com.lifeflow.security

import java.util.concurrent.atomic.AtomicLong

/**
 * Single in-memory authority for "recent biometric auth" session.
 * Goal: explicitly gate access to encryption/decryption operations
 * (in addition to Android Keystore auth requirements).
 */
object SecurityAccessSession {

    private val validUntilEpochMs = AtomicLong(0L)

    fun grant(durationMs: Long) {
        val now = System.currentTimeMillis()
        validUntilEpochMs.set(now + durationMs)
    }

    fun clear() {
        validUntilEpochMs.set(0L)
    }

    fun isAuthorized(): Boolean {
        val now = System.currentTimeMillis()
        return now <= validUntilEpochMs.get()
    }

    fun requireAuthorized(reason: String) {
        if (!isAuthorized()) {
            throw SecurityException("SecurityAccessSession denied: $reason")
        }
    }
}