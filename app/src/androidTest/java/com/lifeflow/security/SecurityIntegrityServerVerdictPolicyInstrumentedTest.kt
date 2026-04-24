package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityIntegrityServerVerdictPolicyInstrumentedTest {

    @Test
    fun clientFailsafeSource_isAnnotatedWithoutChangingVerdict() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val normalized = policy.normalize(
            response = IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = "PLAY_INTEGRITY_NOT_CONFIGURED",
                verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, normalized.verdict)
        assertEquals(
            IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            normalized.verdictSource
        )
        assertTrue(normalized.reason.contains("source=CLIENT_FAILSAFE"))
    }

    @Test
    fun duplicateRequestHashEcho_secondPassFailsClosed() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val response = validServerResponse(
            requestHashEcho = "duplicate-hash"
        )

        val first = policy.normalize(
            response = response,
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )
        val second = policy.normalize(
            response = response.copy(reason = "SERVER_OK_DUPLICATE"),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertTrue(first.reason.isNotBlank())
        assertClientFailsafeCompromised(second)
        assertTrue(second.reason.contains("SERVER_VERDICT_METADATA_INVALID"))
    }

    @Test
    fun invalidPolicyVersion_failsClosed() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val normalized = policy.normalize(
            response = validServerResponse(
                requestHashEcho = "invalid-policy-hash",
                policyVersion = "bad-policy"
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertClientFailsafeCompromised(normalized)
        assertTrue(normalized.reason.contains("SERVER_VERDICT_METADATA_INVALID"))
    }

    @Test
    fun staleMetadata_failsClosed() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val normalized = policy.normalize(
            response = validServerResponse(
                requestHashEcho = "stale-metadata-hash",
                serverTimestampEpochMs = FIXED_NOW_EPOCH_MS - 600_000L
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertClientFailsafeCompromised(normalized)
        assertTrue(normalized.reason.contains("SERVER_VERDICT_METADATA_INVALID"))
    }

    @Test
    fun attestationChallengeMismatch_failsClosed() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val normalized = policy.normalize(
            response = validServerResponse(
                requestHashEcho = "attestation-mismatch-hash",
                attestationVerification = IntegrityTrustAttestationVerification(
                    chainVerdict = IntegrityTrustAttestationChainVerdict.VERIFIED,
                    challengeVerdict = IntegrityTrustAttestationChallengeVerdict.MISMATCHED,
                    rootVerdict = IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED,
                    revocationVerdict = IntegrityTrustAttestationRevocationVerdict.CLEAN,
                    appBindingVerdict = IntegrityTrustAttestationAppBindingVerdict.MATCHED,
                    detail = "androidTest mismatch"
                )
            ),
            nowEpochMs = FIXED_NOW_EPOCH_MS
        )

        assertClientFailsafeCompromised(normalized)
        assertTrue(normalized.reason.contains("INVALID"))
    }

    private fun assertClientFailsafeCompromised(
        response: IntegrityTrustVerdictResponse
    ) {
        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, response.verdict)
        assertEquals(
            IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            response.verdictSource
        )
        assertTrue(response.reason.isNotBlank())
    }

    private fun validServerResponse(
        requestHashEcho: String,
        serverTimestampEpochMs: Long = FIXED_NOW_EPOCH_MS,
        policyVersion: String = "policy-v1",
        attestationVerification: IntegrityTrustAttestationVerification =
            IntegrityTrustAttestationVerification(
                chainVerdict = IntegrityTrustAttestationChainVerdict.VERIFIED,
                challengeVerdict = IntegrityTrustAttestationChallengeVerdict.MATCHED,
                rootVerdict = IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED,
                revocationVerdict = IntegrityTrustAttestationRevocationVerdict.CLEAN,
                appBindingVerdict = IntegrityTrustAttestationAppBindingVerdict.MATCHED,
                detail = "androidTest valid attestation"
            )
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "SERVER_OK",
            requestHashEcho = requestHashEcho,
            serverTimestampEpochMs = serverTimestampEpochMs,
            policyVersion = policyVersion,
            verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            claims = SecurityIntegrityVerdictClaims(
                appRecognitionVerdict =
                    SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
                deviceRecognitionVerdicts = linkedSetOf(
                    SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
                ),
                appLicensingVerdict =
                    SecurityIntegrityAppLicensingVerdict.LICENSED,
                playProtectVerdict =
                    SecurityIntegrityPlayProtectVerdict.NO_ISSUES
            ),
            attestationVerification = attestationVerification
        )
    }

    private companion object {
        private const val FIXED_NOW_EPOCH_MS = 1_800_000_000_000L
    }
}
