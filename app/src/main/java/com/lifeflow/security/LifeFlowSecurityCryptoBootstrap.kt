package com.lifeflow.security

import android.os.Build
import android.util.Log
import com.lifeflow.BuildConfig

private const val SECURITY_CRYPTO_BOOTSTRAP_TAG = "LifeFlowSecurityCryptoBootstrap"
private const val DEBUG_EMULATOR_SESSION_KEY_ALIAS = "lifeflow_debug_emulator_session_key"

internal fun createLifeFlowSecurityCryptoBindings(
    isInstrumentation: Boolean
): SecurityCryptoBindings {
    val debugEmulatorCryptoProfile = isDebugEmulatorCryptoProfile()

    val sessionKeyManager = when {
        isInstrumentation -> KeyManager(
            alias = TEST_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
        )

        debugEmulatorCryptoProfile -> KeyManager(
            alias = DEBUG_EMULATOR_SESSION_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
        )

        else -> KeyManager(
            alias = SESSION_KEY_ALIAS,
            authenticationPolicy = KeyManager.AuthenticationPolicy.BIOMETRIC_TIME_BOUND
        )
    }

    sessionKeyManager.ensureKey()
    val sessionEncryptionService = EncryptionService(sessionKeyManager)

    val authPerUseKeyManager = createLifeFlowSecurityAuthPerUseKeyManager(
        isInstrumentation = isInstrumentation,
        debugEmulatorCryptoProfile = debugEmulatorCryptoProfile
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
    isInstrumentation: Boolean,
    debugEmulatorCryptoProfile: Boolean
): KeyManager? {
    if (isInstrumentation) return null
    if (debugEmulatorCryptoProfile) return null
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


private fun isDebugEmulatorCryptoProfile(): Boolean {
    if (!BuildConfig.DEBUG) return false

    val fingerprint = Build.FINGERPRINT.lowercase()
    val model = Build.MODEL.lowercase()
    val manufacturer = Build.MANUFACTURER.lowercase()
    val brand = Build.BRAND.lowercase()
    val device = Build.DEVICE.lowercase()
    val product = Build.PRODUCT.lowercase()
    val hardware = Build.HARDWARE.lowercase()

    return fingerprint.contains("generic") ||
        fingerprint.contains("emulator") ||
        model.contains("sdk") ||
        model.contains("emulator") ||
        manufacturer.contains("google") && product.contains("sdk") ||
        brand.contains("google") && device.contains("emu") ||
        hardware.contains("goldfish") ||
        hardware.contains("ranchu")
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
