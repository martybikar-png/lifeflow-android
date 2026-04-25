package com.lifeflow.security.integrity

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal suspend fun prepareStandardIntegrityTokenProvider(
    context: Context,
    cloudProjectNumber: Long,
    onPrepared: (StandardIntegrityManager.StandardIntegrityTokenProvider) -> Unit,
    onFailed: () -> Unit
): PlayIntegrityVerifier.PreparationResult {
    return suspendCancellableCoroutine { continuation ->
        try {
            val standardIntegrityManager = IntegrityManagerFactory.createStandard(context)

            standardIntegrityManager
                .prepareIntegrityToken(
                    StandardIntegrityManager.PrepareIntegrityTokenRequest
                        .builder()
                        .setCloudProjectNumber(cloudProjectNumber)
                        .build()
                )
                .addOnSuccessListener { tokenProvider ->
                    onPrepared(tokenProvider)
                    continuation.resume(PlayIntegrityVerifier.PreparationResult.Prepared)
                }
                .addOnFailureListener { exception ->
                    onFailed()
                    continuation.resume(
                        PlayIntegrityVerifier.PreparationResult.Failure(
                            error = exception.message
                                ?: "Failed to prepare integrity token provider",
                            errorCode = resolveStandardIntegrityErrorCode(exception)
                        )
                    )
                }
        } catch (e: Exception) {
            onFailed()
            continuation.resume(
                PlayIntegrityVerifier.PreparationResult.Failure(
                    error = e.message ?: "Failed to initialize standard integrity manager",
                    errorCode = resolveStandardIntegrityErrorCode(e)
                )
            )
        }
    }
}
