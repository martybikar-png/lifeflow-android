package com.lifeflow

/**
 * Shared bridge for all non-startup security recovery triggers.
 *
 * Purpose:
 * - keep LifeFlowAppRuntime free from multiple parallel recovery entrypoints
 * - centralize runtime recovery admission into one place
 * - make future runtime security triggers plug into one bridge
 */
internal class RuntimeSecurityRecoveryBridge(
    private val executeBudgetedRecovery: ((Int) -> RuntimeSecurityRecoveryPlan) -> Boolean
) {
    fun onProtectedAccess(
        quickHardeningCompromised: Boolean,
        trustCompromised: Boolean
    ): Boolean {
        return executeBudgetedRecovery { used ->
            RuntimeSecurityRecoveryController.planOnProtectedAccess(
                quickHardeningCompromised = quickHardeningCompromised,
                trustCompromised = trustCompromised,
                automaticRuntimeSecurityRebuildsUsedInProcess = used
            )
        }
    }

    fun onTrustCompromisedTransition(): Boolean {
        return executeBudgetedRecovery { used ->
            RuntimeSecurityRecoveryController.planOnTrustCompromisedTransition(
                automaticRuntimeSecurityRebuildsUsedInProcess = used
            )
        }
    }

    fun onExplicitSecurityRecoveryRequest(
        logMessage: String
    ): Boolean {
        return executeBudgetedRecovery { used ->
            RuntimeSecurityRecoveryController.planExplicitSecurityRecoveryRequest(
                logMessage = logMessage,
                automaticRuntimeSecurityRebuildsUsedInProcess = used
            )
        }
    }
}
