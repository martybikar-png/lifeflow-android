package com.lifeflow.security.integrity

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * PlayIntegrityVerifier — Google Play Integrity API wrapper.
 * 
 * Provides device attestation to verify:
 * - App is genuine (not modified)
 * - Device has Play Protect certification
 * - Device meets integrity requirements
 */
object PlayIntegrityVerifier {

    sealed class IntegrityResult {
        data class Success(val token: String) : IntegrityResult()
        data class Failure(val error: String, val errorCode: Int? = null) : IntegrityResult()
        object NotConfigured : IntegrityResult()
    }

    suspend fun requestIntegrityToken(
        context: Context,
        nonce: String,
        cloudProjectNumber: Long
    ): IntegrityResult {
        if (cloudProjectNumber == 0L) {
            return IntegrityResult.NotConfigured
        }

        return suspendCancellableCoroutine { continuation ->
            try {
                val integrityManager = IntegrityManagerFactory.create(context)
                
                val request = IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .setCloudProjectNumber(cloudProjectNumber)
                    .build()

                integrityManager.requestIntegrityToken(request)
                    .addOnSuccessListener { response ->
                        continuation.resume(IntegrityResult.Success(response.token()))
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(
                            IntegrityResult.Failure(
                                error = exception.message ?: "Unknown error",
                                errorCode = null
                            )
                        )
                    }
            } catch (e: Exception) {
                continuation.resume(
                    IntegrityResult.Failure(
                        error = e.message ?: "Failed to initialize Integrity API"
                    )
                )
            }
        }
    }

    fun generateNonce(): String {
        val bytes = ByteArray(32)
        java.security.SecureRandom().nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }
}
