package com.lifeflow.domain.core

/**
 * TierManager — manages current effective tier truth.
 *
 * Current source of truth:
 * - persistent local tier source
 *
 * Runtime rule:
 * - no hidden CORE fallback
 * - if no snapshot exists yet, fail closed to FREE until bootstrap seeds truth
 *
 * Future:
 * - server-backed entitlement validation
 * - revoke / grace / sync semantics
 */
class TierManager(
    private val tierTruthSource: TierTruthSource
) {
    fun currentSnapshotOrNull(): TierTruthSnapshot? =
        tierTruthSource.currentSnapshotOrNull()

    fun currentSnapshot(): TierTruthSnapshot =
        currentSnapshotOrNull() ?: TierTruthSnapshot(
            effectiveTier = TierState.FREE,
            provenance = TierTruthProvenance.FALLBACK_DEFAULT,
            auditTag = "tier_missing_fail_closed"
        )

    fun currentTier(): TierState = currentSnapshot().effectiveTier

    fun isFree(): Boolean = currentTier() == TierState.FREE
    fun isCore(): Boolean = currentTier() == TierState.CORE

    fun persistTier(
        tierState: TierState,
        auditTag: String? = null
    ) {
        tierTruthSource.persistSnapshot(
            TierTruthSnapshot.localPersisted(
                tier = tierState,
                auditTag = auditTag
            )
        )
    }

    fun persistSnapshot(snapshot: TierTruthSnapshot) {
        tierTruthSource.persistSnapshot(snapshot)
    }

    fun ensurePersistedSnapshot(seedSnapshot: TierTruthSnapshot) {
        if (tierTruthSource.currentSnapshotOrNull() == null) {
            tierTruthSource.persistSnapshot(seedSnapshot)
        }
    }

    fun clearPersistedTier() {
        tierTruthSource.clear()
    }

    /**
     * Gate check — returns null if tier allows the operation,
     * returns descriptive message if blocked.
     */
    fun gateOrNull(requiredTier: TierState): String? {
        val snapshot = currentSnapshot()
        val current = snapshot.effectiveTier

        if (snapshot.isRevoked) {
            return "Your entitlement is revoked. Protected access remains unavailable."
        }

        if (snapshot.isLocked) {
            return "Your entitlement is locked. Protected access remains unavailable."
        }

        if (current == requiredTier || current == TierState.CORE) {
            return null
        }

        return when (requiredTier) {
            TierState.CORE ->
                "This feature requires Core subscription. " +
                    "Upgrade to unlock Digital Twin, biometric vault and full module access."
            TierState.FREE -> null
        }
    }
}
