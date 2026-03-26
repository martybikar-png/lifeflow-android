package com.lifeflow

import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.TrustState
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelSessionExpiryTest : MainViewModelTestBase() {

    @Test
    fun `session expiry wipes caches and moves ui to error`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "healthy session expiry path"
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
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            SecurityAccessSession.clear()
            advanceMainTimeBy(1000)
            settleMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Session expired"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `app foreground check expires authenticated ui immediately when session is gone`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "foreground recheck should fail closed when session is gone"
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

            assertTrue(viewModel.uiState.value is UiState.Authenticated)
            assertNotNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isNotEmpty())

            SecurityAccessSession.clear()

            viewModel.onAppForegrounded()
            settleMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Session expired"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }
}
