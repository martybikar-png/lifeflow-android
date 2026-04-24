package com.lifeflow.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptionServiceInstrumentedTest {

    private val createdManagers = mutableListOf<KeyManager>()

    @After
    fun tearDown() {
        createdManagers.forEach { manager ->
            runCatching { manager.deleteKey() }
        }
        createdManagers.clear()
    }

    @Test
    fun encryptDecrypt_versionedFormat_roundTripsOnDeviceKeystore() {
        val (_, service) = createEncryptionService("roundtrip")
        val plain = "LifeFlow protected payload".toByteArray(Charsets.UTF_8)
        val aad = "aad-roundtrip".toByteArray(Charsets.UTF_8)

        val encrypted = service.encrypt(plainText = plain, aad = aad)
        val decrypted = service.decrypt(encryptedData = encrypted, aad = aad)

        assertArrayEquals(plain, decrypted)
    }

    @Test
    fun preparedVersionedDecryptCipher_roundTripsThroughPreparedCipherFlow() {
        val (_, service) = createEncryptionService("prepared-cipher")
        val plain = "Prepared cipher payload".toByteArray(Charsets.UTF_8)
        val aad = "aad-prepared".toByteArray(Charsets.UTF_8)

        val encrypted = service.encrypt(plainText = plain, aad = aad)
        val decryptCipher = service.createDecryptCipherVersionedFormat(encrypted)

        val decrypted = service.decryptWithCipherVersionedFormat(
            cipher = decryptCipher,
            encryptedData = encrypted,
            aad = aad
        )

        assertArrayEquals(plain, decrypted)
    }

    @Test
    fun legacyDecryptPath_acceptsLegacyPayloadShape() {
        val (_, service) = createEncryptionService("legacy-shape")
        val plain = "Legacy payload compatibility".toByteArray(Charsets.UTF_8)
        val aad = "aad-legacy".toByteArray(Charsets.UTF_8)

        val versioned = service.encrypt(plainText = plain, aad = aad)
        val ivLen = versioned[0].toInt() and 0xFF
        val legacyPayload = versioned.copyOfRange(1, versioned.size)

        assertTrue(ivLen in 12..16)

        val decrypted = service.decryptLegacyFormat(
            encryptedData = legacyPayload,
            aad = aad
        )

        assertArrayEquals(plain, decrypted)
    }

    @Test
    fun decrypt_withWrongAad_failsClosed() {
        val (_, service) = createEncryptionService("wrong-aad")
        val plain = "AAD protected payload".toByteArray(Charsets.UTF_8)
        val encrypted = service.encrypt(
            plainText = plain,
            aad = "aad-correct".toByteArray(Charsets.UTF_8)
        )

        expectThrowable {
            service.decrypt(
                encryptedData = encrypted,
                aad = "aad-wrong".toByteArray(Charsets.UTF_8)
            )
        }
    }

    @Test
    fun noneAuthKeyPosture_doesNotRequireUserAuthentication() {
        val (manager, _) = createEncryptionService("posture-none-auth")

        val snapshot = manager.readKeyPosture()

        assertTrue(snapshot.keyExists)
        assertEquals(false, snapshot.userAuthenticationRequired)
        assertFalse(snapshot.userAuthenticationRequired)
        assertEquals(managerAlias(manager), snapshot.alias)
    }

    private fun createEncryptionService(
        suffix: String
    ): Pair<KeyManager, EncryptionService> {
        val alias = "LifeFlow_Test_Enc_${suffix}_${System.nanoTime()}"
        val manager = KeyManager(
            alias = alias,
            authenticationPolicy = KeyManager.AuthenticationPolicy.NONE
        )
        manager.ensureKey()
        createdManagers += manager
        return manager to EncryptionService(manager)
    }

    private fun managerAlias(
        manager: KeyManager
    ): String {
        val snapshot = manager.readKeyPosture()
        return snapshot.alias
    }

    private fun expectThrowable(
        block: () -> Unit
    ) {
        try {
            block()
        } catch (_: Throwable) {
            return
        }

        fail("Expected decryption failure")
    }
}
