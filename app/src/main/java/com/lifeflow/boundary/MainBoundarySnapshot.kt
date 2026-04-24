package com.lifeflow.boundary

import com.lifeflow.domain.core.boundary.BoundaryAuditExpectation
import com.lifeflow.domain.core.boundary.EntitlementStatus

data class MainBoundarySnapshot(
    val coreInsights: BoundaryPresentation,
    val adaptiveHabits: BoundaryPresentation,
    val enrichedCapture: BoundaryPresentation,
    val predictiveTimeline: BoundaryPresentation,
    val predictiveShopping: BoundaryPresentation,
    val deepSecondBrain: BoundaryPresentation
) {
    fun presentations(): List<BoundaryPresentation> = listOf(
        coreInsights,
        adaptiveHabits,
        enrichedCapture,
        predictiveTimeline,
        predictiveShopping,
        deepSecondBrain
    )

    companion object {
        fun initial(): MainBoundarySnapshot {
            val initialPresentation = BoundaryPresentation(
                boundaryKey = "initial",
                title = "Initial boundary state",
                state = BoundaryPresentationState.LOCKED,
                showUpgradePrompt = false,
                showLockedBadge = false,
                allowUserActionAudit = false,
                messageCode = "initial",
                owner = "boundary",
                entitlementStatus = EntitlementStatus.UNAVAILABLE,
                entitlementSource = BoundaryEntitlementSource.INITIAL,
                isGraceAccess = false,
                auditExpectation = BoundaryAuditExpectation.NONE,
                detailMessage = "Initial boundary state is not resolved yet."
            )

            return MainBoundarySnapshot(
                coreInsights = initialPresentation.copy(
                    boundaryKey = BoundaryKeys.CORE_INSIGHTS_DASHBOARD,
                    title = "Core Insights Dashboard"
                ),
                adaptiveHabits = initialPresentation.copy(
                    boundaryKey = BoundaryKeys.ADAPTIVE_HABITS,
                    title = "Adaptive Habits"
                ),
                enrichedCapture = initialPresentation.copy(
                    boundaryKey = BoundaryKeys.ENRICHED_CAPTURE,
                    title = "Enriched Capture Analysis"
                ),
                predictiveTimeline = initialPresentation.copy(
                    boundaryKey = BoundaryKeys.PREDICTIVE_TIMELINE,
                    title = "Predictive Timeline"
                ),
                predictiveShopping = initialPresentation.copy(
                    boundaryKey = BoundaryKeys.PREDICTIVE_SHOPPING,
                    title = "Predictive Shopping"
                ),
                deepSecondBrain = initialPresentation.copy(
                    boundaryKey = BoundaryKeys.DEEP_SECOND_BRAIN,
                    title = "Deep Second Brain"
                )
            )
        }
    }
}
