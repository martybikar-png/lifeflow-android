package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.EmergencyAccessReason
import com.lifeflow.domain.security.EmergencyActivationRequest
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

    private val emergencySupport = EmergencyAuthorityBreakGlassTestSupport

    @After
    fun tearDown() {
        runCatching {
            if (SecurityEmergencyAccessAuthority.currentWindow() != null) {
                SecurityEmergencyAccessAuthority.clear("androidTest teardown")
            }
        }

        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()
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
    fun trustedBaseReadOnlyAuthorization_activeEmergencyWindow_allowsThroughBoundary() {
        emergencySupport.initializeBoundary(appContext)
        emergencySupport.resetTrust(
            state = TrustState.DEGRADED,
            reason = "trusted-base read-only emergency regression"
        )
        InstrumentationEmergencyAuditSink.clear()
        InstrumentationEmergencyArtifactRegistry.clear()

        val request = emergencySupport.freshRequest(
            reason = EmergencyAccessReason.CRITICAL_HEALTH_ACCESS,
            requestedDurationMs = 20_000L
        )

        val session = SecurityEmergencyAccessAuthority.createApprovalSession(
            request = request,
            degradedCauseSnapshotId = "snapshot-trusted-base-read",
            firstApproverId = "approver-a",
            secondApproverId = "approver-b"
        )

        val artifact = SecurityEmergencyAccessAuthority.issueActivationArtifact(
            request = EmergencyActivationRequest(
                approvalSession = session,
                audience = "lifeflow-device",
                nonce = "nonce-trusted-base-read",
                requestedAtEpochMs = emergencySupport.nowEpochMs(),
                artifactLifetimeMs = 30_000L,
                keyBinding = emergencySupport.testKeyBinding(
                    keyId = "key-trusted-base-read",
                    thumbprint = "thumb-trusted-base-read"
                )
            )
        )

        SecurityEmergencyAccessAuthority.activate(artifact)
        SecurityAccessSession.grantDefault(appContext)

        val lockedReason =
            SecurityAuthorizationGate.trustedBaseReadOnlyLockedReasonOrNull(
                operation = DomainOperation.READ_TWIN_SNAPSHOT,
                detail = "Trusted-base emergency read"
            )

        assertNull(lockedReason)
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
