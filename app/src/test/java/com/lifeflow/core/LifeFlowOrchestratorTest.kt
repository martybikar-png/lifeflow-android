package com.lifeflow.core

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifeFlowOrchestratorTest {

    @Before
    fun resetSecurityBaseline() {
        resetSecurityBaselineForLifeFlowOrchestratorTests()
    }

    @Test
    fun `returns UNKNOWN when Health Connect is not available`() {
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.NotInstalled,
                requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                stepsValue = 1234L,
                avgHeartRateValue = 72.0
            )
        )

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(HealthConnectUiState.NotInstalled, snapshot.healthConnectState)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `returns BLOCKED when identity is not initialized even if best effort metric reads succeed`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.0
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = false)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.BLOCKED, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.BLOCKED, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(1, repo.readTotalStepsCalls)
        assertEquals(1, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `returns UNKNOWN when required permission set is empty`() {
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = emptySet(),
                grantedPermissionsValue = emptySet(),
                stepsValue = 1234L,
                avgHeartRateValue = 72.0
            )
        )

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertTrue(snapshot.requiredPermissions.isEmpty())
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)
    }

    @Test
    fun `returns UNKNOWN for both metrics when permission snapshot cannot be resolved`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 5000L,
            avgHeartRateValue = 65.0,
            throwsOnRequiredPermissions = true
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.stepsLast24h)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(0, repo.readTotalStepsCalls)
        assertEquals(0, repo.readAvgHeartRateCalls)
    }
}