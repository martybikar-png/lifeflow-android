package com.lifeflow

import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MainViewModelAuthTest : MainViewModelTestBase() {

    @Test
    fun `onAuthenticationSuccess returns error when auth session is missing`() {
        val viewModel = newViewModel()

        try {
            viewModel.onAuthenticationSuccess()
            runMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Active auth session missing"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            runMain()
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
            runMain()

            viewModel.onAuthenticationSuccess()
            runMain()

            assertTrue(viewModel.uiState.value is UiState.Authenticated)
            assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())
        } finally {
            clearViewModel(viewModel)
            runMain()
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
            runMain()

            viewModel.onAuthenticationSuccess()
            runMain()

            viewModel.refreshMetricsAndTwinNow()
            runMain()

            viewModel.onAuthenticationError("Biometric authentication failed")

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Biometric authentication failed"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            runMain()
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
            runMain()

            assertEquals(1, resetCalls)
            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Vault reset complete"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
            assertTrue(!SecurityAccessSession.isAuthorized())
        } finally {
            clearViewModel(viewModel)
            runMain()
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
            runMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Vault reset failed"))
            assertTrue(error.message.contains("reset exploded"))
            assertNull(viewModel.digitalTwinState.value)
            assertTrue(viewModel.grantedHealthPermissions.value.isEmpty())
        } finally {
            clearViewModel(viewModel)
            runMain()
        }
    }
}