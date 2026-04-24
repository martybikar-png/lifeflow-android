package com.lifeflow.security

import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DenialReason
import com.lifeflow.domain.security.ElevationReason
import com.lifeflow.domain.security.LockReason

internal fun AuthorizationResult.toLockedReasonOrNull(
    detail: String
): String? =
    when (this) {
        AuthorizationResult.Allowed ->
            null

        is AuthorizationResult.EmergencyAllowed ->
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail(detail)

        is AuthorizationResult.Denied ->
            deniedReasonToLockedReason(
                denialReason = reason,
                detail = detail
            )

        is AuthorizationResult.RequiresElevation ->
            elevationReasonToLockedReason(
                elevationReason = reason,
                detail = detail
            )

        is AuthorizationResult.Locked ->
            lockReasonToLockedReason(
                lockReason = reason,
                detail = detail
            )
    }

private fun deniedReasonToLockedReason(
    denialReason: DenialReason,
    detail: String
): String =
    when (denialReason) {
        DenialReason.AUTH_CONTEXT_INVALID,
        DenialReason.OPERATION_NOT_ALLOWED,
        DenialReason.POLICY_REJECTED,
        DenialReason.TRUST_NOT_SUFFICIENT ->
            SecurityLockedReason.AUTH_REQUIRED.withDetail(detail)

        DenialReason.TRUSTED_BASE_ONLY_REQUIRED,
        DenialReason.EMERGENCY_NOT_APPROVED,
        DenialReason.EMERGENCY_WINDOW_EXPIRED ->
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail(detail)
    }

private fun elevationReasonToLockedReason(
    elevationReason: ElevationReason,
    detail: String
): String =
    when (elevationReason) {
        ElevationReason.RECENT_AUTH_REQUIRED,
        ElevationReason.STRONGER_AUTH_REQUIRED ->
            SecurityLockedReason.AUTH_REQUIRED.withDetail(detail)

        ElevationReason.EMERGENCY_APPROVAL_REQUIRED ->
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail(detail)
    }

private fun lockReasonToLockedReason(
    lockReason: LockReason,
    detail: String
): String =
    when (lockReason) {
        LockReason.COMPROMISED ->
            SecurityLockedReason.COMPROMISED.withDetail(detail)

        LockReason.EMERGENCY_REJECTED ->
            SecurityLockedReason.EMERGENCY_LIMITED.withDetail(detail)

        LockReason.LOCKED_OUT ->
            SecurityLockedReason.AUTH_REQUIRED.withDetail(detail)

        LockReason.RECOVERY_REQUIRED ->
            SecurityLockedReason.RECOVERY_REQUIRED.withDetail(detail)
    }
