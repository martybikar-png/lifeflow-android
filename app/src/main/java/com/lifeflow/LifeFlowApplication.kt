package com.lifeflow

import android.app.Application
import com.lifeflow.data.repository.LocalIdentityRepository
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.usecase.GetActiveIdentityUseCase
import com.lifeflow.domain.usecase.SaveIdentityUseCase
import com.lifeflow.security.EncryptedIdentityRepository
import com.lifeflow.security.EncryptionService
import com.lifeflow.security.KeyManager

class LifeFlowApplication : Application() {

    lateinit var identityRepository: IdentityRepository
        private set

    lateinit var getActiveIdentityUseCase: GetActiveIdentityUseCase
        private set

    lateinit var saveIdentityUseCase: SaveIdentityUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        // Base repository (data layer)
        val localRepository = LocalIdentityRepository()

        // Security layer (Phase II wiring)
        val keyManager = KeyManager()
        keyManager.generateKey()

        val encryptionService = EncryptionService(keyManager)

        identityRepository = EncryptedIdentityRepository(
            delegate = localRepository,
            encryptionService = encryptionService
        )

        // UseCases depend only on the interface
        getActiveIdentityUseCase = GetActiveIdentityUseCase(identityRepository)
        saveIdentityUseCase = SaveIdentityUseCase(identityRepository)
    }
}