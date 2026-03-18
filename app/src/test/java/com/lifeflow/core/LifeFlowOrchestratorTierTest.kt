package com.lifeflow.core

import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifeFlowOrchestratorTierTest {

    @Before
    fun resetSecurityBaseline() {
        resetSecurityBaselineForLifeFlowOrchestratorTests()
    }

    private fun newFreeTierOrchestrator(): LifeFlowOrchestrator {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0
        )
        return LifeFlowOrchestrator(
            identityRepository = FakeIdentityRepository(),
            digitalTwinOrchestrator = com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator(
                com.lifeflow.domain.core.digitaltwin.DigitalTwinEngine()
            ),
            getHealthConnectStatus = com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase(repo),
            getHealthPermissions = com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase(repo),
            getGrantedHealthPermissions = com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase(repo),
            getStepsLast24h = com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase(repo),
            getAvgHeartRateLast24h = com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase(repo),
            tierManager = TierManager { TierState.FREE }
        )
    }

    @Test
    fun `FREE tier blocks bootstrapIdentityIfNeeded`() {
        val orchestrator = newFreeTierOrchestrator()
        val result = runSuspendTest { orchestrator.bootstrapIdentityIfNeeded() }
        assertTrue(result is ActionResult.Locked)
    }

    @Test
    fun `FREE tier blocks refreshWellbeingSnapshot`() {
        val orchestrator = newFreeTierOrchestrator()
        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = false)
        }
        assertTrue(result is ActionResult.Locked)
    }

    @Test
    fun `CORE tier is default and allows operations`() {
        val orchestrator = newTestLifeFlowOrchestrator()
        assertEquals(TierState.CORE, orchestrator.currentTier())
    }
}
