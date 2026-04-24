package com.lifeflow.security.audit

import com.lifeflow.security.TrustState
import java.time.Instant

internal enum class SecurityIncidentResponseMode {
    NORMAL,
    OBSERVE,
    HEIGHTENED_GUARD,
    RECOVERY_REQUIRED
}

internal data class SecurityIncidentResponseSnapshot(
    val generatedAt: Instant,
    val responseMode: SecurityIncidentResponseMode,
    val incidentLevel: SecurityIncidentLevel,
    val currentTrustState: TrustState,
    val recommendedTrustState: TrustState?,
    val blockProtectedRuntime: Boolean,
    val blockSensitiveOperations: Boolean,
    val requireRecovery: Boolean,
    val notifyMonitoring: Boolean,
    val responseCodes: Set<SecurityIncidentResponseCode>
)

internal object SecurityIncidentResponseBridge {

    fun snapshot(
        incident: SecurityIncidentSignalSnapshot,
        currentTrustState: TrustState,
        generatedAt: Instant = Instant.now()
    ): SecurityIncidentResponseSnapshot {
        val responseCodes = linkedSetOf<SecurityIncidentResponseCode>()

        return when {
            incident.activeCompromiseSignal || currentTrustState == TrustState.COMPROMISED -> {
                if (incident.activeCompromiseSignal) {
                    responseCodes += SecurityIncidentResponseCode.ACTIVE_COMPROMISE_SIGNAL
                }
                if (currentTrustState == TrustState.COMPROMISED) {
                    responseCodes += SecurityIncidentResponseCode.TRUST_ALREADY_COMPROMISED
                }
                responseCodes += SecurityIncidentResponseCode.FORCE_COMPROMISED_LOCKDOWN
                responseCodes += SecurityIncidentResponseCode.RECOVERY_REQUIRED

                SecurityIncidentResponseSnapshot(
                    generatedAt = generatedAt,
                    responseMode = SecurityIncidentResponseMode.RECOVERY_REQUIRED,
                    incidentLevel = incident.incidentLevel,
                    currentTrustState = currentTrustState,
                    recommendedTrustState = TrustState.COMPROMISED,
                    blockProtectedRuntime = true,
                    blockSensitiveOperations = true,
                    requireRecovery = true,
                    notifyMonitoring = true,
                    responseCodes = responseCodes
                )
            }

            incident.incidentLevel == SecurityIncidentLevel.ELEVATED -> {
                if (incident.repeatedAuthFailureSignal) {
                    responseCodes += SecurityIncidentResponseCode.AUTH_FAILURE_BURST
                }
                if (incident.repeatedPolicyViolationSignal) {
                    responseCodes += SecurityIncidentResponseCode.POLICY_VIOLATION_BURST
                }
                if (currentTrustState == TrustState.VERIFIED) {
                    responseCodes += SecurityIncidentResponseCode.RECOMMEND_TRUST_DEGRADE
                }
                responseCodes += SecurityIncidentResponseCode.HEIGHTENED_GUARD

                SecurityIncidentResponseSnapshot(
                    generatedAt = generatedAt,
                    responseMode = SecurityIncidentResponseMode.HEIGHTENED_GUARD,
                    incidentLevel = incident.incidentLevel,
                    currentTrustState = currentTrustState,
                    recommendedTrustState = when (currentTrustState) {
                        TrustState.VERIFIED -> TrustState.DEGRADED
                        TrustState.DEGRADED,
                        TrustState.EMERGENCY_LIMITED,
                        TrustState.COMPROMISED -> null
                    },
                    blockProtectedRuntime = false,
                    blockSensitiveOperations = true,
                    requireRecovery = false,
                    notifyMonitoring = true,
                    responseCodes = responseCodes
                )
            }

            incident.incidentLevel == SecurityIncidentLevel.OBSERVE -> {
                responseCodes += SecurityIncidentResponseCode.OBSERVE_ONLY

                SecurityIncidentResponseSnapshot(
                    generatedAt = generatedAt,
                    responseMode = SecurityIncidentResponseMode.OBSERVE,
                    incidentLevel = incident.incidentLevel,
                    currentTrustState = currentTrustState,
                    recommendedTrustState = null,
                    blockProtectedRuntime = false,
                    blockSensitiveOperations = false,
                    requireRecovery = false,
                    notifyMonitoring = false,
                    responseCodes = responseCodes
                )
            }

            else -> {
                SecurityIncidentResponseSnapshot(
                    generatedAt = generatedAt,
                    responseMode = SecurityIncidentResponseMode.NORMAL,
                    incidentLevel = incident.incidentLevel,
                    currentTrustState = currentTrustState,
                    recommendedTrustState = null,
                    blockProtectedRuntime = false,
                    blockSensitiveOperations = false,
                    requireRecovery = false,
                    notifyMonitoring = false,
                    responseCodes = emptySet()
                )
            }
        }
    }
}
