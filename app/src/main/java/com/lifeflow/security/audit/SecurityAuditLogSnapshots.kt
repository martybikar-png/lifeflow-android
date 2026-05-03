package com.lifeflow.security.audit

import com.lifeflow.security.SecurityRuntimeContainmentPolicy
import com.lifeflow.security.SecurityRuntimeContainmentSnapshot
import com.lifeflow.security.TrustState
import java.time.Instant

internal fun securityAuditIncidentSignalSnapshot(): SecurityIncidentSignalSnapshot =
    SecurityAuditIncidentSignalAnalyzer.snapshot(SecurityAuditLog.getEntries())

internal fun securityAuditIncidentSignalSnapshotSince(
    since: Instant
): SecurityIncidentSignalSnapshot =
    SecurityAuditIncidentSignalAnalyzer.snapshot(SecurityAuditLog.getEntriesSince(since))

internal fun securityAuditIncidentResponseSnapshot(
    currentTrustState: TrustState
): SecurityIncidentResponseSnapshot =
    SecurityIncidentResponseBridge.snapshot(
        incident = securityAuditIncidentSignalSnapshot(),
        currentTrustState = currentTrustState
    )

internal fun securityAuditIncidentResponseSnapshotSince(
    since: Instant,
    currentTrustState: TrustState
): SecurityIncidentResponseSnapshot =
    SecurityIncidentResponseBridge.snapshot(
        incident = securityAuditIncidentSignalSnapshotSince(since),
        currentTrustState = currentTrustState
    )

internal fun securityAuditAbuseMonitoringSnapshot(
    currentTrustState: TrustState
): SecurityAbuseMonitoringSnapshot =
    SecurityAbuseMonitoringAnalyzer.snapshot(
        incident = securityAuditIncidentSignalSnapshot(),
        currentTrustState = currentTrustState
    )

internal fun securityAuditAbuseMonitoringSnapshotSince(
    since: Instant,
    currentTrustState: TrustState
): SecurityAbuseMonitoringSnapshot =
    SecurityAbuseMonitoringAnalyzer.snapshot(
        incident = securityAuditIncidentSignalSnapshotSince(since),
        currentTrustState = currentTrustState
    )

internal fun securityAuditRuntimeContainmentSnapshot(
    currentTrustState: TrustState
): SecurityRuntimeContainmentSnapshot =
    SecurityRuntimeContainmentPolicy.snapshot(
        incidentResponse = securityAuditIncidentResponseSnapshot(currentTrustState)
    )

internal fun securityAuditRuntimeContainmentSnapshotSince(
    since: Instant,
    currentTrustState: TrustState
): SecurityRuntimeContainmentSnapshot =
    SecurityRuntimeContainmentPolicy.snapshot(
        incidentResponse = securityAuditIncidentResponseSnapshotSince(
            since = since,
            currentTrustState = currentTrustState
        )
    )
