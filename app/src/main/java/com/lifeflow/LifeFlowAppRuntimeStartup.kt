package com.lifeflow

import android.content.Context

internal fun ensureLifeFlowAppRuntimeStarted(
    startupInitLock: Any,
    startupInitialized: Boolean,
    startupFailureMessage: String?,
    applicationContext: Context,
    runtimeBindingsFactory: (Context, Boolean) -> LifeFlowAppRuntimeBindings,
    updateRuntimeBindings: (LifeFlowAppRuntimeBindings?) -> Unit,
    updateStartupInitialized: (Boolean) -> Unit,
    updateStartupFailureMessage: (String?) -> Unit,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    runtimeSecuritySurveillanceCoordinator: RuntimeSecuritySurveillanceCoordinator,
    onRuntimeStartFailed: (Throwable) -> Unit
): LifeFlowAppRuntimeStartResult {
    if (startupInitialized) {
        return LifeFlowAppRuntimeStartResult(
            started = true,
            triggerToSchedule = null
        )
    }

    var started = false
    var triggerToSchedule: IntegrityStartupCheckTrigger? = null

    synchronized(startupInitLock) {
        if (startupInitialized) {
            started = true
        } else {
            try {
                val isInstrumentation = isLifeFlowAppRuntimeRunningInstrumentation()
                val startPlan = restartLifecycle.planStart(startupFailureMessage)

                updateRuntimeBindings(
                    runtimeBindingsFactory(
                        applicationContext,
                        isInstrumentation
                    )
                )
                updateStartupInitialized(true)
                updateStartupFailureMessage(null)

                restartLifecycle.commitStart(startPlan)
                runtimeSecuritySurveillanceCoordinator.startForRuntime(
                    isInstrumentation = isInstrumentation
                )

                triggerToSchedule = startPlan.trigger
                started = true
            } catch (exception: Exception) {
                runtimeSecuritySurveillanceCoordinator.stop()
                onRuntimeStartFailed(exception)
                updateRuntimeBindings(null)
                updateStartupInitialized(false)
                updateStartupFailureMessage(
                    buildLifeFlowAppRuntimeStartupFailureMessage(exception)
                )

                restartLifecycle.markStartFailed()
                started = false
            }
        }
    }

    return LifeFlowAppRuntimeStartResult(
        started = started,
        triggerToSchedule = triggerToSchedule
    )
}

internal fun closeLifeFlowAppRuntime(
    startupInitLock: Any,
    runtimeSecuritySurveillanceCoordinator: RuntimeSecuritySurveillanceCoordinator,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?,
    updateRuntimeBindings: (LifeFlowAppRuntimeBindings?) -> Unit,
    updateStartupInitialized: (Boolean) -> Unit,
    updateStartupFailureMessage: (String?) -> Unit,
    restartLifecycle: RuntimeIntegrityLifecycleState
) {
    synchronized(startupInitLock) {
        runtimeSecuritySurveillanceCoordinator.stop()
        runtimeBindingsOrNull()?.close()
        updateRuntimeBindings(null)
        updateStartupInitialized(false)
        updateStartupFailureMessage(null)

        restartLifecycle.clearAll()
    }
}

internal fun requireLifeFlowAppRuntimeBindings(
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?
): LifeFlowAppRuntimeBindings {
    return runtimeBindingsOrNull()
        ?: error("LifeFlowAppRuntimeBindings is not initialized.")
}

internal data class LifeFlowAppRuntimeStartResult(
    val started: Boolean,
    val triggerToSchedule: IntegrityStartupCheckTrigger?
)
