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
            "decisionPath" to classifyDecisionPath(response.reason)
        )

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

            reason.contains("SERVER_VERDICT_POLICY_VIOLATION") ->
                "SERVER_VERDICT_POLICY_VIOLATION"

            reason.contains("claimsPolicy=COMPROMISED") ->
                "CLAIMS_POLICY_COMPROMISED"

            reason.contains("claimsPolicy=DEGRADED") ->
                "CLAIMS_POLICY_DEGRADED"

            reason.contains("PLAY_INTEGRITY_REQUEST_FAILED") ->
                "PLAY_INTEGRITY_REQUEST_FAILED"

            reason.contains("PLAY_INTEGRITY_NOT_CONFIGURED") ->
                "PLAY_INTEGRITY_NOT_CONFIGURED"

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
