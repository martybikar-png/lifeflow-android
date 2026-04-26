package com.lifeflow.security.integrity

import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun requestPreparedStandardIntegrityToken(
    tokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider?,
    requestHash: String
): PlayIntegrityVerifier.IntegrityResult {
    val preparedProvider = tokenProvider
        ?: return PlayIntegrityVerifier.IntegrityResult.Failure(
            error = "Standard integrity token provider is unavailable"
        )

    return suspendCancellableCoroutine { continuation ->
        try {
            preparedProvider
                .request(
                    StandardIntegrityManager.StandardIntegrityTokenRequest
                        .builder()
                        .setRequestHash(requestHash)
                        .build()
                )
                .addOnSuccessListener { response ->
                    if (!continuation.isActive) return@addOnSuccessListener

                    continuation.resume(
                        PlayIntegrityVerifier.IntegrityResult.Success(response.token())
                    )
                }
                .addOnFailureListener { exception ->
                    if (!continuation.isActive) return@addOnFailureListener

                    continuation.resume(
                        PlayIntegrityVerifier.IntegrityResult.Failure(
                            error = exception.message ?: "Failed to request integrity token",
                            errorCode = resolveStandardIntegrityErrorCode(exception)
                        )
                    )
                }
        } catch (exception: RuntimeException) {
            if (!continuation.isActive) return@suspendCancellableCoroutine

            continuation.resume(
                PlayIntegrityVerifier.IntegrityResult.Failure(
                    error = exception.message ?: "Failed to execute integrity token request",
                    errorCode = resolveStandardIntegrityErrorCode(exception)
                )
            )
        }
    }
}
