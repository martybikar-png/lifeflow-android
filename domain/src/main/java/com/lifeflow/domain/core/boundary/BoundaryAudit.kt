package com.lifeflow.domain.core.boundary

enum class BoundaryAuditExpectation {
    NONE,
    LOG_ON_VISIBLE_DENY,
    LOG_ON_LOCKED_PRESENTATION,
    LOG_ON_BLOCKED_ACTION,
    LOG_ON_UPGRADE_PROMPT
}

data class BoundaryAuditRecord(
    val boundaryKey: String,
    val auditName: String,
    val outcome: BoundaryAccessOutcome,
    val reasonCode: String,
    val entitlementTier: EntitlementTier,
    val entitlementStatus: EntitlementStatus,
    val expectation: BoundaryAuditExpectation
)
