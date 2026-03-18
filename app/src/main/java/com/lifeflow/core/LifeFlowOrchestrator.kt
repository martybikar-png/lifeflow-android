package com.lifeflow.core

import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.domain.connection.ConnectionEntry
import com.lifeflow.domain.connection.ConnectionState
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.core.digitaltwin.DigitalTwinState
import com.lifeflow.domain.core.tierGateMessage
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.ShadowDiaryState
import com.lifeflow.domain.habits.HabitsState
import com.lifeflow.domain.insights.InsightsState
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.SecondBrainState
import com.lifeflow.domain.shopping.ShoppingState
import com.lifeflow.domain.shopping.TrackedItem
import com.lifeflow.domain.timeline.AdaptiveTimelineState
import com.lifeflow.domain.wellbeing.HolisticWellbeingNode
import com.lifeflow.domain.wellbeing.WellbeingAssessment
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase

/**
 * LifeFlowOrchestrator — single entrypoint for all sensitive operations.
 *
 * Zero-bypass principle: UI/ViewModel calls only this orchestrator.
 * Fail-closed for security gating on all module operations.
 * Tier-gated: Core operations locked for FREE tier users.
 */
class LifeFlowOrchestrator(
    private val identityRepository: IdentityRepository,
    private val digitalTwinOrchestrator: DigitalTwinOrchestrator,
    private val getHealthConnectStatus: GetHealthConnectStatusUseCase,
    private val getHealthPermissions: GetHealthPermissionsUseCase,
    private val getGrantedHealthPermissions: GetGrantedHealthPermissionsUseCase,
    private val getStepsLast24h: GetStepsLast24hUseCase,
    private val getAvgHeartRateLast24h: GetAvgHeartRateLast24hUseCase,
    private val wellbeingNode: HolisticWellbeingNode = HolisticWellbeingNode(),
    private val tierManager: TierManager = TierManager(),
    private val diaryRepository: LocalDiaryRepository? = null,
    private val memoryRepository: LocalMemoryRepository? = null,
    private val connectionRepository: LocalConnectionRepository? = null,
    private val shoppingRepository: LocalShoppingRepository? = null
) {
    fun currentTier(): TierState = tierManager.currentTier()

    fun healthConnectUiState(): HealthConnectUiState {
        return when (getHealthConnectStatus()) {
            WellbeingRepository.SdkStatus.Available -> HealthConnectUiState.Available
            WellbeingRepository.SdkStatus.NotInstalled -> HealthConnectUiState.NotInstalled
            WellbeingRepository.SdkStatus.NotSupported -> HealthConnectUiState.NotSupported
            WellbeingRepository.SdkStatus.UpdateRequired -> HealthConnectUiState.UpdateRequired
        }
    }

    fun requiredHealthPermissionsSafe(): ActionResult<Set<String>> =
        try {
            ActionResult.Success(getHealthPermissions())
        } catch (t: Throwable) {
            ActionResult.Error("${t::class.java.simpleName}: ${t.message ?: "unknown error"}")
        }

    suspend fun grantedHealthPermissionsSafe(): ActionResult<Set<String>> =
        try {
            ActionResult.Success(getGrantedHealthPermissions())
        } catch (_: Throwable) {
            ActionResult.Success(emptySet())
        }

    suspend fun bootstrapIdentityIfNeeded(): ActionResult<Unit> {
        tierGateMessage(tierManager, TierState.CORE, "bootstrapIdentity")
            ?.let { return ActionResult.Locked(it) }
        return lifeflowOrchestratorBootstrapIdentityIfNeeded(identityRepository)
    }

    suspend fun refreshTwinBestEffort(identityInitialized: Boolean): ActionResult<DigitalTwinState> {
        tierGateMessage(tierManager, TierState.CORE, "refreshTwin")
            ?.let { return ActionResult.Locked(it) }
        val healthConnectState = healthConnectUiState()
        val permissionSnapshot = if (healthConnectState is HealthConnectUiState.Available) {
            lifeflowOrchestratorResolveMetricPermissionSnapshotSafe(
                getHealthPermissions = getHealthPermissions,
                getGrantedHealthPermissions = getGrantedHealthPermissions
            )
        } else lifeflowOrchestratorEmptyMetricPermissionSnapshot()

        val metricReadSnapshot = lifeflowOrchestratorReadMetricsBestEffort(
            healthConnectState = healthConnectState,
            permissionSnapshot = permissionSnapshot,
            getStepsLast24h = getStepsLast24h,
            getAvgHeartRateLast24h = getAvgHeartRateLast24h
        )
        return ActionResult.Success(
            lifeflowOrchestratorRefreshDigitalTwin(
                identityInitialized = identityInitialized,
                metricReadSnapshot = metricReadSnapshot,
                permissionSnapshot = permissionSnapshot,
                digitalTwinOrchestrator = digitalTwinOrchestrator
            )
        )
    }

    suspend fun refreshWellbeingSnapshot(identityInitialized: Boolean): ActionResult<WellbeingRefreshSnapshot> {
        tierGateMessage(tierManager, TierState.CORE, "refreshWellbeing")
            ?.let { return ActionResult.Locked(it) }

        val healthConnectState = healthConnectUiState()

        val requiredPermissions = when (val r = requiredHealthPermissionsSafe()) {
            is ActionResult.Success -> r.value
            is ActionResult.Locked -> return r
            is ActionResult.Error -> emptySet()
        }

        val grantedPermissions = when (val r = grantedHealthPermissionsSafe()) {
            is ActionResult.Success -> r.value
            is ActionResult.Locked -> return r
            is ActionResult.Error -> emptySet()
        }

        val permissionSnapshot = if (healthConnectState is HealthConnectUiState.Available) {
            lifeflowOrchestratorResolveMetricPermissionSnapshot(
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions
            )
        } else lifeflowOrchestratorEmptyMetricPermissionSnapshot()

        val metricReadSnapshot = lifeflowOrchestratorReadMetricsBestEffort(
            healthConnectState = healthConnectState,
            permissionSnapshot = permissionSnapshot,
            getStepsLast24h = getStepsLast24h,
            getAvgHeartRateLast24h = getAvgHeartRateLast24h
        )

        val digitalTwinState = lifeflowOrchestratorRefreshDigitalTwin(
            identityInitialized = identityInitialized,
            metricReadSnapshot = metricReadSnapshot,
            permissionSnapshot = permissionSnapshot,
            digitalTwinOrchestrator = digitalTwinOrchestrator
        )

        val wellbeingAssessment = wellbeingNode.assess(digitalTwinState)

        return ActionResult.Success(
            WellbeingRefreshSnapshot(
                healthConnectState = healthConnectState,
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions,
                stepsPermissionGranted = permissionSnapshot.stepsPermissionGranted,
                heartRatePermissionGranted = permissionSnapshot.heartRatePermissionGranted,
                digitalTwinState = digitalTwinState,
                wellbeingAssessment = wellbeingAssessment
            )
        )
    }

    // --- Module operations (Core tier only) ---

    suspend fun loadDiaryState(identityInitialized: Boolean): ActionResult<ShadowDiaryState> {
        tierGateMessage(tierManager, TierState.CORE, "loadDiary")
            ?.let { return ActionResult.Locked(it) }
        val repo = diaryRepository ?: return ActionResult.Locked("Diary repository not available")
        return lifeflowOrchestratorLoadDiaryState(repo, identityInitialized)
    }

    suspend fun saveDiaryEntry(entry: DiaryEntry): ActionResult<Unit> {
        tierGateMessage(tierManager, TierState.CORE, "saveDiary")
            ?.let { return ActionResult.Locked(it) }
        val repo = diaryRepository ?: return ActionResult.Locked("Diary repository not available")
        return lifeflowOrchestratorSaveDiaryEntry(repo, entry)
    }

    suspend fun loadMemoryState(identityInitialized: Boolean): ActionResult<SecondBrainState> {
        tierGateMessage(tierManager, TierState.CORE, "loadMemory")
            ?.let { return ActionResult.Locked(it) }
        val repo = memoryRepository ?: return ActionResult.Locked("Memory repository not available")
        return lifeflowOrchestratorLoadMemoryState(repo, identityInitialized)
    }

    suspend fun saveMemoryEntry(entry: MemoryEntry): ActionResult<Unit> {
        tierGateMessage(tierManager, TierState.CORE, "saveMemory")
            ?.let { return ActionResult.Locked(it) }
        val repo = memoryRepository ?: return ActionResult.Locked("Memory repository not available")
        return lifeflowOrchestratorSaveMemoryEntry(repo, entry)
    }

    suspend fun loadConnectionState(identityInitialized: Boolean): ActionResult<ConnectionState> {
        tierGateMessage(tierManager, TierState.CORE, "loadConnection")
            ?.let { return ActionResult.Locked(it) }
        val repo = connectionRepository ?: return ActionResult.Locked("Connection repository not available")
        return lifeflowOrchestratorLoadConnectionState(repo, identityInitialized)
    }

    suspend fun saveConnectionEntry(entry: ConnectionEntry): ActionResult<Unit> {
        tierGateMessage(tierManager, TierState.CORE, "saveConnection")
            ?.let { return ActionResult.Locked(it) }
        val repo = connectionRepository ?: return ActionResult.Locked("Connection repository not available")
        return lifeflowOrchestratorSaveConnectionEntry(repo, entry)
    }

    suspend fun loadShoppingState(identityInitialized: Boolean): ActionResult<ShoppingState> {
        tierGateMessage(tierManager, TierState.CORE, "loadShopping")
            ?.let { return ActionResult.Locked(it) }
        val repo = shoppingRepository ?: return ActionResult.Locked("Shopping repository not available")
        return lifeflowOrchestratorLoadShoppingState(repo, identityInitialized)
    }

    suspend fun saveShoppingItem(item: TrackedItem): ActionResult<Unit> {
        tierGateMessage(tierManager, TierState.CORE, "saveShopping")
            ?.let { return ActionResult.Locked(it) }
        val repo = shoppingRepository ?: return ActionResult.Locked("Shopping repository not available")
        return lifeflowOrchestratorSaveShoppingItem(repo, item)
    }

    fun computeTimeline(assessment: WellbeingAssessment): AdaptiveTimelineState =
        lifeflowOrchestratorComputeTimeline(assessment)

    fun computeHabits(timelineState: AdaptiveTimelineState): HabitsState =
        lifeflowOrchestratorComputeHabits(timelineState)

    fun computeInsights(
        wellbeing: WellbeingAssessment,
        timeline: AdaptiveTimelineState,
        diary: ShadowDiaryState
    ): InsightsState = lifeflowOrchestratorComputeInsights(wellbeing, timeline, diary)
}
