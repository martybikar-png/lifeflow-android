package com.lifeflow

import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelSessionAndRefreshTest : MainViewModelTestBase() {

    @Test
    fun `refreshMetricsAndTwinNow updates health state permissions and digital twin after auth`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "healthy refresh path"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 4321L,
                avgHeartRateValue = 71.6
            )
        )

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Authenticated)
            assertEquals(HealthConnectUiState.Available, viewModel.healthConnectState.value)
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.grantedHealthPermissions.value
            )

            val twin = viewModel.digitalTwinState.value
            assertNotNull(twin)
            assertTrue(twin!!.identityInitialized)
            assertEquals(4321L, twin.stepsLast24h)
            assertEquals(72L, twin.avgHeartRateLast24h)
            assertEquals(DigitalTwinState.Availability.OK, twin.stepsAvailability)
            assertEquals(DigitalTwinState.Availability.OK, twin.heartRateAvailability)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `refreshMetricsAndTwinNow without auth refreshes public health state but keeps protected data hidden`() {
        SecurityAccessSession.clear()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "unauthenticated refresh should stay fail closed"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 9876L,
                avgHeartRateValue = 63.4
            )
        )

        try {
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Loading)
            assertEquals(HealthConnectUiState.Available, viewModel.healthConnectState.value)
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.requiredHealthPermissions.value
            )
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertNull(viewModel.digitalTwinState.value)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `onHealthPermissionsResult without auth keeps protected data hidden`() {
        SecurityAccessSession.clear()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "unauthenticated permission callback should stay fail closed"
        )

        val wellbeingRepo = FakeWellbeingRepository(
            sdkStatusValue = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 6543L,
            avgHeartRateValue = 62.8
        )

        val viewModel = newViewModel(wellbeingRepo = wellbeingRepo)

        try {
            settleMain()

            viewModel.onHealthPermissionsResult(setOf(stepsPermission, heartRatePermission))
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Loading)
            assertEquals(HealthConnectUiState.Available, viewModel.healthConnectState.value)
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.requiredHealthPermissions.value
            )
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertNull(viewModel.digitalTwinState.value)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `onHealthPermissionsResult refreshes unified wellbeing snapshot after auth`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "permission callback should use orchestrated snapshot"
        )

        val wellbeingRepo = FakeWellbeingRepository(
            sdkStatusValue = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = emptySet(),
            stepsValue = 3210L,
            avgHeartRateValue = 66.2
        )

        val viewModel = newViewModel(wellbeingRepo = wellbeingRepo)

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertNull(viewModel.digitalTwinState.value)

            wellbeingRepo.grantedPermissionsValue = setOf(stepsPermission, heartRatePermission)

            viewModel.onHealthPermissionsResult(setOf(stepsPermission, heartRatePermission))
            settleMain()

            assertEquals(HealthConnectUiState.Available, viewModel.healthConnectState.value)
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.requiredHealthPermissions.value
            )
            assertEquals(
                setOf(stepsPermission, heartRatePermission),
                viewModel.grantedHealthPermissions.value
            )

            val twin = viewModel.digitalTwinState.value
            assertNotNull(twin)
            assertTrue(twin!!.identityInitialized)
            assertEquals(3210L, twin.stepsLast24h)
            assertEquals(66L, twin.avgHeartRateLast24h)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `refreshMetricsAndTwinNow recomputes twin and does not keep stale metric values when subsequent read fails`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "healthy twin refresh before induced failure"
        )

        val wellbeingRepo = FakeWellbeingRepository(
            sdkStatusValue = WellbeingRepository.SdkStatus.Available,
            requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
            grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
            stepsValue = 4321L,
            avgHeartRateValue = 71.6
        )

        val viewModel = newViewModel(wellbeingRepo = wellbeingRepo)

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            val initialTwin = viewModel.digitalTwinState.value
            assertNotNull(initialTwin)
            assertEquals(4321L, initialTwin!!.stepsLast24h)

            wellbeingRepo.throwsOnReadSteps = true

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            val updatedTwin = viewModel.digitalTwinState.value
            assertNotNull(updatedTwin)
            assertTrue(updatedTwin !== initialTwin)
            assertTrue(updatedTwin!!.stepsLast24h != 4321L)
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }
}