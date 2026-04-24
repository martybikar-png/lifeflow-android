package com.lifeflow.boundary

import com.lifeflow.domain.core.boundary.BoundaryAuditExpectation
import com.lifeflow.domain.core.boundary.EntitlementStatus

enum class BoundaryPresentationState {
    ENABLED,
    DENIED,
    LOCKED
}

data class BoundaryPresentation(
    val boundaryKey: String,
    val title: String,
    val state: BoundaryPresentationState,
    val showUpgradePrompt: Boolean,
    val showLockedBadge: Boolean,
    val allowUserActionAudit: Boolean,
    val messageCode: String,
    val owner: String,
    val entitlementStatus: EntitlementStatus,
    val entitlementSource: BoundaryEntitlementSource,
    val isGraceAccess: Boolean,
    val auditExpectation: BoundaryAuditExpectation,
    val detailMessage: String
) {
    val isInteractive: Boolean
        get() = state == BoundaryPresentationState.ENABLED

    val isBlockedLike: Boolean
        get() = state == BoundaryPresentationState.DENIED || state == BoundaryPresentationState.LOCKED
}
