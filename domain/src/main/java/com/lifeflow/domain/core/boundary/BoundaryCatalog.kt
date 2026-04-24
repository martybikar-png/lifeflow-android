package com.lifeflow.domain.core.boundary

object BoundaryCatalog {

    val classifications: List<BoundaryClassification> = listOf(
        BoundaryClassification(
            key = "module.timeline.predictive",
            surfaceType = BoundarySurfaceType.MODULE,
            displayName = "Predictive Timeline",
            requirement = TierRequirement.core(
                failureMode = RequirementFailureMode.LOCK,
                reasonCode = "core_predictive_timeline_required"
            ),
            auditName = "predictive_timeline_boundary",
            owner = "AdaptiveTimeline2027",
            notes = "Predictive and premium timeline functions are Core-locked."
        ),
        BoundaryClassification(
            key = "module.shopping.predictive",
            surfaceType = BoundarySurfaceType.MODULE,
            displayName = "Predictive Shopping",
            requirement = TierRequirement.core(
                failureMode = RequirementFailureMode.LOCK,
                reasonCode = "core_predictive_shopping_required"
            ),
            auditName = "predictive_shopping_boundary",
            owner = "PredictiveShoppingEngine",
            notes = "Predictive shopping remains outside Free."
        ),
        BoundaryClassification(
            key = "module.secondbrain.deep",
            surfaceType = BoundarySurfaceType.MODULE,
            displayName = "Deep Second Brain",
            requirement = TierRequirement.core(
                failureMode = RequirementFailureMode.LOCK,
                reasonCode = "core_second_brain_required"
            ),
            auditName = "deep_second_brain_boundary",
            owner = "SecondBrainNeuralNetwork",
            notes = "Deep memory synthesis is Core-only."
        ),
        BoundaryClassification(
            key = "action.capture.basic",
            surfaceType = BoundarySurfaceType.ACTION,
            displayName = "Basic Capture",
            requirement = TierRequirement.free(
                failureMode = RequirementFailureMode.DENY,
                reasonCode = "free_basic_capture_allowed"
            ),
            auditName = "basic_capture_boundary",
            owner = "Capture",
            notes = "Basic capture remains available in Free."
        ),
        BoundaryClassification(
            key = "action.capture.enriched",
            surfaceType = BoundarySurfaceType.ACTION,
            displayName = "Enriched Capture Analysis",
            requirement = TierRequirement.core(
                failureMode = RequirementFailureMode.DENY,
                reasonCode = "core_enriched_capture_required"
            ),
            auditName = "enriched_capture_boundary",
            owner = "Capture",
            notes = "Enriched or premium analysis requires Core."
        ),
        BoundaryClassification(
            key = "screen.dashboard.coreInsights",
            surfaceType = BoundarySurfaceType.SCREEN,
            displayName = "Core Insights Dashboard",
            requirement = TierRequirement.core(
                failureMode = RequirementFailureMode.LOCK,
                reasonCode = "core_dashboard_insights_required"
            ),
            auditName = "core_insights_dashboard_boundary",
            owner = "Dashboard",
            notes = "Free may show teaser state, but content stays locked."
        ),
        BoundaryClassification(
            key = "automation.habits.adaptive",
            surfaceType = BoundarySurfaceType.AUTOMATION,
            displayName = "Adaptive Habits",
            requirement = TierRequirement.core(
                failureMode = RequirementFailureMode.LOCK,
                reasonCode = "core_adaptive_habits_required"
            ),
            auditName = "adaptive_habits_boundary",
            owner = "AutonomousHabitsEngine",
            notes = "Adaptive automation is Core-only."
        )
    )

    val auditExpectations: Map<String, BoundaryAuditExpectation> = mapOf(
        "module.timeline.predictive" to BoundaryAuditExpectation.LOG_ON_LOCKED_PRESENTATION,
        "module.shopping.predictive" to BoundaryAuditExpectation.LOG_ON_LOCKED_PRESENTATION,
        "module.secondbrain.deep" to BoundaryAuditExpectation.LOG_ON_LOCKED_PRESENTATION,
        "action.capture.basic" to BoundaryAuditExpectation.NONE,
        "action.capture.enriched" to BoundaryAuditExpectation.LOG_ON_BLOCKED_ACTION,
        "screen.dashboard.coreInsights" to BoundaryAuditExpectation.LOG_ON_LOCKED_PRESENTATION,
        "automation.habits.adaptive" to BoundaryAuditExpectation.LOG_ON_BLOCKED_ACTION
    )

    fun find(key: String): BoundaryClassification? =
        classifications.firstOrNull { it.key == key }

    fun resolveOutcome(
        key: String,
        entitlementState: EntitlementState
    ): BoundaryAccessOutcome {
        val classification = find(key) ?: return BoundaryAccessOutcome.DENY
        if (entitlementState.satisfies(classification.requirement)) {
            return BoundaryAccessOutcome.ALLOW
        }

        return when (classification.requirement.failureMode) {
            RequirementFailureMode.DENY -> BoundaryAccessOutcome.DENY
            RequirementFailureMode.LOCK -> BoundaryAccessOutcome.LOCK
        }
    }

    fun resolveAuditRecord(
        key: String,
        entitlementState: EntitlementState
    ): BoundaryAuditRecord? {
        val classification = find(key) ?: return null
        val outcome = resolveOutcome(key, entitlementState)
        val expectation = auditExpectations[key] ?: BoundaryAuditExpectation.NONE

        if (expectation == BoundaryAuditExpectation.NONE && outcome == BoundaryAccessOutcome.ALLOW) {
            return null
        }

        return BoundaryAuditRecord(
            boundaryKey = classification.key,
            auditName = classification.auditName,
            outcome = outcome,
            reasonCode = classification.requirement.reasonCode,
            entitlementTier = entitlementState.tier,
            entitlementStatus = entitlementState.status,
            expectation = expectation
        )
    }
}
