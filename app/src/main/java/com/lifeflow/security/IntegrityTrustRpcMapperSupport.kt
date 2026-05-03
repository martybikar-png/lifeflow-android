package com.lifeflow.security

internal fun mapIntegrityTrustAttestationEvidenceToRpc(
    evidence: IntegrityTrustAttestationEvidence
): IntegrityTrustRpcAttestationEvidence {
    return IntegrityTrustRpcAttestationEvidence(
        status = when (evidence.status) {
            IntegrityTrustAttestationStatus.CAPTURED ->
                IntegrityTrustRpcAttestationStatus.CAPTURED

            IntegrityTrustAttestationStatus.UNAVAILABLE ->
                IntegrityTrustRpcAttestationStatus.UNAVAILABLE
        },
        keyAlias = evidence.keyAlias,
        chainEntryCount = evidence.chainEntryCount,
        challengeBase64 = evidence.challengeBase64,
        certificateChainDerBase64 = evidence.certificateChainDerBase64,
        challengeSha256 = evidence.challengeSha256,
        leafCertificateSha256 = evidence.leafCertificateSha256,
        strongBoxRequested = evidence.strongBoxRequested,
        failureKind = evidence.failureKind?.let { failureKind ->
            when (failureKind) {
                IntegrityTrustAttestationFailureKind.UNAVAILABLE ->
                    IntegrityTrustRpcAttestationFailureKind.UNAVAILABLE

                IntegrityTrustAttestationFailureKind.HARD_FAILURE ->
                    IntegrityTrustRpcAttestationFailureKind.HARD_FAILURE
            }
        },
        failureReason = evidence.failureReason
    )
}

internal fun mapSecurityIntegrityTrustVerdictFromRpc(
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

internal fun mapIntegrityTrustVerdictSourceFromRpc(
    source: IntegrityTrustRpcVerdictSource
): IntegrityTrustVerdictSource {
    return when (source) {
        IntegrityTrustRpcVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER ->
            IntegrityTrustVerdictSource.PLAY_INTEGRITY_STANDARD_SERVER
    }
}


internal fun mapIntegrityTrustAttestationVerificationFromRpc(
    verification: IntegrityTrustRpcAttestationVerification
): IntegrityTrustAttestationVerification {
    return IntegrityTrustAttestationVerification(
        chainVerdict = when (verification.chainVerdict) {
            IntegrityTrustRpcAttestationChainVerdict.VERIFIED ->
                IntegrityTrustAttestationChainVerdict.VERIFIED

            IntegrityTrustRpcAttestationChainVerdict.FAILED ->
                IntegrityTrustAttestationChainVerdict.FAILED

            IntegrityTrustRpcAttestationChainVerdict.UNEVALUATED ->
                IntegrityTrustAttestationChainVerdict.UNEVALUATED
        },
        challengeVerdict = when (verification.challengeVerdict) {
            IntegrityTrustRpcAttestationChallengeVerdict.MATCHED ->
                IntegrityTrustAttestationChallengeVerdict.MATCHED

            IntegrityTrustRpcAttestationChallengeVerdict.MISMATCHED ->
                IntegrityTrustAttestationChallengeVerdict.MISMATCHED

            IntegrityTrustRpcAttestationChallengeVerdict.UNEVALUATED ->
                IntegrityTrustAttestationChallengeVerdict.UNEVALUATED
        },
        rootVerdict = when (verification.rootVerdict) {
            IntegrityTrustRpcAttestationRootVerdict.GOOGLE_TRUSTED ->
                IntegrityTrustAttestationRootVerdict.GOOGLE_TRUSTED

            IntegrityTrustRpcAttestationRootVerdict.UNTRUSTED ->
                IntegrityTrustAttestationRootVerdict.UNTRUSTED

            IntegrityTrustRpcAttestationRootVerdict.UNEVALUATED ->
                IntegrityTrustAttestationRootVerdict.UNEVALUATED
        },
        revocationVerdict = when (verification.revocationVerdict) {
            IntegrityTrustRpcAttestationRevocationVerdict.CLEAN ->
                IntegrityTrustAttestationRevocationVerdict.CLEAN

            IntegrityTrustRpcAttestationRevocationVerdict.REVOKED ->
                IntegrityTrustAttestationRevocationVerdict.REVOKED

            IntegrityTrustRpcAttestationRevocationVerdict.UNCHECKED ->
                IntegrityTrustAttestationRevocationVerdict.UNCHECKED

            IntegrityTrustRpcAttestationRevocationVerdict.UNEVALUATED ->
                IntegrityTrustAttestationRevocationVerdict.UNEVALUATED
        },
        appBindingVerdict = when (verification.appBindingVerdict) {
            IntegrityTrustRpcAttestationAppBindingVerdict.MATCHED ->
                IntegrityTrustAttestationAppBindingVerdict.MATCHED

            IntegrityTrustRpcAttestationAppBindingVerdict.MISMATCHED ->
                IntegrityTrustAttestationAppBindingVerdict.MISMATCHED

            IntegrityTrustRpcAttestationAppBindingVerdict.UNEVALUATED ->
                IntegrityTrustAttestationAppBindingVerdict.UNEVALUATED
        },
        detail = verification.detail
    )
}

internal fun mapIntegrityTrustDecisionFromRpc(
    decision: IntegrityTrustRpcDecision
): IntegrityTrustDecision {
    return when (decision) {
        IntegrityTrustRpcDecision.ALLOW ->
            IntegrityTrustDecision.ALLOW

        IntegrityTrustRpcDecision.STEP_UP ->
            IntegrityTrustDecision.STEP_UP

        IntegrityTrustRpcDecision.DEGRADED ->
            IntegrityTrustDecision.DEGRADED

        IntegrityTrustRpcDecision.DENY ->
            IntegrityTrustDecision.DENY

        IntegrityTrustRpcDecision.LOCK ->
            IntegrityTrustDecision.LOCK
    }
}
