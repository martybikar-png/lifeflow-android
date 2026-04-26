package com.lifeflow.security

import android.util.Log

private const val SECURITY_CRYPTO_BOOTSTRAP_TAG = "LifeFlowSecurityCryptoBootstrap"

internal fun createLifeFlowSecurityCryptoBindings(
    isInstrumentation: Boolean
): SecurityCryptoBindings {
    val sessionKeyManager = if (isInstrumentation) {
        KeyManager(
            alias = TEST_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
        )
    } else {
        KeyManager(
            alias = SESSION_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.BIOMETRIC_TIME_BOUND
        )
    }

    val sessionEncryptionService = EncryptionService(sessionKeyManager)

    val authPerUseKeyManager = createLifeFlowSecurityAuthPerUseKeyManager(
        isInstrumentation = isInstrumentation
    )
    val authPerUseEncryptionService = authPerUseKeyManager?.let(::EncryptionService)

    return SecurityCryptoBindings(
        sessionKeyManager = sessionKeyManager,
        sessionEncryptionService = sessionEncryptionService,
        authPerUseKeyManager = authPerUseKeyManager,
        authPerUseEncryptionService = authPerUseEncryptionService
    )
}

private fun createLifeFlowSecurityAuthPerUseKeyManager(
    isInstrumentation: Boolean
): KeyManager? {
    if (isInstrumentation) return null
    if (!KeyManager.supportsAuthPerUseBiometric()) return null

    val keyManager = KeyManager(
        alias = AUTH_PER_USE_KEY_ALIAS,
        authenticationPolicy = KeyManager.AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE
    )

    return try {
        keyManager.ensureKey()
        keyManager
    } catch (exception: Exception) {
        handleLifeFlowSecurityAuthPerUseBootstrapFailure(
            keyManager = keyManager,
            exception = exception
        )
        null
    }
}

private fun handleLifeFlowSecurityAuthPerUseBootstrapFailure(
    keyManager: KeyManager,
    exception: Exception
) {
    when (exception) {
        is SecurityKeystorePostureException -> {
            deleteLifeFlowSecurityAuthPerUseKeyIfPresent(
                keyManager = keyManager,
                reason = "posture mismatch"
            )
            Log.w(
                SECURITY_CRYPTO_BOOTSTRAP_TAG,
                "Auth-per-use crypto disabled: keystore posture mismatch.",
                exception
            )
        }

        is SecurityKeystoreOperationException -> {
            if (exception.code == SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE ||
                exception.code == SecurityKeystoreFailureCode.KEY_UNRECOVERABLE
            ) {
                deleteLifeFlowSecurityAuthPerUseKeyIfPresent(
                    keyManager = keyManager,
                    reason = "invalid or unrecoverable key"
                )
            }

            Log.w(
                SECURITY_CRYPTO_BOOTSTRAP_TAG,
                "Auth-per-use crypto disabled: keystore bootstrap failed (${exception.code}).",
                exception
            )
        }

        else -> {
            Log.w(
                SECURITY_CRYPTO_BOOTSTRAP_TAG,
                "Auth-per-use crypto disabled: unexpected bootstrap failure.",
                exception
            )
        }
    }
}

private fun deleteLifeFlowSecurityAuthPerUseKeyIfPresent(
    keyManager: KeyManager,
    reason: String
) {
    try {
        keyManager.deleteKey()
    } catch (exception: Exception) {
        Log.w(
            SECURITY_CRYPTO_BOOTSTRAP_TAG,
            "Auth-per-use key cleanup failed after $reason.",
            exception
        )
    }
}
