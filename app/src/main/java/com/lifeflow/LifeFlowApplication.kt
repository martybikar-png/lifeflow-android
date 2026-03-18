package com.lifeflow

import android.app.Application
import android.util.Log
import com.lifeflow.core.LifeFlowOrchestrator
import com.lifeflow.data.connection.LocalConnectionRepository
import com.lifeflow.data.diary.LocalDiaryRepository
import com.lifeflow.data.memory.LocalMemoryRepository
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.data.shopping.LocalShoppingRepository
import com.lifeflow.data.wellbeing.HealthConnectWellbeingRepository
import com.lifeflow.domain.core.DataSovereigntyVault
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.core.digitaltwin.DigitalTwinEngine
import com.lifeflow.domain.core.digitaltwin.DigitalTwinOrchestrator
import com.lifeflow.domain.security.TrustStatePort
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
import com.lifeflow.security.SecurityAccessSession
import com.lifeflow.security.SecurityTrustStatePortAdapter

class LifeFlowApplication : Application() {

    companion object {
        private const val TAG = "LifeFlowApplication"
    }

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

    // Module repositories
    lateinit var diaryRepository: LocalDiaryRepository
        private set
    lateinit var memoryRepository: LocalMemoryRepository
        private set
    lateinit var connectionRepository: LocalConnectionRepository
        private set
    lateinit var shoppingRepository: LocalShoppingRepository
        private set

    @Volatile
    private var startupInitialized = false

    @Volatile
    var startupFailureMessage: String? = null
        private set

    private val startupInitLock = Any()

    private val sessionAuthorizationChecker: () -> Boolean = {
        SecurityAccessSession.isAuthorized()
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
            getAvgHeartRateLast24h = getAvgHeartRateLast24hUseCase
        )
    }

    val mainViewModelFactory: MainViewModelFactory by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        MainViewModelFactory(
            orchestrator = mainOrchestrator,
            performVaultReset = resetVaultUseCase::invoke,
            isSessionAuthorized = sessionAuthorizationChecker,
            clearSession = sessionClearAction,
            trustStatePort = mainTrustStatePort
        )
    }

    override fun onCreate() {
        super.onCreate()
        ensureStartupInitialized()
    }

    fun ensureStartupInitialized(): Boolean {
        if (startupInitialized) return true
        synchronized(startupInitLock) {
            if (startupInitialized) return true
            return try {
                initializeDependencyGraph()
                startupInitialized = true
                startupFailureMessage = null
                true
            } catch (t: Throwable) {
                startupInitialized = false
                startupFailureMessage = buildStartupFailureMessage(t)
                Log.e(TAG, "Startup initialization failed.", t)
                false
            }
        }
    }

    private fun initializeDependencyGraph() {
        val isInstrumentation = isRunningInstrumentation()

        keyManager = if (isInstrumentation) {
            KeyManager(alias = "LifeFlow_Test_Key", requireUserAuth = false)
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

        // Module encryption
        val encryptionPort = com.lifeflow.security.EncryptionPortAdapter(encryptionService)

        // Module repositories
        diaryRepository = com.lifeflow.data.diary.LocalDiaryRepository(
            com.lifeflow.data.store.EncryptedModuleStore(applicationContext, "lifeflow_diary", encryptionPort)
        )
        memoryRepository = com.lifeflow.data.memory.LocalMemoryRepository(
            com.lifeflow.data.store.EncryptedModuleStore(applicationContext, "lifeflow_memory", encryptionPort)
        )
        connectionRepository = com.lifeflow.data.connection.LocalConnectionRepository(
            com.lifeflow.data.store.EncryptedModuleStore(applicationContext, "lifeflow_connection", encryptionPort)
        )
        shoppingRepository = com.lifeflow.data.shopping.LocalShoppingRepository(
            com.lifeflow.data.store.EncryptedModuleStore(applicationContext, "lifeflow_shopping", encryptionPort)
        )
    }

    private fun buildStartupFailureMessage(t: Throwable): String {
        val type = t::class.java.simpleName.ifBlank { "UnknownError" }
        val detail = t.message?.takeIf { it.isNotBlank() } ?: "unknown"
        return "Application startup failed: $type: $detail"
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

