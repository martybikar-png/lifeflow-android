package com.lifeflow.domain.diary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShadowDiaryCoreEngineTest {

    private val engine = ShadowDiaryCoreEngine()

    private fun entry(
        signal: DiarySignal,
        intensity: SignalIntensity = SignalIntensity.MODERATE,
        timestampEpochMillis: Long = System.currentTimeMillis()
    ) = DiaryEntry(
        id = java.util.UUID.randomUUID().toString(),
        timestampEpochMillis = timestampEpochMillis,
        signal = signal,
        intensity = intensity
    )

    @Test
    fun `identity not initialized returns BLOCKED`() {
        val result = engine.compute(emptyList(), identityInitialized = false)
        assertEquals(DiaryReadiness.BLOCKED, result.readiness)
        assertNull(result.dominantSignal)
    }

    @Test
    fun `empty entries returns EMPTY`() {
        val result = engine.compute(emptyList(), identityInitialized = true)
        assertEquals(DiaryReadiness.EMPTY, result.readiness)
        assertNull(result.dominantSignal)
    }

    @Test
    fun `dominant signal is most weighted`() {
        val entries = listOf(
            entry(DiarySignal.ENERGY_LOW, SignalIntensity.STRONG),
            entry(DiarySignal.ENERGY_LOW, SignalIntensity.STRONG),
            entry(DiarySignal.FOCUS_CLEAR, SignalIntensity.SUBTLE)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(DiaryReadiness.READY, result.readiness)
        assertEquals(DiarySignal.ENERGY_LOW, result.dominantSignal)
    }

    @Test
    fun `persistent low energy produces recovery note`() {
        val entries = listOf(
            entry(DiarySignal.ENERGY_LOW, SignalIntensity.STRONG),
            entry(DiarySignal.ENERGY_LOW, SignalIntensity.STRONG)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(DiarySignal.ENERGY_LOW, result.dominantSignal)
        assert(result.patternNote?.contains("Persistent") == true)
    }

    @Test
    fun `creative peak returns correct pattern note`() {
        val entries = listOf(
            entry(DiarySignal.CREATIVE_PEAK, SignalIntensity.STRONG),
            entry(DiarySignal.CREATIVE_PEAK, SignalIntensity.MODERATE)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(DiarySignal.CREATIVE_PEAK, result.dominantSignal)
        assert(result.patternNote?.contains("creative") == true)
    }

    @Test
    fun `recent entries limited to 10`() {
        val entries = (1..15).map {
            entry(DiarySignal.MOOD_NEUTRAL, timestampEpochMillis = it.toLong())
        }
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(10, result.recentEntries.size)
    }
}
