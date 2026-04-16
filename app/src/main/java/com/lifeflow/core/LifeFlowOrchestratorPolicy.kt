package com.lifeflow.core

internal data class LifeFlowOrchestratorCoreOperationPolicy(
    val operationName: String
)

internal data class LifeFlowOrchestratorAccessPolicy(
    val accessMode: LifeFlowOrchestratorAccessMode,
    val reason: String
)

internal data class LifeFlowOrchestratorReadWriteModulePolicy(
    val readOperation: LifeFlowOrchestratorCoreOperationPolicy,
    val writeOperation: LifeFlowOrchestratorCoreOperationPolicy,
    val repositoryUnavailableMessage: String
)

internal object LifeFlowOrchestratorPolicies {
    val bootstrapIdentity = LifeFlowOrchestratorCoreOperationPolicy(
        operationName = "bootstrapIdentity"
    )

    val refreshTwin = LifeFlowOrchestratorCoreOperationPolicy(
        operationName = "refreshTwin"
    )

    val refreshTwinAccess = LifeFlowOrchestratorAccessPolicy(
        accessMode = LifeFlowOrchestratorAccessMode.TRUSTED_BASE_READ,
        reason = "Twin refresh"
    )

    val refreshWellbeing = LifeFlowOrchestratorCoreOperationPolicy(
        operationName = "refreshWellbeing"
    )

    val refreshWellbeingAccess = LifeFlowOrchestratorAccessPolicy(
        accessMode = LifeFlowOrchestratorAccessMode.TRUSTED_BASE_READ,
        reason = "Wellbeing refresh"
    )

    val diaryModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadDiary"),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveDiary"),
        repositoryUnavailableMessage = "Diary repository not available"
    )

    val memoryModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadMemory"),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveMemory"),
        repositoryUnavailableMessage = "Memory repository not available"
    )

    val connectionModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadConnection"),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveConnection"),
        repositoryUnavailableMessage = "Connection repository not available"
    )

    val shoppingModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadShopping"),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveShopping"),
        repositoryUnavailableMessage = "Shopping repository not available"
    )
}
