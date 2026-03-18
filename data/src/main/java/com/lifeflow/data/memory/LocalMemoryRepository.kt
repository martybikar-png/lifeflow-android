package com.lifeflow.data.memory

import android.content.Context
import com.lifeflow.domain.memory.MemoryEntry
import com.lifeflow.domain.memory.MemorySignificance
import com.lifeflow.domain.memory.MemoryTag
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * LocalMemoryRepository — persistent local storage for memory entries.
 *
 * Uses SharedPreferences with JSON serialization.
 * Fail-closed on read errors — returns empty list.
 * Fail-closed on write errors — throws.
 */
class LocalMemoryRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveEntry(entry: MemoryEntry) {
        val entries = loadAllEntries().toMutableList()
        entries.removeAll { it.id == entry.id }
        entries.add(entry)
        persistEntries(entries)
    }

    fun loadAllEntries(): List<MemoryEntry> {
        return try {
            val json = prefs.getString(KEY_ENTRIES, null) ?: return emptyList()
            val array = JSONArray(json)
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

    fun clearAll() {
        prefs.edit().remove(KEY_ENTRIES).commit()
    }

    private fun persistEntries(entries: List<MemoryEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(serializeEntry(it)) }
        val ok = prefs.edit().putString(KEY_ENTRIES, array.toString()).commit()
        if (!ok) throw IllegalStateException("Memory entries commit failed")
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
                .mapNotNull { i ->
                    runCatching { MemoryTag.valueOf(tagsArray.getString(i)) }.getOrNull()
                }
                .toSet()
            MemoryEntry(
                id = obj.getString("id"),
                timestampEpochMillis = obj.getLong("timestampEpochMillis"),
                content = obj.getString("content"),
                tags = tags,
                significance = MemorySignificance.valueOf(obj.getString("significance"))
            )
        } catch (_: Throwable) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "lifeflow_memory"
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
