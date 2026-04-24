package com.lifeflow.security

import com.lifeflow.domain.security.AuthContext
import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DenialReason
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.ElevationReason
import com.lifeflow.domain.security.LockReason
import com.lifeflow.domain.security.SecurityAuthorizationPort

internal class SecurityAuthorizationPortAdapter : SecurityAuthorizationPort {

    override fun authorize(
        operation: DomainOperation,
        context: AuthContext
    ): AuthorizationResult {
        val decision = SecurityRuntimeAccessPolicy.decideAuthorization(
            request = SecurityRuntimeAuthorizationRequest(
                operation = operation,
                authorizationPolicy = context.authorizationPolicy,
                hasRecentAuthentication = context.hasRecentAuthentication
            )
        )

        return when (decision.outcome) {
            SecurityRuntimeAuthorizationOutcome.ALLOWED ->
                AuthorizationResult.Allowed

            SecurityRuntimeAuthorizationOutcome.DENIED ->
                AuthorizationResult.Denied(
                    mapDenialReason(decision.code)
                )

            SecurityRuntimeAuthorizationOutcome.REQUIRES_ELEVATION ->
                AuthorizationResult.RequiresElevation(
                    mapElevationReason(decision.code)
                )

            SecurityRuntimeAuthorizationOutcome.LOCKED ->
                AuthorizationResult.Locked(
                    mapLockReason(decision.code)
                )

            SecurityRuntimeAuthorizationOutcome.REQUIRES_EMERGENCY_RESOLUTION ->
                resolveEmergencyAuthorization(context)
        }
    }

    private fun resolveEmergencyAuthorization(
        context: AuthContext
    ): AuthorizationResult {
        return when (
            val resolution = SecurityAccessSession.resolveEmergencyAccess(
                context.emergencyRequest
            )
        ) {
            is SecurityEmergencyAccessAuthority.AccessResolution.Approved ->
                AuthorizationResult.EmergencyAllowed(resolution.window)

            SecurityEmergencyAccessAuthority.AccessResolution.Expired ->
                AuthorizationResult.Denied(
                    DenialReason.EMERGENCY_WINDOW_EXPIRED
                )

            SecurityEmergencyAccessAuthority.AccessResolution.Missing ->
                AuthorizationResult.RequiresElevation(
                    ElevationReason.EMERGENCY_APPROVAL_REQUIRED
                )

            SecurityEmergencyAccessAuthority.AccessResolution.ReasonMismatch ->
                AuthorizationResult.Denied(
                    DenialReason.EMERGENCY_NOT_APPROVED
                )
        }
    }

    private fun mapDenialReason(
        code: SecurityRuntimeDecisionCode?
    ): DenialReason =
        when (code) {
            SecurityRuntimeDecisionCode.AUTH_CONTEXT_INVALID ->
                DenialReason.AUTH_CONTEXT_INVALID

            SecurityRuntimeDecisionCode.TRUSTED_BASE_ONLY_REQUIRED ->
                DenialReason.TRUSTED_BASE_ONLY_REQUIRED

            SecurityRuntimeDecisionCode.TRUST_NOT_SUFFICIENT ->
                DenialReason.TRUST_NOT_SUFFICIENT

            SecurityRuntimeDecisionCode.EMERGENCY_NOT_APPROVED ->
                DenialReason.EMERGENCY_NOT_APPROVED

            SecurityRuntimeDecisionCode.EMERGENCY_WINDOW_EXPIRED ->
                DenialReason.EMERGENCY_WINDOW_EXPIRED

            else ->
                DenialReason.OPERATION_NOT_ALLOWED
        }

    private fun mapElevationReason(
        code: SecurityRuntimeDecisionCode?
    ): ElevationReason =
        when (code) {
            SecurityRuntimeDecisionCode.EMERGENCY_APPROVAL_REQUIRED ->
                ElevationReason.EMERGENCY_APPROVAL_REQUIRED

            SecurityRuntimeDecisionCode.STRONGER_AUTH_REQUIRED ->
                ElevationReason.STRONGER_AUTH_REQUIRED

            else ->
                ElevationReason.RECENT_AUTH_REQUIRED
        }

    private fun mapLockReason(
        code: SecurityRuntimeDecisionCode?
    ): LockReason =
        when (code) {
            SecurityRuntimeDecisionCode.COMPROMISED ->
                LockReason.COMPROMISED

            SecurityRuntimeDecisionCode.EMERGENCY_REJECTED ->
                LockReason.EMERGENCY_REJECTED

            else ->
                LockReason.RECOVERY_REQUIRED
        }
}
