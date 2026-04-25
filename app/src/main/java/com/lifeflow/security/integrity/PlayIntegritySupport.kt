package com.lifeflow.security.integrity

import android.util.Base64
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import java.security.MessageDigest
import java.security.SecureRandom

internal fun generatePlayIntegrityRequestHash(payload: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(payload.toByteArray(Charsets.UTF_8))

    return Base64.encodeToString(digest, Base64.NO_WRAP)
}

internal fun generatePlayIntegrityRandomNonce(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

internal fun shouldRefreshStandardIntegrityProvider(
    result: PlayIntegrityVerifier.IntegrityResult
): Boolean {
    return result is PlayIntegrityVerifier.IntegrityResult.Failure &&
        result.errorCode == StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID
}

internal fun resolveStandardIntegrityErrorCode(
    throwable: Throwable
): Int? {
    return (throwable as? StandardIntegrityException)?.errorCode
}
