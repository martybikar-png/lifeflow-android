package com.lifeflow.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityAccessSessionTest {

    @Before
    fun resetSecurityBaseline() {
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "SecurityAccessSessionTest baseline reset"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    @Test
    fun `grantDefault authorizes session when trust state is not compromised`() {
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "Default grant authorization test"
        )

        SecurityAccessSession.grantDefault()

        assertTrue(SecurityAccessSession.isAuthorized())
    }

    @Test
    fun `clear revokes active session`() {
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "Clear revocation test"
        )
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        SecurityAccessSession.clear()

        assertFalse(SecurityAccessSession.isAuthorized())
    }

    @Test
    fun `requireAuthorized throws when no session exists`() {
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "Missing session test"
        )
        SecurityAccessSession.clear()

        val thrown = try {
            SecurityAccessSession.requireAuthorized("Session should be required")
            null
        } catch (e: SecurityException) {
            e
        }

        requireNotNull(thrown) { "Expected SecurityException when no session exists." }
        assertTrue(thrown.message.orEmpty().contains("denied", ignoreCase = true))
    }

    @Test
    fun `requireAuthorized passes when session is active and trust is safe`() {
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "Active session success test"
        )
        SecurityAccessSession.grantDefault()

        SecurityAccessSession.requireAuthorized("Active session should pass")

        assertTrue(SecurityAccessSession.isAuthorized())
    }

    @Test
    fun `compromised trust state forces unauthorized even after grant`() {
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "Compromised authorization block test"
        )
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        SecurityRuleEngine.setTrustState(
            state = TrustState.COMPROMISED,
            reason = "Move to compromised must fail closed"
        )

        assertFalse(SecurityAccessSession.isAuthorized())
    }

    @Test
    fun `requireAuthorized throws in compromised state even if session was granted`() {
        forceResetSecurityState(
            state = TrustState.VERIFIED,
            reason = "Compromised requireAuthorized test"
        )
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        SecurityRuleEngine.setTrustState(
            state = TrustState.COMPROMISED,
            reason = "Compromised must override active session"
        )

        val thrown = try {
            SecurityAccessSession.requireAuthorized("Compromised state must deny")
            null
        } catch (e: SecurityException) {
            e
        }

        requireNotNull(thrown) { "Expected SecurityException in compromised state." }
        assertTrue(thrown.message.orEmpty().contains("denied", ignoreCase = true))
        assertFalse(SecurityAccessSession.isAuthorized())
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