package com.lifeflow.security

import android.security.keystore.UserNotAuthenticatedException
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.lifeflow.security.hardening.SecurityHardeningGuard
import java.security.InvalidKeyException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HardeningAndKeystoreFailureMappingsInstrumentedTest {

    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    @After
    fun tearDown() {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest teardown"
        )
    }

    @Test
    fun instrumentationSignals_detected_and_findingsAreRecorded() {
        val findings = mutableListOf<String>()

        val detected = SecurityHardeningGuard.detectInstrumentationFromSignals(
            installedPackages = setOf("de.robv.android.xposed.installer"),
            resolvableClasses = setOf("de.robv.android.xposed.XposedBridge"),
            findings = findings
        )

        assertTrue(detected)
        assertTrue(
            findings.any { it.contains("suspicious package", ignoreCase = true) }
        )
        assertTrue(
            findings.any { it.contains("suspicious class", ignoreCase = true) }
        )
    }

    @Test
    fun trustedInstallerSource_isNotFlagged() {
        val findings = mutableListOf<String>()

        val warning = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = "com.android.vending",
            findings = findings
        )

        assertFalse(warning)
        assertTrue(findings.isEmpty())
    }

    @Test
    fun sideloadInstallerSource_isFlagged() {
        val findings = mutableListOf<String>()

        val warning = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = "com.android.packageinstaller",
            findings = findings
        )

        assertTrue(warning)
        assertTrue(
            findings.any { it.contains("sideload-style installer", ignoreCase = true) }
        )
    }

    @Test
    fun missingInstallerSource_isFlagged() {
        val findings = mutableListOf<String>()

        val warning = SecurityHardeningGuard.detectInstallerTrustFromSource(
            installerPackageName = null,
            findings = findings
        )

        assertTrue(warning)
        assertTrue(
            findings.any { it.contains("installer source unavailable", ignoreCase = true) }
        )
    }

    @Test
    fun keystoreFailure_userNotAuthenticated_clearsSession_andMapsToAuthRequired() {
        resetSecurityState()
        SecurityAccessSession.grantDefault(appContext)
        assertTrue(SecurityAccessSession.isAuthorized(appContext))

        val error = expectSecurityLockedException {
            SecurityKeystoreFailureHandler.throwForFailure(
                operation = null,
                failureReason = "user not authenticated test",
                genericMessage = "Auth-per-use crypto is not available.",
                throwable = UserNotAuthenticatedException()
            )
        }

        assertEquals(
            SecurityLockedReason.AUTH_REQUIRED.withDetail(
                "Auth-per-use crypto is not available."
            ),
            error.lockedReason
        )
        assertFalse(SecurityAccessSession.isAuthorized(appContext))
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun keystoreFailure_postureViolation_compromisesTrust_andRequiresRecovery() {
        resetSecurityState()
        SecurityAccessSession.grantDefault(appContext)
        assertTrue(SecurityAccessSession.isAuthorized(appContext))

        val error = expectSecurityLockedException {
            SecurityKeystoreFailureHandler.throwForFailure(
                operation = null,
                failureReason = "posture violation test",
                genericMessage = "Keystore posture invalid.",
                throwable = SecurityKeystorePostureException("posture mismatch")
            )
        }

        assertEquals(
            SecurityLockedReason.RECOVERY_REQUIRED.withDetail(
                "Keystore posture invalid."
            ),
            error.lockedReason
        )
        assertFalse(SecurityAccessSession.isAuthorized(appContext))
        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun keystoreFailure_invalidKey_compromisesTrust_andRequiresRecovery() {
        resetSecurityState()
        SecurityAccessSession.grantDefault(appContext)
        assertTrue(SecurityAccessSession.isAuthorized(appContext))

        val error = expectSecurityLockedException {
            SecurityKeystoreFailureHandler.throwForFailure(
                operation = null,
                failureReason = "invalid key test",
                genericMessage = "Key is invalid.",
                throwable = InvalidKeyException("bad key")
            )
        }

        assertEquals(
            SecurityLockedReason.RECOVERY_REQUIRED.withDetail(
                "Key is invalid."
            ),
            error.lockedReason
        )
        assertFalse(SecurityAccessSession.isAuthorized(appContext))
        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())
    }

    @Test
    fun keystoreFailure_genericNullOperation_staysGenericSecurityException() {
        resetSecurityState()

        val error = expectSecurityException {
            SecurityKeystoreFailureHandler.throwForFailure(
                operation = null,
                failureReason = "generic null operation test",
                genericMessage = "Generic keystore failure.",
                throwable = IllegalStateException("unexpected")
            )
        }

        assertEquals("Generic keystore failure.", error.message)
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
    }

    private fun resetSecurityState() {
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        SecurityRuleEngine.forceResetForAdversarialSuite(
            state = TrustState.DEGRADED,
            reason = "androidTest reset"
        )
    }

    private fun expectSecurityLockedException(
        block: () -> Unit
    ): SecurityLockedException {
        try {
            block()
        } catch (e: SecurityLockedException) {
            return e
        }

        fail("Expected SecurityLockedException")
        throw IllegalStateException("Unreachable")
    }

    private fun expectSecurityException(
        block: () -> Unit
    ): SecurityException {
        try {
            block()
        } catch (e: SecurityException) {
            return e
        }

        fail("Expected SecurityException")
        throw IllegalStateException("Unreachable")
    }
}
