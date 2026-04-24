package com.lifeflow.domain.core

data class TierTruthSnapshot(
    val effectiveTier: TierState,
    val provenance: TierTruthProvenance,
    val isGraceAccess: Boolean = false,
    val isRevoked: Boolean = false,
    val isLocked: Boolean = false,
    val auditTag: String? = null
) {
    init {
        require(!(isRevoked && isGraceAccess)) {
            "TierTruthSnapshot cannot be both revoked and grace-enabled."
        }
    }

    val isActive: Boolean
        get() = !isRevoked && !isLocked

    companion object {
        fun localSeed(
            tier: TierState,
            auditTag: String? = null
        ): TierTruthSnapshot = TierTruthSnapshot(
            effectiveTier = tier,
            provenance = TierTruthProvenance.LOCAL_SEED,
            auditTag = auditTag
        )

        fun localPersisted(
            tier: TierState,
            auditTag: String? = null
        ): TierTruthSnapshot = TierTruthSnapshot(
            effectiveTier = tier,
            provenance = TierTruthProvenance.LOCAL_DATASTORE,
            auditTag = auditTag
        )

        fun graceCore(
            auditTag: String? = null
        ): TierTruthSnapshot = TierTruthSnapshot(
            effectiveTier = TierState.CORE,
            provenance = TierTruthProvenance.SERVER_GRACE,
            isGraceAccess = true,
            auditTag = auditTag
        )

        fun revoked(
            tier: TierState = TierState.FREE,
            auditTag: String? = null
        ): TierTruthSnapshot = TierTruthSnapshot(
            effectiveTier = tier,
            provenance = TierTruthProvenance.SERVER_REVOKED,
            isRevoked = true,
            auditTag = auditTag
        )

        fun locked(
            tier: TierState,
            auditTag: String? = null
        ): TierTruthSnapshot = TierTruthSnapshot(
            effectiveTier = tier,
            provenance = TierTruthProvenance.LOCAL_LOCKED,
            isLocked = true,
            auditTag = auditTag
        )
    }
}
