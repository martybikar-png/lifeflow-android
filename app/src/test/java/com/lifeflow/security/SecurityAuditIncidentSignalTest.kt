package com.lifeflow.security

import com.lifeflow.security.audit.SecurityAuditIncidentSignalAnalyzer
import com.lifeflow.security.audit.SecurityAuditLog
import com.lifeflow.security.audit.SecurityIncidentLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

class SecurityAuditIncidentSignalTest {

    @Before
    fun resetAudit() {
        SecurityAuditLog.clear()
    }

    @Test
    fun `empty audit produces normal incident snapshot`() {
        val snapshot = SecurityAuditLog.incidentSignalSnapshot()

        assertEquals(SecurityIncidentLevel.NORMAL, snapshot.incidentLevel)
        assertEquals(0, snapshot.totalEvents)
        assertTrue(snapshot.triggerCodes.isEmpty())
    }

    @Test
    fun `warning activity produces observe level`() {
        SecurityAuditLog.warning(
            eventType = SecurityAuditLog.EventType.AUTH_FAILURE,
            message = "Single auth failure"
        )

        val snapshot = SecurityAuditLog.incidentSignalSnapshot()

        assertEquals(SecurityIncidentLevel.OBSERVE, snapshot.incidentLevel)
        assertEquals(1, snapshot.warningCount)
        assertTrue(snapshot.triggerCodes.contains("WARNING_ACTIVITY"))
    }

    @Test
    fun `auth failure burst produces elevated level`() {
        repeat(3) {
            SecurityAuditLog.warning(
                eventType = SecurityAuditLog.EventType.AUTH_FAILURE,
                message = "Repeated auth failure $it"
            )
        }

        val snapshot = SecurityAuditLog.incidentSignalSnapshot()

        assertEquals(SecurityIncidentLevel.ELEVATED, snapshot.incidentLevel)
        assertTrue(snapshot.repeatedAuthFailureSignal)
        assertTrue(snapshot.triggerCodes.contains("AUTH_FAILURE_BURST"))
    }

    @Test
    fun `compromise event produces critical level`() {
        SecurityAuditLog.critical(
            eventType = SecurityAuditLog.EventType.TAMPER_DETECTED,
            message = "Tamper signal detected"
        )

        val snapshot = SecurityAuditLog.incidentSignalSnapshot()

        assertEquals(SecurityIncidentLevel.CRITICAL, snapshot.incidentLevel)
        assertTrue(snapshot.activeCompromiseSignal)
        assertEquals(SecurityAuditLog.EventType.TAMPER_DETECTED, snapshot.latestCriticalEventType)
        assertTrue(snapshot.triggerCodes.contains("ACTIVE_COMPROMISE_SIGNAL"))
    }

    @Test
    fun `snapshot since filters older entries`() {
        val oldTimestamp = Instant.now().minusSeconds(3600)
        val recentTimestamp = Instant.now().minusSeconds(5)

        val entries = listOf(
            SecurityAuditLog.AuditEntry(
                timestamp = oldTimestamp,
                eventType = SecurityAuditLog.EventType.AUTH_FAILURE,
                severity = SecurityAuditLog.Severity.WARNING,
                message = "Old warning"
            ),
            SecurityAuditLog.AuditEntry(
                timestamp = recentTimestamp,
                eventType = SecurityAuditLog.EventType.POLICY_VIOLATION,
                severity = SecurityAuditLog.Severity.CRITICAL,
                message = "Recent critical"
            )
        )

        val snapshot = SecurityAuditIncidentSignalAnalyzer.snapshot(
            entries = entries.filter { it.timestamp.isAfter(Instant.now().minusSeconds(60)) },
            generatedAt = Instant.now()
        )

        assertEquals(SecurityIncidentLevel.CRITICAL, snapshot.incidentLevel)
        assertEquals(1, snapshot.totalEvents)
        assertTrue(snapshot.triggerCodes.contains("ACTIVE_COMPROMISE_SIGNAL"))
    }
}
