package com.lifeflow.core

import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.domain.connection.ConnectionEntry
import com.lifeflow.domain.connection.ConnectionState
import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.SecondBrainState
import com.lifeflow.domain.shopping.ShoppingState
import com.lifeflow.domain.shopping.TrackedItem

class LifeFlowOrchestratorModuleOperations(
    tierManager: TierManager,
    diaryRepository: LocalDiaryRepository?,
    memoryRepository: LocalMemoryRepository?,
    connectionRepository: LocalConnectionRepository?,
    shoppingRepository: LocalShoppingRepository?
) {
    private val diaryAccess = LifeFlowOrchestratorReadWriteModuleAccess(
        tierManager = tierManager,
        repository = diaryRepository,
        modulePolicy = LifeFlowOrchestratorPolicies.diaryModule
    )

    private val memoryAccess = LifeFlowOrchestratorReadWriteModuleAccess(
        tierManager = tierManager,
        repository = memoryRepository,
        modulePolicy = LifeFlowOrchestratorPolicies.memoryModule
    )

    private val connectionAccess = LifeFlowOrchestratorReadWriteModuleAccess(
        tierManager = tierManager,
        repository = connectionRepository,
        modulePolicy = LifeFlowOrchestratorPolicies.connectionModule
    )

    private val shoppingAccess = LifeFlowOrchestratorReadWriteModuleAccess(
        tierManager = tierManager,
        repository = shoppingRepository,
        modulePolicy = LifeFlowOrchestratorPolicies.shoppingModule
    )

    suspend fun loadDiaryState(identityInitialized: Boolean): ActionResult<ShadowDiaryState> =
        diaryAccess.read { repo ->
            lifeflowOrchestratorLoadDiaryState(repo, identityInitialized)
        }

    suspend fun saveDiaryEntry(entry: DiaryEntry): ActionResult<Unit> =
        diaryAccess.write { repo ->
            lifeflowOrchestratorSaveDiaryEntry(repo, entry)
        }

    suspend fun loadMemoryState(identityInitialized: Boolean): ActionResult<SecondBrainState> =
        memoryAccess.read { repo ->
            lifeflowOrchestratorLoadMemoryState(repo, identityInitialized)
        }

    suspend fun saveMemoryEntry(entry: MemoryEntry): ActionResult<Unit> =
        memoryAccess.write { repo ->
            lifeflowOrchestratorSaveMemoryEntry(repo, entry)
        }

    suspend fun loadConnectionState(identityInitialized: Boolean): ActionResult<ConnectionState> =
        connectionAccess.read { repo ->
            lifeflowOrchestratorLoadConnectionState(repo, identityInitialized)
        }

    suspend fun saveConnectionEntry(entry: ConnectionEntry): ActionResult<Unit> =
        connectionAccess.write { repo ->
            lifeflowOrchestratorSaveConnectionEntry(repo, entry)
        }

    suspend fun loadShoppingState(identityInitialized: Boolean): ActionResult<ShoppingState> =
        shoppingAccess.read { repo ->
            lifeflowOrchestratorLoadShoppingState(repo, identityInitialized)
        }

    suspend fun saveShoppingItem(item: TrackedItem): ActionResult<Unit> =
        shoppingAccess.write { repo ->
            lifeflowOrchestratorSaveShoppingItem(repo, item)
        }
}

