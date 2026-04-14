package com.lifeflow

import android.content.Context
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.security.TrustStatePort
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

    val mainOrchestrator: LifeFlowOrchestrator by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LifeFlowOrchestrator(
            identityRepository = identityRepository,
            digitalTwinOrchestrator = digitalTwinOrchestrator,
            getHealthConnectStatus = getHealthConnectStatusUseCase,
            getHealthPermissions = getHealthPermissionsUseCase,
            getGrantedHealthPermissions = getGrantedHealthPermissionsUseCase,
            getStepsLast24h = getStepsLast24hUseCase,
            getAvgHeartRateLast24h = getAvgHeartRateLast24hUseCase,
            diaryRepository = diaryRepository,
            memoryRepository = memoryRepository,
            connectionRepository = connectionRepository,
            shoppingRepository = shoppingRepository
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
