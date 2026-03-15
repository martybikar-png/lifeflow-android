package com.lifeflow.core

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifeFlowOrchestratorMetricResolutionTest {

    @Before
    fun resetSecurityBaseline() {
        resetSecurityBaselineForLifeFlowOrchestratorTests()
    }

    @Test
    fun `resolves steps granted and heart rate denied independently`() {
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                grantedPermissionsValue = setOf(testStepsPermission),
                stepsValue = 2500L,
                avgHeartRateValue = 68.0
            )
        )

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(true, snapshot.stepsPermissionGranted)
        assertEquals(false, snapshot.heartRatePermissionGranted)

        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(2500L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)

        assertTrue(
            "Expected diagnostic note for denied heart-rate permission.",
            state.notes.any { it.contains("permission denied", ignoreCase = true) }
        )
    }

    @Test
    fun `resolves steps denied and heart rate granted independently`() {
        val orchestrator = newTestLifeFlowOrchestrator(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatus = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
                grantedPermissionsValue = setOf(testHeartRatePermission),
                stepsValue = 2500L,
                avgHeartRateValue = 68.0
            )
        )

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(false, snapshot.stepsPermissionGranted)
        assertEquals(true, snapshot.heartRatePermissionGranted)

        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, state.stepsAvailability)
        assertNull(state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.OK, state.heartRateAvailability)
        assertEquals(68L, state.avgHeartRateLast24h)
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
    fun `resolves steps metric when only steps permission is required`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission),
            grantedPermissionsValue = setOf(testStepsPermission),
            stepsValue = 4321L,
            avgHeartRateValue = 77.0
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertEquals(true, snapshot.stepsPermissionGranted)
        assertNull(snapshot.heartRatePermissionGranted)

        assertEquals(DigitalTwinState.Availability.OK, state.stepsAvailability)
        assertEquals(4321L, state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.heartRateAvailability)
        assertNull(state.avgHeartRateLast24h)

        assertEquals(1, repo.readTotalStepsCalls)
        assertEquals(0, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `resolves heart rate metric when only heart rate permission is required`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testHeartRatePermission),
            grantedPermissionsValue = setOf(testHeartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.6
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        val state = snapshot.digitalTwinState
        assertNull(snapshot.stepsPermissionGranted)
        assertEquals(true, snapshot.heartRatePermissionGranted)

        assertEquals(DigitalTwinState.Availability.UNKNOWN, state.stepsAvailability)
        assertNull(state.stepsLast24h)

        assertEquals(DigitalTwinState.Availability.OK, state.heartRateAvailability)
        assertEquals(72L, state.avgHeartRateLast24h)

        assertEquals(0, repo.readTotalStepsCalls)
        assertEquals(1, repo.readAvgHeartRateCalls)
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