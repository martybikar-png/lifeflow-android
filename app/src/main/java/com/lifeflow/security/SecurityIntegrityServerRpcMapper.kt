package com.lifeflow.security

/**
 * Maps server RPC integrity verdict payloads into the app-level trust model.
 */
internal class SecurityIntegrityServerRpcMapper {
    fun map(
        response: IntegrityTrustRpcResponse
    ): IntegrityTrustVerdictResponse {
        return IntegrityTrustVerdictResponse(
            verdict = mapVerdict(response.verdict),
            reason = response.reason.trim(),
            requestHashEcho = response.requestHashEcho,
            requestBindingVerified = response.requestBindingVerified,
            serverTimestampEpochMs = response.serverTimestampEpochMs,
            policyVersion = response.policyVersion,
            verdictSource = mapSource(response.verdictSource),
            claims = mapSecurityIntegrityVerdictClaimsFromRpc(response.claims),
            attestationVerification = mapAttestationVerification(
                response.attestationVerification
            ),
            decision = mapDecision(response.decision),
            decisionReasonCode = response.decisionReasonCode
        )
    }

    private fun mapVerdict(
        verdict: IntegrityTrustRpcVerdict
    ): SecurityIntegrityTrustVerdict {
        return when (verdict) {
            IntegrityTrustRpcVerdict.VERIFIED ->
                SecurityIntegrityTrustVerdict.VERIFIED

            IntegrityTrustRpcVerdict.DEGRADED ->
                SecurityIntegrityTrustVerdict.DEGRADED

            IntegrityTrustRpcVerdict.COMPROMISED ->
                SecurityIntegrityTrustVerdict.COMPROMISED
        }
    }

    private fun mapSource(
        source: IntegrityTrustRpcVerdictSource
    ): IntegrityTrustVerdictSource {
        return when (source) {
            IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER ->
                IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
        }
    }

    private fun mapDecision(
        decision: IntegrityTrustRpcDecision
    ): IntegrityTrustDecision {
        return when (decision) {
            IntegrityTrustRpcDecision.ALLOW -> IntegrityTrustDecision.ALLOW
            IntegrityTrustRpcDecision.STEP_UP -> IntegrityTrustDecision.STEP_UP
            IntegrityTrustRpcDecision.DEGRADED -> IntegrityTrustDecision.DEGRADED
            IntegrityTrustRpcDecision.DENY -> IntegrityTrustDecision.DENY
            IntegrityTrustRpcDecision.LOCK -> IntegrityTrustDecision.LOCK
        }
    }

    private fun mapAttestationVerification(
        verification: IntegrityTrustRpcAttestationVerification?
    ): IntegrityTrustAttestationVerification? {
        return verification?.let {
            IntegrityTrustAttestationVerification(
                chainVerdict = when (it.chainVerdict) {
                    IntegrityTrustRpcAttestationChainVerdict.VERIFIED ->
                        IntegrityTrustAttestationChainVerdict.VERIFIED

                    IntegrityTrustRpcAttestationChainVerdict.FAILED ->
                        IntegrityTrustAttestationChainVerdict.FAILED

                    IntegrityTrustRpcAttestationChainVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationChainVerdict.UNEVALUATED
                },
                challengeVerdict = when (it.challengeVerdict) {
                    IntegrityTrustRpcAttestationChallengeVerdict.MATCHED ->
                        IntegrityTrustAttestationChallengeVerdict.MATCHED

                    IntegrityTrustRpcAttestationChallengeVerdict.MISMATCHED ->
                        IntegrityTrustAttestationChallengeVerdict.MISMATCHED

                    IntegrityTrustRpcAttestationChallengeVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationChallengeVerdict.UNEVALUATED
                },
                rootVerdict = when (it.rootVerdict) {
                    IntegrityTrustRpcAttestationRootVerdict.GOOGLE_TRUSTED ->
                        IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED

                    IntegrityTrustRpcAttestationRootVerdict.UNTRUSTED ->
                        IntegrityTrustAttestationRootVerdict.UNTRUSTED

                    IntegrityTrustRpcAttestationRootVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationRootVerdict.UNEVALUATED
                },
                revocationVerdict = when (it.revocationVerdict) {
                    IntegrityTrustRpcAttestationRevocationVerdict.CLEAN ->
                        IntegrityTrustAttestationRevocationVerdict.CLEAN

                    IntegrityTrustRpcAttestationRevocationVerdict.REVOKED ->
                        IntegrityTrustAttestationRevocationVerdict.REVOKED

                    IntegrityTrustRpcAttestationRevocationVerdict.UNCHECKED ->
                        IntegrityTrustAttestationRevocationVerdict.UNCHECKED

                    IntegrityTrustRpcAttestationRevocationVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationRevocationVerdict.UNEVALUATED
                },
                appBindingVerdict = when (it.appBindingVerdict) {
                    IntegrityTrustRpcAttestationAppBindingVerdict.MATCHED ->
                        IntegrityTrustAttestationAppBindingVerdict.MATCHED

                    IntegrityTrustRpcAttestationAppBindingVerdict.MISMATCHED ->
                        IntegrityTrustAttestationAppBindingVerdict.MISMATCHED

                    IntegrityTrustRpcAttestationAppBindingVerdict.UNEVALUATED ->
                        IntegrityTrustAttestationAppBindingVerdict.UNEVALUATED
                },
                detail = it.detail
            )
        }
    }
}
