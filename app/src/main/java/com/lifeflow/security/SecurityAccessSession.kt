package com.lifeflow.security

import android.content.Context
import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

object SecurityAccessSession {

    private const val DEFAULT_SESSION_MS: Long = 5 * 60_000L
    private const val MAX_SESSION_MS: Long = 5 * 60_000L

    private val validUntilElapsedMs = AtomicLong(0L)
    private val boundDeviceFingerprint = AtomicReference<String?>(null)
    private val boundBindingId = AtomicReference<String?>(null)
    private val runtimeApplicationContext = AtomicReference<Context?>(null)

    private fun nowElapsedMs(): Long = System.nanoTime() / 1_000_000L

    fun bindRuntimeContext(context: Context) {
        runtimeApplicationContext.compareAndSet(
            null,
            context.applicationContext
        )
    }

    fun grant(context: Context, durationMs: Long) {
        bindRuntimeContext(context)

        val safeDuration = durationMs
            .coerceAtLeast(0L)
            .coerceAtMost(MAX_SESSION_MS)

        if (safeDuration == 0L) {
            clear()
            return
        }

        val binding = resolveBindingForGrant()
        if (binding == null) {
            SecurityAuditLog.critical(
                EventType.POLICY_VIOLATION,
                "Session grant denied because device binding is unavailable"
            )
            clear()
            return
        }

        val fingerprint = DeviceFingerprint.generate(context.applicationContext)
        boundDeviceFingerprint.set(fingerprint)
        boundBindingId.set(binding.bindingId)

        val now = nowElapsedMs()
        validUntilElapsedMs.set(now + safeDuration)

        SecurityAuditLog.info(
            EventType.SESSION_CREATED,
            "Session granted with device binding"
        )
    }

    fun grantDefault(context: Context) {
        grant(context, DEFAULT_SESSION_MS)
    }

    fun clear() {
        validUntilElapsedMs.set(0L)
        boundDeviceFingerprint.set(null)
        boundBindingId.set(null)
        SecurityAuditLog.info(
            EventType.SESSION_INVALIDATED,
            "Session cleared"
        )
    }

    fun isAuthorized(): Boolean {
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
            boundBindingId.set(null)
            return false
        }

        val applicationContext = runtimeApplicationContext.get()
            ?: run {
                SecurityAuditLog.critical(
                    EventType.POLICY_VIOLATION,
                    "Session runtime context missing - cannot validate device binding"
                )
                clear()
                return false
            }

        val storedFingerprint = boundDeviceFingerprint.get()
            ?: run {
                SecurityAuditLog.critical(
                    EventType.POLICY_VIOLATION,
                    "Session missing device fingerprint binding"
                )
                clear()
                return false
            }

        val storedBindingId = boundBindingId.get()
            ?: run {
                SecurityAuditLog.critical(
                    EventType.POLICY_VIOLATION,
                    "Session missing persistent device binding id"
                )
                clear()
                return false
            }

        val isValidDevice = DeviceFingerprint.validate(
            applicationContext,
            storedFingerprint
        )
        if (!isValidDevice) {
            SecurityAuditLog.critical(
                EventType.POLICY_VIOLATION,
                "Device fingerprint mismatch - possible session theft"
            )
            clear()
            return false
        }

        val manager = SecurityDeviceBindingRegistry.currentOrNull()
            ?: run {
                SecurityAuditLog.critical(
                    EventType.POLICY_VIOLATION,
                    "Device binding registry missing during session validation"
                )
                clear()
                return false
            }

        val currentBinding = runCatching {
            manager.requireCurrentBinding()
        }.getOrElse {
            SecurityAuditLog.critical(
                EventType.POLICY_VIOLATION,
                "Current device binding validation failed",
                mapOf(
                    "errorType" to it::class.java.simpleName,
                    "errorMessage" to (it.message ?: "unknown")
                )
            )
            clear()
            return false
        }

        if (currentBinding.bindingId != storedBindingId) {
            SecurityAuditLog.critical(
                EventType.POLICY_VIOLATION,
                "Persistent device binding id mismatch - possible session replay"
            )
            clear()
            return false
        }

        return true
    }

    fun isAuthorized(context: Context): Boolean {
        bindRuntimeContext(context)
        return isAuthorized()
    }

    internal fun resolveEmergencyAccess(
        request: EmergencyAccessRequest?
    ): SecurityEmergencyAccessAuthority.AccessResolution {
        return SecurityEmergencyAccessAuthority.resolve(request)
    }

    @Suppress("unused")
    fun requireAuthorized(reason: String) {
        if (!isAuthorized()) {
            throw SecurityException("SecurityAccessSession denied: $reason")
        }
    }

    @Suppress("unused")
    fun requireAuthorized(context: Context, reason: String) {
        bindRuntimeContext(context)
        requireAuthorized(reason)
    }

    private fun resolveBindingForGrant(): DeviceBindingSnapshot? {
        val manager = SecurityDeviceBindingRegistry.currentOrNull() ?: return null
        return runCatching {
            manager.ensureRegistered()
        }.getOrNull()
    }
}
