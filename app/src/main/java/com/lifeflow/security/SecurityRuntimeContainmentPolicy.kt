package com.lifeflow.security

import com.lifeflow.security.audit.SecurityIncidentLevel
import com.lifeflow.security.audit.SecurityIncidentResponseMode
import com.lifeflow.security.audit.SecurityIncidentResponseSnapshot
import java.time.Instant

internal enum class SecurityRuntimeContainmentLevel {
    NORMAL,
    GUARDED,
    RESTRICTED,
    RECOVERY_ONLY
}

internal data class SecurityRuntimeCapabilityEnvelope(
    val allowProtectedRuntime: Boolean,
    val allowSensitiveOperations: Boolean,
    val allowExternalAuthority: Boolean,
    val allowSecretUnlock: Boolean,
    val allowStateMutation: Boolean,
    val allowRecoveryFlow: Boolean
)

internal data class SecurityRuntimeContainmentSnapshot(
    val generatedAt: Instant,
    val containmentLevel: SecurityRuntimeContainmentLevel,
    val responseMode: SecurityIncidentResponseMode,
    val incidentLevel: SecurityIncidentLevel,
    val currentTrustState: TrustState,
    val effectiveTrustState: TrustState,
    val capabilityEnvelope: SecurityRuntimeCapabilityEnvelope,
    val requireRecovery: Boolean,
    val notifyMonitoring: Boolean,
    val containmentCodes: Set<String>
)

internal object SecurityRuntimeContainmentPolicy {

    fun snapshot(
        incidentResponse: SecurityIncidentResponseSnapshot,
        generatedAt: Instant = incidentResponse.generatedAt
    ): SecurityRuntimeContainmentSnapshot {
        val containmentCodes = linkedSetOf<String>()
        containmentCodes += incidentResponse.responseCodes

        val effectiveTrustState = incidentResponse.recommendedTrustState
            ?: incidentResponse.currentTrustState

        return when {
            incidentResponse.requireRecovery || incidentResponse.blockProtectedRuntime -> {
                containmentCodes += "CONTAINMENT_RECOVERY_ONLY"

                SecurityRuntimeContainmentSnapshot(
                    generatedAt = generatedAt,
                    containmentLevel = SecurityRuntimeContainmentLevel.RECOVERY_ONLY,
                    responseMode = incidentResponse.responseMode,
                    incidentLevel = incidentResponse.incidentLevel,
                    currentTrustState = incidentResponse.currentTrustState,
                    effectiveTrustState = effectiveTrustState,
                    capabilityEnvelope = recoveryOnlyEnvelope(),
                    requireRecovery = true,
                    notifyMonitoring = incidentResponse.notifyMonitoring,
                    containmentCodes = containmentCodes
                )
            }

            incidentResponse.blockSensitiveOperations -> {
                containmentCodes += "CONTAINMENT_RESTRICTED"

                SecurityRuntimeContainmentSnapshot(
                    generatedAt = generatedAt,
                    containmentLevel = SecurityRuntimeContainmentLevel.RESTRICTED,
                    responseMode = incidentResponse.responseMode,
                    incidentLevel = incidentResponse.incidentLevel,
                    currentTrustState = incidentResponse.currentTrustState,
                    effectiveTrustState = effectiveTrustState,
                    capabilityEnvelope = restrictedEnvelope(),
                    requireRecovery = incidentResponse.requireRecovery,
                    notifyMonitoring = incidentResponse.notifyMonitoring,
                    containmentCodes = containmentCodes
                )
            }

            incidentResponse.responseMode == SecurityIncidentResponseMode.OBSERVE -> {
                containmentCodes += "CONTAINMENT_GUARDED"

                SecurityRuntimeContainmentSnapshot(
                    generatedAt = generatedAt,
                    containmentLevel = SecurityRuntimeContainmentLevel.GUARDED,
                    responseMode = incidentResponse.responseMode,
                    incidentLevel = incidentResponse.incidentLevel,
                    currentTrustState = incidentResponse.currentTrustState,
                    effectiveTrustState = effectiveTrustState,
                    capabilityEnvelope = guardedEnvelope(),
                    requireRecovery = incidentResponse.requireRecovery,
                    notifyMonitoring = incidentResponse.notifyMonitoring,
                    containmentCodes = containmentCodes
                )
            }

            else -> {
                containmentCodes += "CONTAINMENT_NORMAL"

                SecurityRuntimeContainmentSnapshot(
                    generatedAt = generatedAt,
                    containmentLevel = SecurityRuntimeContainmentLevel.NORMAL,
                    responseMode = incidentResponse.responseMode,
                    incidentLevel = incidentResponse.incidentLevel,
                    currentTrustState = incidentResponse.currentTrustState,
                    effectiveTrustState = effectiveTrustState,
                    capabilityEnvelope = normalEnvelope(),
                    requireRecovery = incidentResponse.requireRecovery,
                    notifyMonitoring = incidentResponse.notifyMonitoring,
                    containmentCodes = containmentCodes
                )
            }
        }
    }

    private fun normalEnvelope(): SecurityRuntimeCapabilityEnvelope =
        SecurityRuntimeCapabilityEnvelope(
            allowProtectedRuntime = true,
            allowSensitiveOperations = true,
            allowExternalAuthority = true,
            allowSecretUnlock = true,
            allowStateMutation = true,
            allowRecoveryFlow = true
        )

    private fun guardedEnvelope(): SecurityRuntimeCapabilityEnvelope =
        SecurityRuntimeCapabilityEnvelope(
            allowProtectedRuntime = true,
            allowSensitiveOperations = true,
            allowExternalAuthority = true,
            allowSecretUnlock = true,
            allowStateMutation = true,
            allowRecoveryFlow = true
        )

    private fun restrictedEnvelope(): SecurityRuntimeCapabilityEnvelope =
        SecurityRuntimeCapabilityEnvelope(
            allowProtectedRuntime = true,
            allowSensitiveOperations = false,
            allowExternalAuthority = false,
            allowSecretUnlock = false,
            allowStateMutation = true,
            allowRecoveryFlow = true
        )

    private fun recoveryOnlyEnvelope(): SecurityRuntimeCapabilityEnvelope =
        SecurityRuntimeCapabilityEnvelope(
            allowProtectedRuntime = false,
            allowSensitiveOperations = false,
            allowExternalAuthority = false,
            allowSecretUnlock = false,
            allowStateMutation = false,
            allowRecoveryFlow = true
        )
}
