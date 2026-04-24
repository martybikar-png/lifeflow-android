package com.lifeflow

import com.lifeflow.security.TrustState

/**
 * Owns runtime security surveillance concerns:
 * - protected-access compromise evaluation
 * - trust-compromised transition monitor lifecycle
 *
 * Purpose:
 * - keep LifeFlowAppRuntime free from surveillance-specific wiring
 * - route all runtime security surveillance through one coordinator
 */
internal class RuntimeSecuritySurveillanceCoordinator(
    private val runtimeSecurityRecoveryBridge: RuntimeSecurityRecoveryBridge,
    private val quickHardeningCompromised: () -> Boolean,
    private val currentTrustState: () -> TrustState,
    private val logWarning: (String) -> Unit,
    private val startMonitor: ((() -> Unit) -> RuntimeSecurityTransitionMonitor) = { callback ->
        RuntimeSecurityTransitionMonitor.start(
            onTrustCompromisedTransition = callback
        )
    }
) {

    private var transitionMonitor: RuntimeSecurityTransitionMonitor? = null

    fun onProtectedAccessIfNeeded(
        startupInitialized: Boolean,
        hardeningReportAvailable: Boolean
    ) {
        if (!startupInitialized || !hardeningReportAvailable) {
            return
        }

        runtimeSecurityRecoveryBridge.onProtectedAccess(
            quickHardeningCompromised = quickHardeningCompromised(),
            trustCompromised = currentTrustState() == TrustState.COMPROMISED
        )
    }

    fun startForRuntime(
        isInstrumentation: Boolean
    ) {
        stop()

        if (isInstrumentation) {
            return
        }

        transitionMonitor = startMonitor {
            val rebuilt = runtimeSecurityRecoveryBridge.onTrustCompromisedTransition()

            if (!rebuilt) {
                logWarning(
                    "Trust-state transition to COMPROMISED did not start a protected runtime rebuild."
                )
            }
        }
    }

    fun stop() {
        transitionMonitor?.close()
        transitionMonitor = null
    }
}
