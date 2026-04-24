package com.lifeflow.domain.core.boundary

import com.lifeflow.domain.core.TierTruthProvenance

enum class EntitlementTier {
    FREE,
    CORE
}

enum class EntitlementStatus {
    ACTIVE,
    LOCKED,
    UNAVAILABLE,
    REVOKED
}

data class EntitlementState(
    val tier: EntitlementTier,
    val status: EntitlementStatus,
    val provenance: TierTruthProvenance = TierTruthProvenance.LOCAL_DATASTORE,
    val isGraceAccess: Boolean = false,
    val auditTag: String? = null
) {
    val isActive: Boolean
        get() = status == EntitlementStatus.ACTIVE

    val isCore: Boolean
        get() = tier == EntitlementTier.CORE

    fun satisfies(requirement: TierRequirement): Boolean {
        return when (requirement.minimumTier) {
            EntitlementTier.FREE -> isActive
            EntitlementTier.CORE -> isActive && isCore
        }
    }

    companion object {
        fun freeActive(
            provenance: TierTruthProvenance = TierTruthProvenance.LOCAL_DATASTORE,
            auditTag: String? = null
        ): EntitlementState = EntitlementState(
            tier = EntitlementTier.FREE,
            status = EntitlementStatus.ACTIVE,
            provenance = provenance,
            auditTag = auditTag
        )

        fun coreActive(
            provenance: TierTruthProvenance = TierTruthProvenance.LOCAL_DATASTORE,
            auditTag: String? = null
        ): EntitlementState = EntitlementState(
            tier = EntitlementTier.CORE,
            status = EntitlementStatus.ACTIVE,
            provenance = provenance,
            auditTag = auditTag
        )

        fun coreGrace(
            provenance: TierTruthProvenance = TierTruthProvenance.SERVER_GRACE,
            auditTag: String? = null
        ): EntitlementState = EntitlementState(
            tier = EntitlementTier.CORE,
            status = EntitlementStatus.ACTIVE,
            provenance = provenance,
            isGraceAccess = true,
            auditTag = auditTag
        )

        fun locked(
            tier: EntitlementTier,
            provenance: TierTruthProvenance = TierTruthProvenance.LOCAL_LOCKED,
            auditTag: String? = null
        ): EntitlementState = EntitlementState(
            tier = tier,
            status = EntitlementStatus.LOCKED,
            provenance = provenance,
            auditTag = auditTag
        )

        fun revoked(
            tier: EntitlementTier,
            provenance: TierTruthProvenance = TierTruthProvenance.SERVER_REVOKED,
            auditTag: String? = null
        ): EntitlementState = EntitlementState(
            tier = tier,
            status = EntitlementStatus.REVOKED,
            provenance = provenance,
            auditTag = auditTag
        )

        fun unavailable(
            tier: EntitlementTier,
            provenance: TierTruthProvenance = TierTruthProvenance.FALLBACK_DEFAULT,
            auditTag: String? = null
        ): EntitlementState = EntitlementState(
            tier = tier,
            status = EntitlementStatus.UNAVAILABLE,
            provenance = provenance,
            auditTag = auditTag
        )
    }
}
