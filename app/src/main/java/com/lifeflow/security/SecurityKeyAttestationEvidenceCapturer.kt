package com.lifeflow.security

import android.util.Base64
import android.util.Log
import java.security.MessageDigest
import java.security.cert.Certificate

internal class SecurityKeyAttestationEvidenceCapturer {
    fun captureRequestBoundEvidence(
        requestHash: String
    ): SecurityKeyAttestationEvidence {
        return captureEvidence(
            challenge = buildRequestBoundChallenge(requestHash)
        )
    }

    private fun captureEvidence(
        challenge: ByteArray
    ): SecurityKeyAttestationEvidence {
        deleteExistingAttestationAliasIfPresent()

        return try {
            val strongBoxRequested = generateAttestedKeyPair(challenge)
            val certificateChain = loadAttestationCertificateChain()

            if (certificateChain.isEmpty()) {
                unavailableEvidence(
                    reason = "Attestation certificate chain is missing.",
                    strongBoxRequested = strongBoxRequested
                )
            } else {
                capturedEvidence(
                    challenge = challenge,
                    certificateChain = certificateChain,
                    strongBoxRequested = strongBoxRequested
                )
            }
        } catch (exception: Exception) {
            deleteExistingAttestationAliasIfPresent()
            hardFailureEvidence(
                reason = "${exception::class.java.simpleName}: ${exception.message ?: "unknown"}",
                strongBoxRequested = false
            )
        }
    }

    private fun capturedEvidence(
        challenge: ByteArray,
        certificateChain: Array<Certificate>,
        strongBoxRequested: Boolean
    ): SecurityKeyAttestationEvidence {
        val encodedChain = certificateChain.map { certificate ->
            Base64.encodeToString(certificate.encoded, Base64.NO_WRAP)
        }

        return SecurityKeyAttestationEvidence(
            status = SecurityKeyAttestationStatus.CAPTURED,
            keyAlias = ATTESTATION_KEY_ALIAS,
            chainEntryCount = encodedChain.size,
            challengeBase64 = Base64.encodeToString(challenge, Base64.NO_WRAP),
            certificateChainDerBase64 = encodedChain,
            challengeSha256 = sha256Base64(challenge),
            leafCertificateSha256 = sha256Base64(certificateChain.first().encoded),
            strongBoxRequested = strongBoxRequested
        )
    }

    private fun buildRequestBoundChallenge(
        requestHash: String
    ): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(REQUEST_BOUND_CHALLENGE_CONTEXT.toByteArray(Charsets.UTF_8))
        digest.update('\n'.code.toByte())
        digest.update(requestHash.trim().toByteArray(Charsets.UTF_8))
        return digest.digest()
    }

    private fun sha256Base64(
        value: ByteArray
    ): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    private fun unavailableEvidence(
        reason: String,
        strongBoxRequested: Boolean
    ): SecurityKeyAttestationEvidence {
        Log.w(TAG, "Key attestation unavailable: $reason")
        return SecurityKeyAttestationEvidence(
            status = SecurityKeyAttestationStatus.UNAVAILABLE,
            keyAlias = ATTESTATION_KEY_ALIAS,
            chainEntryCount = 0,
            strongBoxRequested = strongBoxRequested,
            failureKind = SecurityKeyAttestationFailureKind.UNAVAILABLE,
            failureReason = reason
        )
    }

    private fun hardFailureEvidence(
        reason: String,
        strongBoxRequested: Boolean
    ): SecurityKeyAttestationEvidence {
        Log.e(TAG, "Key attestation hard failure: $reason")
        return SecurityKeyAttestationEvidence(
            status = SecurityKeyAttestationStatus.UNAVAILABLE,
            keyAlias = ATTESTATION_KEY_ALIAS,
            chainEntryCount = 0,
            strongBoxRequested = strongBoxRequested,
            failureKind = SecurityKeyAttestationFailureKind.HARD_FAILURE,
            failureReason = reason
        )
    }

    private companion object {
        private const val TAG = "SecurityKeyAttestation"
        private const val REQUEST_BOUND_CHALLENGE_CONTEXT =
            "lifeflow-request-bound-attestation-v1"
    }
}
