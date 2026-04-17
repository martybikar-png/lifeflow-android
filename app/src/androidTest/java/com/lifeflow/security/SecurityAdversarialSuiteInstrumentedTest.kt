package com.lifeflow.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SecurityAdversarialSuiteInstrumentedTest {

    @Test
    fun runFullAdversarialSuite() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val keyManager = KeyManager(
            alias = "LifeFlow_Adversarial_Test_Key_${UUID.randomUUID()}",
            authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
        )
        val vault = AndroidDataSovereigntyVault(
            context = context,
            keyManager = keyManager
        )
        val blobStore = EncryptedIdentityBlobStore(context)
        val encryptionService = EncryptionService(keyManager)
        val repository = EncryptedIdentityRepository(
            blobStore = blobStore,
            encryptionService = encryptionService,
            vault = vault
        )

        try {
            runCatching { vault.resetVault() }
            runCatching { blobStore.clearAll() }
            SecurityVaultResetAuthorization.clear()
            SecurityAccessSession.clear()
            SecurityRuleEngine.clearAudit()
            forceResetSecurityState(
                state = TrustState.DEGRADED,
                reason = "SecurityAdversarialSuiteInstrumentedTest baseline"
            )

            vault.ensureInitialized()

            val results = SecurityAdversarialSuite.runAll(
                repository = repository,
                blobStore = blobStore,
                keyManager = keyManager,
                vault = vault
            )

            assertEquals(
                "Unexpected number of adversarial test results",
                7,
                results.size
            )

            val expectedNames = setOf(
                "A) No-session fails closed",
                "B) Corrupted blob -> COMPROMISED + session cleared",
                "C) Ciphertext swap -> COMPROMISED + session cleared",
                "D) Rollback is blocked (monotonic version binding)",
                "E) Legacy downgrade injection is blocked",
                "G) Vault version loss -> fail-closed for versioned blob",
                "F) Key loss -> fail-closed + COMPROMISED"
            )

            val actualNames = results.map { it.name }.toSet()
            assertEquals(
                "Unexpected adversarial result names",
                expectedNames,
                actualNames
            )

            val failed = results.filterNot { it.passed }

            val report = buildString {
                appendLine("SecurityAdversarialSuite results:")
                results.forEach { result ->
                    appendLine(
                        "- ${result.name}: ${if (result.passed) "PASS" else "FAIL"} | ${result.details}"
                    )
                }
            }

            assertTrue(report, failed.isEmpty())

            assertEquals(
                "Suite must restore fail-closed baseline",
                TrustState.DEGRADED,
                SecurityRuleEngine.getTrustState()
            )
            assertFalse(
                "Suite must clear auth session on exit",
                SecurityAccessSession.isAuthorized()
            )
        } finally {
            runCatching { vault.resetVault() }
            runCatching { blobStore.clearAll() }
            SecurityVaultResetAuthorization.clear()
            SecurityAccessSession.clear()
            SecurityRuleEngine.clearAudit()
            runCatching {
                forceResetSecurityState(
                    state = TrustState.DEGRADED,
                    reason = "SecurityAdversarialSuiteInstrumentedTest cleanup"
                )
            }
        }
    }

    private fun forceResetSecurityState(
        state: TrustState,
        reason: String
    ) {
        val method = SecurityRuleEngine::class.java.declaredMethods.firstOrNull { candidate ->
            candidate.name.startsWith("forceResetForAdversarialSuite") &&
                candidate.parameterTypes.size == 2 &&
                candidate.parameterTypes[0] == TrustState::class.java &&
                candidate.parameterTypes[1] == String::class.java
        } ?: throw AssertionError(
            buildString {
                append("Could not find compatible forceResetForAdversarialSuite method on SecurityRuleEngine. Available methods: ")
                append(SecurityRuleEngine::class.java.declaredMethods.joinToString { it.name })
            }
        )

        method.isAccessible = true
        method.invoke(SecurityRuleEngine, state, reason)
    }
}
