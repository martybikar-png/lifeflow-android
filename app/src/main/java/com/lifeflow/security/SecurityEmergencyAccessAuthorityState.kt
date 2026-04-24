package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessWindow
import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyApprovalSession
import com.lifeflow.domain.security.EmergencyArtifactRegistryPort
import com.lifeflow.domain.security.EmergencyAuditSinkPort
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicReference

internal const val SecurityEmergencyAccessMaxWindowMs: Long = 60 * 60_000L

internal class SecurityEmergencyAccessAuthorityState {

    val activeApprovalSession = AtomicReference<EmergencyApprovalSession?>(null)
    val issuedActivationArtifact = AtomicReference<EmergencyActivationArtifact?>(null)
    val activeWindow = AtomicReference<EmergencyAccessWindow?>(null)

    private val emergencyAuditSinkRef = AtomicReference<EmergencyAuditSinkPort?>(null)
    private val emergencyArtifactRegistryRef = AtomicReference<EmergencyArtifactRegistryPort?>(null)

    fun initialize(
        emergencyAuditSink: EmergencyAuditSinkPort,
        emergencyArtifactRegistry: EmergencyArtifactRegistryPort
    ) {
        emergencyAuditSinkRef.set(emergencyAuditSink)
        emergencyArtifactRegistryRef.set(emergencyArtifactRegistry)
    }

    fun auditSink(): EmergencyAuditSinkPort {
        return emergencyAuditSinkRef.get()
            ?: error("SecurityEmergencyAccessAuthority is not initialized with EmergencyAuditSinkPort.")
    }

    fun artifactRegistry(): EmergencyArtifactRegistryPort {
        return emergencyArtifactRegistryRef.get()
            ?: error("SecurityEmergencyAccessAuthority is not initialized with EmergencyArtifactRegistryPort.")
    }
}

internal fun sha256Hex(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))

    return buildString(digest.size * 2) {
        digest.forEach { byte ->
            append("%02x".format(byte))
        }
    }
}