package com.lifeflow.core

import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.tierGateMessage

internal class LifeFlowOrchestratorReadWriteModuleAccess<R>(
    private val tierManager: TierManager,
    private val repository: R?,
    private val modulePolicy: LifeFlowOrchestratorReadWriteModulePolicy
) {
    suspend fun <T> read(block: suspend (R) -> ActionResult<T>): ActionResult<T> {
        return run(
            operationPolicy = modulePolicy.readOperation,
            block = block
        )
    }

    suspend fun <T> write(block: suspend (R) -> ActionResult<T>): ActionResult<T> {
        return run(
            operationPolicy = modulePolicy.writeOperation,
            block = block
        )
    }

    private suspend fun <T> run(
        operationPolicy: LifeFlowOrchestratorCoreOperationPolicy,
        block: suspend (R) -> ActionResult<T>
    ): ActionResult<T> {
        tierGateMessage(tierManager, TierState.CORE, operationPolicy.operationName)
            ?.let { return ActionResult.Locked(it) }

        val resolvedRepository = repository
            ?: return ActionResult.Locked(modulePolicy.repositoryUnavailableMessage)

        return block(resolvedRepository)
    }
}
