package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityKeyAttestationBootstrapInstrumentedTest {

    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @After
    fun tearDown() {
        SecurityKeyAttestationRegistry.clear()
    }

    @Test
    fun captureRequestBoundEvidence_blankHash_isRejected() {
        expectIllegalArgumentException {
            SecurityKeyAttestationBootstrap.captureRequestBoundEvidence("   ")
        }
    }

    @Test
    fun captureRequestBoundEvidence_storesEvidenceInRegistry_andReturnsConsistentShape() {
        SecurityKeyAttestationRegistry.clear()

        val evidence = SecurityKeyAttestationBootstrap.captureRequestBoundEvidence(
            requestHash = "request-hash-android-test"
        )

        val stored = SecurityKeyAttestationRegistry.currentOrNull()
        assertNotNull(stored)
        assertEquals(evidence, stored)

        assertEquals("LifeFlow_Attestation_Key", evidence.keyAlias)
        assertTrue(evidence.chainEntryCount >= 0)

        when (evidence.status) {
            SecurityKeyAttestationStatus.CAPTURED -> {
                assertTrue(evidence.chainEntryCount > 0)
                assertTrue(!evidence.challengeBase64.isNullOrBlank())
                assertTrue(!evidence.challengeSha256.isNullOrBlank())
                assertTrue(!evidence.leafCertificateSha256.isNullOrBlank())
                assertEquals(
                    evidence.chainEntryCount,
                    evidence.certificateChainDerBase64.size
                )
                assertTrue(
                    evidence.certificateChainDerBase64.all { it.isNotBlank() }
                )
            }

            SecurityKeyAttestationStatus.UNAVAILABLE -> {
                assertEquals(0, evidence.chainEntryCount)
                assertTrue(evidence.certificateChainDerBase64.isEmpty())
                assertTrue(!evidence.failureReason.isNullOrBlank())
            }
        }
    }

    @Test
    fun attestationRuntime_close_clearsRegistry() {
        SecurityKeyAttestationRegistry.clear()

        val runtime = SecurityKeyAttestationBootstrap.start(
            applicationContext = appContext,
            isInstrumentation = true
        )

        val evidence = SecurityKeyAttestationBootstrap.captureRequestBoundEvidence(
            requestHash = "request-hash-runtime-close"
        )
        assertEquals(evidence, SecurityKeyAttestationRegistry.currentOrNull())

        runtime.close()

        assertNull(SecurityKeyAttestationRegistry.currentOrNull())
    }

    private fun expectIllegalArgumentException(
        block: () -> Unit
    ) {
        try {
            block()
        } catch (_: IllegalArgumentException) {
            return
        }

        fail("Expected IllegalArgumentException")
    }
}
