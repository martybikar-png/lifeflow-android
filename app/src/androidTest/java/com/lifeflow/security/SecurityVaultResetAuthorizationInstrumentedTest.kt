package com.lifeflow.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityVaultResetAuthorizationInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "SecurityVaultResetAuthorizationInstrumentedTest baseline"
        )
    }

    @After
    fun tearDown() {
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "SecurityVaultResetAuthorizationInstrumentedTest cleanup"
        )
    }

    @Test
    fun grantFromVaultResetBiometricSuccess_allowsConsume_whenAuthorizedSessionExists() {
        SecurityAccessSession.grantDefault(context)

        SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
        SecurityVaultResetAuthorization.consumeFreshAuthorization(
            reason = "instrumented biometric consume"
        )

        assertTrue(SecurityAccessSession.isAuthorized())
    }

    @Test
    fun grantFromVaultResetAuthPerUseSuccess_allowsConsume_whenAuthorizedSessionExists() {
        SecurityAccessSession.grantDefault(context)

        SecurityVaultResetAuthorization.grantFromVaultResetAuthPerUseSuccess()
        SecurityVaultResetAuthorization.consumeFreshAuthorization(
            reason = "instrumented auth-per-use consume"
        )

        assertTrue(SecurityAccessSession.isAuthorized())
    }

    @Test
    fun grantFromVaultResetBiometricSuccess_deniesWithoutAuthorizedSession() {
        val error = runCatching {
            SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
        }.exceptionOrNull()

        assertNotNull(error)
        assertTrue(error is SecurityException)
        assertTrue(
            error?.message?.contains(
                "active auth session is required before reset authorization can be granted"
            ) == true
        )
    }

    @Test
    fun consumeFreshAuthorization_deniesWhenSessionWasClearedAfterGrant() {
        SecurityAccessSession.grantDefault(context)
        SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
        SecurityAccessSession.clear()

        val error = runCatching {
            SecurityVaultResetAuthorization.consumeFreshAuthorization(
                reason = "instrumented consume after session clear"
            )
        }.exceptionOrNull()

        assertNotNull(error)
        assertTrue(error is SecurityException)
        assertTrue(
            error?.message?.contains(
                "active auth session is required"
            ) == true
        )
    }

    @Test
    fun grantFromVaultResetAuthPerUseSuccess_deniesInEmergencyLimitedMode() {
        SecurityAccessSession.grantDefault(context)
        forceResetSecurityState(
            state = TrustState.EMERGENCY_LIMITED,
            reason = "SecurityVaultResetAuthorizationInstrumentedTest emergency limited"
        )

        val error = runCatching {
            SecurityVaultResetAuthorization.grantFromVaultResetAuthPerUseSuccess()
        }.exceptionOrNull()

        assertNotNull(error)
        assertTrue(error is SecurityException)
        assertTrue(
            error?.message?.contains(
                "emergency limited mode must be cleared before reset authorization can be granted"
            ) == true
        )
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
}
