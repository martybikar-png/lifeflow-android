package com.lifeflow.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.LifeFlowApplication
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

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
    }
}