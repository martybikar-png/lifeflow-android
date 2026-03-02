package com.lifeflow.domain.usecase

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity

class SaveIdentityUseCase(
    private val repository: IdentityRepository
) {
    suspend operator fun invoke(identity: LifeFlowIdentity) {
        repository.save(identity)
    }
}