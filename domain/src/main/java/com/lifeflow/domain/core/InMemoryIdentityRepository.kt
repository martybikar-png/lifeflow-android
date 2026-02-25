package com.lifeflow.domain.core

import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple in-memory implementation of IdentityRepository.
 * Used for testing and early development.
 */
class InMemoryIdentityRepository : IdentityRepository {

    private val mutex = Mutex()

    private val identities = mutableMapOf<UUID, LifeFlowIdentity>()

    override suspend fun save(identity: LifeFlowIdentity) {
        mutex.withLock {
            identities[identity.id] = identity
        }
    }

    override suspend fun getById(id: UUID): LifeFlowIdentity? {
        return mutex.withLock {
            identities[id]
        }
    }

    override suspend fun getActiveIdentity(): LifeFlowIdentity? {
        return mutex.withLock {
            identities.values.firstOrNull { it.isActive }
        }
    }

    override suspend fun delete(identity: LifeFlowIdentity) {
        mutex.withLock {
            identities.remove(identity.id)
        }
    }
}