package com.lifeflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.requireNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeSecurityRecoveryControllerInstrumentedTest {

    @Test
    fun protectedAccessTrustCompromise_createsRuntimeSecuritySignalRebuildRequest() {
        val plan = RuntimeSecurityRecoveryController.planOnProtectedAccess(
            quickHardeningCompromised = false,
            trustCompromised = true,
            automaticRuntimeSecurityRebuildsUsedInProcess = 0
        )

        assertNull(plan.skipMessage)

        val request = requireNotNull(plan.rebuildRequest)
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            request.reason
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.RUNTIME_SECURITY_SIGNAL,
            request.rebuildSource
        )
        assertEquals(
            "TRUST_COMPROMISED_ON_PROTECTED_ACCESS",
            request.recoverySignal
        )
        assertTrue(
            requireNotNull(request.logMessage)
                .contains("compromised trust state", ignoreCase = true)
        )
    }

    @Test
    fun exhaustedAutomaticBudget_skipsRuntimeSecurityRecovery() {
        val plan = RuntimeSecurityRecoveryController.planOnProtectedAccess(
            quickHardeningCompromised = true,
            trustCompromised = false,
            automaticRuntimeSecurityRebuildsUsedInProcess = 1
        )

        assertNull(plan.rebuildRequest)
        assertTrue(
            requireNotNull(plan.skipMessage)
                .contains("budget exhausted", ignoreCase = true)
        )
    }

    @Test
    fun explicitSecurityRecovery_usesExplicitSourceAndFallbackMessage() {
        val plan = RuntimeSecurityRecoveryController.planExplicitSecurityRecoveryRequest(
            logMessage = "   ",
            automaticRuntimeSecurityRebuildsUsedInProcess = 0
        )

        val request = requireNotNull(plan.rebuildRequest)
        assertEquals(
            IntegrityRuntimeRestartReason.PROTECTED_RUNTIME_REBUILD,
            request.reason
        )
        assertEquals(
            ProtectedRuntimeRebuildSource.EXPLICIT_SECURITY_RECOVERY,
            request.rebuildSource
        )
        assertEquals(
            "EXPLICIT_RUNTIME_SECURITY_RECOVERY_REQUEST",
            request.recoverySignal
        )
        assertTrue(
            requireNotNull(request.logMessage)
                .contains("explicit runtime security recovery", ignoreCase = true)
        )
    }
}
