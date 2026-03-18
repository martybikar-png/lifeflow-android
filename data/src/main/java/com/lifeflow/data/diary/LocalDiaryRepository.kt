package com.lifeflow.data.diary

import com.lifeflow.data.store.EncryptedModuleStore
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.SignalIntensity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * LocalDiaryRepository — encrypted persistent storage for diary entries.
 *
 * Uses EncryptedModuleStore (AES-256-GCM) for all persistence.
 * Fail-closed on read errors — returns empty list.
 * Fail-closed on write errors — throws.
 */
class LocalDiaryRepository(
    private val store: EncryptedModuleStore
) {
    fun saveEntry(entry: DiaryEntry) {
        val entries = loadAllEntries().toMutableList()
        entries.removeAll { it.id == entry.id }
        entries.add(entry)
        persistEntries(entries)
    }

    fun loadAllEntries(): List<DiaryEntry> {
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

    fun clearAll() {
        store.clearAll()
    }

    private fun persistEntries(entries: List<DiaryEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(serializeEntry(it)) }
        store.put(KEY_ENTRIES, array.toString().toByteArray())
    }

    private fun serializeEntry(entry: DiaryEntry): JSONObject {
        return JSONObject().apply {
            put("id", entry.id)
            put("timestampEpochMillis", entry.timestampEpochMillis)
            put("signal", entry.signal.name)
            put("intensity", entry.intensity.name)
            put("note", entry.note)
        }
    }

    private fun deserializeEntry(obj: JSONObject): DiaryEntry? {
        return try {
            DiaryEntry(
                id = obj.getString("id"),
                timestampEpochMillis = obj.getLong("timestampEpochMillis"),
                signal = DiarySignal.valueOf(obj.getString("signal")),
                intensity = SignalIntensity.valueOf(obj.getString("intensity")),
                note = obj.optString("note", "")
            )
        } catch (_: Throwable) {
            null
        }
    }

    companion object {
        private const val KEY_ENTRIES = "diary_entries"

        fun newEntry(
            signal: DiarySignal,
            intensity: SignalIntensity,
            note: String = ""
        ): DiaryEntry = DiaryEntry(
            id = UUID.randomUUID().toString(),
            timestampEpochMillis = System.currentTimeMillis(),
            signal = signal,
            intensity = intensity,
            note = note
        )
    }
}
