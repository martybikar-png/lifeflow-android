package com.lifeflow

import kotlin.math.max

internal fun createLifeFlowAppRuntimeProtectedRuntimeRebuildExecutor(
    startupInitLock: Any,
    rebuildCoordinator: ProtectedRuntimeRebuildCoordinator,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    runtimeSecuritySurveillanceCoordinator: RuntimeSecuritySurveillanceCoordinator,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?,
    updateRuntimeBindings: (LifeFlowAppRuntimeBindings?) -> Unit,
    updateStartupInitialized: (Boolean) -> Unit,
    updateStartupFailureMessage: (String?) -> Unit,
    automaticProtectedRuntimeRebuildsUsedInProcess: () -> Int,
    updateAutomaticProtectedRuntimeRebuildsUsedInProcess: (Int) -> Unit,
    restart: () -> Boolean,
    logInfo: (String) -> Unit,
    logWarning: (String) -> Unit
): ProtectedRuntimeRebuildExecutor {
    return ProtectedRuntimeRebuildExecutor(
        rebuildCoordinator = rebuildCoordinator,
        prepareRestartAndTeardown = { request, reason, startupAutoRecoveryAttempt ->
            synchronized(startupInitLock) {
                startupAutoRecoveryAttempt?.let { attempt ->
                    updateAutomaticProtectedRuntimeRebuildsUsedInProcess(
                        max(automaticProtectedRuntimeRebuildsUsedInProcess(), attempt)
                    )
                }

                restartLifecycle.preparePendingRestart(
                    reason = reason,
                    autoRecoveryAttempt = startupAutoRecoveryAttempt,
                    rebuildSource = request.rebuildSource,
                    recoverySignal = request.recoverySignal
                )

                runtimeSecuritySurveillanceCoordinator.stop()
                runtimeBindingsOrNull()?.close()
                updateRuntimeBindings(null)
                updateStartupInitialized(false)
                updateStartupFailureMessage(null)

                restartLifecycle.clearCurrentAfterRuntimeTeardown()
            }
        },
        restart = restart,
        logInfo = logInfo,
        logWarning = logWarning
    )
}
