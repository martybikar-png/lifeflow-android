package com.lifeflow.boundary

import com.lifeflow.domain.core.boundary.BoundaryAuditExpectation
import com.lifeflow.domain.core.boundary.BoundaryDecision
import com.lifeflow.domain.core.boundary.BoundaryUiBehavior
import com.lifeflow.domain.core.boundary.EntitlementStatus

class BoundaryPresentationResolver {

    fun resolve(decision: BoundaryDecision): BoundaryPresentation {
        val state = when (decision.uiPolicy.behavior) {
            BoundaryUiBehavior.VISIBLE_ENABLED -> BoundaryPresentationState.ENABLED
            BoundaryUiBehavior.VISIBLE_DENIED -> BoundaryPresentationState.DENIED
            BoundaryUiBehavior.VISIBLE_LOCKED -> BoundaryPresentationState.LOCKED
        }

        return BoundaryPresentation(
            boundaryKey = decision.classification.key,
            title = decision.classification.displayName,
            state = state,
            showUpgradePrompt = decision.uiPolicy.showUpgradePrompt,
            showLockedBadge = decision.uiPolicy.showLockedState,
            allowUserActionAudit = decision.uiPolicy.allowTapAudit,
            messageCode = decision.uiPolicy.userMessageCode,
            owner = decision.classification.owner,
            entitlementStatus = decision.entitlementState.status,
            entitlementSource = BoundaryEntitlementSource.fromProvenanceName(
                decision.entitlementState.provenance.name
            ),
            isGraceAccess = decision.entitlementState.isGraceAccess,
            auditExpectation = decision.auditRecord?.expectation ?: BoundaryAuditExpectation.NONE,
            detailMessage = resolveDetailMessage(decision, state)
        )
    }

    private fun resolveDetailMessage(
        decision: BoundaryDecision,
        state: BoundaryPresentationState
    ): String {
        val entitlementState = decision.entitlementState

        return when {
            entitlementState.status == EntitlementStatus.REVOKED ->
                "Entitlement revoked. This Core surface stays unavailable until access is restored."

            entitlementState.status == EntitlementStatus.UNAVAILABLE ->
                "Entitlement unavailable. This Core surface stays unavailable until entitlement can be resolved."

            entitlementState.status == EntitlementStatus.LOCKED ->
                "Entitlement locked. This surface stays blocked until the lock is cleared."

            entitlementState.isGraceAccess && state == BoundaryPresentationState.ENABLED ->
                "Grace access active. This Core surface is temporarily available while entitlement continuity is resolving."

            state == BoundaryPresentationState.LOCKED ->
                "Core required. This surface remains visible but locked in the current tier."

            state == BoundaryPresentationState.DENIED ->
                "Core required. This action is denied in the current tier."

            else ->
                "Boundary satisfied."
        }
    }
}
