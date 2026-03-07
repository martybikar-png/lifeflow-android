package com.lifeflow.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lifeflow.data.repository.EncryptedIdentityBlobStore
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ResetVaultUseCaseInstrumentedTest {

    private lateinit var context: Context
    private lateinit var keyManager: KeyManager
    private lateinit var vault: AndroidDataSovereigntyVault
    private lateinit var blobStore: EncryptedIdentityBlobStore
    private lateinit var useCase: ResetVaultUseCase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        keyManager = KeyManager(
            alias = "LifeFlow_ResetVault_Test_Key_${UUID.randomUUID()}",
            requireUserAuth = false
        )
        vault = AndroidDataSovereigntyVault(
            context = context,
            keyManager = keyManager
        )
        blobStore = EncryptedIdentityBlobStore(context)
        useCase = ResetVaultUseCase(
            blobStore = blobStore,
            vault = vault
        )

        runCatching { vault.resetVault() }
        runCatching { blobStore.clearAll() }
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "ResetVaultUseCaseInstrumentedTest baseline"
        )
    }

    @After
    fun tearDown() {
        runCatching { vault.resetVault() }
        runCatching { blobStore.clearAll() }
        SecurityAccessSession.clear()
        SecurityRuleEngine.clearAudit()
        forceResetSecurityState(
            state = TrustState.DEGRADED,
            reason = "ResetVaultUseCaseInstrumentedTest cleanup"
        )
    }

    @Test
    fun invoke_resetsVault_clearsBlobStore_reinitializesVault_and_recoversFromCompromised() = runBlocking {
        val id = UUID.randomUUID()

        vault.ensureInitialized()
        vault.nextIdentityVersion(id)
        blobStore.put(id, byteArrayOf(1, 2, 3, 4))

        forceResetSecurityState(
            state = TrustState.COMPROMISED,
            reason = "Pre-reset compromised state"
        )

        assertTrue(vault.isInitialized())
        assertTrue(keyManager.keyExists())
        assertEquals(1L, vault.getIdentityVersion(id))
        assertTrue(blobStore.get(id) != null)
        assertFalse(SecurityAccessSession.isAuthorized())
        assertEquals(TrustState.COMPROMISED, SecurityRuleEngine.getTrustState())

        useCase.invoke()

        assertTrue(vault.isInitialized())
        assertTrue(keyManager.keyExists())
        assertEquals(0L, vault.getIdentityVersion(id))
        assertNull(blobStore.get(id))
        assertFalse(SecurityAccessSession.isAuthorized())
        assertEquals(TrustState.DEGRADED, SecurityRuleEngine.getTrustState())
        assertTrue(SecurityRuleEngine.getRecentEvents().isNotEmpty())

        val lastEvent = SecurityRuleEngine.getRecentEvents().last()
        assertEquals(SecurityRuleEngine.Decision.ALLOW, lastEvent.decision)
        assertEquals(TrustState.DEGRADED, lastEvent.trustState)
        assertTrue(lastEvent.reason.contains("VAULT_RESET_RECOVERY"))
        assertTrue(lastEvent.reason.contains("Vault reset"))
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