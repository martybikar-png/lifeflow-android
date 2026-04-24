package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DenialReason
import com.lifeflow.domain.security.ElevationReason
import com.lifeflow.domain.security.LockReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthorizationAndLockingMappingsInstrumentedTest {

    @Test
    fun authorizationResultMapper_allowed_returnsNull() {
        val lockedReason = AuthorizationResult.Allowed.toLockedReasonOrNull(
            detail = "detail"
        )

        assertNull(lockedReason)
    }

    @Test
    fun authorizationResultMapper_deniedAuthContextInvalid_mapsToAuthRequired() {
        val lockedReason = AuthorizationResult.Denied(
            DenialReason.AUTH_CONTEXT_INVALID
        ).toLockedReasonOrNull(
            detail = "detail"
        )

        assertEquals(
            SecurityLockedReason.AUTH_REQUIRED.withDetail("detail"),
            lockedReason
        )
    }

    @Test
    fun authorizationResultMapper_deniedEmergencyExpired_mapsToEmergencyLimited() {
        val lockedReason = AuthorizationResult.Denied(
            DenialReason.EMERGENCY_WINDOW_EXPIRED
        ).toLockedReasonOrNull(
            detail = "detail"
        )

        assertEquals(
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail("detail"),
            lockedReason
        )
    }

    @Test
    fun authorizationResultMapper_requiresStrongerAuth_mapsToAuthRequired() {
        val lockedReason = AuthorizationResult.RequiresElevation(
            ElevationReason.STRONGER_AUTH_REQUIRED
        ).toLockedReasonOrNull(
            detail = "detail"
        )

        assertEquals(
            SecurityLockedReason.AUTH_REQUIRED.withDetail("detail"),
            lockedReason
        )
    }

    @Test
    fun authorizationResultMapper_requiresEmergencyApproval_mapsToEmergencyLimited() {
        val lockedReason = AuthorizationResult.RequiresElevation(
            ElevationReason.EMERGENCY_APPROVAL_REQUIRED
        ).toLockedReasonOrNull(
            detail = "detail"
        )

        assertEquals(
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail("detail"),
            lockedReason
        )
    }

    @Test
    fun authorizationResultMapper_lockedCompromised_andRecoveryRequired_mapCorrectly() {
        val compromisedReason = AuthorizationResult.Locked(
            LockReason.COMPROMISED
        ).toLockedReasonOrNull(
            detail = "compromised-detail"
        )

        val recoveryReason = AuthorizationResult.Locked(
            LockReason.RECOVERY_REQUIRED
        ).toLockedReasonOrNull(
            detail = "recovery-detail"
        )

        assertEquals(
            SecurityLockedReason.COMPROMISED.withDetail("compromised-detail"),
            compromisedReason
        )
        assertEquals(
            SecurityLockedReason.RECOVERY_REQUIRED.withDetail("recovery-detail"),
            recoveryReason
        )
    }

    @Test
    fun runtimeOperationGate_helpers_mapDecisionStateCorrectly() {
        val allowedDecision = SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.ALLOWED,
            effectiveTrustState = TrustState.VERIFIED,
            code = null
        )
        val compromisedDecision = SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.LOCKED,
            effectiveTrustState = TrustState.COMPROMISED,
            code = SecurityRuntimeDecisionCode.COMPROMISED
        )
        val protectedRuntimeBlockedDecision = SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.LOCKED,
            effectiveTrustState = TrustState.DEGRADED,
            code = SecurityRuntimeDecisionCode.PROTECTED_RUNTIME_BLOCKED
        )
        val emergencyDecision = SecurityRuntimeOperationDecision(
            outcome = SecurityRuntimeOperationOutcome.DENIED,
            effectiveTrustState = TrustState.EMERGENCY_LIMITED,
            code = SecurityRuntimeDecisionCode.EMERGENCY_APPROVAL_REQUIRED
        )

        assertEquals(
            "ALLOWED",
            SecurityRuntimeOperationGate.denialCodeName(allowedDecision)
        )

        assertTrue(
            SecurityRuntimeOperationGate.isCompromisedLockdown(compromisedDecision)
        )
        assertFalse(
            SecurityRuntimeOperationGate.isCompromisedLockdown(protectedRuntimeBlockedDecision)
        )

        assertEquals(
            SecurityLockedReason.PROTECTED_RUNTIME_BLOCKED.withDetail("detail"),
            SecurityRuntimeOperationGate.lockedReason(
                decision = protectedRuntimeBlockedDecision,
                detail = "detail"
            )
        )

        assertEquals(
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail("detail"),
            SecurityRuntimeOperationGate.lockedReason(
                decision = emergencyDecision,
                detail = "detail"
            )
        )
    }

    @Test
    fun lockedReasonToUserMessage_mapsAuthorizationOutputs_toStableUserMessages() {
        val authRequired = SecurityLockedReason.AUTH_REQUIRED.withDetail("detail")
        val emergencyLimited = SecurityLockedReason.EMERGENCY_LIMITED.withDetail("detail")
        val compromised = SecurityLockedReason.COMPROMISED.withDetail("detail")
        val recoveryRequired = SecurityLockedReason.RECOVERY_REQUIRED.withDetail("detail")
        val protectedRuntimeBlocked =
            SecurityLockedReason.PROTECTED_RUNTIME_BLOCKED.withDetail("detail")

        assertEquals(
            AUTH_REQUIRED_USER_MESSAGE,
            lockedReasonToUserMessage(authRequired)
        )
        assertEquals(
            SECURITY_EMERGENCY_LIMITED_USER_MESSAGE,
            lockedReasonToUserMessage(emergencyLimited)
        )
        assertEquals(
            SECURITY_COMPROMISED_USER_MESSAGE,
            lockedReasonToUserMessage(compromised)
        )
        assertEquals(
            RECOVERY_REQUIRED_USER_MESSAGE,
            lockedReasonToUserMessage(recoveryRequired)
        )
        assertEquals(
            PROTECTED_RUNTIME_BLOCKED_USER_MESSAGE,
            lockedReasonToUserMessage(protectedRuntimeBlocked)
        )
    }
}
