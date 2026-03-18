package com.lifeflow.domain.core

/**
 * TierManager — manages current tier state.
 *
 * V1: local-only determination.
 * Future: server-side entitlement validation.
 *
 * Default: CORE for development.
 * Production: will read from local entitlement store.
 */
class TierManager(
    private val tierProvider: () -> TierState = { TierState.CORE }
) {
    fun currentTier(): TierState = tierProvider()

    fun isFree(): Boolean = currentTier() == TierState.FREE
    fun isCore(): Boolean = currentTier() == TierState.CORE

    /**
     * Gate check — returns null if tier allows the operation,
     * returns descriptive message if blocked.
     */
    fun gateOrNull(requiredTier: TierState): String? {
        val current = currentTier()
        if (current == requiredTier || current == TierState.CORE) return null
        return when (requiredTier) {
            TierState.CORE ->
                "This feature requires Core subscription. " +
                "Upgrade to unlock Digital Twin, biometric vault and full module access."
            TierState.FREE -> null
        }
    }
}
