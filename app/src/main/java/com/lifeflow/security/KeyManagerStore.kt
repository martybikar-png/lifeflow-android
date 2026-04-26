package com.lifeflow.security

import android.security.keystore.KeyInfo
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

internal fun readKeyInfo(
    alias: String,
    secretKey: SecretKey
): KeyInfo {
    return try {
        val secretKeyFactory = SecretKeyFactory.getInstance(
            secretKey.algorithm,
            KEY_MANAGER_ANDROID_KEYSTORE
        )
        secretKeyFactory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
    } catch (exception: Exception) {
        throw (keystoreOperationFailure(alias, "read-key-info", exception) ?: exception)
    }
}

internal fun loadSecretKeyOrNull(
    alias: String,
    keyStore: KeyStore,
    operation: String
): SecretKey? {
    return try {
        val key = keyStore.getKey(alias, null) ?: return null
        if (key !is SecretKey) {
            failKeystoreOperation(
                code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
                message = "Invalid key type for alias=$alias during $operation."
            )
        }
        key
    } catch (exception: Exception) {
        throw (keystoreOperationFailure(alias, operation, exception) ?: exception)
    }
}

internal fun failKeyPosture(
    message: String
): Nothing {
    throw SecurityKeystorePostureException(message)
}

internal fun failKeystoreOperation(
    code: SecurityKeystoreFailureCode,
    message: String
): Nothing {
    throw SecurityKeystoreOperationException(
        code = code,
        message = message
    )
}

internal fun loadAndroidKeyStore(
    alias: String
): KeyStore {
    return try {
        val keyStore = KeyStore.getInstance(KEY_MANAGER_ANDROID_KEYSTORE)
        keyStore.load(null)
        keyStore
    } catch (exception: Exception) {
        throw (keystoreOperationFailure(alias, "load-keystore", exception) ?: exception)
    }
}

private fun keystoreOperationFailure(
    alias: String,
    operation: String,
    exception: Exception
): SecurityKeystoreOperationException? {
    return when (exception) {
        is SecurityKeystoreOperationException -> exception

        is UnrecoverableKeyException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEY_UNRECOVERABLE,
            message = "Keystore key is unrecoverable during $operation for alias=$alias.",
            cause = exception
        )

        is InvalidKeySpecException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
            message = "Keystore key material could not be inspected during $operation for alias=$alias.",
            cause = exception
        )

        is IOException,
        is CertificateException,
        is ProviderException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEYSTORE_ACCESS_FAILED,
            message = "Keystore access failed during $operation for alias=$alias: ${exception::class.java.simpleName}.",
            cause = exception
        )

        is GeneralSecurityException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
            message = "Keystore operation failed during $operation for alias=$alias: ${exception::class.java.simpleName}.",
            cause = exception
        )

        else -> null
    }
}
