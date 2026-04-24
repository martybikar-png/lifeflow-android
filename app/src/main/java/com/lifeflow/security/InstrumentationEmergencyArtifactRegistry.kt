package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyActivationArtifact
import com.lifeflow.domain.security.EmergencyArtifactConsumptionStatus
import com.lifeflow.domain.security.EmergencyArtifactRegistryPort
import java.util.concurrent.ConcurrentHashMap

/**
 * Instrumentation-only emergency activation artifact registry.
 *
 * Production rule:
 * - never used as a local fallback
 * - production uses the external authority boundary and fails closed when unavailable
 *
 * Test rule:
 * - used only when EmergencyAuthorityBoundaryBootstrap starts with isInstrumentation = true
 */
object InstrumentationEmergencyArtifactRegistry : EmergencyArtifactRegistryPort {

    private enum class StoredStatus {
        ISSUED,
        CONSUMED,
        EXPIRED_UNUSED
    }

    private data class StoredArtifact(
        val artifact: EmergencyActivationArtifact,
        val status: StoredStatus,
        val statusReason: String? = null,
        val statusAtEpochMs: Long? = null
    )

    private val artifacts = ConcurrentHashMap<String, StoredArtifact>()

    @Synchronized
    override fun registerIssuedArtifact(artifact: EmergencyActivationArtifact) {
        artifacts[artifact.artifactId] = StoredArtifact(
            artifact = artifact,
            status = StoredStatus.ISSUED
        )
    }

    @Synchronized
    override fun consumeIssuedArtifact(
        artifactId: String,
        consumedAtEpochMs: Long
    ): EmergencyArtifactConsumptionStatus {
        val stored = artifacts[artifactId] ?: return EmergencyArtifactConsumptionStatus.MISSING

        return when (stored.status) {
            StoredStatus.CONSUMED ->
                EmergencyArtifactConsumptionStatus.ALREADY_CONSUMED

            StoredStatus.EXPIRED_UNUSED ->
                EmergencyArtifactConsumptionStatus.EXPIRED_UNUSED

            StoredStatus.ISSUED -> {
                if (consumedAtEpochMs > stored.artifact.expiresAtEpochMs) {
                    artifacts[artifactId] = stored.copy(
                        status = StoredStatus.EXPIRED_UNUSED,
                        statusReason = "expired-before-consumption",
                        statusAtEpochMs = consumedAtEpochMs
                    )
                    EmergencyArtifactConsumptionStatus.EXPIRED_UNUSED
                } else {
                    artifacts[artifactId] = stored.copy(
                        status = StoredStatus.CONSUMED,
                        statusReason = "consumed",
                        statusAtEpochMs = consumedAtEpochMs
                    )
                    EmergencyArtifactConsumptionStatus.CONSUMED
                }
            }
        }
    }

    @Synchronized
    override fun markArtifactExpiredUnused(
        artifactId: String,
        reason: String,
        expiredAtEpochMs: Long
    ) {
        val stored = artifacts[artifactId] ?: return

        if (stored.status == StoredStatus.CONSUMED) {
            return
        }

        artifacts[artifactId] = stored.copy(
            status = StoredStatus.EXPIRED_UNUSED,
            statusReason = reason,
            statusAtEpochMs = expiredAtEpochMs
        )
    }

    fun clear() {
        artifacts.clear()
    }
}
