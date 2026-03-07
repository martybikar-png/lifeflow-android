package com.lifeflow.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AndroidDataSovereigntyVaultInstrumentedTest {

    private lateinit var context: Context
    private lateinit var keyManager: KeyManager
    private lateinit var vault: AndroidDataSovereigntyVault

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        keyManager = KeyManager(
            alias = "LifeFlow_Vault_Test_Key_${UUID.randomUUID()}",
            requireUserAuth = false
        )
        vault = AndroidDataSovereigntyVault(
            context = context,
            keyManager = keyManager
        )

        runCatching { vault.resetVault() }
    }

    @After
    fun tearDown() {
        runCatching { vault.resetVault() }
    }

    @Test
    fun isInitialized_returnsFalse_whenVaultIsFresh() {
        assertFalse(vault.isInitialized())
    }

    @Test
    fun ensureInitialized_setsInitializedFlag_andCreatesKey() {
        vault.ensureInitialized()

        assertTrue(vault.isInitialized())
        assertTrue(keyManager.keyExists())
    }

    @Test
    fun ensureInitialized_isIdempotent() {
        vault.ensureInitialized()
        assertTrue(vault.isInitialized())

        vault.ensureInitialized()

        assertTrue(vault.isInitialized())
        assertTrue(keyManager.keyExists())
    }

    @Test
    fun getIdentityVersion_returnsZero_forUnknownIdentity() {
        val id = UUID.randomUUID()

        val version = vault.getIdentityVersion(id)

        assertEquals(0L, version)
    }

    @Test
    fun nextIdentityVersion_incrementsMonotonically() {
        val id = UUID.randomUUID()

        val v1 = vault.nextIdentityVersion(id)
        val v2 = vault.nextIdentityVersion(id)
        val stored = vault.getIdentityVersion(id)

        assertEquals(1L, v1)
        assertEquals(2L, v2)
        assertEquals(2L, stored)
    }

    @Test
    fun clearIdentityVersion_resetsVersionBackToZero() {
        val id = UUID.randomUUID()

        vault.nextIdentityVersion(id)
        assertEquals(1L, vault.getIdentityVersion(id))

        vault.clearIdentityVersion(id)

        assertEquals(0L, vault.getIdentityVersion(id))
    }

    @Test
    fun resetVault_clearsPrefsAndDeletesKey() {
        val id = UUID.randomUUID()

        vault.ensureInitialized()
        vault.nextIdentityVersion(id)

        assertTrue(vault.isInitialized())
        assertTrue(keyManager.keyExists())
        assertEquals(1L, vault.getIdentityVersion(id))

        vault.resetVault()

        assertFalse(vault.isInitialized())
        assertFalse(keyManager.keyExists())
        assertEquals(0L, vault.getIdentityVersion(id))
    }
}