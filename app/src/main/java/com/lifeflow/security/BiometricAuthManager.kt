package com.lifeflow.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType
import com.lifeflow.security.audit.SecurityAuditLog.Severity

class BiometricAuthManager(
    private val activity: FragmentActivity
) {

    fun authenticate(
        onSuccess: () -> Unit,
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

                    SecurityAccessSession.grantDefault()

                    SecurityRuleEngine.setTrustState(
                        TrustState.VERIFIED,
                        reason = "Biometric verified"
                    )

                    val sessionOk = SecurityAccessSession.isAuthorized()
                    val trustOk = SecurityRuleEngine.getTrustState() == TrustState.VERIFIED

                    if (sessionOk && trustOk) {
                        SecurityAuditLog.info(
                            EventType.AUTH_SUCCESS,
                            "Biometric authentication succeeded"
                        )
                        SecurityAuditLog.info(
                            EventType.SESSION_CREATED,
                            "Security session established"
                        )
                        SecurityAuditLog.info(
                            EventType.TRUST_VERIFIED,
                            "Trust state set to VERIFIED"
                        )
                        onSuccess()
                    } else {
                        SecurityAuditLog.critical(
                            EventType.AUTH_FAILURE,
                            "Session establishment failed post-auth",
                            mapOf("sessionOk" to sessionOk.toString(), "trustOk" to trustOk.toString())
                        )
                        failClosed(
                            onError = onError,
                            message = "Biometric verified, but secure session could not be established. Reset vault may be required."
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

        biometricPrompt.authenticate(promptInfo)
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
