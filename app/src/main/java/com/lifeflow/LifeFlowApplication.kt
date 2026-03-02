package com.lifeflow

import android.app.Application
import com.lifeflow.data.repository.LocalIdentityRepository
import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.usecase.GetActiveIdentityUseCase
import com.lifeflow.domain.usecase.SaveIdentityUseCase

class LifeFlowApplication : Application() {

    lateinit var identityRepository: IdentityRepository
        private set

    lateinit var getActiveIdentityUseCase: GetActiveIdentityUseCase
        private set

    lateinit var saveIdentityUseCase: SaveIdentityUseCase
        private set

    override fun onCreate() {
        super.onCreate()

        identityRepository = LocalIdentityRepository()

        getActiveIdentityUseCase = GetActiveIdentityUseCase(identityRepository)
        saveIdentityUseCase = SaveIdentityUseCase(identityRepository)
    }
}