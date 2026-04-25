package com.lifeflow.security

import com.lifeflow.domain.security.EmergencyAccessRequest

internal fun buildSecurityEmergencyApprovalRequestHash(
    request: EmergencyAccessRequest,
    boundedDurationMs: Long,
    degradedCauseSnapshotId: String
): String {
    return sha256Hex(
        buildString {
            append(request.reason.name)
            append('|')
            append(request.requestedAtEpochMs)
            append('|')
            append(boundedDurationMs)
            append('|')
            append("trustedBaseOnly=true")
            append('|')
            append(degradedCauseSnapshotId)
        }
    )
}
