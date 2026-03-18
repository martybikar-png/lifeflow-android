package com.lifeflow.data.diary

import android.content.Context
import com.lifeflow.domain.diary.DiaryEntry
import com.lifeflow.domain.diary.DiarySignal
import com.lifeflow.domain.diary.SignalIntensity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * LocalDiaryRepository — persistent local storage for diary entries.
 *
 * Uses SharedPreferences with JSON serialization.
 * Fail-closed on read errors — returns empty list.
 * Fail-closed on write errors — throws.
 */
class LocalDiaryRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveEntry(entry: DiaryEntry) {
        val entries = loadAllEntries().toMutableList()
        entries.removeAll { it.id == entry.id }
        entries.add(entry)
        persistEntries(entries)
    }

    fun loadAllEntries(): List<DiaryEntry> {
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

    private fun persistEntries(entries: List<DiaryEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(serializeEntry(it)) }
        val ok = prefs.edit().putString(KEY_ENTRIES, array.toString()).commit()
        if (!ok) throw IllegalStateException("Diary entries commit failed")
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
        private const val PREFS_NAME = "lifeflow_diary"
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
