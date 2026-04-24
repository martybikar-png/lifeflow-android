package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityIntegrityClaimsAndRpcNormalizationInstrumentedTest {

    @Test
    fun normalizeRpcResponse_requestHashMismatch_failsClosed() {
        val policy = SecurityIntegrityServerVerdictPolicy()

        val normalized = policy.normalizeRpcResponse(
            response = validRpcResponse(
                requestHashEcho = "rpc-wrong-hash"
            ),
            expectedRequestHash = "rpc-expected-hash"
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, normalized.verdict)
        assertEquals(
            IntegrityTrustVerdictSource.CLIENT_FAILSAFE,
            normalized.verdictSource
        )
        assertTrue(
            normalized.reason.contains(
                "SERVER_VERDICT_METADATA_INVALID",
                ignoreCase = true
            )
        )
        assertTrue(
            normalized.reason.contains(
                "requestHash echo mismatch",
                ignoreCase = true
            )
        )
    }

    @Test
    fun claimsPolicy_cleanVerifiedVerdict_keepsVerified() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val enforced = policy.enforce(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                reason = "SERVER_OK",
                requestHashEcho = "claims-clean-hash",
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
                )
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.VERIFIED, enforced.verdict)
        assertEquals("SERVER_OK", enforced.reason)
    }

    @Test
    fun claimsPolicy_unevaluatedAppRecognition_degradesVerifiedVerdict() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val enforced = policy.enforce(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                reason = "SERVER_OK",
                requestHashEcho = "claims-app-recognition-hash",
                serverTimestampEpochMs = FIXED_NOW_EPOCH_MS,
                policyVersion = "policy-v1",
                verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
                claims = SecurityIntegrityVerdictClaims(
                    appRecognitionVerdict =
                        SecurityIntegrityAppRecognitionVerdict.UNEVALUATED,
                    deviceRecognitionVerdicts = linkedSetOf(
                        SecurityIntegrityDeviceRecognitionVerdict.MEETS_DEVICE_INTEGRITY
                    ),
                    appLicensingVerdict =
                        SecurityIntegrityAppLicensingVerdict.LICENSED,
                    playProtectVerdict =
                        SecurityIntegrityPlayProtectVerdict.NO_ISSUES
                )
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, enforced.verdict)
        assertTrue(
            enforced.reason.contains(
                "claimsPolicy=DEGRADED",
                ignoreCase = true
            )
        )
        assertTrue(
            enforced.reason.contains(
                "app recognition is unevaluated",
                ignoreCase = true
            )
        )
    }

    @Test
    fun claimsPolicy_unlicensedApp_compromisesVerifiedVerdict() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val enforced = policy.enforce(
            IntegrityTrustVerdictResponse(
                verdict = SecurityIntegrityTrustVerdict.VERIFIED,
                reason = "SERVER_OK",
                requestHashEcho = "claims-unlicensed-hash",
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
                        SecurityIntegrityAppLicensingVerdict.UNLICENSED,
                    playProtectVerdict =
                        SecurityIntegrityPlayProtectVerdict.NO_ISSUES
                )
            )
        )

        assertEquals(SecurityIntegrityTrustVerdict.COMPROMISED, enforced.verdict)
        assertTrue(
            enforced.reason.contains(
                "claimsPolicy=COMPROMISED",
                ignoreCase = true
            )
        )
        assertTrue(
            enforced.reason.contains(
                "UNLICENSED",
                ignoreCase = true
            )
        )
    }

    @Test
    fun claimsPolicy_alreadyDegradedVerdict_staysDegraded_forNonCriticalClaims() {
        val policy = SecurityIntegrityClaimsEnforcementPolicy()

        val original = IntegrityTrustVerdictResponse(
            verdict = SecurityIntegrityTrustVerdict.DEGRADED,
            reason = "SERVER_ALREADY_DEGRADED",
            requestHashEcho = "claims-already-degraded-hash",
            serverTimestampEpochMs = FIXED_NOW_EPOCH_MS,
            policyVersion = "policy-v1",
            verdictSource = IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER,
            claims = SecurityIntegrityVerdictClaims(
                appRecognitionVerdict =
                    SecurityIntegrityAppRecognitionVerdict.PLAY_RECOGNIZED,
                deviceRecognitionVerdicts = linkedSetOf(
                    SecurityIntegrityDeviceRecognitionVerdict.MEETS_BASIC_INTEGRITY
                ),
                appLicensingVerdict =
                    SecurityIntegrityAppLicensingVerdict.UNEVALUATED,
                playProtectVerdict =
                    SecurityIntegrityPlayProtectVerdict.NO_DATA
            )
        )

        val enforced = policy.enforce(original)

        assertEquals(SecurityIntegrityTrustVerdict.DEGRADED, enforced.verdict)
        assertEquals("SERVER_ALREADY_DEGRADED", enforced.reason)
    }

    private fun validRpcResponse(
        requestHashEcho: String
    ): IntegrityTrustRpcResponse {
        return IntegrityTrustRpcResponse(
            verdict = IntegrityTrustRpcVerdict.VERIFIED,
            reason = "RPC_SERVER_OK",
            requestHashEcho = requestHashEcho,
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
                detail = "rpc androidTest valid attestation"
            )
        )
    }

    private companion object {
        private const val FIXED_NOW_EPOCH_MS = 1_800_000_000_000L
    }
}
