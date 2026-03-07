package com.lifeflow

import android.app.Application
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.data.wellbeing.HealthConnectWellbeingRepository
import com.lifeflow.domain.core.DataSovereigntyVault
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinEngine
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.usecase.GetActiveIdentityUseCase
import com.lifeflow.domain.usecase.SaveIdentityUseCase
import com.lifeflow.domain.wellbeing.WellbeingRepository
import com.lifeflow.domain.wellbeing.usecase.GetAvgHeartRateLast24hUseCase
import com.lifeflow.domain.wellbeing.usecase.GetGrantedHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthConnectStatusUseCase
import com.lifeflow.domain.wellbeing.usecase.GetHealthPermissionsUseCase
import com.lifeflow.domain.wellbeing.usecase.GetStepsLast24hUseCase
import com.lifeflow.security.AndroidDataSovereigntyVault
import com.lifeflow.security.EncryptedIdentityRepository
import com.lifeflow.security.EncryptionService
import com.lifeflow.security.KeyManager
import com.lifeflow.security.ResetVaultUseCase

class LifeFlowApplication : Application() {

    lateinit var identityRepository: IdentityRepository
        private set

    lateinit var encryptedIdentityRepository: EncryptedIdentityRepository
        private set

    lateinit var identityBlobStore: EncryptedIdentityBlobStore
        private set

    lateinit var vault: DataSovereigntyVault
        private set

    lateinit var androidVault: AndroidDataSovereigntyVault
        private set

    lateinit var keyManager: KeyManager
        private set

    lateinit var getActiveIdentityUseCase: GetActiveIdentityUseCase
        private set

    lateinit var saveIdentityUseCase: SaveIdentityUseCase
        private set

    // Wellbeing
    lateinit var wellbeingRepository: WellbeingRepository
        private set

    lateinit var getHealthConnectStatusUseCase: GetHealthConnectStatusUseCase
        private set

    lateinit var getHealthPermissionsUseCase: GetHealthPermissionsUseCase
        private set

    lateinit var getGrantedHealthPermissionsUseCase: GetGrantedHealthPermissionsUseCase
        private set

    lateinit var getStepsLast24hUseCase: GetStepsLast24hUseCase
        private set

    lateinit var getAvgHeartRateLast24hUseCase: GetAvgHeartRateLast24hUseCase
        private set

    // Digital Twin
    lateinit var digitalTwinOrchestrator: DigitalTwinOrchestrator
        private set

    // Recovery
    lateinit var resetVaultUseCase: ResetVaultUseCase
        private set

    val mainOrchestrator: LifeFlowOrchestrator by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        LifeFlowOrchestrator(
            identityRepository = identityRepository,
            digitalTwinOrchestrator = digitalTwinOrchestrator,
            getHealthConnectStatus = getHealthConnectStatusUseCase,
            getHealthPermissions = getHealthPermissionsUseCase,
            getGrantedHealthPermissions = getGrantedHealthPermissionsUseCase,
            getStepsLast24h = getStepsLast24hUseCase,
            getAvgHeartRateLast24h = getAvgHeartRateLast24hUseCase
        )
    }

    val mainViewModelFactory: MainViewModelFactory by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MainViewModelFactory(
            orchestrator = mainOrchestrator,
            performVaultReset = { resetVaultUseCase() }
        )
    }

    override fun onCreate() {
        super.onCreate()

        val isInstrumentation = isRunningInstrumentation()

        keyManager = if (isInstrumentation) {
            KeyManager(
                alias = "LifeFlow_Test_Key",
                requireUserAuth = false
            )
        } else {
            KeyManager()
        }

        androidVault = AndroidDataSovereigntyVault(applicationContext, keyManager)
        vault = androidVault
        vault.ensureInitialized()

        val encryptionService = EncryptionService(keyManager)

        identityBlobStore = EncryptedIdentityBlobStore(applicationContext)

        encryptedIdentityRepository = EncryptedIdentityRepository(
            blobStore = identityBlobStore,
            encryptionService = encryptionService,
            vault = androidVault
        )

        identityRepository = encryptedIdentityRepository

        getActiveIdentityUseCase = GetActiveIdentityUseCase(identityRepository)
        saveIdentityUseCase = SaveIdentityUseCase(identityRepository)

        wellbeingRepository = HealthConnectWellbeingRepository(applicationContext)

        getHealthConnectStatusUseCase = GetHealthConnectStatusUseCase(wellbeingRepository)
        getHealthPermissionsUseCase = GetHealthPermissionsUseCase(wellbeingRepository)
        getGrantedHealthPermissionsUseCase = GetGrantedHealthPermissionsUseCase(wellbeingRepository)
        getStepsLast24hUseCase = GetStepsLast24hUseCase(wellbeingRepository)
        getAvgHeartRateLast24hUseCase = GetAvgHeartRateLast24hUseCase(wellbeingRepository)

        val digitalTwinEngine = DigitalTwinEngine()
        digitalTwinOrchestrator = DigitalTwinOrchestrator(digitalTwinEngine)

        resetVaultUseCase = ResetVaultUseCase(
            blobStore = identityBlobStore,
            vault = androidVault
        )
    }

    private fun isRunningInstrumentation(): Boolean {
        return try {
            Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            true
        } catch (_: Throwable) {
            false
        }
    }
}