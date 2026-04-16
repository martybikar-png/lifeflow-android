package com.lifeflow.security.integrity

import android.content.Context
import android.util.Base64
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
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
                                error = exception.message ?: "Failed to prepare integrity token provider",
                                errorCode = null
                            )
                        )
                    }
            } catch (e: Exception) {
                tokenProviderRef.set(null)
                continuation.resume(
                    PreparationResult.Failure(
                        error = e.message ?: "Failed to initialize standard integrity manager",
                        errorCode = null
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
                                errorCode = null
                            )
                        )
                    }
            } catch (e: Exception) {
                continuation.resume(
                    IntegrityResult.Failure(
                        error = e.message ?: "Failed to execute integrity token request",
                        errorCode = null
                    )
                )
            }
        }
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
}
