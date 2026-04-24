package com.lifeflow.security.audit

import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityIntegrityTrustVerdict

/**
 * Builds audit-ready explanations for integrity verdict decisions.
 *
 * Purpose:
 * - keep audit enrichment out of SecurityRuleEngine
 * - provide short stable messages plus structured metadata
 * - avoid leaking full hashes while keeping events explainable
 */
internal object SecurityIntegrityAuditExplainability {

    internal data class Snapshot(
        val eventType: SecurityAuditLog.EventType,
        val message: String,
        val metadata: Map<String, String>
    )

    fun snapshot(
        response: IntegrityTrustVerdictResponse
    ): Snapshot {
        val eventType = when (response.verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED ->
                SecurityAuditLog.EventType.TRUST_VERIFIED

            SecurityIntegrityTrustVerdict.DEGRADED ->
                SecurityAuditLog.EventType.TRUST_DEGRADED

            SecurityIntegrityTrustVerdict.COMPROMISED ->
                SecurityAuditLog.EventType.TRUST_COMPROMISED
        }

        val message = when (response.verdict) {
            SecurityIntegrityTrustVerdict.VERIFIED ->
                "Integrity trust accepted after server/client evaluation."

            SecurityIntegrityTrustVerdict.DEGRADED ->
                "Integrity trust downgraded after server/client evaluation."

            SecurityIntegrityTrustVerdict.COMPROMISED ->
                "Integrity trust failed closed after server/client evaluation."
        }

        val metadata = linkedMapOf(
            "verdict" to response.verdict.name,
            "verdictSource" to response.verdictSource.name,
            "decision" to response.decision.name,
            "decisionPath" to classifyDecisionPath(response.reason)
        )

        response.decisionReasonCode?.let {
            metadata["decisionReasonCode"] = it
        }
        response.requestBindingVerified?.let {
            metadata["requestBindingVerified"] = it.toString()
        }
        response.policyVersion?.let {
            metadata["policyVersion"] = it
        }
        response.serverTimestampEpochMs?.let {
            metadata["serverTimestampEpochMs"] = it.toString()
        }
        response.requestHashEcho?.let {
            metadata["requestHashEchoShort"] = abbreviateHash(it)
        }
        response.claims.appRecognitionVerdict?.let {
            metadata["appRecognitionVerdict"] = it.name
        }
        if (response.claims.deviceRecognitionVerdicts.isNotEmpty()) {
            metadata["deviceRecognitionVerdicts"] =
                response.claims.deviceRecognitionVerdicts
                    .map { it.name }
                    .sorted()
                    .joinToString(",")
        }
        response.claims.appLicensingVerdict?.let {
            metadata["appLicensingVerdict"] = it.name
        }
        response.claims.playProtectVerdict?.let {
            metadata["playProtectVerdict"] = it.name
        }
        response.attestationVerification?.let { verification ->
            metadata["attestationChainVerdict"] = verification.chainVerdict.name
            metadata["attestationChallengeVerdict"] = verification.challengeVerdict.name
            metadata["attestationRootVerdict"] = verification.rootVerdict.name
            metadata["attestationRevocationVerdict"] = verification.revocationVerdict.name
            metadata["attestationAppBindingVerdict"] = verification.appBindingVerdict.name
            verification.detail?.let {
                metadata["attestationDetail"] = it.take(220)
            }
        }

        metadata["reasonSummary"] = response.reason.take(220)

        return Snapshot(
            eventType = eventType,
            message = message,
            metadata = metadata
        )
    }

    private fun classifyDecisionPath(
        reason: String
    ): String {
        return when {
            reason.contains("SERVER_VERDICT_METADATA_INVALID") ->
                "SERVER_METADATA_INVALID"

            reason.contains("SERVER_VERDICT_ATTESTATION_INVALID") ->
                "SERVER_ATTESTATION_INVALID"

            reason.contains("SERVER_VERDICT_POLICY_VIOLATION") ->
                "SERVER_VERDICT_POLICY_VIOLATION"

            reason.contains("PLAY_INTEGRITY_TRANSIENT_FAILURE") ->
                "PLAY_INTEGRITY_TRANSIENT_FAILURE"

            reason.contains("PLAY_INTEGRITY_UNAVAILABLE") ->
                "PLAY_INTEGRITY_UNAVAILABLE"

            reason.contains("PLAY_INTEGRITY_PROVIDER_INVALID") ->
                "PLAY_INTEGRITY_PROVIDER_INVALID"

            reason.contains("PLAY_INTEGRITY_HARD_FAILURE") ->
                "PLAY_INTEGRITY_HARD_FAILURE"

            reason.contains("PLAY_INTEGRITY_REQUEST_FAILED") ->
                "PLAY_INTEGRITY_REQUEST_FAILED"

            reason.contains("PLAY_INTEGRITY_NOT_CONFIGURED") ->
                "PLAY_INTEGRITY_NOT_CONFIGURED"

            reason.contains("ATTESTATION_KIND=HARD_FAILURE") ->
                "CLIENT_ATTESTATION_HARD_FAILURE"

            reason.contains("ATTESTATION_KIND=UNAVAILABLE") ->
                "CLIENT_ATTESTATION_UNAVAILABLE"

            reason.contains("ATTESTATION=HARD_FAILURE:") ->
                "CLIENT_ATTESTATION_HARD_FAILURE"

            reason.contains("ATTESTATION=UNAVAILABLE:") ->
                "CLIENT_ATTESTATION_UNAVAILABLE"

            reason.contains("requestBindingVerified=false") ->
                "SERVER_REQUEST_BINDING_NOT_VERIFIED"

            reason.contains("claimsPolicy=COMPROMISED") ->
                "CLAIMS_POLICY_COMPROMISED"

            reason.contains("claimsPolicy=DEGRADED") ->
                "CLAIMS_POLICY_DEGRADED"

            reason.contains("| decision=LOCK") ->
                "SERVER_DECISION_LOCK"

            reason.contains("| decision=DENY") ->
                "SERVER_DECISION_DENY"

            reason.contains("| decision=DEGRADED") ->
                "SERVER_DECISION_DEGRADED"

            reason.contains("| decision=STEP_UP") ->
                "SERVER_DECISION_STEP_UP"

            reason.contains("| decision=ALLOW") ->
                "SERVER_DECISION_ALLOW"

            else ->
                "SERVER_VERDICT_ACCEPTED"
        }
    }

    private fun abbreviateHash(
        value: String
    ): String {
        if (value.length <= 16) return value
        return "${value.take(8)}...${value.takeLast(8)}"
    }
}
