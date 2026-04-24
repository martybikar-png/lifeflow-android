package com.lifeflow

import android.content.Context
import com.lifeflow.domain.core.TierState
import com.lifeflow.domain.core.TierTruthSnapshot
import com.lifeflow.domain.core.digitaltwin.DigitalTwinEngine
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.security.LifeFlowSecurityBootstrap
import com.lifeflow.security.SecurityAuthPerUseCryptoProvider

internal object LifeFlowAppGraphFactory {
    fun createRuntimeBindings(
        applicationContext: Context,
        isInstrumentation: Boolean
    ): LifeFlowAppRuntimeBindings {
        val securityBootstrap = LifeFlowSecurityBootstrap.start(
            applicationContext = applicationContext,
            isInstrumentation = isInstrumentation
        )

        val wellbeingBindings = LifeFlowWellbeingBindings.create(applicationContext)
        val digitalTwinOrchestrator = createDigitalTwinOrchestrator()
        val moduleRepositoryBindings = LifeFlowModuleRepositoryBindings.create(
            applicationContext = applicationContext,
            encryptionPort = securityBootstrap.encryptionPort,
            deviceBindingIdProvider = {
                securityBootstrap.deviceBindingManager
                    .requireCurrentBinding()
                    .bindingId
            }
        )

        val performVaultReset: suspend () -> Unit = {
            securityBootstrap.resetVaultUseCase.invoke()
            moduleRepositoryBindings.clearAll()
            securityBootstrap.deviceBindingManager.resetAndRebind()
        }

        val initialTierSnapshot = TierTruthSnapshot.localSeed(
            tier = TierState.CORE,
            auditTag = "tier_seed_app_graph_factory"
        )

        val mainRuntimeBindings = MainRuntimeBindings(
            applicationContext = applicationContext,
            identityRepository = securityBootstrap.encryptedIdentityRepository,
            digitalTwinOrchestrator = digitalTwinOrchestrator,
            getHealthConnectStatusUseCase = wellbeingBindings.getHealthConnectStatusUseCase,
            getHealthPermissionsUseCase = wellbeingBindings.getHealthPermissionsUseCase,
            getGrantedHealthPermissionsUseCase =
                wellbeingBindings.getGrantedHealthPermissionsUseCase,
            getStepsLast24hUseCase = wellbeingBindings.getStepsLast24hUseCase,
            getAvgHeartRateLast24hUseCase = wellbeingBindings.getAvgHeartRateLast24hUseCase,
            diaryRepository = moduleRepositoryBindings.diaryRepository,
            memoryRepository = moduleRepositoryBindings.memoryRepository,
            connectionRepository = moduleRepositoryBindings.connectionRepository,
            shoppingRepository = moduleRepositoryBindings.shoppingRepository,
            performVaultReset = performVaultReset,
            initialTierSnapshot = initialTierSnapshot
        )

        val appGraph = LifeFlowAppGraph(
            securityBootstrap = securityBootstrap,
            wellbeingBindings = wellbeingBindings,
            digitalTwinOrchestrator = digitalTwinOrchestrator,
            moduleRepositoryBindings = moduleRepositoryBindings,
            mainRuntimeBindings = mainRuntimeBindings
        )

        return LifeFlowAppRuntimeBindings(
            appGraph = appGraph,
            mainViewModelFactory = mainRuntimeBindings.mainViewModelFactory,
            hardeningReport = securityBootstrap.hardeningReport,
            authPerUseCryptoProvider =
                securityBootstrap.cryptoBindings.authPerUseEncryptionService?.let {
                    SecurityAuthPerUseCryptoProvider(it)
                },
            integrityTrustRuntime = securityBootstrap.integrityTrustRuntime
        )
    }

    private fun createDigitalTwinOrchestrator(): DigitalTwinOrchestrator {
        val digitalTwinEngine = DigitalTwinEngine()
        return DigitalTwinOrchestrator(digitalTwinEngine)
    }
}
