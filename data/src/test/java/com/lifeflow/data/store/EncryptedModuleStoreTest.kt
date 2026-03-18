package com.lifeflow.data.store

import android.content.SharedPreferences
import com.lifeflow.domain.core.EncryptionPort
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EncryptedModuleStoreTest {

    private lateinit var store: EncryptedModuleStore
    private val fakeEncryption = FakeEncryptionPort()
    private val fakePrefs = FakeSharedPreferences()

    @Before
    fun setup() {
        fakePrefs.clearAll()
        store = EncryptedModuleStore(fakePrefs, fakeEncryption)
    }

    @Test
    fun `put and get returns original plaintext`() {
        val plaintext = "hello lifeflow".toByteArray()
        store.put("key1", plaintext)
        val result = store.get("key1")
        assertNotNull(result)
        assertArrayEquals(plaintext, result)
    }

    @Test
    fun `get returns null for missing key`() {
        assertNull(store.get("nonexistent"))
    }

    @Test
    fun `remove deletes key`() {
        store.put("key1", "data".toByteArray())
        store.remove("key1")
        assertNull(store.get("key1"))
    }

    @Test
    fun `clearAll removes all entries`() {
        store.put("key1", "data1".toByteArray())
        store.put("key2", "data2".toByteArray())
        store.clearAll()
        assertNull(store.get("key1"))
        assertNull(store.get("key2"))
    }

    @Test
    fun `get returns null when decrypt fails`() {
        val brokenStore = EncryptedModuleStore(FakeSharedPreferences(), BrokenDecryptionPort())
        brokenStore.put("key1", "data".toByteArray())
        assertNull(brokenStore.get("key1"))
    }

    @Test
    fun `size limit blocks oversized write`() {
        val tinyStore = EncryptedModuleStore(FakeSharedPreferences(), fakeEncryption, maxStoreBytes = 10)
        val bigData = ByteArray(100) { it.toByte() }
        var threw = false
        try {
            tinyStore.put("key1", bigData)
        } catch (e: IllegalStateException) {
            threw = true
        }
        assertTrue(threw)
    }

    @Test
    fun `containsKey returns true after put`() {
        store.put("key1", "data".toByteArray())
        assertTrue(store.containsKey("key1"))
    }

    @Test
    fun `containsKey returns false for missing key`() {
        assertFalse(store.containsKey("missing"))
    }

    @Test
    fun `overwrite existing key works correctly`() {
        store.put("key1", "original".toByteArray())
        store.put("key1", "updated".toByteArray())
        assertArrayEquals("updated".toByteArray(), store.get("key1"))
    }
}

private class FakeEncryptionPort : EncryptionPort {
    override fun encrypt(plaintext: ByteArray): ByteArray = byteArrayOf(0x01) + plaintext
    override fun decrypt(ciphertext: ByteArray): ByteArray = ciphertext.drop(1).toByteArray()
}

private class BrokenDecryptionPort : EncryptionPort {
    override fun encrypt(plaintext: ByteArray): ByteArray = byteArrayOf(0x01) + plaintext
    override fun decrypt(ciphertext: ByteArray): ByteArray = throw IllegalStateException("Decryption failed")
}

private class FakeSharedPreferences : SharedPreferences {
    private val data = mutableMapOf<String, Any?>()

    fun clearAll() { data.clear() }

    override fun getAll(): Map<String, Any?> = data.toMap()
    override fun getString(key: String, defValue: String?) = data[key] as? String ?: defValue
    override fun getStringSet(key: String, defValues: Set<String>?) = null
    override fun getInt(key: String, defValue: Int) = data[key] as? Int ?: defValue
    override fun getLong(key: String, defValue: Long) = data[key] as? Long ?: defValue
    override fun getFloat(key: String, defValue: Float) = data[key] as? Float ?: defValue
    override fun getBoolean(key: String, defValue: Boolean) = data[key] as? Boolean ?: defValue
    override fun contains(key: String) = data.containsKey(key)
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}

    override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private val removes = mutableSetOf<String>()
        private var clearAll = false

        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: Set<String>?) = apply { pending[key] = values }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { removes.add(key) }
        override fun clear() = apply { clearAll = true }

        override fun commit(): Boolean {
            if (clearAll) data.clear()
            removes.forEach { data.remove(it) }
            data.putAll(pending)
            return true
        }
        override fun apply() { commit() }
    }
}
