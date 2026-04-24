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
 * Module data access layer — pure repository and derivation operations.
 *
 * Security/tier/repository boundary checks live in the orchestrator access layer.
 */

// --- Diary ---

internal suspend fun lifeflowOrchestratorLoadDiaryState(
    diaryRepository: LocalDiaryRepository,
    identityInitialized: Boolean
): ActionResult<ShadowDiaryState> {
    return try {
        val entries = diaryRepository.loadAllEntries()
        ActionResult.Success(
            ShadowDiaryCoreEngine().compute(entries, identityInitialized)
        )
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Diary load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveDiaryEntry(
    diaryRepository: LocalDiaryRepository,
    entry: DiaryEntry
): ActionResult<Unit> {
    return try {
        diaryRepository.saveEntry(entry)
        ActionResult.Success(Unit)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Diary save failed")
    }
}

// --- Memory ---

internal suspend fun lifeflowOrchestratorLoadMemoryState(
    memoryRepository: LocalMemoryRepository,
    identityInitialized: Boolean
): ActionResult<SecondBrainState> {
    return try {
        val entries = memoryRepository.loadAllEntries()
        ActionResult.Success(
            SecondBrainEngine().compute(entries, identityInitialized)
        )
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Memory load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveMemoryEntry(
    memoryRepository: LocalMemoryRepository,
    entry: MemoryEntry
): ActionResult<Unit> {
    return try {
        memoryRepository.saveEntry(entry)
        ActionResult.Success(Unit)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Memory save failed")
    }
}

// --- Connection ---

internal suspend fun lifeflowOrchestratorLoadConnectionState(
    connectionRepository: LocalConnectionRepository,
    identityInitialized: Boolean
): ActionResult<ConnectionState> {
    return try {
        val entries = connectionRepository.loadAllEntries()
        ActionResult.Success(
            IntimacyConnectionEngine().compute(entries, identityInitialized)
        )
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Connection load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveConnectionEntry(
    connectionRepository: LocalConnectionRepository,
    entry: ConnectionEntry
): ActionResult<Unit> {
    return try {
        connectionRepository.saveEntry(entry)
        ActionResult.Success(Unit)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Connection save failed")
    }
}

// --- Shopping ---

internal suspend fun lifeflowOrchestratorLoadShoppingState(
    shoppingRepository: LocalShoppingRepository,
    identityInitialized: Boolean
): ActionResult<ShoppingState> {
    return try {
        val items = shoppingRepository.loadAllItems()
        ActionResult.Success(
            PredictiveShoppingEngine().compute(items, identityInitialized)
        )
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Shopping load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveShoppingItem(
    shoppingRepository: LocalShoppingRepository,
    item: TrackedItem
): ActionResult<Unit> {
    return try {
        shoppingRepository.saveItem(item)
        ActionResult.Success(Unit)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Shopping save failed")
    }
}
