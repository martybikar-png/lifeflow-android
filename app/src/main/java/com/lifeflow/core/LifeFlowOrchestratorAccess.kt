package com.lifeflow.core

import com.lifeflow.security.SecurityRuntimeAccessDecision
import com.lifeflow.security.SecurityRuntimeAccessMode
import com.lifeflow.security.SecurityRuntimeAccessPolicy

internal enum class LifeFlowOrchestratorAccessMode {
    STANDARD_PROTECTED,
    TRUSTED_BASE_READ
}

internal fun lifeflowOrchestratorAuthorizeOperation(
    accessMode: LifeFlowOrchestratorAccessMode,
    reason: String
): ActionResult.Locked? {
    val decision = SecurityRuntimeAccessPolicy.decide(
        accessMode = accessMode.toSecurityRuntimeAccessMode()
    )

    return decision.toLocked(reason)
}

internal suspend fun <T> lifeflowOrchestratorRunAccessControlledOperation(
    accessMode: LifeFlowOrchestratorAccessMode,
    reason: String,
    block: suspend () -> ActionResult<T>
): ActionResult<T> {
    lifeflowOrchestratorAuthorizeOperation(
        accessMode = accessMode,
        reason = reason
    )?.let { return it }

    return block()
}

internal suspend fun <T> lifeflowOrchestratorRunAccessControlledCatchingOperation(
    accessMode: LifeFlowOrchestratorAccessMode,
    reason: String,
    defaultErrorMessage: String,
    block: suspend () -> T
): ActionResult<T> {
    return lifeflowOrchestratorRunAccessControlledOperation(
        accessMode = accessMode,
        reason = reason
    ) {
        try {
            ActionResult.Success(block())
        } catch (t: Throwable) {
            ActionResult.Error(t.message ?: defaultErrorMessage)
        }
    }
}

private fun LifeFlowOrchestratorAccessMode.toSecurityRuntimeAccessMode():
    SecurityRuntimeAccessMode =
    when (this) {
        LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED ->
            SecurityRuntimeAccessMode.STANDARD_PROTECTED

        LifeFlowOrchestratorAccessMode.TRUSTED_BASE_READ ->
            SecurityRuntimeAccessMode.TRUSTED_BASE_READ
    }

private fun SecurityRuntimeAccessDecision.toLocked(
    reason: String
): ActionResult.Locked? {
    if (allowed) return null
    return ActionResult.Locked("${denialCode ?: "ACCESS_DENIED"}: $reason")
}
