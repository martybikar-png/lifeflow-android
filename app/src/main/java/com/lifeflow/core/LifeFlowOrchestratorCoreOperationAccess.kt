package com.lifeflow.core

import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.tierGateMessage

internal class LifeFlowOrchestratorCoreOperationAccess(
    private val tierManager: TierManager
) {
    suspend fun <T> run(
        operationPolicy: LifeFlowOrchestratorCoreOperationPolicy,
        accessPolicy: LifeFlowOrchestratorAccessPolicy? = null,
        block: suspend () -> ActionResult<T>
    ): ActionResult<T> {
        tierGateMessage(tierManager, TierState.CORE, operationPolicy.operationName)
            ?.let { return ActionResult.Locked(it) }

        if (accessPolicy == null) {
            return block()
        }

        return lifeflowOrchestratorRunAccessControlledOperation(
            operation = accessPolicy.operation,
            detail = accessPolicy.detail,
            contextKind = accessPolicy.contextKind,
            block = block
        )
    }

    suspend fun <T> runValue(
        operationPolicy: LifeFlowOrchestratorCoreOperationPolicy,
        accessPolicy: LifeFlowOrchestratorAccessPolicy? = null,
        block: suspend () -> T
    ): ActionResult<T> {
        return run(
            operationPolicy = operationPolicy,
            accessPolicy = accessPolicy
        ) {
            ActionResult.Success(block())
        }
    }
}
