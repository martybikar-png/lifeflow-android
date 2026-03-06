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
            onError("Biometric authentication not available: $canAuthenticate")
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
                        onError("Device credential is not allowed. Please use biometric authentication.")
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
                        onError("Biometric verified, but secure session could not be established. Reset vault may be required.")
                    }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    onError("Auth error ($errorCode): $errString")
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
}