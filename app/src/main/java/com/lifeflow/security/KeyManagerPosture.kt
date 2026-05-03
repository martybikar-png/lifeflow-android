package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties

internal fun readKeyPostureSnapshot(
    alias: String
): KeyManager.KeyPostureSnapshot {
    val keyStore = loadAndroidKeyStore(alias = alias)
    if (!keyStore.containsAlias(alias)) {
        return KeyManager.KeyPostureSnapshot(
            alias = alias,
            keyExists = false,
            securityLevel = null,
            secureHardwareBacked = false,
            userAuthenticationRequired = false,
            userAuthenticationType = null,
            userAuthenticationValiditySeconds = null,
            userAuthenticationEnforcedBySecureHardware = false,
            invalidatedByBiometricEnrollment = false
        )
    }

    val secretKey = loadSecretKeyOrNull(
        alias = alias,
        keyStore = keyStore,
        operation = "read-key-posture"
    ) ?: failKeystoreOperation(
        code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
        message = "Keystore alias exists but key is missing for alias=$alias during read-key-posture."
    )

    val keyInfo = readKeyInfo(
        alias = alias,
        secretKey = secretKey
    )
    val securityLevel = resolveSecurityLevel(keyInfo)

    return KeyManager.KeyPostureSnapshot(
        alias = alias,
        keyExists = true,
        securityLevel = securityLevel,
        secureHardwareBacked = isSecureHardwareBacked(
            keyInfo = keyInfo,
            securityLevel = securityLevel
        ),
        userAuthenticationRequired = keyInfo.isUserAuthenticationRequired,
        userAuthenticationType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                keyInfo.userAuthenticationType
            } else {
                null
            },
        userAuthenticationValiditySeconds =
            keyInfo.userAuthenticationValidityDurationSeconds,
        userAuthenticationEnforcedBySecureHardware =
            keyInfo.isUserAuthenticationRequirementEnforcedBySecureHardware,
        invalidatedByBiometricEnrollment =
            keyInfo.isInvalidatedByBiometricEnrollment
    )
}

private fun resolveSecurityLevel(
    keyInfo: KeyInfo
): Int? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        keyInfo.securityLevel
    } else {
        null
    }
}

private fun isSecureHardwareBacked(
    keyInfo: KeyInfo,
    securityLevel: Int?
): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && securityLevel != null) {
        securityLevel == KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT ||
            securityLevel == KeyProperties.SECURITY_LEVEL_STRONGBOX ||
            securityLevel == KeyProperties.SECURITY_LEVEL_UNKNOWN_SECURE
    } else {
        @Suppress("DEPRECATION")
        keyInfo.isInsideSecureHardware
    }
}
