package com.lifeflow.security

import com.lifeflow.domain.security.CompromiseReason
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.LockReason
import com.lifeflow.domain.security.LockoutContext
import com.lifeflow.domain.security.RecoveryOption
import com.lifeflow.domain.security.TrustState as DomainTrustState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SecurityLifecyclePortAdapterTest {

    private val adapter = SecurityLifecyclePortAdapter()

    @Before
    fun resetSecurityBaseline() {
        forceTrustState(
            state = TrustState.DEGRADED,
            reason = "SecurityLifecyclePortAdapterTest baseline"
        )
    }

    @Test
    fun `onCompromised clears session and transitions to compromised`() {
        forceTrustState(
            state = TrustState.VERIFIED,
            reason = "Compromise transition test"
        )
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        adapter.onCompromised(CompromiseReason.CRYPTO_FAILURE)

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("Lifecycle compromise"))
        assertTrue(lastEvent.reason.contains(CompromiseReason.CRYPTO_FAILURE.name))
    }

    @Test
    fun `onLockout with compromised reason clears session and transitions to compromised`() {
        forceTrustState(
            state = TrustState.VERIFIED,
            reason = "Compromised lockout test"
        )
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        adapter.onLockout(
            LockoutContext(
                attemptedOperation = DomainOperation.DELETE_IDENTITY,
                trustState = anyDomainTrustState(),
                reason = LockReason.COMPROMISED
            )
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("Lifecycle lockout"))
        assertTrue(lastEvent.reason.contains(LockReason.COMPROMISED.name))
    }

    @Test
    fun `onLockout with locked out reason clears session and transitions to degraded`() {
        forceTrustState(
            state = TrustState.VERIFIED,
            reason = "Locked out transition test"
        )
        SecurityAccessSession.grantDefault()
        assertTrue(SecurityAccessSession.isAuthorized())

        adapter.onLockout(
            LockoutContext(
                attemptedOperation = DomainOperation.READ_ACTIVE_IDENTITY,
                trustState = anyDomainTrustState(),
                reason = LockReason.LOCKED_OUT
            )
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertFalse(SecurityAccessSession.isAuthorized())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(TrustState.DEGRADED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("Lifecycle lockout"))
        assertTrue(lastEvent.reason.contains(LockReason.LOCKED_OUT.name))
        assertTrue(lastEvent.reason.contains(DomainOperation.READ_ACTIVE_IDENTITY.name))
    }

    @Test
    fun `recoveryOptions reflect current trust state`() {
        forceTrustState(
            state = TrustState.VERIFIED,
            reason = "Recovery options verified test"
        )
        assertTrue(adapter.recoveryOptions().isEmpty())

        forceTrustState(
            state = TrustState.DEGRADED,
            reason = "Recovery options degraded test"
        )
        assertEquals(
            listOf(
                RecoveryOption.RETRY_AUTHENTICATION,
                RecoveryOption.RESTART_SECURE_SESSION,
                RecoveryOption.ENTER_BREAK_GLASS
            ),
            adapter.recoveryOptions()
        )

        forceTrustState(
            state = TrustState.EMERGENCY_LIMITED,
            reason = "Recovery options emergency test"
        )
        assertEquals(
            listOf(RecoveryOption.EXIT_BREAK_GLASS),
            adapter.recoveryOptions()
        )

        forceTrustState(
            state = TrustState.COMPROMISED,
            reason = "Recovery options compromised test"
        )
        assertEquals(
            listOf(
                RecoveryOption.RESET_VAULT,
                RecoveryOption.CONTACT_SUPPORT
            ),
            adapter.recoveryOptions()
        )
    }

    private fun forceTrustState(
        state: TrustState,
        reason: String
    ) {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = state,
            reason = reason
        )
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
    }

    private fun anyDomainTrustState(): DomainTrustState =
        enumValues<DomainTrustState>().first()
}
