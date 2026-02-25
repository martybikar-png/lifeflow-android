package com.lifeflow.domain.usecase

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity

/**
 * Creates and persists a new LifeFlow identity.
 * Pure domain use case.
 */
class CreateIdentityUseCase(
    private val repository: IdentityRepository
) {

    suspend operator fun invoke(
        createdAtEpochMillis: Long
    ): LifeFlowIdentity {

        val identity = LifeFlowIdentity(
            createdAtEpochMillis = createdAtEpochMillis,
            isBiometricProtected = false,
            vaultInitialized = false,
            isActive = true
        )

        repository.save(identity)

        return identity
    }
}