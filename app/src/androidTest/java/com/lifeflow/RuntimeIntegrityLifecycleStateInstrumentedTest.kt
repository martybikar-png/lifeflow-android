package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeIntegrityLifecycleStateInstrumentedTest {

    @Test
    fun coldStartPlan_defaultsToApplicationColdStart() {
        val state = RuntimeIntegrityLifecycleState()

        val plan = state.planStart(startupFailureMessage = null)

        assertEquals(
            IntegrityStartupCheckTrigger.APPLICATION_COLD_START,
            plan.trigger
        )
        assertNull(plan.restartReason)
        assertNull(plan.autoRecoveryAttempt)
        assertNull(plan.rebuildSource)
        assertNull(plan.recoverySignal)
    }

    @Test
    fun securityRestartIntent_survivesFailedStart_andBindsToExecutionRequest() {
        val state = RuntimeIntegrityLifecycleState()

        state.preparePendingRestart(
            reason = IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            autoRecoveryAttempt = 2,
            rebuildSource = ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            recoverySignal = "SIGNAL_A"
        )

        val firstPlan = state.planStart(startupFailureMessage = "ignored")
        assertEquals(
            IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART,
            firstPlan.trigger
        )
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            firstPlan.restartReason
        )
        assertEquals(2, firstPlan.autoRecoveryAttempt)
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            firstPlan.rebuildSource
        )
        assertEquals("SIGNAL_A", firstPlan.recoverySignal)

        state.markStartFailed()

        val secondPlan = state.planStart(
            startupFailureMessage = "Application startup failed"
        )
        assertEquals(
            IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART,
            secondPlan.trigger
        )
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            secondPlan.restartReason
        )
        assertEquals(2, secondPlan.autoRecoveryAttempt)
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            secondPlan.rebuildSource
        )
        assertEquals("SIGNAL_A", secondPlan.recoverySignal)

        state.commitStart(secondPlan)

        val appContext =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        val request = state.nextExecutionRequest(
            trigger = IntegrityStartupCheckTrigger.SECURITY_RUNTIME_RESTART,
            applicationContext = appContext
        )

        assertEquals(1, request.startupProcessSequence)
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            request.lifecycleSnapshot.restartReason
        )
        assertEquals(2, request.lifecycleSnapshot.autoRecoveryAttempt)
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.lifecycleSnapshot.rebuildSource
        )
        assertEquals(
            "SIGNAL_A",
            request.lifecycleSnapshot.recoverySignal
        )
        assertTrue(request.startupRequestId.isNotBlank())
        assertTrue(request.requestedAtEpochMs > 0L)
    }
}
