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
    } catch (t: Throwable) {
        throw (keystoreOperationFailure(alias, "read-key-info", t) ?: t)
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
    } catch (t: Throwable) {
        throw (keystoreOperationFailure(alias, operation, t) ?: t)
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
    } catch (t: Throwable) {
        throw (keystoreOperationFailure(alias, "load-keystore", t) ?: t)
    }
}

private fun keystoreOperationFailure(
    alias: String,
    operation: String,
    throwable: Throwable
): SecurityKeystoreOperationException? {
    return when (throwable) {
        is SecurityKeystoreOperationException -> throwable

        is UnrecoverableKeyException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEY_UNRECOVERABLE,
            message = "Keystore key is unrecoverable during $operation for alias=$alias.",
            cause = throwable
        )

        is InvalidKeySpecException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
            message = "Keystore key material could not be inspected during $operation for alias=$alias.",
            cause = throwable
        )

        is IOException,
        is CertificateException,
        is ProviderException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEYSTORE_ACCESS_FAILED,
            message = "Keystore access failed during $operation for alias=$alias: ${throwable::class.java.simpleName}.",
            cause = throwable
        )

        is GeneralSecurityException -> SecurityKeystoreOperationException(
            code = SecurityKeystoreFailureCode.KEY_INVALID_OR_UNAVAILABLE,
            message = "Keystore operation failed during $operation for alias=$alias: ${throwable::class.java.simpleName}.",
            cause = throwable
        )

        else -> null
    }
}