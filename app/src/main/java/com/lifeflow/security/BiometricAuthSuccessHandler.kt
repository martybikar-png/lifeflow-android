package com.lifeflow.security

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal class BiometricAuthSuccessHandler(
    private val activity: FragmentActivity,
    private val authPerUseCryptoProvider: SecurityAuthPerUseCryptoProvider?,
    private val failureHandler: BiometricAuthFailureHandler
) {
    fun handleSuccess(
        cryptoObject: BiometricPrompt.CryptoObject?,
        result: BiometricPrompt.AuthenticationResult,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val authType = result.authenticationType
        if (authType != BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
            SecurityAuditLog.warning(
                EventType.AUTH_FAILURE,
                "Non-biometric auth type rejected",
                mapOf("authType" to authType.toString())
            )
            failureHandler.failClosed(
                onError = onError,
                message = "Device credential is not allowed. Please use biometric authentication."
            )
            return
        }

        if (cryptoObject != null && result.cryptoObject == null) {
            SecurityAuditLog.critical(
                EventType.AUTH_FAILURE,
                "CryptoObject missing after biometric success"
            )
            failureHandler.failClosed(
                onError = onError,
                message = "Biometric verified, but secure crypto binding was missing."
            )
            return
        }

        if (cryptoObject != null &&
            !completeAuthPerUseCryptoProof(
                result = result,
                onError = onError
            )
        ) {
            return
        }

        SecurityAccessSession.grantDefault(activity.applicationContext)

        val sessionOk = SecurityAccessSession.isAuthorized(activity.applicationContext)
        val handoffDecision =
            SecurityRuntimeAccessPolicy.decideBiometricAuthenticationHandoff()

        if (sessionOk && handoffDecision.allowed) {
            SecurityAuditLog.info(
                EventType.AUTH_SUCCESS,
                "Biometric authentication succeeded"
            )
            SecurityAuditLog.info(
                EventType.SESSION_CREATED,
                "Security session established with device binding"
            )
            SecurityAuditLog.info(
                EventType.HARDENING_CHECK_PASSED,
                "Post-auth biometric handoff allowed",
                mapOf(
                    "effectiveTrustState" to handoffDecision.effectiveTrustState.name
                )
            )
            onSuccess(result)
        } else {
            SecurityAuditLog.critical(
                EventType.AUTH_FAILURE,
                "Session establishment or biometric handoff authorization failed post-auth",
                mapOf(
                    "sessionOk" to sessionOk.toString(),
                    "handoffAllowed" to handoffDecision.allowed.toString(),
                    "effectiveTrustState" to handoffDecision.effectiveTrustState.name,
                    "denialCode" to (handoffDecision.denialCode?.name ?: "none")
                )
            )
            failureHandler.failClosed(
                onError = onError,
                message = failureHandler.postAuthFailureMessage(handoffDecision)
            )
        }
    }

    private fun completeAuthPerUseCryptoProof(
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

        val proofResult = runCatching {
            provider.completeEncryptProof(result)
        }

        val proof = proofResult.getOrElse { throwable ->
            val resolvedMessage = failureHandler.resolveThrowableMessage(
                throwable = throwable,
                fallbackMessage = "Biometric verified, but auth-per-use crypto proof failed."
            )
            SecurityAuditLog.critical(
                EventType.AUTH_FAILURE,
                "Auth-per-use crypto proof failed",
                mapOf(
                    "errorType" to throwable::class.java.simpleName,
                    "errorMessage" to (throwable.message ?: "unknown")
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
}
