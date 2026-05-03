package com.lifeflow.security

import androidx.biometric.BiometricPrompt
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal fun completeBiometricAuthPerUseCryptoProof(
    authPerUseCryptoProvider: SecurityAuthPerUseCryptoProvider?,
    failureHandler: BiometricAuthFailureHandler,
    result: BiometricPrompt.AuthenticationResult,
    onError: (String) -> Unit
): Boolean {
    val provider = authPerUseCryptoProvider
    if (provider == null) {
        SecurityAuditLog.critical(
            EventType.AUTH_FAILURE,
            "Auth-per-use crypto provider missing"
        )
        failureHandler.failClosed(
            onError = onError,
            message = "Biometric verified, but auth-per-use crypto provider was missing."
        )
        return false
    }

    val proof = try {
        provider.completeEncryptProof(result)
    } catch (exception: Exception) {
        val resolvedMessage = failureHandler.resolveThrowableMessage(
            throwable = exception,
            fallbackMessage = "Biometric verified, but auth-per-use crypto proof failed."
        )
        SecurityAuditLog.critical(
            EventType.AUTH_FAILURE,
            "Auth-per-use crypto proof failed",
            mapOf(
                "errorType" to exception::class.java.simpleName,
                "errorMessage" to (exception.message ?: "unknown")
            )
        )
        failureHandler.failClosed(
            onError = onError,
            message = resolvedMessage
        )
        return false
    }

    if (proof.isEmpty()) {
        SecurityAuditLog.critical(
            EventType.AUTH_FAILURE,
            "Auth-per-use crypto proof returned empty payload"
        )
        failureHandler.failClosed(
            onError = onError,
            message = "Biometric verified, but auth-per-use crypto proof was empty."
        )
        return false
    }

    SecurityAuditLog.info(
        EventType.AUTH_SUCCESS,
        "Auth-per-use crypto proof completed"
    )
    return true
}
