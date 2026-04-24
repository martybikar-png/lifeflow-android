package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.audit.SecurityAuditLog
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityRuleEngineStateMachineInstrumentedTest {

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
    fun reportIntegrityTrustVerdict_verified_setsVerified() {
        resetState(TrustState.DEGRADED, "verified transition test")

        SecurityRuleEngine.reportIntegrityTrustVerdict(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "SERVER_VERIFIED"
        )

        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("INTEGRITY_TRUST_VERIFIED"))
        assertEquals(TrustState.VERIFIED, lastEvent.trustState)
    }

    @Test
    fun reportIntegrityTrustVerdict_degraded_setsDegraded() {
        resetState(TrustState.VERIFIED, "degraded transition test")

        SecurityRuleEngine.reportIntegrityTrustVerdict(
            verdict = SecurityIntegrityTrustVerdict.DEGRADED,
            reason = "SERVER_DEGRADED"
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("INTEGRITY_TRUST_DEGRADED"))
        assertEquals(TrustState.DEGRADED, lastEvent.trustState)
    }

    @Test
    fun reportIntegrityTrustVerdict_compromised_setsCompromised() {
        resetState(TrustState.VERIFIED, "compromised transition test")

        SecurityRuleEngine.reportIntegrityTrustVerdict(
            verdict = SecurityIntegrityTrustVerdict.COMPROMISED,
            reason = "SERVER_COMPROMISED"
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("INTEGRITY_TRUST_COMPROMISED"))
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
    }

    @Test
    fun compromisedState_blocksSetTrustStateOverride() {
        resetState(TrustState.COMPROMISED, "override block test")

        SecurityRuleEngine.setTrustState(
            state = TrustState.VERIFIED,
            reason = "attempt verified override"
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("DENY_TRUST_OVERRIDE"))
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
    }

    @Test
    fun compromisedState_blocksIntegrityVerdictOverride() {
        resetState(TrustState.COMPROMISED, "integrity override block test")

        SecurityRuleEngine.reportIntegrityTrustVerdict(
            verdict = SecurityIntegrityTrustVerdict.VERIFIED,
            reason = "attempt verified integrity override"
        )

        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertTrue(lastEvent.reason.contains("DENY_INTEGRITY_OVERRIDE"))
        assertEquals(TrustState.COMPROMISED, lastEvent.trustState)
    }

    @Test
    fun recoverAfterVaultReset_resetsAuditAndReturnsToDegraded() {
        resetState(TrustState.COMPROMISED, "vault reset recovery test")

        SecurityRuleEngine.recoverAfterVaultReset(
            reason = "androidTest vault reset recovery"
        )

        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())

        val events = SecurityRuleEngine.getRecentEvents()
        assertEquals(1, events.size)
        assertTrue(events.single().reason.contains("VAULT_RESET_RECOVERY"))
        assertEquals(TrustState.DEGRADED, events.single().trustState)
    }

    @Test
    fun forceResetForAdversarialSuite_clearsAuditAndSetsRequestedState() {
        resetState(TrustState.COMPROMISED, "before adversarial reset")

        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.VERIFIED,
            reason = "androidTest adversarial reset"
        )

        assertEquals(TrustState.VERIFIED, SecurityRuleEngine.getTrustState())

        val events = SecurityRuleEngine.getRecentEvents()
        assertEquals(1, events.size)
        assertTrue(events.single().reason.contains("ADVERSARIAL_SUITE_FORCE_RESET"))
        assertEquals(TrustState.VERIFIED, events.single().trustState)
    }

    @Test
    fun clearAudit_clearsEngineAudit_andSecurityAuditLog() {
        resetState(TrustState.VERIFIED, "clear audit test")

        SecurityRuleEngine.reportIntegrityTrustVerdict(
            verdict = SecurityIntegrityTrustVerdict.DEGRADED,
            reason = "populate audit"
        )
        SecurityAuditLog.warning(
            SecurityAuditLog.EventType.POLICY_VIOLATION,
            "populate security audit"
        )

        SecurityRuleEngine.clearAudit()

        assertTrue(SecurityRuleEngine.getRecentEvents().isEmpty())
        assertTrue(SecurityAuditLog.getEntries().isEmpty())
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
    }

    private fun resetState(
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
