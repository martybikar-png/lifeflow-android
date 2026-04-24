package com.lifeflow.security

import androidx.biometric.BiometricManager
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal class BiometricAuthFailureHandler {
    fun postAuthFailureMessage(
        runtimeAccessDecision: SecurityRuntimeAccessDecision
    ): String = runtimeAccessDecision.toFailureMessage()

    fun resolveThrowableMessage(
        throwable: Throwable,
        fallbackMessage: String
    ): String {
        val normalized = throwable.message?.trim().orEmpty()
        return if (normalized.isBlank()) {
            fallbackMessage
        } else {
            normalized
        }
    }

    fun failClosed(
        onError: (String) -> Unit,
        message: String
    ) {
        SecurityAccessSession.clear()
        SecurityAuditLog.info(
            EventType.SESSION_INVALIDATED,
            "Session cleared on auth failure"
        )
        onError(message)
    }

    fun userFriendlyBiometricAvailabilityMessage(code: Int): String {
        return when (code) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "Biometric authentication is not available on this device."

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Biometric hardware is currently unavailable. Please try again."

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "No biometric data is enrolled on this device. Please add a fingerprint or other biometric first."

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                "A security update is required before biometric authentication can be used."

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                "This device does not support the required biometric authentication mode."

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                "Biometric availability could not be determined. Please try again."

            else ->
                "Biometric authentication is not available right now (code: $code)."
        }
    }
}