package com.lifeflow

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.boundary.MainBoundarySnapshot
import com.lifeflow.core.HealthConnectUiState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.SecurityVaultResetAuthorization
import com.lifeflow.security.TrustState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActiveRuntimeActionsInstrumentedTest {

    private lateinit var viewModel: RecordingActiveRuntimeViewModel
    private var lastActionMessage: String? = null

    @Before
    fun setUp() {
        viewModel = RecordingActiveRuntimeViewModel()
        lastActionMessage = null
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "ActiveRuntimeActionsInstrumentedTest baseline"
        )
    }

    @After
    fun tearDown() {
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "ActiveRuntimeActionsInstrumentedTest cleanup"
        )
    }

    @Test
    fun completeActiveRuntimeVaultResetAuthorization_success_updatesLastAction_and_callsResetVault() {
        completeActiveRuntimeVaultResetAuthorization(
            grantAuthorization = {},
            successMessage = "Vault reset auth-per-use authentication succeeded",
            viewModel = viewModel,
            setLastAction = { lastActionMessage = it }
        )

        assertEquals(
            "Vault reset auth-per-use authentication succeeded",
            lastActionMessage
        )
        assertTrue(viewModel.resetVaultCalled)
        assertNull(viewModel.authenticationErrorMessage)
    }

    @Test
    fun completeActiveRuntimeVaultResetAuthorization_failure_updatesError_and_doesNotCallResetVault() {
        completeActiveRuntimeVaultResetAuthorization(
            grantAuthorization = {
                throw SecurityException("Vault reset denied: test authorization failure.")
            },
            successMessage = "unused",
            viewModel = viewModel,
            setLastAction = { lastActionMessage = it }
        )

        assertEquals(
            "Vault reset authentication failed: Vault reset denied: test authorization failure.",
            lastActionMessage
        )
        assertEquals(
            "Vault reset denied: test authorization failure.",
            viewModel.authenticationErrorMessage
        )
        assertTrue(!viewModel.resetVaultCalled)
    }

    private fun forceResetSecurityState(
        state: TrustState,
        reason: String
    ) {
        val method = SecurityRuleEngine::class.java.declaredMethods.firstOrNull { candidate ->
            candidate.name.startsWith("forceResetForAdversarialSuite") &&
                candidate.parameterTypes.size == 2 &&
                candidate.parameterTypes[0] == TrustState::class.java &&
                candidate.parameterTypes[1] == String::class.java
        } ?: throw AssertionError(
            buildString {
                append("Could not find compatible forceResetForAdversarialSuite method on SecurityRuleEngine. Available methods: ")
                append(SecurityRuleEngine::class.java.declaredMethods.joinToString { it.name })
            }
        )

        method.isAccessible = true
        method.invoke(SecurityRuleEngine, state, reason)
    }

    private class RecordingActiveRuntimeViewModel : ActiveRuntimeViewModelContract {
        override val uiState: State<UiState> = mutableStateOf(UiState.Loading)
        override val lastAction: State<String> = mutableStateOf("")
        override val freeTierMessage: State<String> = mutableStateOf("")
        override val healthConnectState: State<HealthConnectUiState> =
            unusedState("healthConnectState")
        override val requiredHealthPermissions: State<Set<String>> =
            mutableStateOf(emptySet())
        override val grantedHealthPermissions: State<Set<String>> =
            mutableStateOf(emptySet())
        override val healthPermissionsInitError: State<String?> =
            mutableStateOf(null)
        override val digitalTwinState: State<DigitalTwinState?> =
            mutableStateOf(null)
        override val wellbeingAssessment: State<WellbeingAssessment?> =
            mutableStateOf(null)
        override val boundarySnapshot: State<MainBoundarySnapshot> =
            mutableStateOf(MainBoundarySnapshot.initial())

        var resetVaultCalled: Boolean = false
        var authenticationErrorMessage: String? = null

        override fun refreshMetricsAndTwinNow() = Unit

        override fun onHealthPermissionsResult(granted: Set<String>) = Unit

        override fun onAuthenticationSuccess() = Unit

        override fun onAuthenticationError(message: String) {
            authenticationErrorMessage = message
        }

        override fun onAppBackgrounded() = Unit

        override fun onAppForegrounded() = Unit

        override fun resetVault() {
            resetVaultCalled = true
        }

        override fun isSessionAuthorizedForUi(): Boolean = false

        private companion object {
            private fun <T> unusedState(name: String): State<T> =
                object : State<T> {
                    override val value: T
                        get() = error("$name is not used in this test")
                }
        }
    }
}
