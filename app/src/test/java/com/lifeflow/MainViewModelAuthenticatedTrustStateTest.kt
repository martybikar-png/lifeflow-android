package com.lifeflow

import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.TrustState
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelAuthenticatedTrustStateTest : MainViewModelTestBase() {

    @Test
    fun `degraded trust while authenticated clears session caches and moves ui to error`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "authenticate before degrade"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 1234L,
                avgHeartRateValue = 65.0
            )
        )

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            assertNotNull(viewModel.digitalTwinState.value)
            assertNotNull(viewModel.wellbeingAssessment.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            forceResetSecurityState(
                state = TrustState.DEGRADED,
                reason = "degraded after auth must re-lock session"
            )
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Error)
            assertTrue(viewModel.uiState.value !is UiState.Authenticated)
            assertNull(viewModel.digitalTwinState.value)
            assertNull(viewModel.wellbeingAssessment.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `compromised trust while authenticated clears session caches and requires vault reset`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "authenticate before compromise"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 9876L,
                avgHeartRateValue = 72.0
            )
        )

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            viewModel.refreshMetricsAndTwinNow()
            settleMain()

            assertNotNull(viewModel.digitalTwinState.value)
            assertNotNull(viewModel.wellbeingAssessment.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            forceResetSecurityState(
                state = TrustState.COMPROMISED,
                reason = "compromised after auth must fail closed"
            )
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Error)
            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Security compromised"))
            assertTrue(error.message.contains("Reset vault is required"))
            assertNull(viewModel.digitalTwinState.value)
            assertNull(viewModel.wellbeingAssessment.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }
}
