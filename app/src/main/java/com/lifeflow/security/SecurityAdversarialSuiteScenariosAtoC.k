package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

// ---------------------------
// A) No-session -> DENY
// ---------------------------
internal suspend fun testNoSessionFailsClosed(
    repository: EncryptedIdentityRepository
): SecurityAdversarialSuite.TestResult {
    val name = "A) No-session fails closed"

    return try {
        prepareVerifiedNoSessionBaselineForSecuritySuite("suite A baseline")

        try {
            repository.getActiveIdentity()
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Expected SecurityException, but getActiveIdentity() returned without throwing"
            )
        } catch (e: SecurityException) {
            SecurityAdversarialSuite.TestResult(name, true, "Denied as expected: ${e.message}")
        }
    } catch (t: Throwable) {
        SecurityAdversarialSuite.TestResult(
            name,
            false,
            "Unexpected failure: ${t::class.java.simpleName}: ${t.message}"
        )
    }
}

// ---------------------------
// B) Corrupted blob -> COMPROMISED + session cleared
// ---------------------------
internal suspend fun testCorruptedBlobTriggersCompromised(
    repository: EncryptedIdentityRepository,
    blobStore: EncryptedIdentityBlobStore
): SecurityAdversarialSuite.TestResult {
    val name = "B) Corrupted blob -> COMPROMISED + session cleared"

    return try {
        prepareVerifiedBaselineForSecuritySuite("suite B setup")

        val id = UUID.randomUUID()
        val identity = LifeFlowIdentity(
            id = id,
            createdAtEpochMillis = System.currentTimeMillis(),
            isActive = true
        )

        repository.save(identity)

        val original = blobStore.get(id)
            ?: return SecurityAdversarialSuite.TestResult(name, false, "Blob missing after save()")

        if (original.size < 8) {
            return SecurityAdversarialSuite.TestResult(name, false, "Blob too small to corrupt safely")
        }

        val corrupted = original.clone()
        val idx = corrupted.size - 3
        corrupted[idx] = (corrupted[idx].toInt() xor 0x01).toByte()
        blobStore.put(id, corrupted)

        try {
            repository.getById(id)
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Expected SecurityException on decrypt, but read succeeded"
            )
        } catch (_: SecurityException) {
            compromisedResultForSecuritySuite(
                name,
                "Fail-closed OK (COMPROMISED + session cleared)"
            )
        } catch (t: Throwable) {
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}"
            )
        }
    } catch (t: Throwable) {
        SecurityAdversarialSuite.TestResult(
            name,
            false,
            "Unexpected failure: ${t::class.java.simpleName}: ${t.message}"
        )
    }
}

// ---------------------------
// C) Ciphertext swap -> COMPROMISED + session cleared
// ---------------------------
internal suspend fun testCiphertextSwapTriggersCompromised(
    repository: EncryptedIdentityRepository,
    blobStore: EncryptedIdentityBlobStore
): SecurityAdversarialSuite.TestResult {
    val name = "C) Ciphertext swap -> COMPROMISED + session cleared"

    return try {
        prepareVerifiedBaselineForSecuritySuite("suite C setup")

        val idA = UUID.randomUUID()
        val idB = UUID.randomUUID()

        repository.save(LifeFlowIdentity(idA, System.currentTimeMillis(), isActive = false))
        repository.save(LifeFlowIdentity(idB, System.currentTimeMillis(), isActive = true))

        val blobA = blobStore.get(idA)
            ?: return SecurityAdversarialSuite.TestResult(name, false, "Blob A missing")
        val blobB = blobStore.get(idB)
            ?: return SecurityAdversarialSuite.TestResult(name, false, "Blob B missing")

        blobStore.put(idA, blobB)
        blobStore.put(idB, blobA)

        try {
            repository.getById(idA)
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Expected SecurityException on swapped blob, but read succeeded"
            )
        } catch (_: SecurityException) {
            compromisedResultForSecuritySuite(
                name,
                "Fail-closed OK (COMPROMISED + session cleared)"
            )
        } catch (t: Throwable) {
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}"
            )
        }
    } catch (t: Throwable) {
        SecurityAdversarialSuite.TestResult(
            name,
            false,
            "Unexpected failure: ${t::class.java.simpleName}: ${t.message}"
        )
    }
}