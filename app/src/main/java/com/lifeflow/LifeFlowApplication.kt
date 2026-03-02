package com.lifeflow

import android.app.Application
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.core.DataSovereigntyVault
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.usecase.GetActiveIdentityUseCase
import com.lifeflow.domain.usecase.SaveIdentityUseCase
import com.lifeflow.security.AndroidDataSovereigntyVault
import com.lifeflow.security.EncryptedIdentityRepository
import com.lifeflow.security.EncryptionService
import com.lifeflow.security.KeyManager

class LifeFlowApplication : Application() {

    lateinit var identityRepository: IdentityRepository
        private set

    lateinit var vault: DataSovereigntyVault
        private set

    lateinit var getActiveIdentityUseCase: GetActiveIdentityUseCase
        private set

    lateinit var saveIdentityUseCase: SaveIdentityUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        // Vault + Security layer (Phase II)
        val keyManager = KeyManager()
        vault = AndroidDataSovereigntyVault(applicationContext, keyManager)
        vault.ensureInitialized()

        val encryptionService = EncryptionService(keyManager)

        // Phase II.5: authoritative ciphertext persistence (encrypted-at-rest)
        val blobStore = EncryptedIdentityBlobStore(applicationContext)

        identityRepository = EncryptedIdentityRepository(
            blobStore = blobStore,
            encryptionService = encryptionService
        )

        // UseCases depend only on the interface
        getActiveIdentityUseCase = GetActiveIdentityUseCase(identityRepository)
        saveIdentityUseCase = SaveIdentityUseCase(identityRepository)
    }
}