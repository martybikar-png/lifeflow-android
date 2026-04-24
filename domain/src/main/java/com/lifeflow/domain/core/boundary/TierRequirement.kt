package com.lifeflow.domain.core.boundary

enum class RequirementFailureMode {
    DENY,
    LOCK
}

data class TierRequirement(
    val minimumTier: EntitlementTier,
    val failureMode: RequirementFailureMode,
    val reasonCode: String
) {
    companion object {
        fun free(
            failureMode: RequirementFailureMode = RequirementFailureMode.DENY,
            reasonCode: String = "free_allowed"
        ): TierRequirement = TierRequirement(
            minimumTier = EntitlementTier.FREE,
            failureMode = failureMode,
            reasonCode = reasonCode
        )

        fun core(
            failureMode: RequirementFailureMode = RequirementFailureMode.LOCK,
            reasonCode: String = "core_required"
        ): TierRequirement = TierRequirement(
            minimumTier = EntitlementTier.CORE,
            failureMode = failureMode,
            reasonCode = reasonCode
        )
    }
}
