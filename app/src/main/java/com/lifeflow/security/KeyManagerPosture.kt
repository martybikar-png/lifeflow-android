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

internal fun requireKeystoreOperationalPosture(
    alias: String,
    authenticationPolicy: KeyManager.AuthenticationPolicy,
    snapshot: KeyManager.KeyPostureSnapshot
): KeyManager.KeyPostureSnapshot {
    if (!snapshot.keyExists) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: keystore key is missing."
        )
    }

    when (authenticationPolicy) {
        KeyManager.AuthenticationPolicy.NONE ->
            requireNoAuthenticationPosture(snapshot)

        KeyManager.AuthenticationPolicy.BIOMETRIC_TIME_BOUND ->
            requireBiometricTimeBoundPosture(snapshot)

        KeyManager.AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE ->
            requireBiometricAuthPerUsePosture(snapshot)
    }

    return snapshot
}

private fun requireNoAuthenticationPosture(
    snapshot: KeyManager.KeyPostureSnapshot
) {
    if (snapshot.userAuthenticationRequired) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: authentication must not be required."
        )
    }
}

private fun requireBiometricTimeBoundPosture(
    snapshot: KeyManager.KeyPostureSnapshot
) {
    if (!snapshot.secureHardwareBacked) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must be hardware-backed."
        )
    }
    if (!snapshot.userAuthenticationRequired) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must require authentication."
        )
    }
    if (!snapshot.userAuthenticationEnforcedBySecureHardware) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound auth must be enforced by secure hardware."
        )
    }
    if (!snapshot.invalidatedByBiometricEnrollment) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must invalidate on biometric enrollment change."
        )
    }
    if ((snapshot.userAuthenticationValiditySeconds ?: 0) <= 0) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must have a positive auth window."
        )
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val authType = snapshot.userAuthenticationType ?: 0
        if (authType and KeyProperties.AUTH_BIOMETRIC_STRONG == 0) {
            failKeyPosture(
                "Key posture mismatch for alias=${snapshot.alias}: biometric time-bound key must require BIOMETRIC_STRONG."
            )
        }
    }
}

private fun requireBiometricAuthPerUsePosture(
    snapshot: KeyManager.KeyPostureSnapshot
) {
    if (!supportsKeystoreAuthPerUseBiometric()) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: biometric auth-per-use requires Android 11+."
        )
    }
    if (!snapshot.secureHardwareBacked) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must be hardware-backed."
        )
    }
    if (!snapshot.userAuthenticationRequired) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must require authentication."
        )
    }
    if (!snapshot.userAuthenticationEnforcedBySecureHardware) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: auth-per-use auth must be enforced by secure hardware."
        )
    }
    if (!snapshot.invalidatedByBiometricEnrollment) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must invalidate on biometric enrollment change."
        )
    }

    val authType = snapshot.userAuthenticationType ?: 0
    if (authType and KeyProperties.AUTH_BIOMETRIC_STRONG == 0) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must require BIOMETRIC_STRONG."
        )
    }
    if (snapshot.userAuthenticationValiditySeconds != KEY_MANAGER_AUTH_PER_USE_VALIDITY_SECONDS) {
        failKeyPosture(
            "Key posture mismatch for alias=${snapshot.alias}: auth-per-use key must require authentication for every use."
        )
    }
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