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

        // ✅ Security policy: BIOMETRIC_STRONG only
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
                    // ✅ Fail-closed: accept ONLY real biometric
                    val authType = result.authenticationType
                    if (authType == BiometricPrompt.AUTHENTICATION_RESULT_TYPE_BIOMETRIC) {

                        // ✅ CRITICAL: open an active session BEFORE allowing any secured reads
                        SecurityAccessSession.grantDefault()

                        // ✅ Reset to VERIFIED after a successful biometric
                        // (prevents "stuck in DEGRADED" after pre-auth denies)
                        SecurityRuleEngine.setTrustState(
                            TrustState.VERIFIED,
                            reason = "Biometric verified"
                        )

                        onSuccess()
                    } else {
                        // Some OEM UIs can still surface credential options.
                        // Even if Android returns success (rare), we deny here.
                        onError("Device credential is not allowed. Please use biometric authentication.")
                    }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    onError("Auth error ($errorCode): $errString")
                }

                override fun onAuthenticationFailed() {
                    // user presented a different biometric; do not fail the whole flow
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("LifeFlow Security")
            .setSubtitle("Authenticate to access encrypted identity")
            .setAllowedAuthenticators(authenticators)
            // ✅ Without DEVICE_CREDENTIAL, provide an explicit cancel button
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}