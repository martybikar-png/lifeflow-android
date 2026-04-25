package com.lifeflow.security.integrity

import android.content.Context
import com.google.android.play.core.integrity.StandardIntegrityManager
import java.util.concurrent.atomic.AtomicReference

/**
 * PlayIntegrityVerifier — standard Play Integrity API facade.
 *
 * Design goals:
 * - keep public verifier API stable for integrity runtime callers
 * - keep token-provider preparation cached and reusable
 * - keep request payload binding explicit through requestHash
 * - remain fail-closed when cloud project is not configured
 * - delegate Play Integrity request mechanics to focused helpers
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

        return prepareStandardIntegrityTokenProvider(
            context = context,
            cloudProjectNumber = cloudProjectNumber,
            onPrepared = { tokenProvider ->
                tokenProviderRef.set(tokenProvider)
            },
            onFailed = {
                tokenProviderRef.set(null)
            }
        )
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

        val firstAttempt = requestPreparedStandardIntegrityToken(
            tokenProvider = tokenProviderRef.get(),
            requestHash = requestHash
        )

        if (shouldRefreshStandardIntegrityProvider(firstAttempt)) {
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

            return requestPreparedStandardIntegrityToken(
                tokenProvider = tokenProviderRef.get(),
                requestHash = requestHash
            )
        }

        return firstAttempt
    }

    fun clearPreparedProvider() {
        tokenProviderRef.set(null)
    }

    fun generateRequestHash(payload: String): String {
        return generatePlayIntegrityRequestHash(payload)
    }

    fun generateRandomNonce(): String {
        return generatePlayIntegrityRandomNonce()
    }
}
