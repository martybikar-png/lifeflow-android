package com.lifeflow

/**
 * Budget guard for automatic startup-triggered protected-runtime rebuilds.
 *
 * Purpose:
 * - allow one controlled automatic rebuild after a cold-start integrity problem
 * - prevent accidental re-entry loops or request storms in a single process
 * - keep budgeting semantics out of LifeFlowAppRuntime branching
 */
internal object IntegrityStartupRecoveryBudget {

    private const val MAX_AUTOMATIC_PROTECTED_RUNTIME_REBUILDS_PER_PROCESS = 1

    fun shouldAllowAutomaticProtectedRuntimeRebuild(
        trigger: IntegrityStartupCheckTrigger,
        decision: IntegrityStartupRecoveryDecision,
        automaticProtectedRuntimeRebuildsUsedInProcess: Int
    ): Boolean {
        if (decision.action != IntegrityStartupRecoveryAction.REQUEST_PROTECTED_RUNTIME_REBUILD) {
            return false
        }

        if (trigger != IntegrityStartupCheckTrigger.APPLICATION_COLD_START) {
            return false
        }

        return automaticProtectedRuntimeRebuildsUsedInProcess <
            MAX_AUTOMATIC_PROTECTED_RUNTIME_REBUILDS_PER_PROCESS
    }

    fun nextAutomaticProtectedRuntimeRebuildAttempt(
        automaticProtectedRuntimeRebuildsUsedInProcess: Int
    ): Int {
        require(
            automaticProtectedRuntimeRebuildsUsedInProcess <
                MAX_AUTOMATIC_PROTECTED_RUNTIME_REBUILDS_PER_PROCESS
        ) {
            "Automatic protected runtime rebuild budget exhausted."
        }

        return automaticProtectedRuntimeRebuildsUsedInProcess + 1
    }

    fun exhaustedMessage(
        trigger: IntegrityStartupCheckTrigger,
        used: Int
    ): String {
        return "Automatic protected runtime rebuild skipped for ${trigger.name}: " +
            "budget exhausted ($used/$MAX_AUTOMATIC_PROTECTED_RUNTIME_REBUILDS_PER_PROCESS)."
    }
}
