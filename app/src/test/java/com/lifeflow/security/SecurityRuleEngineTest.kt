package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityRuleEngineTest {

    @Before
    fun resetSecurityBaseline() {
        setBaseline(TrustState.DEGRADED)
    }

    @Test
    fun `degraded with authorized session allows read actions only`() {
        setBaseline(TrustState.DEGRADED)
        SecurityAccessSession.grantDefault()

        SecurityRuleEngine.requireAllowed(
            action = RuleAction.READ_BY_ID,
            reason = "Read by id should be allowed in DEGRADED with session"
        )
        SecurityRuleEngine.requireAllowed(
            action = RuleAction.READ_ACTIVE,
            reason = "Read active should be allowed in DEGRADED with session"
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())

        val events = SecurityRuleEngine.getRecentEvents()
        assertEquals(2, events.size)
        assertTrue(events.all { it.decision == SecurityRuleEngine.Decision.ALLOW })
        assertTrue(events.all { it.trustState == TrustState.DEGRADED })
    }

    @Test
    fun `degraded with authorized session denies write actions`() {
        setBaseline(TrustState.DEGRADED)
        SecurityAccessSession.grantDefault()

        val saveError = assertDenied(
            action = RuleAction.WRITE_SAVE,
            reason = "Write save must be denied in DEGRADED"
        )
        val deleteError = assertDenied(
            action = RuleAction.WRITE_DELETE,
            reason = "Write delete must be denied in DEGRADED"
        )

        assertTrue(saveError.message.orEmpty().contains("DEGRADED"))
        assertTrue(deleteError.message.orEmpty().contains("DEGRADED"))
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())

        val events = SecurityRuleEngine.getRecentEvents()
        assertEquals(2, events.size)
        assertTrue(events.all { it.decision == SecurityRuleEngine.Decision.DENY })
        assertTrue(events.all { it.trustState == TrustState.DEGRADED })
    }

    @Test
    fun `verified with authorized session allows read and write actions`() {
        setBaseline(TrustState.VERIFIED)
        SecurityAccessSession.grantDefault()

        SecurityRuleEngine.requireAllowed(
            action = RuleAction.READ_BY_ID,
            reason = "Read by id should be allowed in VERIFIED with session"
        )
        SecurityRuleEngine.requireAllowed(
            action = RuleAction.READ_ACTIVE,
            reason = "Read active should be allowed in VERIFIED with session"
        )
        SecurityRuleEngine.requireAllowed(
            action = RuleAction.WRITE_SAVE,
            reason = "Write save should be allowed in VERIFIED with session"
        )
        SecurityRuleEngine.requireAllowed(
            action = RuleAction.WRITE_DELETE,
            reason = "Write delete should be allowed in VERIFIED with session"
        )

        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val events = SecurityRuleEngine.getRecentEvents()
        assertEquals(4, events.size)
        assertTrue(events.all { it.decision == SecurityRuleEngine.Decision.ALLOW })
        assertTrue(events.all { it.trustState == TrustState.VERIFIED })
    }

    @Test
    fun `verified without session auto degrades after three denies`() {
        setBaseline(TrustState.VERIFIED)
        SecurityAccessSession.clear()

        val firstError = assertDenied(
            action = RuleAction.READ_ACTIVE,
            reason = "First deny in VERIFIED without session"
        )
        assertTrue(firstError.message.orEmpty().contains("VERIFIED"))
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val secondError = assertDenied(
            action = RuleAction.READ_ACTIVE,
            reason = "Second deny in VERIFIED without session"
        )
        assertTrue(secondError.message.orEmpty().contains("VERIFIED"))
        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val thirdError = assertDenied(
            action = RuleAction.READ_ACTIVE,
            reason = "Third deny in VERIFIED without session"
        )
        assertTrue(
            thirdError.message.orEmpty().contains("DEGRADED") ||
                thirdError.message.orEmpty().contains("VERIFIED")
        )
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val events = SecurityRuleEngine.getRecentEvents()
        assertTrue(
            events.any { it.reason.contains("AUTO_DEGRADE") }
        )
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun `explicit downgrade to degraded clears active session`() {
        setBaseline(TrustState.VERIFIED)
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        SecurityRuleEngine.setTrustState(
            state = TrustState.DEGRADED,
            reason = "Manual downgrade should clear session"
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(SecurityRuleEngine.Decision.ALLOW, lastEvent.decision)
        assertEquals(TrustState.DEGRADED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("TrustState set to DEGRADED"))
    }

    @Test
    fun `report crypto failure transitions to compromised and clears session`() {
        setBaseline(TrustState.VERIFIED)
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        SecurityRuleEngine.reportCryptoFailure(
            action = RuleAction.WRITE_SAVE,
            reason = "Encryption integrity failure",
            throwable = IllegalStateException("boom")
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(SecurityRuleEngine.Decision.DENY, lastEvent.decision)
        assertEquals(RuleAction.WRITE_SAVE, lastEvent.action)
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("CRYPTO_FAILURE"))
        assertTrue(lastEvent.reason.contains("IllegalStateException"))
    }

    @Test
    fun `compromised state denies all actions fail closed`() {
        setBaseline(TrustState.VERIFIED)
        SecurityAccessSession.grantDefault()

        SecurityRuleEngine.reportCryptoFailure(
            action = RuleAction.WRITE_DELETE,
            reason = "Compromise test trigger",
            throwable = IllegalStateException("forced compromise")
        )

        val error = assertDenied(
            action = RuleAction.READ_ACTIVE,
            reason = "Any action must be denied in COMPROMISED"
        )

        assertTrue(error.message.orEmpty().contains("COMPROMISED"))
        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(SecurityRuleEngine.Decision.DENY, lastEvent.decision)
        assertEquals(RuleAction.READ_ACTIVE, lastEvent.action)
        assertTrue(lastEvent.reason.contains("LOCKDOWN(COMPROMISED)"))
    }

    @Test
    fun `normal setTrustState cannot override compromised state`() {
        setBaseline(TrustState.COMPROMISED)

        SecurityRuleEngine.setTrustState(
            state = TrustState.VERIFIED,
            reason = "Normal flow must not escape COMPROMISED"
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(SecurityRuleEngine.Decision.DENY, lastEvent.decision)
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("DENY_TRUST_OVERRIDE"))
    }

    private fun setBaseline(state: TrustState) {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = state,
            reason = "SecurityRuleEngineTest baseline -> $state"
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    private fun assertDenied(
        action: RuleAction,
        reason: String
    ): SecurityException {
        return try {
            SecurityRuleEngine.requireAllowed(action, reason)
            throw AssertionError("Expected SecurityException for action=$action")
        } catch (e: SecurityException) {
            e
        }
    }
}