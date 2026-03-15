package com.lifeflow.core

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LifeFlowOrchestratorMetricDataStateTest {

    @Before
    fun resetSecurityBaseline() {
        resetSecurityBaselineForLifeFlowOrchestratorTests()
    }

    @Test
    fun `keeps zero steps as OK and maps missing heart rate to NO_DATA`() {
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                stepsValue = 0L,
                avgHeartRateValue = null
            )
        )

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(0L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `returns NO_DATA for both metrics when permissions are granted but no usable data exists`() {
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                stepsValue = -1L,
                avgHeartRateValue = 0.0
            )
        )

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `maps metric read failures to NO_DATA when permissions are granted`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 5000L,
            avgHeartRateValue = 65.0,
            throwsOnReadSteps = true,
            throwsOnReadHeartRate = true
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.NO_DATA, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(1, repo.readTotalStepsCalls)
        assertEquals(1, repo.readAvgHeartRateCalls)
    }
}