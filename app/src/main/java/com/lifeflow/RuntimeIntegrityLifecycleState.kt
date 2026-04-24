package com.lifeflow

import android.content.Context
import java.util.UUID

internal data class RuntimeIntegrityStartPlan(
    val trigger: IntegrityStartupCheckTrigger,
    val restartReason: IntegrityRuntimeRestartReason?,
    val autoRecoveryAttempt: Int?,
    val rebuildSource: ProtectedRuntimeRebuildSource?,
    val recoverySignal: String?
)

internal class RuntimeIntegrityLifecycleState {

    private var currentStartupTrigger: IntegrityStartupCheckTrigger? = null
    private var currentRestartReason: IntegrityRuntimeRestartReason? = null
    private var currentAutoRecoveryAttempt: Int? = null
    private var currentRebuildSource: ProtectedRuntimeRebuildSource? = null
    private var currentRecoverySignal: String? = null

    private var pendingRestartReason: IntegrityRuntimeRestartReason? = null
    private var pendingAutoRecoveryAttempt: Int? = null
    private var pendingRebuildSource: ProtectedRuntimeRebuildSource? = null
    private var pendingRecoverySignal: String? = null

    private var nextStartupProcessSequence: Int = 1

    private val scheduledIntegrityStartupCheckTriggers =
        linkedSetOf<IntegrityStartupCheckTrigger>()

    fun currentTriggerOrDefault(): IntegrityStartupCheckTrigger =
        currentStartupTrigger ?: IntegrityStartupCheckTrigger.APPLICATION_COLD_START

    fun planStart(
        startupFailureMessage: String?
    ): RuntimeIntegrityStartPlan {
        val derivedRestartReason = when {
            pendingRestartReason != null ->
                pendingRestartReason

            !startupFailureMessage.isNullOrBlank() ->
                IntegrityRuntimeRestartReason.SECURITY_RECOVERY_REENTRY

            else ->
                null
        }

        val trigger = if (derivedRestartReason != null) {
            IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART
        } else {
            IntegrityStartupCheckTrigger.APPLICATION_COLD_START
        }

        return RuntimeIntegrityStartPlan(
            trigger = trigger,
            restartReason = derivedRestartReason,
            autoRecoveryAttempt = pendingAutoRecoveryAttempt,
            rebuildSource = pendingRebuildSource,
            recoverySignal = pendingRecoverySignal
        )
    }

    fun commitStart(
        plan: RuntimeIntegrityStartPlan
    ) {
        scheduledIntegrityStartupCheckTriggers.clear()

        currentStartupTrigger = plan.trigger
        currentRestartReason = plan.restartReason
        currentAutoRecoveryAttempt = plan.autoRecoveryAttempt
        currentRebuildSource = plan.rebuildSource
        currentRecoverySignal = plan.recoverySignal

        pendingRestartReason = null
        pendingAutoRecoveryAttempt = null
        pendingRebuildSource = null
        pendingRecoverySignal = null
    }

    /**
     * Startup failed while trying to start.
     * Keep pending restart intent so the next ensureStarted() still knows
     * whether it is recovering from a controlled runtime restart.
     */
    fun markStartFailed() {
        scheduledIntegrityStartupCheckTriggers.clear()

        currentStartupTrigger = null
        currentRestartReason = null
        currentAutoRecoveryAttempt = null
        currentRebuildSource = null
        currentRecoverySignal = null
    }

    fun preparePendingRestart(
        reason: IntegrityRuntimeRestartReason,
        autoRecoveryAttempt: Int?,
        rebuildSource: ProtectedRuntimeRebuildSource?,
        recoverySignal: String?
    ) {
        pendingRestartReason = reason
        pendingAutoRecoveryAttempt = autoRecoveryAttempt
        pendingRebuildSource = rebuildSource
        pendingRecoverySignal = recoverySignal
    }

    /**
     * Used during controlled runtime teardown before restart.
     * Keeps pending restart intent, clears only current runtime view.
     */
    fun clearCurrentAfterRuntimeTeardown() {
        scheduledIntegrityStartupCheckTriggers.clear()

        currentStartupTrigger = null
        currentRestartReason = null
        currentAutoRecoveryAttempt = null
        currentRebuildSource = null
        currentRecoverySignal = null
    }

    fun clearAll() {
        scheduledIntegrityStartupCheckTriggers.clear()

        currentStartupTrigger = null
        currentRestartReason = null
        currentAutoRecoveryAttempt = null
        currentRebuildSource = null
        currentRecoverySignal = null

        pendingRestartReason = null
        pendingAutoRecoveryAttempt = null
        pendingRebuildSource = null
        pendingRecoverySignal = null
    }

    fun tryScheduleTrigger(
        trigger: IntegrityStartupCheckTrigger,
        isConfigured: Boolean
    ): Boolean {
        if (!isConfigured) {
            return false
        }
        if (trigger in scheduledIntegrityStartupCheckTriggers) {
            return false
        }

        scheduledIntegrityStartupCheckTriggers += trigger
        return true
    }

    fun nextExecutionRequest(
        trigger: IntegrityStartupCheckTrigger,
        applicationContext: Context
    ): StartupIntegrityExecutionRequest {
        val request = StartupIntegrityExecutionRequest(
            trigger = trigger,
            applicationContext = applicationContext,
            lifecycleSnapshot = snapshotForTrigger(trigger),
            startupProcessSequence = nextStartupProcessSequence,
            startupRequestId = UUID.randomUUID().toString(),
            requestedAtEpochMs = System.currentTimeMillis()
        )

        nextStartupProcessSequence += 1
        return request
    }

    private fun snapshotForTrigger(
        trigger: IntegrityStartupCheckTrigger
    ): StartupIntegrityLifecycleSnapshot {
        return if (trigger == IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART) {
            StartupIntegrityLifecycleSnapshot(
                restartReason = currentRestartReason,
                autoRecoveryAttempt = currentAutoRecoveryAttempt,
                rebuildSource = currentRebuildSource,
                recoverySignal = currentRecoverySignal
            )
        } else {
            StartupIntegrityLifecycleSnapshot(
                restartReason = null,
                autoRecoveryAttempt = null,
                rebuildSource = null,
                recoverySignal = null
            )
        }
    }
}
