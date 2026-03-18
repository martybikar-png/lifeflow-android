package com.lifeflow.domain.memory

import org.junit.Assert.assertEquals
import org.junit.Test

class SecondBrainEngineTest {

    private val engine = SecondBrainEngine()

    private fun entry(
        significance: MemorySignificance = MemorySignificance.MEDIUM,
        timestampEpochMillis: Long = System.currentTimeMillis(),
        tags: Set<MemoryTag> = setOf(MemoryTag.INSIGHT)
    ) = MemoryEntry(
        id = java.util.UUID.randomUUID().toString(),
        timestampEpochMillis = timestampEpochMillis,
        content = "Test entry",
        tags = tags,
        significance = significance
    )

    @Test
    fun `identity not initialized returns BLOCKED`() {
        val result = engine.compute(emptyList(), identityInitialized = false)
        assertEquals(MemoryReadiness.BLOCKED, result.readiness)
        assertEquals(0, result.recentEntries.size)
        assertEquals(0, result.pinnedEntries.size)
    }

    @Test
    fun `empty entries returns EMPTY`() {
        val result = engine.compute(emptyList(), identityInitialized = true)
        assertEquals(MemoryReadiness.EMPTY, result.readiness)
    }

    @Test
    fun `recent entries limited to 10`() {
        val entries = (1..15).map { entry(timestampEpochMillis = it.toLong()) }
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(10, result.recentEntries.size)
    }

    @Test
    fun `pinned entries are HIGH significance only`() {
        val entries = listOf(
            entry(significance = MemorySignificance.HIGH),
            entry(significance = MemorySignificance.HIGH),
            entry(significance = MemorySignificance.MEDIUM),
            entry(significance = MemorySignificance.LOW)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(2, result.pinnedEntries.size)
        assert(result.pinnedEntries.all { it.significance == MemorySignificance.HIGH })
    }

    @Test
    fun `pinned entries limited to 5`() {
        val entries = (1..8).map {
            entry(significance = MemorySignificance.HIGH, timestampEpochMillis = it.toLong())
        }
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(5, result.pinnedEntries.size)
    }

    @Test
    fun `recent entries sorted newest first`() {
        val entries = listOf(
            entry(timestampEpochMillis = 100L),
            entry(timestampEpochMillis = 300L),
            entry(timestampEpochMillis = 200L)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(300L, result.recentEntries.first().timestampEpochMillis)
    }
}
