package com.lifeflow.data.memory

import com.lifeflow.data.store.EncryptedModuleStore
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.MemorySignificance
import com.lifeflow.domain.memory.MemoryTag
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalMemoryRepository(
    private val store: EncryptedModuleStore
) {
    fun saveEntry(entry: MemoryEntry) {
        val entries = loadAllEntries().toMutableList()
        entries.removeAll { it.id == entry.id }
        entries.add(entry)
        persistEntries(entries)
    }

    fun loadAllEntries(): List<MemoryEntry> {
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

    private fun persistEntries(entries: List<MemoryEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(serializeEntry(it)) }
        store.put(KEY_ENTRIES, array.toString().toByteArray())
    }

    private fun serializeEntry(entry: MemoryEntry): JSONObject {
        val tagsArray = JSONArray()
        entry.tags.forEach { tagsArray.put(it.name) }
        return JSONObject().apply {
            put("id", entry.id)
            put("timestampEpochMillis", entry.timestampEpochMillis)
            put("content", entry.content)
            put("tags", tagsArray)
            put("significance", entry.significance.name)
        }
    }

    private fun deserializeEntry(obj: JSONObject): MemoryEntry? {
        return try {
            val tagsArray = obj.getJSONArray("tags")
            val tags = (0 until tagsArray.length())
                .mapNotNull { i -> runCatching { MemoryTag.valueOf(tagsArray.getString(i)) }.getOrNull() }
                .toSet()
            MemoryEntry(
                id = obj.getString("id"),
                timestampEpochMillis = obj.getLong("timestampEpochMillis"),
                content = obj.getString("content"),
                tags = tags,
                significance = MemorySignificance.valueOf(obj.getString("significance"))
            )
        } catch (_: Throwable) { null }
    }

    companion object {
        private const val KEY_ENTRIES = "memory_entries"

        fun newEntry(
            content: String,
            tags: Set<MemoryTag>,
            significance: MemorySignificance = MemorySignificance.MEDIUM
        ): MemoryEntry = MemoryEntry(
            id = UUID.randomUUID().toString(),
            timestampEpochMillis = System.currentTimeMillis(),
            content = content,
            tags = tags,
            significance = significance
        )
    }
}
