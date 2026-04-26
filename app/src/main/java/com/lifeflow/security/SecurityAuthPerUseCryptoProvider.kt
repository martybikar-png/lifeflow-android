package com.lifeflow.security

import androidx.biometric.BiometricPrompt
import java.security.SecureRandom

internal class SecurityAuthPerUseCryptoProvider(
    private val authPerUseEncryptionService: EncryptionService
) {
    private val secureRandom = SecureRandom()

    fun createEncryptCryptoObject(): BiometricPrompt.CryptoObject {
        return try {
            BiometricPrompt.CryptoObject(
                authPerUseEncryptionService.createEncryptCipher()
            )
        } catch (exception: Exception) {
            SecurityKeystoreFailureHandler.throwForFailure(
                operation = null,
                failureReason = "auth-per-use encrypt cipher creation failed",
                genericMessage = "Auth-per-use crypto is not available.",
                throwable = exception
            )
        }
    }

    fun completeEncryptProof(
        authenticationResult: BiometricPrompt.AuthenticationResult
    ): ByteArray {
        val cipher = authenticationResult.cryptoObject?.cipher
            ?: throw SecurityException("Auth-per-use cipher missing from authentication result")

        val challenge = ByteArray(PROOF_CHALLENGE_BYTES).also(secureRandom::nextBytes)

        return try {
            authPerUseEncryptionService.encryptWithCipher(
                cipher = cipher,
                plainText = challenge,
                aad = PROOF_AAD
            )
        } catch (exception: Exception) {
            SecurityKeystoreFailureHandler.throwForFailure(
                operation = null,
                failureReason = "auth-per-use proof encryption failed",
                genericMessage = "Auth-per-use crypto proof failed.",
                throwable = exception
            )
        }
    }

    private companion object {
        private const val PROOF_CHALLENGE_BYTES = 32
        private val PROOF_AAD =
            "lifeflow-auth-per-use-proof-v1".toByteArray(Charsets.UTF_8)
    }
}
