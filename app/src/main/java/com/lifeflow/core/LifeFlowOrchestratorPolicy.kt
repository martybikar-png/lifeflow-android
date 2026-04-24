package com.lifeflow.core

import com.lifeflow.domain.security.DomainOperation

internal data class LifeFlowOrchestratorCoreOperationPolicy(
    val operationName: String
)

internal data class LifeFlowOrchestratorAccessPolicy(
    val operation: DomainOperation,
    val detail: String,
    val contextKind: LifeFlowOrchestratorAuthContextKind
)

internal data class LifeFlowOrchestratorReadWriteModulePolicy(
    val readOperation: LifeFlowOrchestratorCoreOperationPolicy,
    val readAccess: LifeFlowOrchestratorAccessPolicy,
    val writeOperation: LifeFlowOrchestratorCoreOperationPolicy,
    val writeAccess: LifeFlowOrchestratorAccessPolicy,
    val repositoryUnavailableMessage: String
)

internal object LifeFlowOrchestratorPolicies {
    val bootstrapIdentity = LifeFlowOrchestratorCoreOperationPolicy(
        operationName = "bootstrapIdentity"
    )

    val bootstrapIdentityReadAccess = LifeFlowOrchestratorAccessPolicy(
        operation = DomainOperation.READ_ACTIVE_IDENTITY,
        detail = "Identity bootstrap",
        contextKind = LifeFlowOrchestratorAuthContextKind.STRICT_PROTECTED_CURRENT_SESSION
    )

    val bootstrapIdentityWriteAccess = LifeFlowOrchestratorAccessPolicy(
        operation = DomainOperation.SAVE_IDENTITY,
        detail = "Identity bootstrap",
        contextKind = LifeFlowOrchestratorAuthContextKind.STRICT_PROTECTED_CURRENT_SESSION
    )

    val refreshTwin = LifeFlowOrchestratorCoreOperationPolicy(
        operationName = "refreshTwin"
    )

    val refreshTwinAccess = LifeFlowOrchestratorAccessPolicy(
        operation = DomainOperation.READ_TWIN_SNAPSHOT,
        detail = "Twin refresh",
        contextKind = LifeFlowOrchestratorAuthContextKind.TRUSTED_BASE_READ_ONLY_CURRENT_SESSION
    )

    val refreshWellbeing = LifeFlowOrchestratorCoreOperationPolicy(
        operationName = "refreshWellbeing"
    )

    val refreshWellbeingAccess = LifeFlowOrchestratorAccessPolicy(
        operation = DomainOperation.READ_WELLBEING_SNAPSHOT,
        detail = "Wellbeing refresh",
        contextKind = LifeFlowOrchestratorAuthContextKind.TRUSTED_BASE_READ_ONLY_CURRENT_SESSION
    )

    val diaryModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadDiary"),
        readAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.READ_DIARY,
            detail = "Diary read",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveDiary"),
        writeAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.WRITE_DIARY,
            detail = "Diary write",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        repositoryUnavailableMessage = "Diary repository not available"
    )

    val memoryModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadMemory"),
        readAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.READ_MEMORY,
            detail = "Memory read",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveMemory"),
        writeAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.WRITE_MEMORY,
            detail = "Memory write",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        repositoryUnavailableMessage = "Memory repository not available"
    )

    val connectionModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadConnection"),
        readAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.READ_CONNECTION,
            detail = "Connection read",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveConnection"),
        writeAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.WRITE_CONNECTION,
            detail = "Connection write",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        repositoryUnavailableMessage = "Connection repository not available"
    )

    val shoppingModule = LifeFlowOrchestratorReadWriteModulePolicy(
        readOperation = LifeFlowOrchestratorCoreOperationPolicy("loadShopping"),
        readAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.READ_SHOPPING,
            detail = "Shopping read",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        writeOperation = LifeFlowOrchestratorCoreOperationPolicy("saveShopping"),
        writeAccess = LifeFlowOrchestratorAccessPolicy(
            operation = DomainOperation.WRITE_SHOPPING,
            detail = "Shopping write",
            contextKind = LifeFlowOrchestratorAuthContextKind.STANDARD_PROTECTED_CURRENT_SESSION
        ),
        repositoryUnavailableMessage = "Shopping repository not available"
    )
}
