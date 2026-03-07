package com.lifeflow.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.LifeFlowApplication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityAdversarialSuiteInstrumentedTest {

    @Test
    fun runFullAdversarialSuite() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = context.applicationContext as LifeFlowApplication

        val results = SecurityAdversarialSuite.runAll(
            repository = app.encryptedIdentityRepository,
            blobStore = app.identityBlobStore,
            keyManager = app.keyManager,
            vault = app.androidVault
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
    }
}