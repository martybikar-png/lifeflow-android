package com.lifeflow

internal fun prepareLifeFlowAppRuntimeSecurityRuntimeRestart(
    startupInitLock: Any,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    reason: IntegrityRuntimeRestartReason
) {
    synchronized(startupInitLock) {
        restartLifecycle.preparePendingRestart(
            reason = reason,
            autoRecoveryAttempt = null,
            rebuildSource = null,
            recoverySignal = null
        )
    }
}

internal fun closeLifeFlowAppRuntimeForSecurityRuntimeRestart(
    startupInitLock: Any,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    runtimeSecuritySurveillanceCoordinator: RuntimeSecuritySurveillanceCoordinator,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?,
    clearRuntimeBindings: () -> Unit,
    updateStartupInitialized: (Boolean) -> Unit,
    reason: IntegrityRuntimeRestartReason
) {
    synchronized(startupInitLock) {
        restartLifecycle.preparePendingRestart(
            reason = reason,
            autoRecoveryAttempt = null,
            rebuildSource = null,
            recoverySignal = null
        )

        runtimeSecuritySurveillanceCoordinator.stop()
        runtimeBindingsOrNull()?.close()
        clearRuntimeBindings()
        updateStartupInitialized(false)

        restartLifecycle.clearCurrentAfterRuntimeTeardown()
    }
}

internal fun requestLifeFlowAppRuntimeProtectedRuntimeRebuild(
    performControlledProtectedRuntimeRebuild: (ProtectedRuntimeRebuildRequest) -> Boolean
): Boolean {
    return performControlledProtectedRuntimeRebuild(
        ProtectedRuntimeRebuildRequest(
            reason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            rebuildSource = ProtectedRuntimeRebuildSource.INTERNAL
        )
    )
}

internal fun performLifeFlowAppRuntimeControlledProtectedRuntimeRebuild(
    protectedRuntimeRebuildExecutor: ProtectedRuntimeRebuildExecutor,
    request: ProtectedRuntimeRebuildRequest
): Boolean {
    return protectedRuntimeRebuildExecutor.execute(request)
}
