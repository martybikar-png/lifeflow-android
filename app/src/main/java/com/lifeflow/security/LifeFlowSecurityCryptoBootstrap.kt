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

    return runCatching {
        keyManager.ensureKey()
        keyManager
    }.getOrElse { throwable ->
        handleLifeFlowSecurityAuthPerUseBootstrapFailure(
            keyManager = keyManager,
            throwable = throwable
        )
        null
    }
}

private fun handleLifeFlowSecurityAuthPerUseBootstrapFailure(
    keyManager: KeyManager,
    throwable: Throwable
) {
    when (throwable) {
        is SecurityKeystorePostureException -> {
            deleteLifeFlowSecurityAuthPerUseKeySilently(
                keyManager = keyManager,
                reason = "posture mismatch"
            )
            Log.w(
                SECURITY_CRYPTO_BOOTSTRAP_TAG,
                "Auth-per-use crypto disabled: keystore posture mismatch.",
                throwable
            )
        }

        is SecurityKeystoreOperationException -> {
            if (throwable.code == SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE ||
                throwable.code == SecurityKeystoreFailureCode.KEY_UNRECOVERABLE
            ) {
                deleteLifeFlowSecurityAuthPerUseKeySilently(
                    keyManager = keyManager,
                    reason = "invalid or unrecoverable key"
                )
            }

            Log.w(
                SECURITY_CRYPTO_BOOTSTRAP_TAG,
                "Auth-per-use crypto disabled: keystore bootstrap failed (${throwable.code}).",
                throwable
            )
        }

        else -> {
            Log.w(
                SECURITY_CRYPTO_BOOTSTRAP_TAG,
                "Auth-per-use crypto disabled: unexpected bootstrap failure.",
                throwable
            )
        }
    }
}

private fun deleteLifeFlowSecurityAuthPerUseKeySilently(
    keyManager: KeyManager,
    reason: String
) {
    runCatching {
        keyManager.deleteKey()
    }.onFailure { deleteFailure ->
        Log.w(
            SECURITY_CRYPTO_BOOTSTRAP_TAG,
            "Auth-per-use key cleanup failed after $reason.",
            deleteFailure
        )
    }
}
