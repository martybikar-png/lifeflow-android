package com.lifeflow.domain.core.boundary

enum class BoundaryUiBehavior {
    VISIBLE_ENABLED,
    VISIBLE_DENIED,
    VISIBLE_LOCKED
}

data class BoundaryUiPolicy(
    val behavior: BoundaryUiBehavior,
    val showUpgradePrompt: Boolean,
    val showLockedState: Boolean,
    val allowTapAudit: Boolean,
    val userMessageCode: String
) {
    companion object {
        val Allowed = BoundaryUiPolicy(
            behavior = BoundaryUiBehavior.VISIBLE_ENABLED,
            showUpgradePrompt = false,
            showLockedState = false,
            allowTapAudit = false,
            userMessageCode = "allowed"
        )

        fun denied(reasonCode: String): BoundaryUiPolicy = BoundaryUiPolicy(
            behavior = BoundaryUiBehavior.VISIBLE_DENIED,
            showUpgradePrompt = true,
            showLockedState = false,
            allowTapAudit = true,
            userMessageCode = reasonCode
        )

        fun locked(reasonCode: String): BoundaryUiPolicy = BoundaryUiPolicy(
            behavior = BoundaryUiBehavior.VISIBLE_LOCKED,
            showUpgradePrompt = true,
            showLockedState = true,
            allowTapAudit = true,
            userMessageCode = reasonCode
        )
    }
}

fun BoundaryClassification.resolveUiPolicy(
    entitlementState: EntitlementState
): BoundaryUiPolicy {
    if (entitlementState.satisfies(requirement)) {
        return BoundaryUiPolicy.Allowed
    }

    return when (requirement.failureMode) {
        RequirementFailureMode.DENY -> BoundaryUiPolicy.denied(requirement.reasonCode)
        RequirementFailureMode.LOCK -> BoundaryUiPolicy.locked(requirement.reasonCode)
    }
}
