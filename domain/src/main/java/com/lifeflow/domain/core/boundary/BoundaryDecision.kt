package com.lifeflow.domain.core.boundary

data class BoundaryDecision(
    val classification: BoundaryClassification,
    val entitlementState: EntitlementState,
    val outcome: BoundaryAccessOutcome,
    val uiPolicy: BoundaryUiPolicy,
    val auditRecord: BoundaryAuditRecord?
) {
    val isAllowed: Boolean
        get() = outcome == BoundaryAccessOutcome.ALLOW

    val isDenied: Boolean
        get() = outcome == BoundaryAccessOutcome.DENY

    val isLocked: Boolean
        get() = outcome == BoundaryAccessOutcome.LOCK
}
