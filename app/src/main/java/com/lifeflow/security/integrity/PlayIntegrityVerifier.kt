package com.lifeflow.security.integrity

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityException
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.model.StandardIntegrityErrorCode
import kotlinx.coroutines.suspendCancellableCoroutine
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

/**
 * PlayIntegrityVerifier — standard Play Integrity API wrapper.
 *
 * Design goals:
 * - use Standard requests as the primary app integrity path
 * - keep token-provider preparation cached and reusable
 * - keep request payload binding explicit through requestHash
 * - remain fail-closed when cloud project is not configured
 * - preserve Standard Integrity error semantics for callers
 */
object PlayIntegrityVerifier {

    sealed class PreparationResult {
        data object Prepared : PreparationResult()
        data class Failure(
            val error: String,
            val errorCode: Int? = null
        ) : PreparationResult()

        data object NotConfigured : PreparationResult()
    }

    sealed class IntegrityResult {
        data class Success(val token: String) : IntegrityResult()
        data class Failure(
            val error: String,
            val errorCode: Int? = null
        ) : IntegrityResult()

        data object NotConfigured : IntegrityResult()
    }

    private val tokenProviderRef =
        AtomicReference<StandardIntegrityManager.StandardIntegrityTokenProvider?>(null)

    suspend fun prepareTokenProvider(
        context: Context,
        cloudProjectNumber: Long
    ): PreparationResult {
        if (cloudProjectNumber == 0L) {
            tokenProviderRef.set(null)
            return PreparationResult.NotConfigured
        }

        tokenProviderRef.get()?.let {
            return PreparationResult.Prepared
        }

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
                        tokenProviderRef.set(tokenProvider)
                        continuation.resume(PreparationResult.Prepared)
                    }
                    .addOnFailureListener { exception ->
                        tokenProviderRef.set(null)
                        continuation.resume(
                            PreparationResult.Failure(
                                error = exception.message
                                    ?: "Failed to prepare integrity token provider",
                                errorCode = resolveStandardIntegrityErrorCode(exception)
                            )
                        )
                    }
            } catch (e: Exception) {
                tokenProviderRef.set(null)
                continuation.resume(
                    PreparationResult.Failure(
                        error = e.message ?: "Failed to initialize standard integrity manager",
                        errorCode = resolveStandardIntegrityErrorCode(e)
                    )
                )
            }
        }
    }

    suspend fun requestIntegrityToken(
        context: Context,
        requestHash: String,
        cloudProjectNumber: Long
    ): IntegrityResult {
        if (requestHash.isBlank()) {
            return IntegrityResult.Failure("requestHash must not be blank")
        }

        when (val preparation = prepareTokenProvider(context, cloudProjectNumber)) {
            is PreparationResult.NotConfigured -> return IntegrityResult.NotConfigured
            is PreparationResult.Failure -> {
                return IntegrityResult.Failure(
                    error = preparation.error,
                    errorCode = preparation.errorCode
                )
            }

            PreparationResult.Prepared -> Unit
        }

        val firstAttempt = requestPreparedIntegrityToken(requestHash)

        if (shouldRefreshProvider(firstAttempt)) {
            tokenProviderRef.set(null)

            when (val retryPreparation = prepareTokenProvider(context, cloudProjectNumber)) {
                is PreparationResult.NotConfigured -> return IntegrityResult.NotConfigured
                is PreparationResult.Failure -> {
                    return IntegrityResult.Failure(
                        error = retryPreparation.error,
                        errorCode = retryPreparation.errorCode
                    )
                }

                PreparationResult.Prepared -> Unit
            }

            return requestPreparedIntegrityToken(requestHash)
        }

        return firstAttempt
    }

    fun clearPreparedProvider() {
        tokenProviderRef.set(null)
    }

    fun generateRequestHash(payload: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(Charsets.UTF_8))

        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    fun generateRandomNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private suspend fun requestPreparedIntegrityToken(
        requestHash: String
    ): IntegrityResult {
        val tokenProvider = tokenProviderRef.get()
            ?: return IntegrityResult.Failure(
                error = "Standard integrity token provider is unavailable"
            )

        return suspendCancellableCoroutine { continuation ->
            try {
                tokenProvider
                    .request(
                        StandardIntegrityManager.StandardIntegrityTokenRequest
                            .builder()
                            .setRequestHash(requestHash)
                            .build()
                    )
                    .addOnSuccessListener { response ->
                        continuation.resume(IntegrityResult.Success(response.token()))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(
                            IntegrityResult.Failure(
                                error = exception.message ?: "Failed to request integrity token",
                                errorCode = resolveStandardIntegrityErrorCode(exception)
                            )
                        )
                    }
            } catch (e: Exception) {
                continuation.resume(
                    IntegrityResult.Failure(
                        error = e.message ?: "Failed to execute integrity token request",
                        errorCode = resolveStandardIntegrityErrorCode(e)
                    )
                )
            }
        }
    }

    private fun shouldRefreshProvider(
        result: IntegrityResult
    ): Boolean {
        return result is IntegrityResult.Failure &&
            result.errorCode == StandardIntegrityErrorCode.INTEGRITY_TOKEN_PROVIDER_INVALID
    }

    private fun resolveStandardIntegrityErrorCode(
        throwable: Throwable
    ): Int? {
        return (throwable as? StandardIntegrityException)?.errorCode
    }
}
