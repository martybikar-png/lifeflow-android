package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeSecuritySurveillanceCoordinatorInstrumentedTest {

    @Test
    fun protectedAccess_isIgnoredWhenRuntimeIsNotReady() {
        var plannerCalls = 0

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            plannerCalls += 1
            planner(0)
            false
        }

        val coordinator = RuntimeSecuritySurveillanceCoordinator(
            runtimeSecurityRecoveryBridge = bridge,
            quickHardeningCompromised = { true },
            currentTrustState = { TrustState.COMPROMISED },
            logWarning = {},
            startMonitor = { error("startMonitor must not be called here") }
        )

        coordinator.onProtectedAccessIfNeeded(
            startupInitialized = false,
            hardeningReportAvailable = true
        )
        coordinator.onProtectedAccessIfNeeded(
            startupInitialized = true,
            hardeningReportAvailable = false
        )

        assertEquals(0, plannerCalls)
    }

    @Test
    fun protectedAccess_delegatesRecoveryPlanning_whenRuntimeIsReady() {
        var capturedPlan: RuntimeSecurityRecoveryPlan? = null

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            val plan = planner(0)
            capturedPlan = plan
            plan.rebuildRequest != null
        }

        val coordinator = RuntimeSecuritySurveillanceCoordinator(
            runtimeSecurityRecoveryBridge = bridge,
            quickHardeningCompromised = { true },
            currentTrustState = { TrustState.COMPROMISED },
            logWarning = {},
            startMonitor = { error("startMonitor must not be called here") }
        )

        coordinator.onProtectedAccessIfNeeded(
            startupInitialized = true,
            hardeningReportAvailable = true
        )

        val request = requireNotNull(capturedPlan?.rebuildRequest)
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            request.reason
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.rebuildSource
        )
    }

    @Test
    fun instrumentationMode_doesNotStartTransitionMonitor() {
        var startCalls = 0

        val bridge = RuntimeSecurityRecoveryBridge { false }

        val coordinator = RuntimeSecuritySurveillanceCoordinator(
            runtimeSecurityRecoveryBridge = bridge,
            quickHardeningCompromised = { false },
            currentTrustState = { TrustState.DEGRADED },
            logWarning = {},
            startMonitor = {
                startCalls += 1
                error("Instrumentation mode must not start transition monitor")
            }
        )

        coordinator.startForRuntime(isInstrumentation = true)
        coordinator.stop()

        assertEquals(0, startCalls)
    }
}
