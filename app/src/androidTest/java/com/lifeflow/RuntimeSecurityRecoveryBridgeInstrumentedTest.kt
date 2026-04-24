package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.requireNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeSecurityRecoveryBridgeInstrumentedTest {

    @Test
    fun protectedAccess_bridgeDelegatesQuickHardeningSignal() {
        var capturedPlan: RuntimeSecurityRecoveryPlan? = null

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            val plan = planner(0)
            capturedPlan = plan
            plan.rebuildRequest != null
        }

        val result = bridge.onProtectedAccess(
            quickHardeningCompromised = true,
            trustCompromised = false
        )

        assertTrue(result)

        val request = requireNotNull(capturedPlan?.rebuildRequest)
        assertEquals(
            "QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS",
            request.recoverySignal
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.rebuildSource
        )
    }

    @Test
    fun trustCompromisedTransition_bridgeDelegatesTransitionSignal() {
        var capturedPlan: RuntimeSecurityRecoveryPlan? = null

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            val plan = planner(0)
            capturedPlan = plan
            plan.rebuildRequest != null
        }

        val result = bridge.onTrustCompromisedTransition()

        assertTrue(result)

        val request = requireNotNull(capturedPlan?.rebuildRequest)
        assertEquals(
            "TRUST_COMPROMISED_TRANSITION",
            request.recoverySignal
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.rebuildSource
        )
    }

    @Test
    fun explicitRecovery_bridgeDelegatesExplicitSignal() {
        var capturedPlan: RuntimeSecurityRecoveryPlan? = null

        val bridge = RuntimeSecurityRecoveryBridge { planner ->
            val plan = planner(0)
            capturedPlan = plan
            plan.rebuildRequest != null
        }

        val result = bridge.onExplicitSecurityRecoveryRequest(
            logMessage = "Manual runtime recovery"
        )

        assertTrue(result)

        val request = requireNotNull(capturedPlan?.rebuildRequest)
        assertEquals(
            "EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST",
            request.recoverySignal
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.EXPLICIT_SECURITY_RECOVERY,
            request.rebuildSource
        )
    }
}
