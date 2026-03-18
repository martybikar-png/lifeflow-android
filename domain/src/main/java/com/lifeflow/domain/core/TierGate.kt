package com.lifeflow.domain.core

/**
 * TierGate — tier-aware operation gating.
 *
 * Returns block message if operation is not allowed for current tier.
 * Returns null if operation is allowed.
 */
fun tierGateMessage(
    tierManager: TierManager,
    requiredTier: TierState,
    operation: String
): String? {
    val message = tierManager.gateOrNull(requiredTier) ?: return null
    return "TIER_GATE($operation): $message"
}
