package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityRuntimeAccessPolicyInstrumentedTest {

    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @After
    fun tearDown() {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest teardown"
        )
    }

    @Test
    fun standardProtectedEntry_verifiedSession_allowsAccess() {
        resetTrust(TrustState.VERIFIED, "verified session access")
        SecurityAccessSession.grantDefault(appContext)

        val decision = SecurityRuntimeAccessPolicy.decideStandardProtectedEntry()

        assertTrue(decision.allowed)
        assertEquals(TrustState.VERIFIED, decision.effectiveTrustState)
        assertNull(decision.denialCode)
        assertNull(decision.toLockedReason("detail"))
    }

    @Test
    fun standardProtectedEntry_withoutSession_failsClosedToAuthRequired() {
        resetTrust(TrustState.VERIFIED, "missing session access")
        SecurityAccessSession.clear()

        val decision = SecurityRuntimeAccessPolicy.decideStandardProtectedEntry()

        assertFalse(decision.allowed)
        assertEquals(TrustState.VERIFIED, decision.effectiveTrustState)
        assertEquals(SecurityRuntimeDecisionCode.AUTH_REQUIRED, decision.denialCode)
        assertEquals(
            SecurityLockedReason.AUTH_REQUIRED.withDetail("detail"),
            decision.toLockedReason("detail")
        )
        assertEquals(
            AUTH_REQUIRED_USER_MESSAGE,
            decision.toFailureMessage()
        )
    }

    @Test
    fun standardProtectedEntry_compromisedTrust_failsClosedToCompromised() {
        resetTrust(TrustState.COMPROMISED, "compromised access")
        SecurityAccessSession.grantDefault(appContext)

        val decision = SecurityRuntimeAccessPolicy.decideStandardProtectedEntry()

        assertFalse(decision.allowed)
        assertEquals(TrustState.COMPROMISED, decision.effectiveTrustState)
        assertEquals(SecurityRuntimeDecisionCode.COMPROMISED, decision.denialCode)
        assertEquals(
            SecurityLockedReason.COMPROMISED.withDetail("detail"),
            decision.toLockedReason("detail")
        )
        assertEquals(
            SECURITY_COMPROMISED_USER_MESSAGE,
            decision.toFailureMessage()
        )
    }

    @Test
    fun trustedBaseReadEntry_emergencyLimitedWithoutWindow_staysBlocked() {
        resetTrust(TrustState.EMERGENCY_LIMITED, "emergency limited read")
        SecurityAccessSession.clear()

        val decision = SecurityRuntimeAccessPolicy.decideTrustedBaseReadEntry()

        assertFalse(decision.allowed)
        assertEquals(TrustState.EMERGENCY_LIMITED, decision.effectiveTrustState)
        assertEquals(SecurityRuntimeDecisionCode.EMERGENCY_LIMITED, decision.denialCode)
        assertEquals(
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail("detail"),
            decision.toLockedReason("detail")
        )
        assertEquals(
            SECURITY_EMERGENCY_LIMITED_USER_MESSAGE,
            decision.toFailureMessage()
        )
    }

    @Test
    fun lockedReasonMapping_preservesRecoveryAndProtectedRuntimeMessages() {
        val recoveryDecision = SecurityRuntimeAccessDecision(
            allowed = false,
            effectiveTrustState = TrustState.COMPROMISED,
            denialCode = SecurityRuntimeDecisionCode.RECOVERY_REQUIRED
        )
        val protectedRuntimeBlockedDecision = SecurityRuntimeAccessDecision(
            allowed = false,
            effectiveTrustState = TrustState.DEGRADED,
            denialCode = SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED
        )

        assertEquals(
            SecurityLockedReason.RECOVERY_REQUIRED.withDetail("detail"),
            recoveryDecision.toLockedReason("detail")
        )
        assertEquals(
            RECOVERY_REQUIRED_USER_MESSAGE,
            recoveryDecision.toFailureMessage()
        )

        assertEquals(
            SecurityLockedReason.PROTECTED_RUNTIME_BLOCKED.withDetail("detail"),
            protectedRuntimeBlockedDecision.toLockedReason("detail")
        )
        assertEquals(
            PROTECTED_RUNTIME_BLOCKED_USER_MESSAGE,
            protectedRuntimeBlockedDecision.toFailureMessage()
        )
    }

    private fun resetTrust(
        state: TrustState,
        reason: String
    ) {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = state,
            reason = reason
        )
    }
}
