package com.lifeflow

import android.content.Context
import com.lifeflow.security.IntegrityTrustVerdictResponse
import kotlinx.coroutines.runBlocking

internal fun scheduleLifeFlowAppRuntimeIntegrityTrustStartupCheck(
    startupInitLock: Any,
    runtimeBindingsOrNull: () -> LifeFlowAppRuntimeBindings?,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    trigger: IntegrityStartupCheckTrigger,
    launchBackgroundTask: (String, () -> Unit) -> Unit,
    runIntegrityTrustStartupCheckNow: (
        IntegrityStartupCheckTrigger,
        suspend (String) -> IntegrityTrustVerdictResponse
    ) -> Unit
) {
    val integrityTrustRuntime = synchronized(startupInitLock) {
        val currentBindings = runtimeBindingsOrNull() ?: return
        if (
            !restartLifecycle.tryScheduleTrigger(
                trigger = trigger,
                isConfigured = currentBindings.integrityTrustRuntime.isConfigured()
            )
        ) {
            return
        }
        currentBindings.integrityTrustRuntime
    }

    launchBackgroundTask("LifeFlow-IntegrityStartupCheck-${trigger.name}") {
        runIntegrityTrustStartupCheckNow(
            trigger,
            integrityTrustRuntime::requestServerVerdict
        )
    }
}

internal fun tryScheduleLifeFlowAppRuntimeIntegrityTrustStartupCheck(
    startupInitLock: Any,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    isConfigured: Boolean
): Boolean = synchronized(startupInitLock) {
    restartLifecycle.tryScheduleTrigger(
        trigger = restartLifecycle.currentTriggerOrDefault(),
        isConfigured = isConfigured
    )
}

internal fun tryScheduleLifeFlowAppRuntimeIntegrityTrustStartupCheck(
    startupInitLock: Any,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    trigger: IntegrityStartupCheckTrigger,
    isConfigured: Boolean
): Boolean = synchronized(startupInitLock) {
    restartLifecycle.tryScheduleTrigger(
        trigger = trigger,
        isConfigured = isConfigured
    )
}

internal fun runLifeFlowAppRuntimeIntegrityTrustStartupCheckNow(
    startupInitLock: Any,
    restartLifecycle: RuntimeIntegrityLifecycleState,
    applicationContext: Context,
    startupIntegrityCoordinator: StartupIntegrityCoordinator,
    trigger: IntegrityStartupCheckTrigger,
    requestServerVerdict: suspend (String) -> IntegrityTrustVerdictResponse,
    logInfo: (String) -> Unit,
    logWarning: (String, Throwable?) -> Unit,
    performStartupRecoveryDecision: (
        IntegrityStartupCheckTrigger,
        IntegrityStartupRecoveryDecision,
        ProtectedRuntimeRebuildSource,
        String
    ) -> Unit
) {
    val request = synchronized(startupInitLock) {
        restartLifecycle.nextExecutionRequest(
            trigger = trigger,
            applicationContext = applicationContext
        )
    }

    val result = runBlocking {
        startupIntegrityCoordinator.execute(
            request = request,
            requestServerVerdict = requestServerVerdict
        )
    }

    result.infoLogMessage?.let(logInfo)
    result.warningLogMessage?.let { message ->
        logWarning(message, result.warningThrowable)
    }

    result.recoveryDecision?.let { recoveryDecision ->
        performStartupRecoveryDecision(
            trigger,
            recoveryDecision,
            result.recoveryRebuildSource
                ?: error("Missing recovery rebuild source."),
            result.recoverySignal
                ?: error("Missing recovery signal.")
        )
    }
}
