package com.lifeflow.security

import android.content.Context
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal fun validateSecurityAccessSession(
    applicationContext: Context?,
    storedFingerprint: String?,
    storedBindingId: String?,
    clearSession: () -> Unit
): Boolean {
    if (applicationContext == null) {
        SecurityAuditLog.critical(
            EventType.POLICY_VIOLATION,
            "Session runtime context missing - cannot validate device binding"
        )
        clearSession()
        return false
    }

    if (storedFingerprint == null) {
        SecurityAuditLog.critical(
            EventType.POLICY_VIOLATION,
            "Session missing device fingerprint binding"
        )
        clearSession()
        return false
    }

    if (storedBindingId == null) {
        SecurityAuditLog.critical(
            EventType.POLICY_VIOLATION,
            "Session missing persistent device binding id"
        )
        clearSession()
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
        clearSession()
        return false
    }

    val manager = SecurityDeviceBindingRegistry.currentOrNull()
        ?: run {
            SecurityAuditLog.critical(
                EventType.POLICY_VIOLATION,
                "Device binding registry missing during session validation"
            )
            clearSession()
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
        clearSession()
        return false
    }

    if (currentBinding.bindingId != storedBindingId) {
        SecurityAuditLog.critical(
            EventType.POLICY_VIOLATION,
            "Persistent device binding id mismatch - possible session replay"
        )
        clearSession()
        return false
    }

    return true
}
