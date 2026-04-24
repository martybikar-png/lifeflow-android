package com.lifeflow.domain.core.boundary

class BoundaryEvaluator(
    private val catalog: BoundaryCatalogProvider = DefaultBoundaryCatalogProvider
) {
    fun evaluate(
        boundaryKey: String,
        entitlementState: EntitlementState
    ): BoundaryDecision {
        val classification = catalog.find(boundaryKey)
            ?: error("Unknown boundary classification key: $boundaryKey")

        val outcome = if (entitlementState.satisfies(classification.requirement)) {
            BoundaryAccessOutcome.ALLOW
        } else {
            when (classification.requirement.failureMode) {
                RequirementFailureMode.DENY -> BoundaryAccessOutcome.DENY
                RequirementFailureMode.LOCK -> BoundaryAccessOutcome.LOCK
            }
        }

        val uiPolicy = classification.resolveUiPolicy(entitlementState)
        val auditRecord = catalog.resolveAuditRecord(boundaryKey, entitlementState)

        return BoundaryDecision(
            classification = classification,
            entitlementState = entitlementState,
            outcome = outcome,
            uiPolicy = uiPolicy,
            auditRecord = auditRecord
        )
    }
}

interface BoundaryCatalogProvider {
    fun find(key: String): BoundaryClassification?
    fun resolveAuditRecord(
        key: String,
        entitlementState: EntitlementState
    ): BoundaryAuditRecord?
}

object DefaultBoundaryCatalogProvider : BoundaryCatalogProvider {
    override fun find(key: String): BoundaryClassification? =
        BoundaryCatalog.find(key)

    override fun resolveAuditRecord(
        key: String,
        entitlementState: EntitlementState
    ): BoundaryAuditRecord? =
        BoundaryCatalog.resolveAuditRecord(key, entitlementState)
}
