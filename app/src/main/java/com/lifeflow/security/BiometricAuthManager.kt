package com.lifeflow.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal class BiometricAuthManager(
    private val activity: FragmentActivity,
    private val authPerUseCryptoProvider: SecurityAuthPerUseCryptoProvider? = null
) {

    fun hasAuthPerUseCrypto(): Boolean = authPerUseCryptoProvider != null

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authenticateInternal(
            cryptoObject = null,
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun authenticateForAuthPerUseCrypto(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val provider = authPerUseCryptoProvider
        if (provider == null) {
            failClosed(
                onError = onError,
                message = "Auth-per-use crypto is not available on this device."
            )
            return
        }

        authenticateWithCryptoObject(
            cryptoObject = provider.createEncryptCryptoObject(),
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun authenticateWithCryptoObject(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        authenticateInternal(
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun authenticateInternal(
        cryptoObject: BiometricPrompt.CryptoObject?,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG

        val canAuthenticate = biometricManager.canAuthenticate(authenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            val message = userFriendlyBiometricAvailabilityMessage(canAuthenticate)
            SecurityAuditLog.warning(
                EventType.AUTH_FAILURE,
                "Biometric not available",
                mapOf("code" to canAuthenticate.toString())
            )
            failClosed(onError = onError, message = message)
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    val authType = result.authenticationType
                    if (authType != BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {
                        SecurityAuditLog.warning(
                            EventType.AUTH_FAILURE,
                            "Non-biometric auth type rejected",
                            mapOf("authType" to authType.toString())
                        )
                        failClosed(
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
                        failClosed(
                            onError = onError,
                            message = "Biometric verified, but secure crypto binding was missing."
                        )
                        return
                    }

                    if (cryptoObject != null) {
                        val provider = authPerUseCryptoProvider
                        if (provider == null) {
                            SecurityAuditLog.critical(
                                EventType.AUTH_FAILURE,
                                "Auth-per-use crypto provider missing"
                            )
                            failClosed(
                                onError = onError,
                                message = "Biometric verified, but auth-per-use crypto provider was missing."
                            )
                            return
                        }

                        val proofResult = runCatching {
                            provider.completeEncryptProof(result)
                        }

                        val proof = proofResult.getOrElse { throwable ->
                            SecurityAuditLog.critical(
                                EventType.AUTH_FAILURE,
                                "Auth-per-use crypto proof failed",
                                mapOf(
                                    "errorType" to throwable::class.java.simpleName,
                                    "errorMessage" to (throwable.message ?: "unknown")
                                )
                            )
                            failClosed(
                                onError = onError,
                                message = "Biometric verified, but auth-per-use crypto proof failed."
                            )
                            return
                        }

                        if (proof.isEmpty()) {
                            SecurityAuditLog.critical(
                                EventType.AUTH_FAILURE,
                                "Auth-per-use crypto proof returned empty payload"
                            )
                            failClosed(
                                onError = onError,
                                message = "Biometric verified, but auth-per-use crypto proof was empty."
                            )
                            return
                        }

                        SecurityAuditLog.info(
                            EventType.AUTH_SUCCESS,
                            "Auth-per-use crypto proof completed"
                        )
                    }

                    SecurityAccessSession.grantDefault(activity.applicationContext)

                    val sessionOk = SecurityAccessSession.isAuthorized(activity.applicationContext)
                    val runtimeAccessDecision = SecurityRuntimeAccessPolicy.decide(
                        accessMode = SecurityRuntimeAccessMode.STANDARD_PROTECTED
                    )

                    if (sessionOk && runtimeAccessDecision.allowed) {
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
                            "Post-auth runtime access allowed",
                            mapOf(
                                "effectiveTrustState" to runtimeAccessDecision.effectiveTrustState.name
                            )
                        )
                        onSuccess(result)
                    } else {
                        SecurityAuditLog.critical(
                            EventType.AUTH_FAILURE,
                            "Session establishment or runtime authorization failed post-auth",
                            mapOf(
                                "sessionOk" to sessionOk.toString(),
                                "runtimeAllowed" to runtimeAccessDecision.allowed.toString(),
                                "effectiveTrustState" to runtimeAccessDecision.effectiveTrustState.name,
                                "denialCode" to (runtimeAccessDecision.denialCode ?: "none")
                            )
                        )
                        failClosed(
                            onError = onError,
                            message = postAuthFailureMessage(runtimeAccessDecision)
                        )
                    }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    SecurityAuditLog.warning(
                        EventType.AUTH_FAILURE,
                        "Biometric auth error",
                        mapOf("errorCode" to errorCode.toString())
                    )
                    failClosed(
                        onError = onError,
                        message = "Auth error ($errorCode): $errString"
                    )
                }

                override fun onAuthenticationFailed() {
                    SecurityAuditLog.info(
                        EventType.AUTH_FAILURE,
                        "Biometric mismatch - prompt remains active"
                    )
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("LifeFlow Security")
            .setSubtitle("Authenticate to access encrypted identity")
            .setAllowedAuthenticators(authenticators)
            .setNegativeButtonText("Cancel")
            .build()

        if (cryptoObject == null) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        }
    }

    private fun postAuthFailureMessage(
        runtimeAccessDecision: SecurityRuntimeAccessDecision
    ): String =
        when (runtimeAccessDecision.denialCode) {
            "COMPROMISED" ->
                "Security compromised. Reset vault is required before continuing."

            "RECOVERY_REQUIRED" ->
                "Recovery is required before protected access can continue."

            "EMERGENCY_LIMITED" ->
                "Emergency limited mode is active. Standard protected runtime remains blocked."

            "PROTECTED_RUNTIME_BLOCKED" ->
                "Protected runtime is blocked by current security policy."

            "AUTH_REQUIRED" ->
                "Biometric verified, but secure session could not be established."

            else ->
                "Biometric verified, but protected runtime access is not allowed under the current security posture."
        }

    private fun failClosed(
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

    private fun userFriendlyBiometricAvailabilityMessage(code: Int): String {
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
