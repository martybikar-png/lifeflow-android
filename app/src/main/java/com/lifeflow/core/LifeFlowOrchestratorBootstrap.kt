package com.lifeflow.core

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.domain.security.AuthContext
import com.lifeflow.domain.security.AuthorizationResult
import com.lifeflow.domain.security.DenialReason
import com.lifeflow.domain.security.DomainOperation
import com.lifeflow.domain.security.ElevationReason
import com.lifeflow.domain.security.LockReason
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityAuthorizationPortAdapter
import java.util.UUID

/**
 * Bootstrap/auth boundary for orchestrator-sensitive identity initialization.
 * Fail-closed on:
 * - missing session
 * - emergency-limited trust
 * - compromised trust
 *
 * Important:
 * - trust elevation must NOT happen here
 * - this layer consumes the canonical authorization boundary instead of
 *   mutating trust state locally
 */
internal suspend fun lifeflowOrchestratorBootstrapIdentityIfNeeded(
    identityRepository: IdentityRepository
): ActionResult<Unit> {
    authorizeBootstrapOperation(
        operation = DomainOperation.READ_ACTIVE_IDENTITY,
        reason = "Identity bootstrap"
    )?.let { return it }

    return try {
        val active = identityRepository.getActiveIdentity()

        if (active == null) {
            authorizeBootstrapOperation(
                operation = DomainOperation.SAVE_IDENTITY,
                reason = "Identity bootstrap"
            )?.let { return it }

            val newIdentity = LifeFlowIdentity(
                id = UUID.randomUUID(),
                createdAtEpochMillis = System.currentTimeMillis(),
                isActive = true
            )
            identityRepository.save(newIdentity)
        }

        ActionResult.Success(Unit)
    } catch (e: SecurityException) {
        SecurityAccessSession.clear()
        ActionResult.Locked(e.message ?: "Security denied")
    } catch (t: Throwable) {
        SecurityAccessSession.clear()
        ActionResult.Error(t.message ?: "Bootstrap failed")
    }
}

private fun authorizeBootstrapOperation(
    operation: DomainOperation,
    reason: String
): ActionResult.Locked? {
    val authorization = SecurityAuthorizationPortAdapter().authorize(
        operation = operation,
        context = currentBootstrapStrictAuthContext()
    )

    return when (authorization) {
        AuthorizationResult.Allowed -> null

        is AuthorizationResult.EmergencyAllowed ->
            ActionResult.Locked("EMERGENCY_LIMITED: $reason")

        is AuthorizationResult.Denied ->
            ActionResult.Locked(
                bootstrapDeniedReasonToLockedReason(
                    denialReason = authorization.reason,
                    reason = reason
                )
            )

        is AuthorizationResult.RequiresElevation ->
            ActionResult.Locked(
                bootstrapElevationReasonToLockedReason(
                    elevationReason = authorization.reason,
                    reason = reason
                )
            )

        is AuthorizationResult.Locked ->
            ActionResult.Locked(
                bootstrapLockReasonToLockedReason(
                    lockReason = authorization.reason,
                    reason = reason
                )
            )
    }
}

private fun currentBootstrapStrictAuthContext(): AuthContext {
    return AuthContext(
        hasRecentAuthentication = SecurityAccessSession.isAuthorized(),
        requiresStrictAuth = true
    )
}

private fun bootstrapDeniedReasonToLockedReason(
    denialReason: DenialReason,
    reason: String
): String =
    when (denialReason) {
        DenialReason.AUTH_CONTEXT_INVALID,
        DenialReason.OPERATION_NOT_ALLOWED,
        DenialReason.POLICY_REJECTED,
        DenialReason.TRUST_NOT_SUFFICIENT ->
            "AUTH_REQUIRED: $reason"

        DenialReason.TRUSTED_BASE_ONLY_REQUIRED,
        DenialReason.EMERGENCY_NOT_APPROVED,
        DenialReason.EMERGENCY_WINDOW_EXPIRED ->
            "EMERGENCY_LIMITED: $reason"
    }

private fun bootstrapElevationReasonToLockedReason(
    elevationReason: ElevationReason,
    reason: String
): String =
    when (elevationReason) {
        ElevationReason.RECENT_AUTH_REQUIRED,
        ElevationReason.STRONGER_AUTH_REQUIRED ->
            "AUTH_REQUIRED: $reason"

        ElevationReason.EMERGENCY_APPROVAL_REQUIRED ->
            "EMERGENCY_LIMITED: $reason"
    }

private fun bootstrapLockReasonToLockedReason(
    lockReason: LockReason,
    reason: String
): String =
    when (lockReason) {
        LockReason.COMPROMISED ->
            "COMPROMISED: $reason"

        LockReason.EMERGENCY_REJECTED ->
            "EMERGENCY_LIMITED: $reason"

        LockReason.LOCKED_OUT,
        LockReason.RECOVERY_REQUIRED ->
            "AUTH_REQUIRED: $reason"
    }
