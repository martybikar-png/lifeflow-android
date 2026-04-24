package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionAndVaultResetSecurityInstrumentedTest {

    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @After
    fun tearDown() {
        SecurityVaultResetAuthorization.clear()
        SecurityAccessSession.clear()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest teardown"
        )
    }

    @Test
    fun sessionGrant_and_clear_follow_failClosed_behavior() {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "session test"
        )
        SecurityAccessSession.clear()

        SecurityAccessSession.grantDefault(appContext)

        assertTrue(SecurityAccessSession.isAuthorized(appContext))

        SecurityAccessSession.clear()

        assertFalse(SecurityAccessSession.isAuthorized(appContext))
    }

    @Test
    fun vaultResetAuthorization_grant_and_consume_require_fresh_single_use_window() {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "vault reset auth single-use test"
        )
        SecurityAccessSession.clear()
        SecurityVaultResetAuthorization.clear()

        SecurityAccessSession.grantDefault(appContext)
        assertTrue(SecurityAccessSession.isAuthorized(appContext))

        SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
        SecurityVaultResetAuthorization.consumeFreshAuthorization(
            reason = "androidTest first consume"
        )

        val error = expectSecurityException {
            SecurityVaultResetAuthorization.consumeFreshAuthorization(
                reason = "androidTest second consume"
            )
        }

        assertTrue(
            error.message.orEmpty().contains(
                "missing fresh authorization",
                ignoreCase = true
            )
        )
    }

    @Test
    fun vaultResetAuthorization_is_denied_without_active_session() {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "vault reset auth requires session"
        )
        SecurityAccessSession.clear()
        SecurityVaultResetAuthorization.clear()

        val error = expectSecurityException {
            SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
        }

        assertTrue(
            error.message.orEmpty().contains(
                "active auth session is required",
                ignoreCase = true
            )
        )
    }

    @Test
    fun vaultResetAuthorization_is_denied_in_emergencyLimited_mode() {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.EMERGENCY_LIMITED,
            reason = "vault reset auth emergency limited"
        )
        SecurityAccessSession.clear()
        SecurityVaultResetAuthorization.clear()

        SecurityAccessSession.grantDefault(appContext)
        assertTrue(SecurityAccessSession.isAuthorized(appContext))

        val error = expectSecurityException {
            SecurityVaultResetAuthorization.grantFromVaultResetBiometricSuccess()
        }

        assertTrue(
            error.message.orEmpty().contains(
                "emergency limited mode",
                ignoreCase = true
            )
        )
    }

    private fun expectSecurityException(
        block: () -> Unit
    ): SecurityException {
        try {
            block()
        } catch (e: SecurityException) {
            return e
        }

        fail("Expected SecurityException")
        throw IllegalStateException("Unreachable")
    }
}
