package com.lifeflow

import com.lifeflow.boundary.BoundaryAccessController
import com.lifeflow.boundary.BoundaryAuditLogger
import com.lifeflow.boundary.BoundaryKeys
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.boundary.EntitlementStatus

internal data class MainViewModelBoundaryRefreshResult(
    val currentTier: TierState,
    val boundarySnapshot: MainBoundarySnapshot,
    val freeTierMessage: String,
    val blockedCount: Int
)

internal fun refreshMainViewModelBoundaryState(
    orchestrator: LifeFlowOrchestrator,
    boundaryAccessController: BoundaryAccessController
): MainViewModelBoundaryRefreshResult {
    val currentTier = orchestrator.currentTier()
    val entitlementState = orchestrator.currentEntitlementState()

    val coreInsights = boundaryAccessController.evaluate(
        boundaryKey = BoundaryKeys.CORE_INSIGHTS_DASHBOARD,
        entitlementState = entitlementState
    )
    val adaptiveHabits = boundaryAccessController.evaluate(
        boundaryKey = BoundaryKeys.ADAPTIVE_HABITS,
        entitlementState = entitlementState
    )
    val enrichedCapture = boundaryAccessController.evaluate(
        boundaryKey = BoundaryKeys.ENRICHED_CAPTURE,
        entitlementState = entitlementState
    )
    val predictiveTimeline = boundaryAccessController.evaluate(
        boundaryKey = BoundaryKeys.PREDICTIVE_TIMELINE,
        entitlementState = entitlementState
    )
    val predictiveShopping = boundaryAccessController.evaluate(
        boundaryKey = BoundaryKeys.PREDICTIVE_SHOPPING,
        entitlementState = entitlementState
    )
    val deepSecondBrain = boundaryAccessController.evaluate(
        boundaryKey = BoundaryKeys.DEEP_SECOND_BRAIN,
        entitlementState = entitlementState
    )

    val boundarySnapshot = MainBoundarySnapshot(
        coreInsights = coreInsights.presentation,
        adaptiveHabits = adaptiveHabits.presentation,
        enrichedCapture = enrichedCapture.presentation,
        predictiveTimeline = predictiveTimeline.presentation,
        predictiveShopping = predictiveShopping.presentation,
        deepSecondBrain = deepSecondBrain.presentation
    )

    BoundaryAuditLogger.log(coreInsights.decision)
    BoundaryAuditLogger.log(adaptiveHabits.decision)
    BoundaryAuditLogger.log(enrichedCapture.decision)
    BoundaryAuditLogger.log(predictiveTimeline.decision)
    BoundaryAuditLogger.log(predictiveShopping.decision)
    BoundaryAuditLogger.log(deepSecondBrain.decision)

    val presentations = boundarySnapshot.presentations()
    val revokedCount = presentations.count { it.entitlementStatus == EntitlementStatus.REVOKED }
    val lockedCount = presentations.count { it.entitlementStatus == EntitlementStatus.LOCKED }
    val unavailableCount = presentations.count { it.entitlementStatus == EntitlementStatus.UNAVAILABLE }
    val graceCount = presentations.count { it.isGraceAccess }
    val blockedTitles = presentations.filter { it.isBlockedLike }.map { it.title }

    val freeTierMessage = when {
        revokedCount > 0 ->
            "Free tier active. Some Core surfaces are revoked and remain unavailable."

        lockedCount > 0 ->
            "Free tier active. Some Core surfaces are locked and remain unavailable."

        unavailableCount > 0 ->
            "Free tier active. Some Core surfaces are unavailable until entitlement state is resolved."

        graceCount > 0 ->
            "Free tier active. Grace entitlement is present for part of the boundary state."

        blockedTitles.isNotEmpty() ->
            "Free tier active. Locked in this mode: ${blockedTitles.joinToString()}."

        else ->
            "Free tier active."
    }

    return MainViewModelBoundaryRefreshResult(
        currentTier = currentTier,
        boundarySnapshot = boundarySnapshot,
        freeTierMessage = freeTierMessage,
        blockedCount = presentations.count { it.isBlockedLike }
    )
}
