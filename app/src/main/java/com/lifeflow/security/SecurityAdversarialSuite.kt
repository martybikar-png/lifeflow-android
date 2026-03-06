package com.lifeflow.security

import android.util.Log
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import com.lifeflow.domain.model.LifeFlowIdentity
import java.util.UUID

/**
 * Phase V.5 — Adversarial Simulation (manual runner)
 *
 * Updated for Phase VI behavior:
 * - Corrupt/swap triggers COMPROMISED + session clear + SecurityException (fail-closed)
 * - Phase D hardened: scheme marker + strict decrypt (no downgrade fallback)
 *
 * Phase F additions:
 * - Key loss simulation -> fail-closed + COMPROMISED
 * - Vault version loss simulation -> fail-closed for versioned blobs
 *
 * Suite hardening:
 * - Uses explicit adversarial-suite baseline reset between tests
 * - Does NOT rely on normal setTrustState() to escape COMPROMISED
 * - Restores app to safe fail-closed baseline when suite ends
 */
object SecurityAdversarialSuite {

    private const val TAG = "LF_SecurityAdversarial"
    private const val SUITE_SESSION_MS = 30_000L

    data class TestResult(
        val name: String,
        val passed: Boolean,
        val details: String
    )

    suspend fun runAll(
        repository: EncryptedIdentityRepository,
        blobStore: EncryptedIdentityBlobStore,
        keyManager: KeyManager,
        vault: AndroidDataSovereigntyVault
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()

        try {
            blobStore.clearAll()
            results += testNoSessionFailsClosed(repository)

            blobStore.clearAll()
            results += testCorruptedBlobTriggersCompromised(repository, blobStore)

            blobStore.clearAll()
            results += testCiphertextSwapTriggersCompromised(repository, blobStore)

            blobStore.clearAll()
            results += testRollbackIsBlockedByMonotonicVersion(repository, blobStore)

            blobStore.clearAll()
            results += testLegacyDowngradeInjectionIsBlocked(repository, blobStore)

            blobStore.clearAll()
            results += testVaultVersionLossFailsClosed(repository, blobStore, vault)

            blobStore.clearAll()
            results += testKeyLossFailsClosed(repository, keyManager)

            results.forEach { r ->
                val status = if (r.passed) "PASS" else "FAIL"
                Log.i(TAG, "$status — ${r.name}: ${r.details}")
            }

            return results
        } finally {
            runCatching { blobStore.clearAll() }
            runCatching { keyManager.generateKey() }

            SecurityAccessSession.clear()
            SecurityRuleEngine.forceResetForAdversarialSuite(
                state = TrustState.DEGRADED,
                reason = "suite end fail-closed baseline"
            )
        }
    }

