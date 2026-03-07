package com.lifeflow.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class EncryptedIdentityBlobStoreInstrumentedTest {

    private lateinit var context: Context
    private lateinit var store: EncryptedIdentityBlobStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        store = EncryptedIdentityBlobStore(context)
        runCatching { store.clearAll() }
        clearPrefsDirectly()
    }

    @After
    fun tearDown() {
        runCatching { store.clearAll() }
        clearPrefsDirectly()
    }

    @Test
    fun put_and_get_roundTrip_preservesBlob() {
        val id = UUID.randomUUID()
        val blob = byteArrayOf(1, 2, 3, 4, 5, 6)

        store.put(id, blob)

        val restored = awaitValue("Blob was not persisted in time") {
            store.get(id)
        }

        assertArrayEquals(blob, restored)
    }

    @Test
    fun get_returnsNull_forUnknownIdentity() {
        val restored = store.get(UUID.randomUUID())
        assertNull(restored)
    }

    @Test
    fun delete_removesStoredBlob() {
        val id = UUID.randomUUID()
        store.put(id, byteArrayOf(9, 8, 7))
        awaitValue("Blob was not stored before delete") { store.get(id) }

        store.delete(id)

        awaitCondition("Blob was not removed in time") {
            store.get(id) == null && store.entries().none { it.first == id }
        }

        assertNull(store.get(id))
        assertTrue(store.entries().none { it.first == id })
    }

    @Test
    fun entries_returnsAllStoredPairs() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val blob1 = byteArrayOf(1, 1, 1)
        val blob2 = byteArrayOf(2, 2, 2, 2)

        store.put(id1, blob1)
        store.put(id2, blob2)

        val entries = awaitValue("Entries were not fully persisted in time") {
            val map = store.entries().associate { it.first to it.second }
            if (map.size == 2) map else null
        }

        assertEquals(2, entries.size)
        assertArrayEquals(blob1, entries[id1])
        assertArrayEquals(blob2, entries[id2])
    }

    @Test
    fun clearAll_removesAllStoredBlobs_andIndex() {
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()

        store.put(id1, byteArrayOf(1, 2, 3))
        store.put(id2, byteArrayOf(4, 5, 6))

        awaitValue("First blob was not stored before clearAll") { store.get(id1) }
        awaitValue("Second blob was not stored before clearAll") { store.get(id2) }

        store.clearAll()

        assertNull(store.get(id1))
        assertNull(store.get(id2))
        assertTrue(store.entries().isEmpty())
    }

    @Test
    fun entries_skipsInvalidIndexKeys_andMissingPayloads() {
        val validId = UUID.randomUUID()
        val validBlob = byteArrayOf(7, 7, 7)
        store.put(validId, validBlob)
        awaitValue("Valid blob was not stored before index mutation") { store.get(validId) }

        val ok = prefs().edit()
            .putStringSet(
                KEY_INDEX,
                setOf(
                    "id_$validId",
                    "id_not-a-uuid",
                    "totally_invalid_key",
                    "id_${UUID.randomUUID()}"
                )
            )
            .commit()

        assertTrue(ok)

        val entries = store.entries()

        assertEquals(1, entries.size)
        assertEquals(validId, entries.first().first)
        assertArrayEquals(validBlob, entries.first().second)
    }

    @Test
    fun put_overwritesExistingBlob_forSameIdentity() {
        val id = UUID.randomUUID()
        val first = byteArrayOf(1, 2, 3)
        val second = byteArrayOf(4, 5, 6, 7)

        store.put(id, first)
        awaitValue("First blob was not stored in time") { store.get(id) }

        store.put(id, second)

        val restored = awaitValue("Overwritten blob was not stored in time") {
            val current = store.get(id)
            if (current != null && current.contentEquals(second)) current else null
        }

        assertArrayEquals(second, restored)
        assertEquals(1, store.entries().count { it.first == id })
    }

    private fun prefs(): android.content.SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun clearPrefsDirectly() {
        prefs().edit().clear().commit()
    }

    private fun awaitCondition(
        message: String,
        timeoutMs: Long = 2_000L,
        pollMs: Long = 25L,
        predicate: () -> Boolean
    ) {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000L
        while (System.nanoTime() < deadline) {
            if (predicate()) return
            Thread.sleep(pollMs)
        }
        throw AssertionError(message)
    }

    private fun <T> awaitValue(
        message: String,
        timeoutMs: Long = 2_000L,
        pollMs: Long = 25L,
        supplier: () -> T?
    ): T {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000L
        while (System.nanoTime() < deadline) {
            val value = supplier()
            if (value != null) return value
            Thread.sleep(pollMs)
        }
        throw AssertionError(message)
    }

    private companion object {
        private const val PREFS_NAME = "lifeflow_identity_cipher_store_v1"
        private const val KEY_INDEX = "cipher_keys_index"
    }
}