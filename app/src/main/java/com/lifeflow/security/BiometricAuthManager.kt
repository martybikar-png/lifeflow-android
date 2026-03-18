package com.lifeflow.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

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
            failClosed(
                onError = onError,
                message = userFriendlyBiometricAvailabilityMessage(canAuthenticate)
            )
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
                        onSuccess()
                    } else {
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
                    failClosed(
                        onError = onError,
                        message = "Auth error ($errorCode): $errString"
                    )
                }

                override fun onAuthenticationFailed() {
                    // User presented a different biometric; keep prompt active.
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