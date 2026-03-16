package com.lifeflow.core

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityRuleEngine
import com.lifeflow.security.TrustState
import java.util.UUID

/**
 * Bootstrap/auth boundary for orchestrator-sensitive identity initialization.
 * Fail-closed on missing session or compromised trust.
 */
internal suspend fun lifeflowOrchestratorBootstrapIdentityIfNeeded(
    identityRepository: IdentityRepository
): ActionResult<Unit> {
    lifeflowOrchestratorGateOrLocked("Identity bootstrap")?.let { return it }

    return try {
        SecurityRuleEngine.setTrustState(
            TrustState.VERIFIED,
            reason = "Auth session active (bootstrapIdentityIfNeeded)"
        )

        val active = identityRepository.getActiveIdentity()
        if (active == null) {
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

/**
 * Hard gate for sensitive operations.
 * Fail-closed:
 * - no session => locked
 * - compromised trust => locked
 */
@Suppress("SameParameterValue")
internal fun lifeflowOrchestratorGateOrLocked(reason: String): ActionResult.Locked? {
    if (SecurityRuleEngine.getTrustState() == TrustState.COMPROMISED) {
        SecurityAccessSession.clear()
        return ActionResult.Locked("COMPROMISED: $reason")
    }
    if (!SecurityAccessSession.isAuthorized()) {
        return ActionResult.Locked("AUTH_REQUIRED: $reason")
    }
    return null
}