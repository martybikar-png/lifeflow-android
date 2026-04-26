package com.lifeflow.security

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityAuditLog.EventType

internal class BiometricAuthManager(
    private val activity: FragmentActivity,
    private val authPerUseCryptoProvider: SecurityAuthPerUseCryptoProvider? = null
) {
    private val failureHandler = BiometricAuthFailureHandler()
    private val promptRunner = BiometricAuthPromptRunner(
        activity = activity,
        authPerUseCryptoProvider = authPerUseCryptoProvider,
        failureHandler = failureHandler
    )

    fun hasAuthPerUseCrypto(): Boolean =
        authPerUseCryptoProvider != null

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        promptRunner.authenticate(
            cryptoObject = null,
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun authenticateForVaultReset(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        promptRunner.authenticateVaultReset(
            cryptoObject = null,
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun authenticateForAuthPerUseCrypto(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val cryptoObject = createAuthPerUseCryptoObjectOrFail(
            onError = onError
        ) ?: return

        authenticateWithCryptoObject(
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun authenticateForVaultResetAuthPerUseCrypto(
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val cryptoObject = createAuthPerUseCryptoObjectOrFail(
            onError = onError
        ) ?: return

        promptRunner.authenticateVaultReset(
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    fun authenticateWithCryptoObject(
        cryptoObject: BiometricPrompt.CryptoObject,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (String) -> Unit
    ) {
        promptRunner.authenticate(
            cryptoObject = cryptoObject,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun createAuthPerUseCryptoObjectOrFail(
        onError: (String) -> Unit
    ): BiometricPrompt.CryptoObject? {
        val provider = authPerUseCryptoProvider
        if (provider == null) {
            failureHandler.failClosed(
                onError = onError,
                message = "Auth-per-use crypto is not available on this device."
            )
            return null
        }

        return runCatching {
            provider.createEncryptCryptoObject()
        }.getOrElse { throwable ->
            val resolvedMessage = failureHandler.resolveThrowableMessage(
                throwable = throwable,
                fallbackMessage = "Auth-per-use crypto is not available."
            )
            SecurityAuditLog.critical(
                EventType.AUTH_FAILURE,
                "Auth-per-use crypto object creation failed",
                mapOf(
                    "errorType" to throwable::class.java.simpleName,
                    "errorMessage" to (throwable.message ?: "unknown")
                )
            )
            failureHandler.failClosed(
                onError = onError,
                message = resolvedMessage
            )
            null
        }
    }
}
