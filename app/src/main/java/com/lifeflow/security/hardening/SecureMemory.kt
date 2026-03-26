package com.lifeflow.security.hardening

import java.util.Arrays

/**
 * SecureMemory — sanitizes sensitive data from memory after use.
 * 
 * Prevents memory dump attacks by zeroing out sensitive byte arrays
 * and char arrays as soon as they're no longer needed.
 */
object SecureMemory {

    /**
     * Securely wipes a byte array by overwriting with zeros.
     * Call this immediately after using sensitive data (keys, passwords, etc.)
     */
    fun wipe(data: ByteArray?) {
        data?.let { Arrays.fill(it, 0.toByte()) }
    }

    /**
     * Securely wipes a char array by overwriting with zeros.
     * Use for password char arrays.
     */
    fun wipe(data: CharArray?) {
        data?.let { Arrays.fill(it, '\u0000') }
    }

    /**
     * Executes a block with sensitive byte data, then wipes it.
     * Ensures cleanup even if exception is thrown.
     */
    inline fun <T> withSecureBytes(data: ByteArray, block: (ByteArray) -> T): T {
        return try {
            block(data)
        } finally {
            wipe(data)
        }
    }

    /**
     * Executes a block with sensitive char data, then wipes it.
     * Ensures cleanup even if exception is thrown.
     */
    inline fun <T> withSecureChars(data: CharArray, block: (CharArray) -> T): T {
        return try {
            block(data)
        } finally {
            wipe(data)
        }
    }

    /**
     * Creates a copy of sensitive bytes, executes block, wipes both.
     * Use when you need to preserve original during operation.
     */
    inline fun <T> withSecureCopy(original: ByteArray, block: (ByteArray) -> T): T {
        val copy = original.copyOf()
        return try {
            block(copy)
        } finally {
            wipe(copy)
        }
    }
}
