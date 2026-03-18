package com.lifeflow.domain.diary

/**
 * ShadowDiaryCore V1 — introspective signal layer.
 *
 * Captures subtle internal signals and long-term patterns.
 * V1 = local entry storage + basic pattern detection.
 * No ML, no cloud. Fail-closed on missing identity.
 */
data class DiaryEntry(
    val id: String,
    val timestampEpochMillis: Long,
    val signal: DiarySignal,
    val intensity: SignalIntensity,
    val note: String = ""
)

enum class DiarySignal {
    ENERGY_LOW,
    ENERGY_HIGH,
    FOCUS_CLEAR,
    FOCUS_SCATTERED,
    MOOD_POSITIVE,
    MOOD_NEUTRAL,
    MOOD_NEGATIVE,
    STRESS_LOW,
    STRESS_HIGH,
    RECOVERY_NEEDED,
    CREATIVE_PEAK,
    SOCIAL_DRAINED,
    SOCIAL_RECHARGED
}

enum class SignalIntensity {
    SUBTLE,
    MODERATE,
    STRONG
}

data class ShadowDiaryState(
    val recentEntries: List<DiaryEntry>,
    val dominantSignal: DiarySignal?,
    val patternNote: String?,
    val readiness: DiaryReadiness
)

enum class DiaryReadiness {
    BLOCKED,
    EMPTY,
    READY
}
