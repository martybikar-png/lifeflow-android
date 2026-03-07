package com.lifeflow.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EncryptionServiceTest {

    private val service = newEncryptionServiceForFormatGuardTests()

    @Test
    fun `decrypt rejects empty versioned payload`() {
        val error = expectFailure {
            service.decrypt(ByteArray(0))
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid versioned encrypted data", error.message)
    }

    @Test
    fun `decryptVersionedFormat rejects payload shorter than header`() {
        val error = expectFailure {
            service.decryptVersionedFormat(byteArrayOf(12))
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid versioned encrypted data", error.message)
    }

    @Test
    fun `decryptVersionedFormat rejects iv prefix below minimum`() {
        val error = expectFailure {
            service.decryptVersionedFormat(byteArrayOf(11, 1, 2, 3))
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid versioned IV length prefix: 11", error.message)
    }

    @Test
    fun `decryptVersionedFormat rejects iv prefix above maximum`() {
        val error = expectFailure {
            service.decryptVersionedFormat(byteArrayOf(17, 1, 2, 3))
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid versioned IV length prefix: 17", error.message)
    }

    @Test
    fun `decryptVersionedFormat rejects payload with missing ciphertext`() {
        val payload = ByteArray(13)
        payload[0] = 12

        val error = expectFailure {
            service.decryptVersionedFormat(payload)
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid versioned encrypted data length", error.message)
    }

    @Test
    fun `decryptLegacyFormat rejects payload shorter than legacy minimum`() {
        val error = expectFailure {
            service.decryptLegacyFormat(ByteArray(12))
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid legacy encrypted data", error.message)
    }

    @Test
    fun `decrypt uses strict versioned path`() {
        val error = expectFailure {
            service.decrypt(byteArrayOf(17, 9, 9, 9))
        }

        assertTrue(error is IllegalArgumentException)
        assertEquals("Invalid versioned IV length prefix: 17", error.message)
    }

    private fun newEncryptionServiceForFormatGuardTests(): EncryptionService {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val theUnsafeField = unsafeClass.getDeclaredField("theUnsafe")
        theUnsafeField.isAccessible = true
        val unsafe = theUnsafeField.get(null)

        val allocateInstance = unsafeClass.getMethod("allocateInstance", Class::class.java)
        return allocateInstance.invoke(unsafe, EncryptionService::class.java) as EncryptionService
    }

    private fun expectFailure(block: () -> Unit): Throwable {
        return try {
            block()
            fail("Expected failure but block completed successfully.")
            throw AssertionError("Unreachable")
        } catch (t: Throwable) {
            t
        }
    }
}