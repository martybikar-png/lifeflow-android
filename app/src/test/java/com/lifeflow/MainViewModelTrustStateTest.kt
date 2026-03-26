package com.lifeflow

import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.TrustState
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
            settleMain()

            viewModel.onAuthenticationSuccess()
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
    fun `compromised trust before authentication fails closed and requires vault reset`() {
        SecurityAccessSession.grantDefault()
        forceResetSecurityState(
            state = TrustState.COMPROMISED,
            reason = "pre-auth compromised must fail closed"
        )

        val viewModel = newViewModel()

        try {
            settleMain()

            val error = viewModel.uiState.value as UiState.Error
            assertTrue(error.message.contains("Security compromised"))
            assertTrue(error.message.contains("Reset vault is required"))
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
}
