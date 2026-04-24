package com.lifeflow.security

import android.os.Build

internal enum class SecurityKeystoreFailureCode {
    POSTURE_MISMATCH,
    AUTHENTICATION_REQUIRED,
    KEY_PERMANENTLY_INVALIDATED,
    KEY_UNRECOVERABLE,
    KEY_INVALID_OR_UNAVAILABLE,
    KEYSTORE_ACCESS_FAILED
}

internal open class SecurityKeystoreOperationException(
    val code: SecurityKeystoreFailureCode,
    message: String,
    cause: Throwable? = null
) : SecurityException(message, cause)

internal class SecurityKeystorePostureException(
    message: String
) : SecurityKeystoreOperationException(
    code = SecurityKeystoreFailureCode.POSTURE_MISMATCH,
    message = message
)

internal const val KEY_MANAGER_ANDROID_KEYSTORE = "AndroidKeyStore"
internal const val KEY_MANAGER_DEFAULT_ALIAS = SESSION_KEY_ALIAS
internal const val KEY_MANAGER_KEY_SIZE_BITS = 256
internal const val KEY_MANAGER_AUTH_VALIDITY_SECONDS = 30
internal const val KEY_MANAGER_AUTH_PER_USE_VALIDITY_SECONDS = -1

internal fun supportsKeystoreAuthPerUseBiometric(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}