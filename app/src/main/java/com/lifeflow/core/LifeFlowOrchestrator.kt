package com.lifeflow.core

import com.lifeflow.domain.connection.ConnectionEntry
import com.lifeflow.domain.connection.ConnectionState
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.habits.HabitsState
import com.lifeflow.domain.insights.InsightsState
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.SecondBrainState
import com.lifeflow.domain.shopping.ShoppingState
import com.lifeflow.domain.shopping.TrackedItem
import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.wellbeing.WellbeingAssessment

/**
 * LifeFlowOrchestrator — single entrypoint for all sensitive operations.
 *
 * Zero-bypass principle: UI/ViewModel calls only this orchestrator.
 * Fail-closed for security gating on all module operations.
 * Tier-gated: Core operations locked for FREE tier users.
 */
class LifeFlowOrchestrator(
    private val protectedOperations: LifeFlowOrchestratorProtectedOperations,
    private val moduleOperations: LifeFlowOrchestratorModuleOperations,
    private val derivationOperations: LifeFlowOrchestratorDerivationOperations
) {
    fun currentTier(): TierState =
        protectedOperations.currentTier()

    fun healthConnectUiState(): HealthConnectUiState =
        protectedOperations.healthConnectUiState()

    fun requiredHealthPermissionsSafe(): ActionResult<Set<String>> =
        protectedOperations.requiredHealthPermissionsSafe()

    suspend fun grantedHealthPermissionsSafe(): ActionResult<Set<String>> =
        protectedOperations.grantedHealthPermissionsSafe()

    suspend fun bootstrapIdentityIfNeeded(): ActionResult<Unit> =
        protectedOperations.bootstrapIdentityIfNeeded()

    suspend fun refreshTwinBestEffort(identityInitialized: Boolean): ActionResult<com.lifeflow.domain.core.digitaltwin.DigitalTwinState> =
        protectedOperations.refreshTwinBestEffort(identityInitialized)

    suspend fun refreshWellbeingSnapshot(identityInitialized: Boolean): ActionResult<WellbeingRefreshSnapshot> =
        protectedOperations.refreshWellbeingSnapshot(identityInitialized)

    suspend fun loadDiaryState(identityInitialized: Boolean): ActionResult<ShadowDiaryState> =
        moduleOperations.loadDiaryState(identityInitialized)

    suspend fun saveDiaryEntry(entry: DiaryEntry): ActionResult<Unit> =
        moduleOperations.saveDiaryEntry(entry)

    suspend fun loadMemoryState(identityInitialized: Boolean): ActionResult<SecondBrainState> =
        moduleOperations.loadMemoryState(identityInitialized)

    suspend fun saveMemoryEntry(entry: MemoryEntry): ActionResult<Unit> =
        moduleOperations.saveMemoryEntry(entry)

    suspend fun loadConnectionState(identityInitialized: Boolean): ActionResult<ConnectionState> =
        moduleOperations.loadConnectionState(identityInitialized)

    suspend fun saveConnectionEntry(entry: ConnectionEntry): ActionResult<Unit> =
        moduleOperations.saveConnectionEntry(entry)

    suspend fun loadShoppingState(identityInitialized: Boolean): ActionResult<ShoppingState> =
        moduleOperations.loadShoppingState(identityInitialized)

    suspend fun saveShoppingItem(item: TrackedItem): ActionResult<Unit> =
        moduleOperations.saveShoppingItem(item)

    fun computeTimeline(assessment: WellbeingAssessment): AdaptiveTimelineState =
        derivationOperations.computeTimeline(assessment)

    fun computeHabits(timelineState: AdaptiveTimelineState): HabitsState =
        derivationOperations.computeHabits(timelineState)

    fun computeInsights(
        wellbeing: WellbeingAssessment,
        timeline: AdaptiveTimelineState,
        diary: ShadowDiaryState
    ): InsightsState = derivationOperations.computeInsights(
        wellbeing = wellbeing,
        timeline = timeline,
        diary = diary
    )
}
