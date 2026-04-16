package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityIncidentLevel
import com.lifeflow.security.audit.SecurityIncidentResponseBridge
import com.lifeflow.security.audit.SecurityIncidentResponseMode
import com.lifeflow.security.audit.SecurityIncidentSignalSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SecurityIncidentResponseBridgeTest {

    @Before
    fun resetAudit() {
        SecurityAuditLog.clear()
    }

    @Test
    fun `normal incident snapshot keeps normal response posture`() {
        val response = SecurityIncidentResponseBridge.snapshot(
            incident = incidentSnapshot(
                incidentLevel = SecurityIncidentLevel.NORMAL
            ),
            currentTrustState = TrustState.DEGRADED,
            generatedAt = Instant.EPOCH
        )

        assertEquals(SecurityIncidentResponseMode.NORMAL, response.responseMode)
        assertEquals(TrustState.DEGRADED, response.currentTrustState)
        assertEquals(null, response.recommendedTrustState)
        assertFalse(response.blockProtectedRuntime)
        assertFalse(response.blockSensitiveOperations)
        assertFalse(response.requireRecovery)
        assertFalse(response.notifyMonitoring)
        assertTrue(response.responseCodes.isEmpty())
    }

    @Test
    fun `elevated auth failure burst recommends degraded guard from verified trust`() {
        val response = SecurityIncidentResponseBridge.snapshot(
            incident = incidentSnapshot(
                incidentLevel = SecurityIncidentLevel.ELEVATED,
                repeatedAuthFailureSignal = true,
                triggerCodes = linkedSetOf("AUTH_FAILURE_BURST")
            ),
            currentTrustState = TrustState.VERIFIED,
            generatedAt = Instant.EPOCH
        )

        assertEquals(SecurityIncidentResponseMode.HEIGHTENED_GUARD, response.responseMode)
        assertEquals(TrustState.DEGRADED, response.recommendedTrustState)
        assertFalse(response.blockProtectedRuntime)
        assertTrue(response.blockSensitiveOperations)
        assertFalse(response.requireRecovery)
        assertTrue(response.notifyMonitoring)
        assertTrue(response.responseCodes.contains("AUTH_FAILURE_BURST"))
        assertTrue(response.responseCodes.contains("RECOMMEND_TRUST_DEGRADE"))
    }

    @Test
    fun `critical compromise signal requires recovery and compromised lockdown`() {
        val response = SecurityIncidentResponseBridge.snapshot(
            incident = incidentSnapshot(
                incidentLevel = SecurityIncidentLevel.CRITICAL,
                activeCompromiseSignal = true,
                triggerCodes = linkedSetOf("ACTIVE_COMPROMISE_SIGNAL")
            ),
            currentTrustState = TrustState.DEGRADED,
            generatedAt = Instant.EPOCH
        )

        assertEquals(SecurityIncidentResponseMode.RECOVERY_REQUIRED, response.responseMode)
        assertEquals(TrustState.COMPROMISED, response.recommendedTrustState)
        assertTrue(response.blockProtectedRuntime)
        assertTrue(response.blockSensitiveOperations)
        assertTrue(response.requireRecovery)
        assertTrue(response.notifyMonitoring)
        assertTrue(response.responseCodes.contains("ACTIVE_COMPROMISE_SIGNAL"))
        assertTrue(response.responseCodes.contains("FORCE_COMPROMISED_LOCKDOWN"))
        assertTrue(response.responseCodes.contains("RECOVERY_REQUIRED"))
    }

    @Test
    fun `audit log incident response snapshot bridges warning activity`() {
        SecurityAuditLog.warning(
            eventType = SecurityAuditLog.EventType.AUTH_FAILURE,
            message = "Single auth failure"
        )

        val response = SecurityAuditLog.incidentResponseSnapshot(
            currentTrustState = TrustState.DEGRADED
        )

        assertEquals(SecurityIncidentResponseMode.OBSERVE, response.responseMode)
        assertEquals(SecurityIncidentLevel.OBSERVE, response.incidentLevel)
        assertEquals(null, response.recommendedTrustState)
        assertTrue(response.responseCodes.contains("OBSERVE_ONLY"))
    }

    @Test
    fun `audit log incident response snapshot treats compromised trust as recovery required`() {
        val response = SecurityAuditLog.incidentResponseSnapshot(
            currentTrustState = TrustState.COMPROMISED
        )

        assertEquals(SecurityIncidentResponseMode.RECOVERY_REQUIRED, response.responseMode)
        assertEquals(TrustState.COMPROMISED, response.recommendedTrustState)
        assertTrue(response.requireRecovery)
        assertTrue(response.responseCodes.contains("TRUST_ALREADY_COMPROMISED"))
    }

    private fun incidentSnapshot(
        incidentLevel: SecurityIncidentLevel,
        activeCompromiseSignal: Boolean = false,
        repeatedAuthFailureSignal: Boolean = false,
        repeatedPolicyViolationSignal: Boolean = false,
        triggerCodes: Set<String> = emptySet()
    ): SecurityIncidentSignalSnapshot {
        return SecurityIncidentSignalSnapshot(
            generatedAt = Instant.EPOCH,
            incidentLevel = incidentLevel,
            totalEvents = triggerCodes.size,
            criticalCount = if (incidentLevel == SecurityIncidentLevel.CRITICAL) 1 else 0,
            warningCount = if (incidentLevel == SecurityIncidentLevel.OBSERVE ||
                incidentLevel == SecurityIncidentLevel.ELEVATED
            ) 1 else 0,
            infoCount = 0,
            activeCompromiseSignal = activeCompromiseSignal,
            repeatedAuthFailureSignal = repeatedAuthFailureSignal,
            repeatedPolicyViolationSignal = repeatedPolicyViolationSignal,
            latestEventType = null,
            latestCriticalEventType = null,
            triggerCodes = triggerCodes
        )
    }
}
