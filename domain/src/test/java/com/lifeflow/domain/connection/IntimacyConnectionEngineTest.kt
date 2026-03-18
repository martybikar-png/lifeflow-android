package com.lifeflow.domain.connection

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IntimacyConnectionEngineTest {

    private val engine = IntimacyConnectionEngine()

    private fun entry(
        signal: ConnectionSignal,
        depth: ConnectionDepth = ConnectionDepth.MODERATE,
        timestampEpochMillis: Long = System.currentTimeMillis()
    ) = ConnectionEntry(
        id = java.util.UUID.randomUUID().toString(),
        timestampEpochMillis = timestampEpochMillis,
        personTag = "test",
        signal = signal,
        depth = depth
    )

    @Test
    fun `identity not initialized returns BLOCKED`() {
        val result = engine.compute(emptyList(), identityInitialized = false)
        assertEquals(ConnectionReadiness.BLOCKED, result.readiness)
        assertNull(result.dominantSignal)
    }

    @Test
    fun `empty entries returns EMPTY`() {
        val result = engine.compute(emptyList(), identityInitialized = true)
        assertEquals(ConnectionReadiness.EMPTY, result.readiness)
    }

    @Test
    fun `dominant signal is most weighted`() {
        val entries = listOf(
            entry(ConnectionSignal.MEANINGFUL_INTERACTION, ConnectionDepth.DEEP),
            entry(ConnectionSignal.MEANINGFUL_INTERACTION, ConnectionDepth.DEEP),
            entry(ConnectionSignal.SURFACE_INTERACTION, ConnectionDepth.SHALLOW)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(ConnectionSignal.MEANINGFUL_INTERACTION, result.dominantSignal)
    }

    @Test
    fun `persistent conflict produces strong note`() {
        val entries = listOf(
            entry(ConnectionSignal.CONFLICT, ConnectionDepth.DEEP),
            entry(ConnectionSignal.CONFLICT, ConnectionDepth.DEEP)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(ConnectionSignal.CONFLICT, result.dominantSignal)
        assert(result.connectionNote?.contains("Persistent") == true)
    }

    @Test
    fun `boundary crossed deep produces alert note`() {
        val entries = listOf(
            entry(ConnectionSignal.BOUNDARY_CROSSED, ConnectionDepth.DEEP),
            entry(ConnectionSignal.BOUNDARY_CROSSED, ConnectionDepth.DEEP)
        )
        val result = engine.compute(entries, identityInitialized = true)
        assert(result.connectionNote?.contains("Persistent") == true)
    }

    @Test
    fun `recent entries limited to 10`() {
        val entries = (1..15).map {
            entry(ConnectionSignal.SURFACE_INTERACTION, timestampEpochMillis = it.toLong())
        }
        val result = engine.compute(entries, identityInitialized = true)
        assertEquals(10, result.recentEntries.size)
    }
}
