package com.lifeflow.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal class BiometricAuthPromptRunner(
    private val activity: FragmentActivity,
    authPerUseCryptoProvider: SecurityAuthPerUseCryptoProvider?,
    private val failureHandler: BiometricAuthFailureHandler
) {
    private val successHandler = BiometricAuthSuccessHandler(
        activity = activity,
        authPerUseCryptoProvider = authPerUseCryptoProvider,
        failureHandler = failureHandler
    )

    fun authenticate(
        cryptoObject: BiometricPrompt.CryptoObject?,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        authenticateWithSuccessMode(
            successMode = BiometricAuthSuccessMode.STANDARD_PROTECTED_HANDOFF,
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun authenticateVaultReset(
        cryptoObject: BiometricPrompt.CryptoObject?,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        authenticateWithSuccessMode(
            successMode = BiometricAuthSuccessMode.VAULT_RESET_AUTHORIZATION,
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun authenticateWithSuccessMode(
        successMode: BiometricAuthSuccessMode,
        cryptoObject: BiometricPrompt.CryptoObject?,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG

        val canAuthenticate = biometricManager.canAuthenticate(authenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            val message = failureHandler.userFriendlyBiometricAvailabilityMessage(
                canAuthenticate
            )
            SecurityAuditLog.warning(
                EventType.AUTH_FAILURE,
                "Biometric not available",
                mapOf("code" to canAuthenticate.toString())
            )
            failureHandler.failClosed(onError = onError, message = message)
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
                    successHandler.handleSuccess(
                        successMode = successMode,
                        cryptoObject = cryptoObject,
                        result = result,
                        onSuccess = onSuccess,
                        onError = onError
                    )
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
                    failureHandler.failClosed(
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
}
