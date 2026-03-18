package com.lifeflow.core

import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.domain.connection.ConnectionEntry
import com.lifeflow.domain.connection.IntimacyConnectionEngine
import com.lifeflow.domain.connection.ConnectionState
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.ShadowDiaryCoreEngine
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.habits.AutonomousHabitsEngine
import com.lifeflow.domain.habits.HabitsState
import com.lifeflow.domain.insights.InsightsState
import com.lifeflow.domain.insights.QuantumInsightsEngine
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.SecondBrainEngine
import com.lifeflow.domain.memory.SecondBrainState
import com.lifeflow.domain.shopping.PredictiveShoppingEngine
import com.lifeflow.domain.shopping.ShoppingState
import com.lifeflow.domain.shopping.TrackedItem
import com.lifeflow.domain.timeline.AdaptiveTimelineEngine
import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

/**
 * Module data access layer — consent-gated reads and writes for all domain modules.
 *
 * All operations go through lifeflowOrchestratorGateOrLocked.
 * Fail-closed on missing session or compromised trust.
 */

// --- Diary ---

internal suspend fun lifeflowOrchestratorLoadDiaryState(
    diaryRepository: LocalDiaryRepository,
    identityInitialized: Boolean
): ActionResult<ShadowDiaryState> {
    lifeflowOrchestratorGateOrLocked("Diary read")?.let { return it }
    return try {
        val entries = diaryRepository.loadAllEntries()
        val state = ShadowDiaryCoreEngine().compute(entries, identityInitialized)
        ActionResult.Success(state)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Diary load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveDiaryEntry(
    diaryRepository: LocalDiaryRepository,
    entry: DiaryEntry
): ActionResult<Unit> {
    lifeflowOrchestratorGateOrLocked("Diary write")?.let { return it }
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
    lifeflowOrchestratorGateOrLocked("Memory read")?.let { return it }
    return try {
        val entries = memoryRepository.loadAllEntries()
        val state = SecondBrainEngine().compute(entries, identityInitialized)
        ActionResult.Success(state)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Memory load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveMemoryEntry(
    memoryRepository: LocalMemoryRepository,
    entry: MemoryEntry
): ActionResult<Unit> {
    lifeflowOrchestratorGateOrLocked("Memory write")?.let { return it }
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
    lifeflowOrchestratorGateOrLocked("Connection read")?.let { return it }
    return try {
        val entries = connectionRepository.loadAllEntries()
        val state = IntimacyConnectionEngine().compute(entries, identityInitialized)
        ActionResult.Success(state)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Connection load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveConnectionEntry(
    connectionRepository: LocalConnectionRepository,
    entry: ConnectionEntry
): ActionResult<Unit> {
    lifeflowOrchestratorGateOrLocked("Connection write")?.let { return it }
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
    lifeflowOrchestratorGateOrLocked("Shopping read")?.let { return it }
    return try {
        val items = shoppingRepository.loadAllItems()
        val state = PredictiveShoppingEngine().compute(items, identityInitialized)
        ActionResult.Success(state)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Shopping load failed")
    }
}

internal suspend fun lifeflowOrchestratorSaveShoppingItem(
    shoppingRepository: LocalShoppingRepository,
    item: TrackedItem
): ActionResult<Unit> {
    lifeflowOrchestratorGateOrLocked("Shopping write")?.let { return it }
    return try {
        shoppingRepository.saveItem(item)
        ActionResult.Success(Unit)
    } catch (t: Throwable) {
        ActionResult.Error(t.message ?: "Shopping save failed")
    }
}

// --- Timeline + Habits + Insights (derived, no persistence) ---

internal fun lifeflowOrchestratorComputeTimeline(
    assessment: WellbeingAssessment
): AdaptiveTimelineState {
    return AdaptiveTimelineEngine().compute(assessment)
}

internal fun lifeflowOrchestratorComputeHabits(
    timelineState: AdaptiveTimelineState
): HabitsState {
    return AutonomousHabitsEngine().compute(timelineState)
}

internal fun lifeflowOrchestratorComputeInsights(
    wellbeing: WellbeingAssessment,
    timeline: AdaptiveTimelineState,
    diary: ShadowDiaryState
): InsightsState {
    return QuantumInsightsEngine().compute(
        wellbeing = wellbeing,
        timeline = timeline,
        diary = diary
    )
}
