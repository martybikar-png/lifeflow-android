package com.lifeflow.domain.core.boundary

import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.TierTruthProvenance
import com.lifeflow.domain.core.TierTruthSnapshot

fun TierTruthSnapshot.toEntitlementState(): EntitlementState {
    val mappedTier = when (effectiveTier) {
        TierState.FREE -> EntitlementTier.FREE
        TierState.CORE -> EntitlementTier.CORE
    }

    return when {
        isRevoked -> EntitlementState.revoked(
            tier = mappedTier,
            provenance = provenance,
            auditTag = auditTag
        )

        isLocked -> EntitlementState.locked(
            tier = mappedTier,
            provenance = provenance,
            auditTag = auditTag
        )

        isGraceAccess && effectiveTier == TierState.CORE -> EntitlementState.coreGrace(
            provenance = provenance,
            auditTag = auditTag
        )

        effectiveTier == TierState.FREE -> EntitlementState.freeActive(
            provenance = provenance,
            auditTag = auditTag
        )

        effectiveTier == TierState.CORE -> EntitlementState.coreActive(
            provenance = provenance,
            auditTag = auditTag
        )

        else -> EntitlementState.unavailable(
            tier = mappedTier,
            provenance = provenance,
            auditTag = auditTag
        )
    }
}

fun TierState.toEntitlementState(
    provenance: TierTruthProvenance = TierTruthProvenance.LOCAL_DATASTORE,
    auditTag: String? = null
): EntitlementState {
    return TierTruthSnapshot(
        effectiveTier = this,
        provenance = provenance,
        auditTag = auditTag
    ).toEntitlementState()
}
