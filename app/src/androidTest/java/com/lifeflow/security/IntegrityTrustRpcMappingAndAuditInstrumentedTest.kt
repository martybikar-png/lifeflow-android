package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.audit.SecurityIntegrityAuditExplainability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IntegrityTrustRpcMappingAndAuditInstrumentedTest {

    @Test
    fun rpcMapper_rejectsRequestHashEchoMismatch_failClosed() {
        val response = IntegrityTrustRpcResponse(
            verdict = IntegrityTrustRpcVerdict.VERIFIED,
            reason = "SERVER_OK",
            requestHashEcho = "different-hash",
            serverTimestampEpochMs = FIXED_NOW_EPOCH_MS,
            policyVersion = "policy-v1",
            verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
        )

        val mapped = IntegrityTrustRpcMapper.fromRpcResponse(
            response = response,
            expectedRequestHash = "expected-hash"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, mapped.verdict)
        assertEquals(
            IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            mapped.verdictSource
        )
        assertTrue(
            mapped.reason.contains(
                "SERVER_VERDICT_METADATA_INVALID",
                ignoreCase = true
            )
        )
    }

    @Test
    fun rpcMapper_mapsFullVerifiedResponse_withClaimsAndAttestation() {
        val response = IntegrityTrustRpcResponse(
            verdict = IntegrityTrustRpcVerdict.VERIFIED,
            reason = "SERVER_OK",
            requestHashEcho = "expected-hash",
            serverTimestampEpochMs = FIXED_NOW_EPOCH_MS,
            policyVersion = "policy-v1",
            verdictSource = IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            claims = IntegrityTrustRpcClaims(
                appRecognitionVerdict =
                    IntegrityTrustRpcAppRecognitionVerdict.PLAY_RECOGNIZED,
                deviceRecognitionVerdicts = linkedSetOf(
                    IntegrityTrustRpcDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
                ),
                appLicensingVerdict =
                    IntegrityTrustRpcAppLicensingVerdict.LICENSED,
                playProtectVerdict =
                    IntegrityTrustRpcPlayProtectVerdict.NO_ISSUES
            ),
            attestationVerification = IntegrityTrustRpcAttestationVerification(
                chainVerdict = IntegrityTrustRpcAttestationChainVerdict.VERIFIED,
                challengeVerdict = IntegrityTrustRpcAttestationChallengeVerdict.MATCHED,
                rootVerdict = IntegrityTrustRpcAttestationRootVerdict.GOOGLE_TRUSTED,
                revocationVerdict = IntegrityTrustRpcAttestationRevocationVerdict.CLEAN,
                appBindingVerdict = IntegrityTrustRpcAttestationAppBindingVerdict.MATCHED,
                detail = "rpc attestation valid"
            )
        )

        val mapped = IntegrityTrustRpcMapper.fromRpcResponse(
            response = response,
            expectedRequestHash = "expected-hash"
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, mapped.verdict)
        assertEquals(
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            mapped.verdictSource
        )
        assertEquals("expected-hash", mapped.requestHashEcho)
        assertEquals("policy-v1", mapped.policyVersion)
        assertEquals(
            SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
            mapped.claims.appRecognitionVerdict
        )
        assertTrue(
            mapped.claims.deviceRecognitionVerdicts.contains(
                SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
            )
        )
        assertEquals(
            IntegrityTrustAttestationChainVerdict.VERIFIED,
            mapped.attestationVerification?.chainVerdict
        )
        assertEquals(
            IntegrityTrustAttestationAppBindingVerdict.MATCHED,
            mapped.attestationVerification?.appBindingVerdict
        )
    }

    @Test
    fun auditExplainability_classifiesPlayIntegrityTransientFailure() {
        val snapshot = SecurityIntegrityAuditExplainability.snapshot(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = "PLAY_INTEGRITY_TRANSIENT_FAILURE[-100]: network timeout",
                verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
            )
        )

        assertEquals(
            SecurityIntegrityTrustVerdict.DEGRADED.name,
            snapshot.metadata["verdict"]
        )
        assertEquals(
            "PLAY_INTEGRITY_TRANSIENT_FAILURE",
            snapshot.metadata["decisionPath"]
        )
        assertTrue(
            snapshot.message.contains("downgraded", ignoreCase = true)
        )
    }

    @Test
    fun auditExplainability_classifiesClientAttestationHardFailure_byCurrentOrdering() {
        val snapshot = SecurityIntegrityAuditExplainability.snapshot(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.DEGRADED,
                reason = "PLAY_INTEGRITY_HARD_FAILURE[-999]: unavailable | ATTESTATION=HARD_FAILURE: ProviderException",
                verdictSource = IntegrityTrustVerdictSource.CLIENT_FAILSAFE
            )
        )

        assertEquals(
            "PLAY_INTEGRITY_HARD_FAILURE",
            snapshot.metadata["decisionPath"]
        )
        assertTrue(
            snapshot.metadata["reasonSummary"]
                .orEmpty()
                .contains("ATTESTATION=HARD_FAILURE:")
        )
    }

    @Test
    fun auditExplainability_abbreviatesRequestHashEcho_andKeepsAttestationMetadata() {
        val snapshot = SecurityIntegrityAuditExplainability.snapshot(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
                reason = "SERVER_VERDICT_ATTESTATION_INVALID: challenge mismatch",
                requestHashEcho = "1234567890ABCDEF1234567890ABCDEF",
                serverTimestampEpochMs = FIXED_NOW_EPOCH_MS,
                policyVersion = "policy-v1",
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
                attestationVerification = IntegrityTrustAttestationVerification(
                    chainVerdict = IntegrityTrustAttestationChainVerdict.FAILED,
                    challengeVerdict = IntegrityTrustAttestationChallengeVerdict.MISMATCHED,
                    rootVerdict = IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED,
                    revocationVerdict = IntegrityTrustAttestationRevocationVerdict.CLEAN,
                    appBindingVerdict = IntegrityTrustAttestationAppBindingVerdict.MATCHED,
                    detail = "challenge mismatch detail"
                )
            )
        )

        assertEquals("SERVER_ATTESTATION_INVALID", snapshot.metadata["decisionPath"])
        assertEquals("12345678...90ABCDEF", snapshot.metadata["requestHashEchoShort"])
        assertEquals(
            IntegrityTrustAttestationChallengeVerdict.MISMATCHED.name,
            snapshot.metadata["attestationChallengeVerdict"]
        )
        assertEquals(
            SecurityIntegrityPlayProtectVerdict.NO_ISSUES.name,
            snapshot.metadata["playProtectVerdict"]
        )
    }

    private companion object {
        private const val FIXED_NOW_EPOCH_MS = 1_800_000_000_000L
    }
}
