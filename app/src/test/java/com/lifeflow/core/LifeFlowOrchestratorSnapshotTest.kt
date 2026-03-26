package com.lifeflow.core

import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.ActivityLevel
import com.lifeflow.domain.wellbeing.HeartRateStatus
import com.lifeflow.domain.wellbeing.OverallReadiness
import com.lifeflow.domain.wellbeing.WellbeingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LifeFlowOrchestratorSnapshotTest {

    @Before
    fun resetSecurityBaseline() {
        resetSecurityBaselineForLifeFlowOrchestratorTests()
    }

    @Test
    fun `returns Error when required permissions lookup throws`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0,
            throwsOnRequiredPermissions = true
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = orchestrator.requiredHealthPermissionsSafe()

        when (result) {
            is ActionResult.Success ->
                throw AssertionError("Expected Error but got Success: ${result.value}")

            is ActionResult.Locked ->
                throw AssertionError("Expected Error but got Locked: ${result.reason}")

            is ActionResult.Error -> {
                assertTrue(
                    "Expected IllegalStateException in error message.",
                    result.message.contains("IllegalStateException")
                )
            }
        }
    }

    @Test
    fun `returns empty set when granted permissions lookup throws`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 1000L,
            avgHeartRateValue = 70.0,
            throwsOnGrantedPermissions = true
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest { orchestrator.grantedHealthPermissionsSafe() }

        when (result) {
            is ActionResult.Success -> {
                assertTrue(
                    "Expected deterministic empty set when granted permission lookup fails.",
                    result.value.isEmpty()
                )
            }

            is ActionResult.Locked ->
                throw AssertionError("Expected Success but got Locked: ${result.reason}")

            is ActionResult.Error ->
                throw AssertionError("Expected Success but got Error: ${result.message}")
        }
    }

    @Test
    fun `refreshWellbeingSnapshot returns unified snapshot when health connect is available and permissions are granted`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.6
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        assertEquals(HealthConnectUiState.Available, snapshot.healthConnectState)
        assertEquals(
            setOf(testStepsPermission, testHeartRatePermission),
            snapshot.requiredPermissions
        )
        assertEquals(
            setOf(testStepsPermission, testHeartRatePermission),
            snapshot.grantedPermissions
        )
        assertEquals(true, snapshot.stepsPermissionGranted)
        assertEquals(true, snapshot.heartRatePermissionGranted)

        val twin = snapshot.digitalTwinState
        assertTrue(twin.identityInitialized)
        assertEquals(4321L, twin.stepsLast24h)
        assertEquals(72L, twin.avgHeartRateLast24h)
        assertEquals(DigitalTwinState.Availability.OK, twin.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.OK, twin.heartRateAvailability)

        val wellbeing = snapshot.wellbeingAssessment
        assertEquals(ActivityLevel.LOW, wellbeing.activityLevel)
        assertEquals(HeartRateStatus.NORMAL, wellbeing.heartRateStatus)
        assertEquals(OverallReadiness.LOW, wellbeing.overallReadiness)
        assertTrue(wellbeing.notes.isEmpty())
    }

    @Test
    fun `refreshWellbeingSnapshot preserves permission sets but skips metric reads when health connect is unavailable`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.NotInstalled,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 9999L,
            avgHeartRateValue = 80.0
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        assertEquals(HealthConnectUiState.NotInstalled, snapshot.healthConnectState)
        assertEquals(
            setOf(testStepsPermission, testHeartRatePermission),
            snapshot.requiredPermissions
        )
        assertEquals(
            setOf(testStepsPermission, testHeartRatePermission),
            snapshot.grantedPermissions
        )
        assertNull(snapshot.stepsPermissionGranted)
        assertNull(snapshot.heartRatePermissionGranted)

        val twin = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.UNKNOWN, twin.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.UNKNOWN, twin.heartRateAvailability)
        assertNull(twin.stepsLast24h)
        assertNull(twin.avgHeartRateLast24h)

        val wellbeing = snapshot.wellbeingAssessment
        assertEquals(ActivityLevel.UNKNOWN, wellbeing.activityLevel)
        assertEquals(HeartRateStatus.UNKNOWN, wellbeing.heartRateStatus)
        assertEquals(OverallReadiness.INSUFFICIENT_DATA, wellbeing.overallReadiness)
        assertTrue(
            wellbeing.notes.any { it.contains("Not enough signals", ignoreCase = true) }
        )

        assertEquals(0, repo.readTotalStepsCalls)
        assertEquals(0, repo.readAvgHeartRateCalls)
    }

    @Test
    fun `refreshWellbeingSnapshot degrades to empty granted permissions and denied metrics when granted lookup fails`() {
        val repo = FakeWellbeingRepository(
            sdkStatus = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            grantedPermissionsValue = setOf(testStepsPermission, testHeartRatePermission),
            stepsValue = 5000L,
            avgHeartRateValue = 65.0,
            throwsOnGrantedPermissions = true
        )
        val orchestrator = newTestLifeFlowOrchestrator(repo)

        val result = runSuspendTest {
            orchestrator.refreshWellbeingSnapshot(identityInitialized = true)
        }

        val snapshot = assertWellbeingSnapshotSuccess(result)
        assertEquals(HealthConnectUiState.Available, snapshot.healthConnectState)
        assertEquals(
            setOf(testStepsPermission, testHeartRatePermission),
            snapshot.requiredPermissions
        )
        assertTrue(snapshot.grantedPermissions.isEmpty())
        assertEquals(false, snapshot.stepsPermissionGranted)
        assertEquals(false, snapshot.heartRatePermissionGranted)

        val twin = snapshot.digitalTwinState
        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, twin.stepsAvailability)
        assertEquals(DigitalTwinState.Availability.PERMISSION_DENIED, twin.heartRateAvailability)
        assertNull(twin.stepsLast24h)
        assertNull(twin.avgHeartRateLast24h)

        val wellbeing = snapshot.wellbeingAssessment
        assertEquals(ActivityLevel.NO_ACCESS, wellbeing.activityLevel)
        assertEquals(HeartRateStatus.NO_ACCESS, wellbeing.heartRateStatus)
        assertEquals(OverallReadiness.NO_ACCESS, wellbeing.overallReadiness)
        assertTrue(wellbeing.notes.isEmpty())

        assertEquals(0, repo.readTotalStepsCalls)
        assertEquals(0, repo.readAvgHeartRateCalls)
    }
}
