package com.lifeflow.security

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class EncryptedIdentityRepository(
    private val delegate: IdentityRepository,
    @Suppress("UNUSED_PARAMETER")
    private val encryptionService: EncryptionService
) : IdentityRepository {

    private val mutex = Mutex()

    override suspend fun save(identity: LifeFlowIdentity) {
        mutex.withLock {
            // Encryption layer placeholder: no-op for now.
            delegate.save(identity)
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return mutex.withLock {
            delegate.getById(id)
        }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        return mutex.withLock {
            delegate.getActiveIdentity()
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            delegate.delete(identity)
        }
    }
}