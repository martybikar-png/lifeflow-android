package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.security.audit.SecurityAbuseMonitoringLevel
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityIncidentLevel
import com.lifeflow.security.audit.SecurityIncidentTriggerCode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityAuditAbuseMonitoringInstrumentedTest {

    @Before
    fun setUp() {
        SecurityAuditLog.clear()
    }

    @After
    fun tearDown() {
        SecurityAuditLog.clear()
    }

    @Test
    fun repeatedAuthFailures_raiseThrottleMonitoring() {
        repeat(3) { index ->
            SecurityAuditLog.warning(
                eventType = SecurityAuditLog.EventType.AUTH_FAILURE,
                message = "Auth failure burst #$index"
            )
        }

        val incident = SecurityAuditLog.incidentSignalSnapshot()
        val abuse = SecurityAuditLog.abuseMonitoringSnapshot(TrustState.VERIFIED)

        assertEquals(SecurityIncidentLevel.ELEVATED, incident.incidentLevel)
        assertTrue(incident.repeatedAuthFailureSignal)
        assertTrue(
            SecurityIncidentTriggerCode.AUTH_FAILURE_BURST in incident.triggerCodes
        )

        assertEquals(SecurityAbuseMonitoringLevel.THROTTLE, abuse.monitoringLevel)
        assertTrue(abuse.shouldThrottleAuthentication)
        assertTrue(abuse.notifyMonitoring)
    }

    @Test
    fun criticalCompromiseSignal_escalatesOperatorReview() {
        SecurityAuditLog.critical(
            eventType = SecurityAuditLog.EventType.TAMPER_DETECTED,
            message = "Tamper signal detected during adversarial simulation."
        )

        val incident = SecurityAuditLog.incidentSignalSnapshot()
        val abuse = SecurityAuditLog.abuseMonitoringSnapshot(TrustState.VERIFIED)

        assertEquals(SecurityIncidentLevel.CRITICAL, incident.incidentLevel)
        assertTrue(incident.activeCompromiseSignal)
        assertTrue(
            SecurityIncidentTriggerCode.ACTIVE_COMPROMISE_SIGNAL in incident.triggerCodes
        )

        assertEquals(SecurityAbuseMonitoringLevel.ESCALATE, abuse.monitoringLevel)
        assertTrue(abuse.shouldRequireOperatorReview)
        assertTrue(abuse.notifyMonitoring)
    }
}
