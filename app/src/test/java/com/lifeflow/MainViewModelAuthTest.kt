package com.lifeflow

import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelAuthTest : MainViewModelTestBase() {

    @Test
    fun `onAuthenticationSuccess returns error when auth session is missing`() {
        val viewModel = newViewModel()

        try {
            viewModel.onAuthenticationSuccess()
            settleMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Active auth session missing"))
            assertNull(viewModel.digitalTwinState.value)
            assertNull(viewModel.wellbeingAssessment.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `onAuthenticationSuccess moves to authenticated when session exists`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "healthy auth path"
        )

        val viewModel = newViewModel()

        try {
            settleMain()

            viewModel.onAuthenticationSuccess()
            settleMain()

            assertTrue(viewModel.uiState.value is UiState.Authenticated)
            assertNull(viewModel.wellbeingAssessment.value)
            assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }

    @Test
    fun `onAuthenticationError clears session wipes caches and exposes error`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "healthy auth error path"
        )

        val viewModel = newViewModel(
            wellbeingRepo = FakeWellbeingRepository(
                sdkStatusValue = WellbeingRepository.SdkStatus.Available,
                requiredPermissionsValue = setOf(stepsPermission, heartRatePermission),
                grantedPermissionsValue = setOf(stepsPermission, heartRatePermission),
                stepsValue = 2222L,
                avgHeartRateValue = 70.0
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

            viewModel.onAuthenticationError("Biometric authentication failed")
            settleMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Biometric authentication failed"))
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
    fun `resetVault success moves ui to recovery error and invokes reset action`() {
        var resetCalls = 0

        val viewModel = newViewModel(
            performVaultReset = {
                resetCalls++
            }
        )

        try {
            viewModel.resetVault()
            settleMain()

            assertEquals(1, resetCalls)
            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Vault reset complete"))
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
    fun `resetVault failure exposes failure message`() {
        val viewModel = newViewModel(
            performVaultReset = {
                throw IllegalStateException("reset exploded")
            }
        )

        try {
            viewModel.resetVault()
            settleMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Vault reset failed"))
            assertTrue(error.message.contains("reset exploded"))
            assertNull(viewModel.digitalTwinState.value)
            assertNull(viewModel.wellbeingAssessment.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            settleMain()
        }
    }
}