    private fun prepareVerifiedBaseline(reason: String) {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.VERIFIED,
            reason = reason
        )
        SecurityAccessSession.grant(SUITE_SESSION_MS)
    }

    private fun prepareVerifiedNoSessionBaseline(reason: String) {
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.VERIFIED,
            reason = reason
        )
        SecurityAccessSession.clear()
    }

    private fun compromisedResult(
        name: String,
        successDetails: String
    ): TestResult {
        val stateOk = SecurityRuleEngine.getTrustState() == TrustState.COMPROMISED
        val sessionOk = !SecurityAccessSession.isAuthorized()

        return if (stateOk && sessionOk) {
            TestResult(name, true, successDetails)
        } else {
            TestResult(
                name,
                false,
                "Mismatch: trustState=${SecurityRuleEngine.getTrustState()}, sessionAuthorized=${SecurityAccessSession.isAuthorized()}"
            )
        }
    }

    // ---------------------------
    // A) No-session -> DENY
    // ---------------------------
    private suspend fun testNoSessionFailsClosed(
        repository: EncryptedIdentityRepository
    ): TestResult {
        val name = "A) No-session fails closed"

        return try {
            prepareVerifiedNoSessionBaseline("suite A baseline")

            try {
                repository.getActiveIdentity()
                TestResult(
                    name,
                    false,
                    "Expected SecurityException, but getActiveIdentity() returned without throwing"
                )
            } catch (e: SecurityException) {
                TestResult(name, true, "Denied as expected: ${e.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }

    // ---------------------------
    // B) Corrupted blob -> COMPROMISED + session cleared
    // ---------------------------
    private suspend fun testCorruptedBlobTriggersCompromised(
        repository: EncryptedIdentityRepository,
        blobStore: EncryptedIdentityBlobStore
    ): TestResult {
        val name = "B) Corrupted blob -> COMPROMISED + session cleared"

        return try {
            prepareVerifiedBaseline("suite B setup")

            val id = UUID.randomUUID()
            val identity = LifeFlowIdentity(
                id = id,
                createdAtEpochMillis = System.currentTimeMillis(),
                isActive = true
            )

            repository.save(identity)

            val original = blobStore.get(id)
                ?: return TestResult(name, false, "Blob missing after save()")

            if (original.size < 8) {
                return TestResult(name, false, "Blob too small to corrupt safely")
            }

            val corrupted = original.clone()
            val idx = corrupted.size - 3
            corrupted[idx] = (corrupted[idx].toInt() xor 0x01).toByte()
            blobStore.put(id, corrupted)

            try {
                repository.getById(id)
                TestResult(name, false, "Expected SecurityException on decrypt, but read succeeded")
            } catch (_: SecurityException) {
                compromisedResult(name, "Fail-closed OK (COMPROMISED + session cleared)")
            } catch (t: Throwable) {
                TestResult(name, false, "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }

    // ---------------------------
    // C) Ciphertext swap -> COMPROMISED + session cleared
    // ---------------------------
    private suspend fun testCiphertextSwapTriggersCompromised(
        repository: EncryptedIdentityRepository,
        blobStore: EncryptedIdentityBlobStore
    ): TestResult {
        val name = "C) Ciphertext swap -> COMPROMISED + session cleared"

        return try {
            prepareVerifiedBaseline("suite C setup")

            val idA = UUID.randomUUID()
            val idB = UUID.randomUUID()

            repository.save(LifeFlowIdentity(idA, System.currentTimeMillis(), isActive = false))
            repository.save(LifeFlowIdentity(idB, System.currentTimeMillis(), isActive = true))

            val blobA = blobStore.get(idA) ?: return TestResult(name, false, "Blob A missing")
            val blobB = blobStore.get(idB) ?: return TestResult(name, false, "Blob B missing")

            blobStore.put(idA, blobB)
            blobStore.put(idB, blobA)

            try {
                repository.getById(idA)
                TestResult(name, false, "Expected SecurityException on swapped blob, but read succeeded")
            } catch (_: SecurityException) {
                compromisedResult(name, "Fail-closed OK (COMPROMISED + session cleared)")
            } catch (t: Throwable) {
                TestResult(name, false, "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }

    // ---------------------------
    // D) Rollback blocked by monotonic version binding
    // ---------------------------
    private suspend fun testRollbackIsBlockedByMonotonicVersion(
        repository: EncryptedIdentityRepository,
        blobStore: EncryptedIdentityBlobStore
    ): TestResult {
        val name = "D) Rollback is blocked (monotonic version binding)"

        return try {
            prepareVerifiedBaseline("suite D setup")

            val id = UUID.randomUUID()

            repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = false))
            val blobV1 = blobStore.get(id) ?: return TestResult(name, false, "Missing blob V1")

            repository.save(LifeFlowIdentity(id, System.currentTimeMillis() + 1, isActive = true))

            blobStore.put(id, blobV1)

            try {
                repository.getById(id)
                TestResult(name, false, "Rollback unexpectedly succeeded. Expected SecurityException.")
            } catch (_: SecurityException) {
                compromisedResult(name, "Rollback blocked as expected (SecurityException + fail-closed)")
            } catch (t: Throwable) {
                TestResult(name, false, "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }

    // ---------------------------
    // E) Legacy downgrade injection blocked
    // ---------------------------
    private suspend fun testLegacyDowngradeInjectionIsBlocked(
        repository: EncryptedIdentityRepository,
        blobStore: EncryptedIdentityBlobStore
    ): TestResult {
        val name = "E) Legacy downgrade injection is blocked"

        return try {
            prepareVerifiedBaseline("suite E setup")

            val id = UUID.randomUUID()

            repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = true))
            val versionedBlob = blobStore.get(id) ?: return TestResult(name, false, "Missing versioned blob")
            if (versionedBlob.size < 2) return TestResult(name, false, "Blob too small for marker test")

            val injectedLegacyLike = versionedBlob.copyOfRange(1, versionedBlob.size)
            blobStore.put(id, injectedLegacyLike)

            try {
                repository.getById(id)
                TestResult(name, false, "Downgrade injection unexpectedly succeeded. Expected SecurityException.")
            } catch (_: SecurityException) {
                compromisedResult(name, "Downgrade injection blocked as expected (SecurityException + fail-closed)")
            } catch (t: Throwable) {
                TestResult(name, false, "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }

    // ---------------------------
    // G) Vault version loss simulation
    // ---------------------------
    private suspend fun testVaultVersionLossFailsClosed(
        repository: EncryptedIdentityRepository,
        blobStore: EncryptedIdentityBlobStore,
        vault: AndroidDataSovereigntyVault
    ): TestResult {
        val name = "G) Vault version loss -> fail-closed for versioned blob"

        return try {
            prepareVerifiedBaseline("suite G setup")

            val id = UUID.randomUUID()
            repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = true))

            val stored = blobStore.get(id) ?: return TestResult(name, false, "Missing blob")

            vault.clearIdentityVersion(id)
            blobStore.put(id, stored)

            try {
                repository.getById(id)
                TestResult(name, false, "Read unexpectedly succeeded with missing vault version")
            } catch (_: SecurityException) {
                compromisedResult(name, "Fail-closed OK (missing vault version blocks versioned blob)")
            } catch (t: Throwable) {
                TestResult(name, false, "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }

    // ---------------------------
    // F) Key loss simulation
    // ---------------------------
    private suspend fun testKeyLossFailsClosed(
        repository: EncryptedIdentityRepository,
        keyManager: KeyManager
    ): TestResult {
        val name = "F) Key loss -> fail-closed + COMPROMISED"

        return try {
            prepareVerifiedBaseline("suite F setup")

            val id = UUID.randomUUID()
            repository.save(LifeFlowIdentity(id, System.currentTimeMillis(), isActive = true))

            keyManager.deleteKey()

            try {
                repository.getById(id)
                TestResult(name, false, "Decrypt unexpectedly succeeded after key deletion")
            } catch (_: SecurityException) {
                compromisedResult(name, "Fail-closed OK (COMPROMISED + session cleared)")
            } catch (t: Throwable) {
                TestResult(name, false, "Expected SecurityException, got ${t::class.java.simpleName}: ${t.message}")
            }
        } catch (t: Throwable) {
            TestResult(name, false, "Unexpected failure: ${t::class.java.simpleName}: ${t.message}")
        }
    }
}