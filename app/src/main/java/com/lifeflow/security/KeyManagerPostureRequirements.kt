package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyProperties

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
