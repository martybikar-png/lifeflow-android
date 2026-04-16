package com.lifeflow

import android.content.Context
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.core.LifeFlowOrchestratorDerivationOperations
import com.lifeflow.core.LifeFlowOrchestratorModuleOperations
import com.lifeflow.core.LifeFlowOrchestratorProtectedOperations
import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.TierManager
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.security.TrustStatePort
import com.lifeflow.domain.wellbeing.HolisticWellbeingNode
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityTrustStatePortAdapter

internal class MainRuntimeBindings(
    applicationContext: Context,
    identityRepository: IdentityRepository,
    digitalTwinOrchestrator: DigitalTwinOrchestrator,
    getHealthConnectStatusUseCase: GetHealthConnectStatusUseCase,
    getHealthPermissionsUseCase: GetHealthPermissionsUseCase,
    getGrantedHealthPermissionsUseCase: GetGrantedHealthPermissionsUseCase,
    getStepsLast24hUseCase: GetStepsLast24hUseCase,
    getAvgHeartRateLast24hUseCase: GetAvgHeartRateLast24hUseCase,
    diaryRepository: LocalDiaryRepository,
    memoryRepository: LocalMemoryRepository,
    connectionRepository: LocalConnectionRepository,
    shoppingRepository: LocalShoppingRepository,
    performVaultReset: suspend () -> Unit
) {
    private val sessionAuthorizationChecker: () -> Boolean = {
        SecurityAccessSession.isAuthorized(applicationContext)
    }

    private val sessionClearAction: () -> Unit = {
        SecurityAccessSession.clear()
    }

    private val mainTrustStatePort: TrustStatePort by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SecurityTrustStatePortAdapter()
    }

    private val mainTierManager: TierManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        TierManager()
    }

    private val mainWellbeingNode: HolisticWellbeingNode by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        HolisticWellbeingNode()
    }

    private val protectedOperations: LifeFlowOrchestratorProtectedOperations by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LifeFlowOrchestratorProtectedOperations(
            identityRepository = identityRepository,
            getHealthPermissions = getHealthPermissionsUseCase,
            getGrantedHealthPermissions = getGrantedHealthPermissionsUseCase,
            getHealthConnectStatus = getHealthConnectStatusUseCase,
            getStepsLast24h = getStepsLast24hUseCase,
            getAvgHeartRateLast24h = getAvgHeartRateLast24hUseCase,
            digitalTwinOrchestrator = digitalTwinOrchestrator,
            wellbeingNode = mainWellbeingNode,
            tierManager = mainTierManager
        )
    }

    private val moduleOperations: LifeFlowOrchestratorModuleOperations by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LifeFlowOrchestratorModuleOperations(
            tierManager = mainTierManager,
            diaryRepository = diaryRepository,
            memoryRepository = memoryRepository,
            connectionRepository = connectionRepository,
            shoppingRepository = shoppingRepository
        )
    }

    private val derivationOperations: LifeFlowOrchestratorDerivationOperations by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LifeFlowOrchestratorDerivationOperations()
    }

    val mainOrchestrator: LifeFlowOrchestrator by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LifeFlowOrchestrator(
            protectedOperations = protectedOperations,
            moduleOperations = moduleOperations,
            derivationOperations = derivationOperations
        )
    }

    val mainViewModelFactory: MainViewModelFactory by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MainViewModelFactory(
            orchestrator = mainOrchestrator,
            performVaultReset = performVaultReset,
            isSessionAuthorized = sessionAuthorizationChecker,
            clearSession = sessionClearAction,
            trustStatePort = mainTrustStatePort
        )
    }
}
