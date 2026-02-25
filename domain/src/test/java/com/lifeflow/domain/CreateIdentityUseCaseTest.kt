package com.lifeflow.domain

import com.lifeflow.domain.core.InMemoryIdentityRepository
import com.lifeflow.domain.usecase.CreateIdentityUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

class CreateIdentityUseCaseTest {

    @Test
    fun `create identity should save and return active identity`() = runBlocking {

        // Arrange
        val repository = InMemoryIdentityRepository()
        val useCase = CreateIdentityUseCase(repository)
        val timestamp = System.currentTimeMillis()

        // Act
        val createdIdentity = useCase(timestamp)
        val storedIdentity = repository.getById(createdIdentity.id)
        val activeIdentity = repository.getActiveIdentity()

        // Assert
        assertNotNull(createdIdentity)
        assertNotNull(storedIdentity)
        assertEquals(createdIdentity.id, storedIdentity?.id)
        assertTrue(activeIdentity?.isActive == true)
        assertEquals(timestamp, createdIdentity.createdAtEpochMillis)
    }
}