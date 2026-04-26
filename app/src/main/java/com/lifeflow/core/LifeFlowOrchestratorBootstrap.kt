package com.lifeflow.core

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityIdentityBootstrapAuthorization
import com.lifeflow.security.SecurityLockedException
import com.lifeflow.security.SecurityLockedReason
import com.lifeflow.security.withDetail
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
 * - this layer now consumes shared orchestrator access policy inputs
 *   instead of calling the security gate directly
 */
internal suspend fun lifeflowOrchestratorBootstrapIdentityIfNeeded(
    identityRepository: IdentityRepository,
    readAccessPolicy: LifeFlowOrchestratorAccessPolicy,
    writeAccessPolicy: LifeFlowOrchestratorAccessPolicy
): ActionResult<Unit> {
    authorizeBootstrapAccess(readAccessPolicy)?.let { return it }

    return try {
        SecurityIdentityBootstrapAuthorization.withFreshBootstrapAuthorization(
            reason = "Identity bootstrap"
        ) {
            val active = identityRepository.getActiveIdentity()

            if (active == null) {
                val writeLock = authorizeBootstrapAccess(writeAccessPolicy)
                if (writeLock != null) {
                    return@withFreshBootstrapAuthorization writeLock
                }

                val newIdentity = LifeFlowIdentity(
                    id = UUID.randomUUID(),
                    createdAtEpochMillis = System.currentTimeMillis(),
                    isActive = true
                )
                identityRepository.save(newIdentity)
            }

            ActionResult.Success(Unit)
        }
    } catch (e: SecurityLockedException) {
        SecurityAccessSession.clear()
        ActionResult.Locked(e.lockedReason)
    } catch (e: SecurityException) {
        SecurityAccessSession.clear()
        ActionResult.Locked(
            SecurityLockedReason.AUTH_REQUIRED.withDetail(
                e.message ?: "Security denied"
            )
        )
    } catch (t: Throwable) {
        SecurityAccessSession.clear()
        ActionResult.Error(t.message ?: "Bootstrap failed")
    }
}

private fun authorizeBootstrapAccess(
    accessPolicy: LifeFlowOrchestratorAccessPolicy
): ActionResult.Locked? {
    return lifeflowOrchestratorAuthorizeOperation(
        operation = accessPolicy.operation,
        detail = accessPolicy.detail,
        contextKind = accessPolicy.contextKind
    )
}
