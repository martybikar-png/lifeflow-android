package com.lifeflow

internal fun performLifeFlowAppRuntimeBudgetedRuntimeSecurityRecoveryLocked(
    startupInitLock: Any,
    planner: (Int) -> RuntimeSecurityRecoveryPlan,
    automaticRuntimeSecurityRebuildsUsedInProcess: Int,
    updateAutomaticRuntimeSecurityRebuildsUsedInProcess: (Int) -> Unit,
    logWarning: (String) -> Unit,
    performControlledProtectedRuntimeRebuild: (ProtectedRuntimeRebuildRequest) -> Boolean
): Boolean {
    return synchronized(startupInitLock) {
        performLifeFlowAppRuntimeBudgetedRuntimeSecurityRecovery(
            planner = planner,
            automaticRuntimeSecurityRebuildsUsedInProcess =
                automaticRuntimeSecurityRebuildsUsedInProcess,
            updateAutomaticRuntimeSecurityRebuildsUsedInProcess =
                updateAutomaticRuntimeSecurityRebuildsUsedInProcess,
            logWarning = logWarning,
            performControlledProtectedRuntimeRebuild =
                performControlledProtectedRuntimeRebuild
        )
    }
}

internal fun performLifeFlowAppRuntimeStartupRecoveryDecisionLocked(
    startupInitLock: Any,
    trigger: IntegrityStartupCheckTrigger,
    decision: IntegrityStartupRecoveryDecision,
    automaticProtectedRuntimeRebuildsUsedInProcess: Int,
    rebuildSource: ProtectedRuntimeRebuildSource,
    recoverySignal: String,
    logWarning: (String) -> Unit,
    performControlledProtectedRuntimeRebuild: (ProtectedRuntimeRebuildRequest) -> Boolean
) {
    val usedRebuilds = synchronized(startupInitLock) {
        automaticProtectedRuntimeRebuildsUsedInProcess
    }

    performLifeFlowAppRuntimeStartupRecoveryDecision(
        trigger = trigger,
        decision = decision,
        automaticProtectedRuntimeRebuildsUsedInProcess = usedRebuilds,
        rebuildSource = rebuildSource,
        recoverySignal = recoverySignal,
        logWarning = logWarning,
        performControlledProtectedRuntimeRebuild =
            performControlledProtectedRuntimeRebuild
    )
}
