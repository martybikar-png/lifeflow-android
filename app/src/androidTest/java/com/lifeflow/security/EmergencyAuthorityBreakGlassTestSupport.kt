package com.lifeflow.security

import android.content.Context
import com.lifeflow.domain.security.EmergencyAccessReason
import com.lifeflow.domain.security.EmergencyAccessRequest
import com.lifeflow.domain.security.EmergencyActivationKeyBinding
import org.junit.Assert.fail

internal object EmergencyAuthorityBreakGlassTestSupport {
    fun initializeBoundary(
        appContext: Context
    ) {
        EmergencyAuthorityBoundaryBootstrap.start(
            applicationContext = appContext,
            isInstrumentation = true
        )
    }

    fun resetTrust(
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

    fun nowEpochMs(): Long =
        System.currentTimeMillis()

    fun freshRequest(
        reason: EmergencyAccessReason,
        requestedDurationMs: Long
    ): EmergencyAccessRequest {
        return EmergencyAccessRequest(
            reason = reason,
            requestedAtEpochMs = nowEpochMs() - 1_000L,
            requestedDurationMs = requestedDurationMs
        )
    }

    fun testKeyBinding(
        keyId: String,
        thumbprint: String
    ) = EmergencyActivationKeyBinding(
        keyId = keyId,
        confirmationKeyThumbprint = thumbprint,
        algorithm = "EC",
        createdAtEpochMs = nowEpochMs() - 1_000L,
        hardwareBacked = true,
        exportable = false
    )

    fun expectSecurityException(
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

    fun expectIllegalArgumentException(
        block: () -> Unit
    ): IllegalArgumentException {
        try {
            block()
        } catch (e: IllegalArgumentException) {
            return e
        }

        fail("Expected IllegalArgumentException")
        throw IllegalStateException("Unreachable")
    }
}