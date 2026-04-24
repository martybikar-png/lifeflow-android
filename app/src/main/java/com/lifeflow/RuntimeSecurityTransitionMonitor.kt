package com.lifeflow

import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Observes runtime trust-state transitions outside startup / protected-entry checks.
 *
 * Purpose:
 * - react to real runtime compromise transitions, not only access-time checks
 * - keep trust-state monitoring out of LifeFlowAppRuntime branching
 * - provide a closeable lifecycle aligned with runtime rebuild / shutdown
 */
internal class RuntimeSecurityTransitionMonitor private constructor(
    private val closeAction: () -> Unit
) : AutoCloseable {

    override fun close() {
        closeAction()
    }

    companion object {
        fun start(
            onTrustCompromisedTransition: () -> Unit
        ): RuntimeSecurityTransitionMonitor {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

            val job = scope.launch {
                var initialized = false
                var previousState: TrustState? = null

                SecurityRuleEngine.trustState.collect { currentState ->
                    val previous = previousState
                    previousState = currentState

                    if (!initialized) {
                        initialized = true
                        return@collect
                    }

                    if (previous != TrustState.COMPROMISED &&
                        currentState == TrustState.COMPROMISED
                    ) {
                        onTrustCompromisedTransition()
                    }
                }
            }

            return RuntimeSecurityTransitionMonitor {
                job.cancel()
                scope.cancel()
            }
        }
    }
}
