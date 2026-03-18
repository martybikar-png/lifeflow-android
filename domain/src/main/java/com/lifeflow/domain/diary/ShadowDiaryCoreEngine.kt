package com.lifeflow.domain.diary

/**
 * ShadowDiaryCoreEngine V1 — pattern detection from diary entries.
 *
 * Input:  List<DiaryEntry> + identityInitialized flag
 * Output: ShadowDiaryState with dominant signal + pattern note
 *
 * Zero Android dependencies. No coroutines. Fail-closed.
 */
class ShadowDiaryCoreEngine {

    fun compute(
        entries: List<DiaryEntry>,
        identityInitialized: Boolean
    ): ShadowDiaryState {
        if (!identityInitialized) {
            return ShadowDiaryState(
                recentEntries = emptyList(),
                dominantSignal = null,
                patternNote = "Diary blocked: identity not initialized.",
                readiness = DiaryReadiness.BLOCKED
            )
        }

        val recent = entries
            .sortedByDescending { it.timestampEpochMillis }
            .take(MAX_RECENT_ENTRIES)

        if (recent.isEmpty()) {
            return ShadowDiaryState(
                recentEntries = emptyList(),
                dominantSignal = null,
                patternNote = "No diary entries yet. Start recording signals.",
                readiness = DiaryReadiness.EMPTY
            )
        }

        val dominantSignal = resolveDominantSignal(recent)
        val patternNote = resolvePatternNote(dominantSignal, recent)

        return ShadowDiaryState(
            recentEntries = recent,
            dominantSignal = dominantSignal,
            patternNote = patternNote,
            readiness = DiaryReadiness.READY
        )
    }

    private fun resolveDominantSignal(
        entries: List<DiaryEntry>
    ): DiarySignal? {
        return entries
            .groupBy { it.signal }
            .mapValues { (_, group) ->
                group.sumOf { intensityWeight(it.intensity) }
            }
            .maxByOrNull { it.value }
            ?.key
    }

    private fun intensityWeight(intensity: SignalIntensity): Int {
        return when (intensity) {
            SignalIntensity.SUBTLE -> 1
            SignalIntensity.MODERATE -> 2
            SignalIntensity.STRONG -> 3
        }
    }

    private fun resolvePatternNote(
        dominantSignal: DiarySignal?,
        entries: List<DiaryEntry>
    ): String {
        if (dominantSignal == null) return "No dominant pattern detected yet."

        val strongCount = entries.count {
            it.signal == dominantSignal &&
            it.intensity == SignalIntensity.STRONG
        }

        return when (dominantSignal) {
            DiarySignal.ENERGY_LOW ->
                if (strongCount >= 2) "Persistent low energy detected. Recovery recommended."
                else "Low energy signal present."
            DiarySignal.ENERGY_HIGH ->
                "High energy pattern — good window for demanding tasks."
            DiarySignal.FOCUS_CLEAR ->
                "Clear focus pattern — leverage for deep work."
            DiarySignal.FOCUS_SCATTERED ->
                "Scattered focus pattern. Consider shorter sessions."
            DiarySignal.MOOD_POSITIVE ->
                "Positive mood pattern — good time for social or creative work."
            DiarySignal.MOOD_NEGATIVE ->
                if (strongCount >= 2) "Persistent low mood detected. Consider rest or recovery."
                else "Low mood signal present."
            DiarySignal.MOOD_NEUTRAL ->
                "Neutral mood — steady state, maintain current rhythm."
            DiarySignal.STRESS_LOW ->
                "Low stress pattern — good capacity window."
            DiarySignal.STRESS_HIGH ->
                if (strongCount >= 2) "Persistent high stress detected. Prioritize recovery."
                else "Elevated stress signal present."
            DiarySignal.RECOVERY_NEEDED ->
                "Recovery signal dominant. Defer demanding tasks."
            DiarySignal.CREATIVE_PEAK ->
                "Creative peak pattern — prioritize creative work now."
            DiarySignal.SOCIAL_DRAINED ->
                "Social drain pattern. Consider solo recovery time."
            DiarySignal.SOCIAL_RECHARGED ->
                "Social recharge pattern — collaborative work suitable."
        }
    }

    companion object {
        private const val MAX_RECENT_ENTRIES = 10
    }
}
