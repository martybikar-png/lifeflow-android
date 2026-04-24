package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtectedRuntimeRebuildExecutorInstrumentedTest {

    @Test
    fun executor_runs_explicitRecoveryPlan_through_prepare_and_restart() {
        val events = mutableListOf<String>()

        val plan = RuntimeSecurityRecoveryController.planExplicitSecurityRecoveryRequest(
            logMessage = "Manual runtime recovery",
            automaticRuntimeSecurityRebuildsUsedInProcess = 0
        )

        val request = checkNotNull(plan.rebuildRequest)

        val executor = ProtectedRuntimeRebuildExecutor(
            rebuildCoordinator = ProtectedRuntimeRebuildCoordinator(),
            prepareRestartAndTeardown = { preparedRequest, reason, startupAutoRecoveryAttempt ->
                events += "prepare"
                events += "reason=${reason.name}"
                events += "source=${preparedRequest.rebuildSource.name}"
                events += "signal=${preparedRequest.recoverySignal}"
                events += "attempt=${startupAutoRecoveryAttempt ?: "null"}"
            },
            restart = {
                events += "restart"
                true
            },
            logInfo = { message ->
                events += "info=$message"
            },
            logWarning = { message ->
                events += "warning=$message"
            }
        )

        val result = executor.execute(request)

        assertTrue(result)
        assertTrue(events.contains("prepare"))
        assertTrue(events.contains("restart"))
        assertTrue(events.contains("reason=PROTECTED_RUNTIME_REBUILD"))
        assertTrue(events.contains("source=EXPLICIT_SECURITY_RECOVERY"))
        assertTrue(events.contains("signal=EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST"))
    }

    @Test
    fun executor_runs_runtimeSecuritySignalPlan_through_prepare_and_restart() {
        val events = mutableListOf<String>()

        val plan = RuntimeSecurityRecoveryController.planOnProtectedAccess(
            quickHardeningCompromised = true,
            trustCompromised = false,
            automaticRuntimeSecurityRebuildsUsedInProcess = 0
        )

        val request = checkNotNull(plan.rebuildRequest)

        val executor = ProtectedRuntimeRebuildExecutor(
            rebuildCoordinator = ProtectedRuntimeRebuildCoordinator(),
            prepareRestartAndTeardown = { preparedRequest, reason, startupAutoRecoveryAttempt ->
                events += "prepare"
                events += "reason=${reason.name}"
                events += "source=${preparedRequest.rebuildSource.name}"
                events += "signal=${preparedRequest.recoverySignal}"
                events += "attempt=${startupAutoRecoveryAttempt ?: "null"}"
            },
            restart = {
                events += "restart"
                true
            },
            logInfo = { message ->
                events += "info=$message"
            },
            logWarning = { message ->
                events += "warning=$message"
            }
        )

        val result = executor.execute(request)

        assertTrue(result)
        assertTrue(events.contains("prepare"))
        assertTrue(events.contains("restart"))
        assertTrue(events.contains("reason=PROTECTED_RUNTIME_REBUILD"))
        assertTrue(events.contains("source=RUNTIME_SECURITY_SIGNAL"))
        assertTrue(events.contains("signal=QUICK_HARDENING_COMPROMISE_ON_PROTECTED_ACCESS"))
    }

    @Test
    fun explicitRecoveryPlan_budgetExhausted_skipsBeforeExecutor() {
        val plan = RuntimeSecurityRecoveryController.planExplicitSecurityRecoveryRequest(
            logMessage = "Manual runtime recovery",
            automaticRuntimeSecurityRebuildsUsedInProcess = 1
        )

        assertEquals(null, plan.rebuildRequest)
        assertTrue(
            checkNotNull(plan.skipMessage)
                .contains("budget exhausted", ignoreCase = true)
        )
    }
}
