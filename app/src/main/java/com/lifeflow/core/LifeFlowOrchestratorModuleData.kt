package com.lifeflow.core

import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.domain.connection.ConnectionEntry
import com.lifeflow.domain.connection.ConnectionState
import com.lifeflow.domain.connection.IntimacyConnectionEngine
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.ShadowDiaryCoreEngine
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.SecondBrainEngine
import com.lifeflow.domain.memory.SecondBrainState
import com.lifeflow.domain.shopping.PredictiveShoppingEngine
import com.lifeflow.domain.shopping.ShoppingState
import com.lifeflow.domain.shopping.TrackedItem

/**
 * Module data access layer — consent-gated reads and writes for persistent domain modules.
 *
 * All operations go through the shared orchestrator access boundary.
 * Fail-closed on missing session or compromised trust.
 */

// --- Diary ---

internal suspend fun lifeflowOrchestratorLoadDiaryState(
    diaryRepository: LocalDiaryRepository,
    identityInitialized: Boolean
): ActionResult<ShadowDiaryState> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Diary read",
        defaultErrorMessage = "Diary load failed"
    ) {
        val entries = diaryRepository.loadAllEntries()
        ShadowDiaryCoreEngine().compute(entries, identityInitialized)
    }
}

internal suspend fun lifeflowOrchestratorSaveDiaryEntry(
    diaryRepository: LocalDiaryRepository,
    entry: DiaryEntry
): ActionResult<Unit> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Diary write",
        defaultErrorMessage = "Diary save failed"
    ) {
        diaryRepository.saveEntry(entry)
    }
}

// --- Memory ---

internal suspend fun lifeflowOrchestratorLoadMemoryState(
    memoryRepository: LocalMemoryRepository,
    identityInitialized: Boolean
): ActionResult<SecondBrainState> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Memory read",
        defaultErrorMessage = "Memory load failed"
    ) {
        val entries = memoryRepository.loadAllEntries()
        SecondBrainEngine().compute(entries, identityInitialized)
    }
}

internal suspend fun lifeflowOrchestratorSaveMemoryEntry(
    memoryRepository: LocalMemoryRepository,
    entry: MemoryEntry
): ActionResult<Unit> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Memory write",
        defaultErrorMessage = "Memory save failed"
    ) {
        memoryRepository.saveEntry(entry)
    }
}

// --- Connection ---

internal suspend fun lifeflowOrchestratorLoadConnectionState(
    connectionRepository: LocalConnectionRepository,
    identityInitialized: Boolean
): ActionResult<ConnectionState> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Connection read",
        defaultErrorMessage = "Connection load failed"
    ) {
        val entries = connectionRepository.loadAllEntries()
        IntimacyConnectionEngine().compute(entries, identityInitialized)
    }
}

internal suspend fun lifeflowOrchestratorSaveConnectionEntry(
    connectionRepository: LocalConnectionRepository,
    entry: ConnectionEntry
): ActionResult<Unit> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Connection write",
        defaultErrorMessage = "Connection save failed"
    ) {
        connectionRepository.saveEntry(entry)
    }
}

// --- Shopping ---

internal suspend fun lifeflowOrchestratorLoadShoppingState(
    shoppingRepository: LocalShoppingRepository,
    identityInitialized: Boolean
): ActionResult<ShoppingState> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Shopping read",
        defaultErrorMessage = "Shopping load failed"
    ) {
        val items = shoppingRepository.loadAllItems()
        PredictiveShoppingEngine().compute(items, identityInitialized)
    }
}

internal suspend fun lifeflowOrchestratorSaveShoppingItem(
    shoppingRepository: LocalShoppingRepository,
    item: TrackedItem
): ActionResult<Unit> {
    return lifeflowOrchestratorRunAccessControlledCatchingOperation(
        accessMode = LifeFlowOrchestratorAccessMode.STANDARD_PROTECTED,
        reason = "Shopping write",
        defaultErrorMessage = "Shopping save failed"
    ) {
        shoppingRepository.saveItem(item)
    }
}
