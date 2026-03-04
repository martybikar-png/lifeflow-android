package com.lifeflow

import android.app.Application
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.data.wellbeing.HealthConnectWellbeingRepository
import com.lifeflow.domain.core.DataSovereigntyVault
import com.lifeflow.domain.core.IdentityRepository
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

class LifeFlowApplication : Application() {

    lateinit var identityRepository: IdentityRepository
        private set

    // Concrete references (for SecurityAdversarialSuite runner)
    lateinit var encryptedIdentityRepository: EncryptedIdentityRepository
        private set

    lateinit var identityBlobStore: EncryptedIdentityBlobStore
        private set

    lateinit var vault: DataSovereigntyVault
        private set

    // Concrete vault (needed by repo)
    lateinit var androidVault: AndroidDataSovereigntyVault
        private set

    // Concrete key manager (Phase F tests: key loss / reset flow)
    lateinit var keyManager: KeyManager
        private set

    lateinit var getActiveIdentityUseCase: GetActiveIdentityUseCase
        private set

    lateinit var saveIdentityUseCase: SaveIdentityUseCase
        private set

    // ✅ Wellbeing (domain + data)
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

    override fun onCreate() {
        super.onCreate()

        keyManager = KeyManager()

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

        // ✅ Wellbeing wiring
        wellbeingRepository = HealthConnectWellbeingRepository(applicationContext)

        getHealthConnectStatusUseCase = GetHealthConnectStatusUseCase(wellbeingRepository)
        getHealthPermissionsUseCase = GetHealthPermissionsUseCase(wellbeingRepository)
        getGrantedHealthPermissionsUseCase = GetGrantedHealthPermissionsUseCase(wellbeingRepository)
        getStepsLast24hUseCase = GetStepsLast24hUseCase(wellbeingRepository)
        getAvgHeartRateLast24hUseCase = GetAvgHeartRateLast24hUseCase(wellbeingRepository)
    }
}