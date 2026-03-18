package com.lifeflow.security

import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

// ---------------------------
// D) Rollback blocked by monotonic version binding
// ---------------------------
internal suspend fun testRollbackIsBlockedByMonotonicVersion(
    repository: EncryptedIdentityRepository,
    blobStore: EncryptedIdentityBlobStore
): SecurityAdversarialSuite.TestResult {
    val name = "D) Rollback is blocked (monotonic version binding)"

    return try {
        prepareVerifiedBaselineForSecuritySuite("suite D setup")

        val id = UUID.randomUUID()

        repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = false))
        val blobV1 = blobStore.get(id)
            ?: return SecurityAdversarialSuite.TestResult(name, false, "Missing blob V1")

        repository.save(LifeFlowIdentity(id, System.currentTimeMillis() + 1, isActive = true))

        blobStore.put(id, blobV1)

        try {
            repository.getById(id)
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Rollback unexpectedly succeeded. Expected SecurityException."
            )
        } catch (_: SecurityException) {
            compromisedResultForSecuritySuite(
                name,
                "Rollback blocked as expected (SecurityException + fail-closed)"
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
// E) Legacy downgrade injection blocked
// ---------------------------
internal suspend fun testLegacyDowngradeInjectionIsBlocked(
    repository: EncryptedIdentityRepository,
    blobStore: EncryptedIdentityBlobStore
): SecurityAdversarialSuite.TestResult {
    val name = "E) Legacy downgrade injection is blocked"

    return try {
        prepareVerifiedBaselineForSecuritySuite("suite E setup")

        val id = UUID.randomUUID()

        repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = true))
        val versionedBlob = blobStore.get(id)
            ?: return SecurityAdversarialSuite.TestResult(name, false, "Missing versioned blob")

        if (versionedBlob.size < 2) {
            return SecurityAdversarialSuite.TestResult(name, false, "Blob too small for marker test")
        }

        val injectedLegacyLike = versionedBlob.copyOfRange(1, versionedBlob.size)
        blobStore.put(id, injectedLegacyLike)

        try {
            repository.getById(id)
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Downgrade injection unexpectedly succeeded. Expected SecurityException."
            )
        } catch (_: SecurityException) {
            compromisedResultForSecuritySuite(
                name,
                "Downgrade injection blocked as expected (SecurityException + fail-closed)"
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
// G) Vault version loss simulation
// ---------------------------
internal suspend fun testVaultVersionLossFailsClosed(
    repository: EncryptedIdentityRepository,
    blobStore: EncryptedIdentityBlobStore,
    vault: AndroidDataSovereigntyVault
): SecurityAdversarialSuite.TestResult {
    val name = "G) Vault version loss -> fail-closed for versioned blob"

    return try {
        prepareVerifiedBaselineForSecuritySuite("suite G setup")

        val id = UUID.randomUUID()
        repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = true))

        val stored = blobStore.get(id)
            ?: return SecurityAdversarialSuite.TestResult(name, false, "Missing blob")

        vault.clearIdentityVersion(id)
        blobStore.put(id, stored)

        try {
            repository.getById(id)
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Read unexpectedly succeeded with missing vault version"
            )
        } catch (_: SecurityException) {
            compromisedResultForSecuritySuite(
                name,
                "Fail-closed OK (missing vault version blocks versioned blob)"
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
// F) Key loss simulation
// ---------------------------
internal suspend fun testKeyLossFailsClosed(
    repository: EncryptedIdentityRepository,
    keyManager: KeyManager
): SecurityAdversarialSuite.TestResult {
    val name = "F) Key loss -> fail-closed + COMPROMISED"

    return try {
        prepareVerifiedBaselineForSecuritySuite("suite F setup")

        val id = UUID.randomUUID()
        repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = true))

        keyManager.deleteKey()

        try {
            repository.getById(id)
            SecurityAdversarialSuite.TestResult(
                name,
                false,
                "Decrypt unexpectedly succeeded after key deletion"
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