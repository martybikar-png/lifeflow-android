package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import javax.crypto.SecretKey

@RunWith(AndroidJUnit4::class)
class KeyManagerInstrumentedTest {

    @Test
    fun generateKey_createsKey_andKeyExistsReturnsTrue() {
        val keyManager = newTestKeyManager()

        keyManager.deleteKey()
        assertFalse(keyManager.keyExists())

        keyManager.generateKey()

        assertTrue(keyManager.keyExists())

        val key = keyManager.getKey()
        assertNotNull(key)
        assertEquals("AES", key.algorithm)
    }

    @Test
    fun getKey_selfHeals_whenKeyDoesNotExist() {
        val keyManager = newTestKeyManager()

        keyManager.deleteKey()
        assertFalse(keyManager.keyExists())

        val key = keyManager.getKey()

        assertNotNull(key)
        assertTrue(keyManager.keyExists())
        assertEquals("AES", key.algorithm)
    }

    @Test
    fun deleteKey_removesExistingAlias() {
        val keyManager = newTestKeyManager()

        keyManager.generateKey()
        assertTrue(keyManager.keyExists())

        keyManager.deleteKey()

        assertFalse(keyManager.keyExists())
    }

    @Test
    fun generateKey_isIdempotent_whenCalledTwice() {
        val keyManager = newTestKeyManager()

        keyManager.deleteKey()
        keyManager.generateKey()

        val firstKey: SecretKey = keyManager.getKey()
        assertTrue(keyManager.keyExists())

        keyManager.generateKey()

        val secondKey: SecretKey = keyManager.getKey()
        assertTrue(keyManager.keyExists())
        assertEquals(firstKey.algorithm, secondKey.algorithm)
    }

    @Test
    fun readKeyPosture_reportsNoAuthPolicy_afterKeyGeneration() {
        val keyManager = newTestKeyManager()

        keyManager.deleteKey()
        keyManager.generateKey()

        val posture = keyManager.readKeyPosture()

        assertTrue(posture.alias.startsWith("LifeFlow_Test_Key_"))
        assertTrue(posture.keyExists)
        assertFalse(posture.userAuthenticationRequired)
        assertFalse(posture.invalidatedByBiometricEnrollment)
    }

    private fun newTestKeyManager(): KeyManager {
        return KeyManager(
            alias = "LifeFlow_Test_Key_${UUID.randomUUID()}",
            authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
        )
    }
}
