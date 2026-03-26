package com.lifeflow.security.hardening

import org.junit.Assert.*
import org.junit.Test

class SecureMemoryTest {

    @Test
    fun `wipe clears byte array`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        
        SecureMemory.wipe(data)
        
        assertTrue(data.all { it == 0.toByte() })
    }

    @Test
    fun `wipe clears char array`() {
        val data = charArrayOf('p', 'a', 's', 's')
        
        SecureMemory.wipe(data)
        
        assertTrue(data.all { it == '\u0000' })
    }

    @Test
    fun `wipe handles null byte array`() {
        val data: ByteArray? = null
        
        // Should not throw
        SecureMemory.wipe(data)
    }

    @Test
    fun `wipe handles null char array`() {
        val data: CharArray? = null
        
        // Should not throw
        SecureMemory.wipe(data)
    }

    @Test
    fun `withSecureBytes wipes after block`() {
        val data = byteArrayOf(10, 20, 30)
        
        val result = SecureMemory.withSecureBytes(data) { bytes ->
            bytes.sum()
        }
        
        assertEquals(60, result)
        assertTrue(data.all { it == 0.toByte() })
    }

    @Test
    fun `withSecureBytes wipes even on exception`() {
        val data = byteArrayOf(1, 2, 3)
        
        try {
            SecureMemory.withSecureBytes(data) {
                throw RuntimeException("Test exception")
            }
        } catch (_: RuntimeException) {
            // Expected
        }
        
        assertTrue(data.all { it == 0.toByte() })
    }

    @Test
    fun `withSecureChars wipes after block`() {
        val data = charArrayOf('a', 'b', 'c')
        
        val result = SecureMemory.withSecureChars(data) { chars ->
            String(chars)
        }
        
        assertEquals("abc", result)
        assertTrue(data.all { it == '\u0000' })
    }

    @Test
    fun `withSecureCopy wipes copy not original`() {
        val original = byteArrayOf(5, 10, 15)
        var copyWasCorrect = false
        
        SecureMemory.withSecureCopy(original) { copy ->
            copyWasCorrect = copy.contentEquals(byteArrayOf(5, 10, 15))
        }
        
        // Original should be unchanged
        assertTrue(original.contentEquals(byteArrayOf(5, 10, 15)))
        assertTrue(copyWasCorrect)
    }
}
