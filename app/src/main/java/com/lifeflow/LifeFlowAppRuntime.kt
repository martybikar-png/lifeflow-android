package com.lifeflow

import android.content.Context
import com.lifeflow.security.IntegrityTrustVerdictResponse
import com.lifeflow.security.SecurityAuthPerUseCryptoProvider
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.hardening.SecurityHardeningGuard

internal class LifeFlowAppRuntime(
    private val applicationContext: Context,
    private val runtimeBindingsFactory: (Context, Boolean) -> LifeFlowAppRuntimeBindings =
        ::createDefaultLifeFlowAppRuntimeBindings,
    private val launchBackgroundTask: (String, () -> Unit) -> Unit =
        ::launchDefaultLifeFlowAppRuntimeBackgroundTask,
    private val reportIntegrityTrustVerdictResponse: (IntegrityTrustVerdictResponse) -> Unit =
        ::reportDefaultLifeFlowAppRuntimeIntegrityTrustVerdictResponse,
    private val startupIntegrityContextFactory: (Context) -> IntegrityStartupRequestContext =
        ::createDefaultLifeFlowAppRuntimeStartupIntegrityRequestContext
) : StartupRuntimeEntryPoint, AutoCloseable {

    @Volatile
    private var runtimeBindings: LifeFlowAppRuntimeBindings? = null
    @Volatile
    private var startupInitialized = false
    @Volatile
    private var startupFailureMessage: String? = null

    private var automaticProtectedRuntimeRebuildsUsedInProcess = 0
    private var automaticRuntimeSecurityRebuildsUsedInProcess = 0

    private val rebuildCoordinator = ProtectedRuntimeRebuildCoordinator()
    private val runtimeSecurityRecoveryBridge = RuntimeSecurityRecoveryBridge(
        executeBudgetedRecovery = ::performBudgetedRuntimeSecurityRecovery
    )
    private val startupIntegrityCoordinator = StartupIntegrityCoordinator(
        startupIntegrityContextFactory = startupIntegrityContextFactory,
        reportIntegrityTrustVerdictResponse = reportIntegrityTrustVerdictResponse
    )
    private val restartLifecycle = RuntimeIntegrityLifecycleState()
    private val runtimeSecuritySurveillanceCoordinator =
        RuntimeSecuritySurveillanceCoordinator(
            runtimeSecurityRecoveryBridge = runtimeSecurityRecoveryBridge,
            quickHardeningCompromised = SecurityHardeningGuard::isCompromisedQuick,
            currentTrustState = SecurityRuleEngine::getTrustState,
            logWarning = ::logLifeFlowAppRuntimeWarning
        )
    private val startupInitLock = Any()
    private val protectedRuntimeRebuildExecutor =
        createLifeFlowAppRuntimeProtectedRuntimeRebuildExecutor(
            startupInitLock = startupInitLock,
            rebuildCoordinator = rebuildCoordinator,
            restartLifecycle = restartLifecycle,
            runtimeSecuritySurveillanceCoordinator = runtimeSecuritySurveillanceCoordinator,
            runtimeBindingsOrNull = { runtimeBindings },
            updateRuntimeBindings = { runtimeBindings = it },
            updateStartupInitialized = { startupInitialized = it },
            updateStartupFailureMessage = { startupFailureMessage = it },
            automaticProtectedRuntimeRebuildsUsedInProcess = {
                automaticProtectedRuntimeRebuildsUsedInProcess
            },
            updateAutomaticProtectedRuntimeRebuildsUsedInProcess = {
                automaticProtectedRuntimeRebuildsUsedInProcess = it
            },
            restart = ::ensureStarted,
            logInfo = ::logLifeFlowAppRuntimeInfo,
            logWarning = ::logLifeFlowAppRuntimeWarning
        )

    val hardeningReport: SecurityHardeningGuard.HardeningReport?
        get() = runtimeBindings?.hardeningReport

    override fun ensureStarted(): Boolean {
        val result = ensureLifeFlowAppRuntimeStarted(
            startupInitLock = startupInitLock,
            startupInitialized = startupInitialized,
            startupFailureMessage = startupFailureMessage,
            applicationContext = applicationContext,
            runtimeBindingsFactory = runtimeBindingsFactory,
            updateRuntimeBindings = { runtimeBindings = it },
            updateStartupInitialized = { startupInitialized = it },
            updateStartupFailureMessage = { startupFailureMessage = it },
            restartLifecycle = restartLifecycle,
            runtimeSecuritySurveillanceCoordinator = runtimeSecuritySurveillanceCoordinator,
            onRuntimeStartFailed = { throwable ->
                logLifeFlowAppRuntimeError("Startup initialization failed.", throwable)
            }
        )
        if (result.started) {
            result.triggerToSchedule?.let(::scheduleIntegrityTrustStartupCheck)
        }
        return result.started
    }

    override fun requireMainViewModelFactory(): MainViewModelFactory =
        requireLifeFlowAppRuntimeMainViewModelFactory(
            evaluateProtectedAccessSecurityRecoveryIfNeeded =
                ::evaluateProtectedAccessSecurityRecoveryIfNeeded,
            runtimeBindingsOrNull = { runtimeBindings }
        )

    override fun authPerUseCryptoProviderOrNull(): SecurityAuthPerUseCryptoProvider? =
        requireLifeFlowAppRuntimeAuthPerUseCryptoProviderOrNull(
            evaluateProtectedAccessSecurityRecoveryIfNeeded =
                ::evaluateProtectedAccessSecurityRecoveryIfNeeded,
            runtimeBindingsOrNull = { runtimeBindings }
        )

    override fun scheduleIntegrityTrustStartupCheck() {
        scheduleIntegrityTrustStartupCheck(
            trigger = restartLifecycle.currentTriggerOrDefault()
        )
    }

    internal fun scheduleIntegrityTrustStartupCheck(
        trigger: IntegrityStartupCheckTrigger
    ) = scheduleLifeFlowAppRuntimeIntegrityTrustStartupCheck(
        startupInitLock = startupInitLock,
        runtimeBindingsOrNull = { runtimeBindings },
        restartLifecycle = restartLifecycle,
        trigger = trigger,
        launchBackgroundTask = launchBackgroundTask,
        runIntegrityTrustStartupCheckNow = ::runIntegrityTrustStartupCheckNow
    )

    internal fun prepareSecurityRuntimeRestart(
        reason: IntegrityRuntimeRestartReason
    ) = prepareLifeFlowAppRuntimeSecurityRuntimeRestart(
        startupInitLock = startupInitLock,
        restartLifecycle = restartLifecycle,
        reason = reason
    )

    internal fun closeForSecurityRuntimeRestart(
        reason: IntegrityRuntimeRestartReason
    ) = closeLifeFlowAppRuntimeForSecurityRuntimeRestart(
        startupInitLock = startupInitLock,
        restartLifecycle = restartLifecycle,
        runtimeSecuritySurveillanceCoordinator = runtimeSecuritySurveillanceCoordinator,
        runtimeBindingsOrNull = { runtimeBindings },
        clearRuntimeBindings = { runtimeBindings = null },
        updateStartupInitialized = { startupInitialized = it },
        reason = reason
    )

    internal fun requestProtectedRuntimeRebuild(): Boolean =
        requestLifeFlowAppRuntimeProtectedRuntimeRebuild(
            performControlledProtectedRuntimeRebuild =
                ::performControlledProtectedRuntimeRebuild
        )

    internal fun requestProtectedRuntimeRebuildForSecurityRecovery(
        logMessage: String
    ): Boolean = runtimeSecurityRecoveryBridge.onExplicitSecurityRecoveryRequest(logMessage)

    internal fun tryScheduleIntegrityTrustStartupCheck(
        isConfigured: Boolean
    ): Boolean = tryScheduleLifeFlowAppRuntimeIntegrityTrustStartupCheck(
        startupInitLock = startupInitLock,
        restartLifecycle = restartLifecycle,
        isConfigured = isConfigured
    )

    internal fun tryScheduleIntegrityTrustStartupCheck(
        trigger: IntegrityStartupCheckTrigger,
        isConfigured: Boolean
    ): Boolean = tryScheduleLifeFlowAppRuntimeIntegrityTrustStartupCheck(
        startupInitLock = startupInitLock,
        restartLifecycle = restartLifecycle,
        trigger = trigger,
        isConfigured = isConfigured
    )

    internal fun runIntegrityTrustStartupCheckNow(
        requestServerVerdict: suspend (String) -> IntegrityTrustVerdictResponse
    ) {
        runIntegrityTrustStartupCheckNow(
            trigger = restartLifecycle.currentTriggerOrDefault(),
            requestServerVerdict = requestServerVerdict
        )
    }

    internal fun runIntegrityTrustStartupCheckNow(
        trigger: IntegrityStartupCheckTrigger,
        requestServerVerdict: suspend (String) -> IntegrityTrustVerdictResponse
    ) = runLifeFlowAppRuntimeIntegrityTrustStartupCheckNow(
        startupInitLock = startupInitLock,
        restartLifecycle = restartLifecycle,
        applicationContext = applicationContext,
        startupIntegrityCoordinator = startupIntegrityCoordinator,
        trigger = trigger,
        requestServerVerdict = requestServerVerdict,
        logInfo = ::logLifeFlowAppRuntimeInfo,
        logWarning = ::logLifeFlowAppRuntimeWarning,
        performStartupRecoveryDecision = ::performStartupRecoveryDecision
    )

    override fun readStartupFailureMessage(): String? =
        readLifeFlowAppRuntimeStartupFailureMessage(
            startupFailureMessage = startupFailureMessage
        )

    override fun close() = closeLifeFlowAppRuntime(
        startupInitLock = startupInitLock,
        runtimeSecuritySurveillanceCoordinator = runtimeSecuritySurveillanceCoordinator,
        runtimeBindingsOrNull = { runtimeBindings },
        updateRuntimeBindings = { runtimeBindings = it },
        updateStartupInitialized = { startupInitialized = it },
        updateStartupFailureMessage = { startupFailureMessage = it },
        restartLifecycle = restartLifecycle
    )

    private fun evaluateProtectedAccessSecurityRecoveryIfNeeded() =
        evaluateLifeFlowAppRuntimeProtectedAccess(
            startupInitLock = startupInitLock,
            startupInitialized = startupInitialized,
            runtimeBindingsOrNull = { runtimeBindings },
            runtimeSecuritySurveillanceCoordinator =
                runtimeSecuritySurveillanceCoordinator
        )

    private fun performBudgetedRuntimeSecurityRecovery(
        planner: (Int) -> RuntimeSecurityRecoveryPlan
    ): Boolean = performLifeFlowAppRuntimeBudgetedRuntimeSecurityRecoveryLocked(
        startupInitLock = startupInitLock,
        planner = planner,
        automaticRuntimeSecurityRebuildsUsedInProcess =
            automaticRuntimeSecurityRebuildsUsedInProcess,
        updateAutomaticRuntimeSecurityRebuildsUsedInProcess = {
            automaticRuntimeSecurityRebuildsUsedInProcess = it
        },
        logWarning = ::logLifeFlowAppRuntimeWarning,
        performControlledProtectedRuntimeRebuild =
            ::performControlledProtectedRuntimeRebuild
    )

    private fun performStartupRecoveryDecision(
        trigger: IntegrityStartupCheckTrigger,
        decision: IntegrityStartupRecoveryDecision,
        rebuildSource: ProtectedRuntimeRebuildSource,
        recoverySignal: String
    ) = performLifeFlowAppRuntimeStartupRecoveryDecisionLocked(
        startupInitLock = startupInitLock,
        trigger = trigger,
        decision = decision,
        automaticProtectedRuntimeRebuildsUsedInProcess =
            automaticProtectedRuntimeRebuildsUsedInProcess,
        rebuildSource = rebuildSource,
        recoverySignal = recoverySignal,
        logWarning = ::logLifeFlowAppRuntimeWarning,
        performControlledProtectedRuntimeRebuild =
            ::performControlledProtectedRuntimeRebuild
    )

    private fun performControlledProtectedRuntimeRebuild(
        request: ProtectedRuntimeRebuildRequest
    ): Boolean = performLifeFlowAppRuntimeControlledProtectedRuntimeRebuild(
        protectedRuntimeRebuildExecutor = protectedRuntimeRebuildExecutor,
        request = request
    )
}
