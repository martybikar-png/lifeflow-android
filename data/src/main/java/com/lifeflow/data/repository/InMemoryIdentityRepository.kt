package com.lifeflow.data.repository

import com.lifeflow.domain.core.IdentityRepository
import com.lifeflow.domain.model.LifeFlowIdentity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

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