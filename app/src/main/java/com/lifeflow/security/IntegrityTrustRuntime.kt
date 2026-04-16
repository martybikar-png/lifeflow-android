package com.lifeflow.security

import android.content.Context
import com.lifeflow.security.integrity.PlayIntegrityBootstrapHandle
import com.lifeflow.security.integrity.PlayIntegrityVerifier

/**
 * Central runtime holder for the integrity trust boundary.
 *
 * Current scope:
 * - own prepared Play Integrity provider lifecycle
 * - own external verdict transport lifecycle
 * - expose a narrow token -> server-verdict entrypoint for future trust enforcement
 */
internal class IntegrityTrustRuntime internal constructor(
    private val applicationContext: Context,
    private val cloudProjectNumber: Long,
    private val bootstrapHandle: PlayIntegrityBootstrapHandle,
    private val transport: IntegrityTrustTransport?
) : IntegrityTrustBoundaryHandle {

    private val verdictRuntime = IntegrityTrustVerdictRuntime(
        cloudProjectNumber = cloudProjectNumber,
        generateRequestHash = PlayIntegrityVerifier::generateRequestHash,
        requestIntegrityToken = { requestHash, projectNumber ->
            PlayIntegrityVerifier.requestIntegrityToken(
                context = applicationContext,
                requestHash = requestHash,
                cloudProjectNumber = projectNumber
            )
        },
        requestExternalVerdict = { request ->
            requireTransport().requestVerdict(request)
        }
    )

    fun isConfigured(): Boolean =
        cloudProjectNumber != 0L && transport != null

    suspend fun requestServerVerdict(
        payload: String
    ): IntegrityTrustVerdictResponse {
        return verdictRuntime.requestServerVerdict(payload)
    }

    override fun close() {
        transport?.close()
        bootstrapHandle.close()
    }

    private fun requireTransport(): IntegrityTrustTransport {
        return transport
            ?: throw SecurityException(
                "Integrity trust transport is not configured. Server-side integrity verdict remains fail-closed."
            )
    }
}
