package com.lifeflow

/**
 * Startup recovery planner for integrity-driven automatic rebuilds.
 *
 * Purpose:
 * - keep startup recovery budgeting out of LifeFlowAppRuntime
 * - decide whether an automatic protected-runtime rebuild may happen
 * - produce a reusable ProtectedRuntimeRebuildRequest when allowed
 */
internal data class IntegrityStartupRecoveryPlan(
    val rebuildRequest: ProtectedRuntimeRebuildRequest? = null,
    val skipMessage: String? = null
)

internal object IntegrityStartupRecoveryController {

    fun plan(
        trigger: IntegrityStartupCheckTrigger,
        decision: IntegrityStartupRecoveryDecision,
        automaticProtectedRuntimeRebuildsUsedInProcess: Int
    ): IntegrityStartupRecoveryPlan {
        if (decision.action != IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD) {
            return IntegrityStartupRecoveryPlan()
        }

        if (!IntegrityStartupRecoveryBudget.shouldAllowAutomaticProtectedRuntimeRebuild(
                trigger = trigger,
                decision = decision,
                automaticProtectedRuntimeRebuildsUsedInProcess =
                    automaticProtectedRuntimeRebuildsUsedInProcess
            )
        ) {
            return IntegrityStartupRecoveryPlan(
                skipMessage = IntegrityStartupRecoveryBudget.exhaustedMessage(
                    trigger = trigger,
                    used = automaticProtectedRuntimeRebuildsUsedInProcess
                )
            )
        }

        val nextAttempt =
            IntegrityStartupRecoveryBudget.nextAutomaticProtectedRuntimeRebuildAttempt(
                automaticProtectedRuntimeRebuildsUsedInProcess
            )

        return IntegrityStartupRecoveryPlan(
            rebuildRequest = ProtectedRuntimeRebuildRequest(
                reason = decision.restartReason
                    ?: IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
                logMessage = decision.logMessage,
                startupAutoRecoveryAttempt = nextAttempt
            )
        )
    }
}
