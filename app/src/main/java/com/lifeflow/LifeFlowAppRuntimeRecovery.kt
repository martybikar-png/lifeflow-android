package com.lifeflow

internal data class LifeFlowAppRuntimeProtectedAccessSnapshot(
    val startupInitialized: Boolean,
    val hardeningReportAvailable: Boolean
)

internal fun createLifeFlowAppRuntimeProtectedAccessSnapshot(
    startupInitialized: Boolean,
    hardeningReportAvailable: Boolean
): LifeFlowAppRuntimeProtectedAccessSnapshot {
    return LifeFlowAppRuntimeProtectedAccessSnapshot(
        startupInitialized = startupInitialized,
        hardeningReportAvailable = hardeningReportAvailable
    )
}

internal fun evaluateLifeFlowAppRuntimeProtectedAccessSecurityRecoveryIfNeeded(
    snapshot: LifeFlowAppRuntimeProtectedAccessSnapshot,
    runtimeSecuritySurveillanceCoordinator: RuntimeSecuritySurveillanceCoordinator
) {
    runtimeSecuritySurveillanceCoordinator.onProtectedAccessIfNeeded(
        startupInitialized = snapshot.startupInitialized,
        hardeningReportAvailable = snapshot.hardeningReportAvailable
    )
}

internal fun performLifeFlowAppRuntimeBudgetedRuntimeSecurityRecovery(
    planner: (Int) -> RuntimeSecurityRecoveryPlan,
    automaticRuntimeSecurityRebuildsUsedInProcess: Int,
    updateAutomaticRuntimeSecurityRebuildsUsedInProcess: (Int) -> Unit,
    logWarning: (String) -> Unit,
    performControlledProtectedRuntimeRebuild: (ProtectedRuntimeRebuildRequest) -> Boolean
): Boolean {
    val plan = planner(automaticRuntimeSecurityRebuildsUsedInProcess)

    if (plan.rebuildRequest != null) {
        updateAutomaticRuntimeSecurityRebuildsUsedInProcess(
            automaticRuntimeSecurityRebuildsUsedInProcess + 1
        )
    }

    plan.skipMessage?.let(logWarning)

    return plan.rebuildRequest?.let(performControlledProtectedRuntimeRebuild) ?: false
}

internal fun performLifeFlowAppRuntimeStartupRecoveryDecision(
    trigger: IntegrityStartupCheckTrigger,
    decision: IntegrityStartupRecoveryDecision,
    automaticProtectedRuntimeRebuildsUsedInProcess: Int,
    rebuildSource: ProtectedRuntimeRebuildSource,
    recoverySignal: String,
    logWarning: (String) -> Unit,
    performControlledProtectedRuntimeRebuild: (ProtectedRuntimeRebuildRequest) -> Boolean
) {
    val recoveryPlan = IntegrityStartupRecoveryController.plan(
        trigger = trigger,
        decision = decision,
        automaticProtectedRuntimeRebuildsUsedInProcess =
            automaticProtectedRuntimeRebuildsUsedInProcess
    )

    recoveryPlan.skipMessage?.let(logWarning)

    recoveryPlan.rebuildRequest?.let { request ->
        performControlledProtectedRuntimeRebuild(
            request.copy(
                rebuildSource = rebuildSource,
                recoverySignal = recoverySignal
            )
        )
    }
}
