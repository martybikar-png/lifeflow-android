package com.lifeflow

import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.TrustState
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelTrustStateTest : MainViewModelTestBase() {

    @Test
    fun `degraded trust before authentication fails closed and does not enter authenticated`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "pre-auth degraded must fail closed"
        )

        val viewModel = newViewModel()

        try {
            runMain()

            viewModel.onAuthenticationSuccess()
            runMain()

            assertTrue(viewModel.uiState.value is UiState.Error)
            assertTrue(viewModel.uiState.value !is UiState.Authenticated)
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            runMain()
        }
    }

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
            runMain()

            viewModel.onAuthenticationSuccess()
            runMain()

            viewModel.refreshMetricsAndTwinNow()
            runMain()

            assertNotNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            forceResetSecurityState(
                state = TrustState.DEGRADED,
                reason = "degraded after auth must re-lock session"
            )
            runMain()

            assertTrue(viewModel.uiState.value is UiState.Error)
            assertTrue(viewModel.uiState.value !is UiState.Authenticated)
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            runMain()
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
            runMain()

            viewModel.onAuthenticationSuccess()
            runMain()

            viewModel.refreshMetricsAndTwinNow()
            runMain()

            assertNotNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            forceResetSecurityState(
                state = TrustState.COMPROMISED,
                reason = "compromised after auth must fail closed"
            )
            runMain()

            assertTrue(viewModel.uiState.value is UiState.Error)
            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Security compromised"))
            assertTrue(error.message.contains("Reset vault is required"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            runMain()
        }
    }
}