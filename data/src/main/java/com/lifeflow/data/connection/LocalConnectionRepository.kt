package com.lifeflow.data.connection

import com.lifeflow.data.store.EncryptedModuleStore
import com.lifeflow.domain.connection.ConnectionDepth
import com.lifeflow.domain.connection.ConnectionEntry
import com.lifeflow.domain.connection.ConnectionSignal
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalConnectionRepository(
    private val store: EncryptedModuleStore
) {
    fun saveEntry(entry: ConnectionEntry) {
        val entries = loadAllEntries().toMutableList()
        entries.removeAll { it.id == entry.id }
        entries.add(entry)
        persistEntries(entries)
    }

    fun loadAllEntries(): List<ConnectionEntry> {
        return try {
            val bytes = store.get(KEY_ENTRIES) ?: return emptyList()
            val array = JSONArray(String(bytes))
            (0 until array.length()).mapNotNull { i ->
                deserializeEntry(array.getJSONObject(i))
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun deleteEntry(id: String) {
        val entries = loadAllEntries().filter { it.id != id }
        persistEntries(entries)
    }

    fun clearAll() { store.clearAll() }

    private fun persistEntries(entries: List<ConnectionEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(serializeEntry(it)) }
        store.put(KEY_ENTRIES, array.toString().toByteArray())
    }

    private fun serializeEntry(entry: ConnectionEntry): JSONObject {
        return JSONObject().apply {
            put("id", entry.id)
            put("timestampEpochMillis", entry.timestampEpochMillis)
            put("personTag", entry.personTag)
            put("signal", entry.signal.name)
            put("depth", entry.depth.name)
        }
    }

    private fun deserializeEntry(obj: JSONObject): ConnectionEntry? {
        return try {
            ConnectionEntry(
                id = obj.getString("id"),
                timestampEpochMillis = obj.getLong("timestampEpochMillis"),
                personTag = obj.getString("personTag"),
                signal = ConnectionSignal.valueOf(obj.getString("signal")),
                depth = ConnectionDepth.valueOf(obj.getString("depth"))
            )
        } catch (_: Throwable) { null }
    }

    companion object {
        private const val KEY_ENTRIES = "connection_entries"

        fun newEntry(
            personTag: String,
            signal: ConnectionSignal,
            depth: ConnectionDepth
        ): ConnectionEntry = ConnectionEntry(
            id = UUID.randomUUID().toString(),
            timestampEpochMillis = System.currentTimeMillis(),
            personTag = personTag,
            signal = signal,
            depth = depth
        )
    }
}
