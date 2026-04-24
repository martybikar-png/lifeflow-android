package com.lifeflow.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import javax.crypto.KeyGenerator

internal fun generateKeystoreKey(
    alias: String,
    authenticationPolicy: KeyManager.AuthenticationPolicy,
    useStrongBox: Boolean
) {
    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        KEY_MANAGER_ANDROID_KEYSTORE
    )

    val builder = KeyGenParameterSpec.Builder(
        alias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setKeySize(KEY_MANAGER_KEY_SIZE_BITS)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)

    configureKeyAuthentication(
        builder = builder,
        authenticationPolicy = authenticationPolicy
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && useStrongBox) {
        builder.setIsStrongBoxBacked(true)
    }

    keyGenerator.init(builder.build())
    keyGenerator.generateKey()
}

private fun configureKeyAuthentication(
    builder: KeyGenParameterSpec.Builder,
    authenticationPolicy: KeyManager.AuthenticationPolicy
) {
    when (authenticationPolicy) {
        KeyManager.AuthenticationPolicy.NONE -> Unit

        KeyManager.AuthenticationPolicy.BIOMETRIC_TIME_BOUND -> {
            builder.setUserAuthenticationRequired(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                builder.setUserAuthenticationParameters(
                    KEY_MANAGER_AUTH_VALIDITY_SECONDS,
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
            } else {
                @Suppress("DEPRECATION")
                builder.setUserAuthenticationValidityDurationSeconds(
                    KEY_MANAGER_AUTH_VALIDITY_SECONDS
                )
            }
        }

        KeyManager.AuthenticationPolicy.BIOMETRIC_AUTH_PER_USE -> {
            require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                "Biometric auth-per-use requires Android 11+ (API 30)."
            }

            builder.setUserAuthenticationRequired(true)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(true)
            }

            builder.setUserAuthenticationParameters(
                0,
                KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        }
    }
}