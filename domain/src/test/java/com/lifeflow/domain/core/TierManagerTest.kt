package com.lifeflow.domain.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TierManagerTest {

    @Test
    fun `default tier is CORE`() {
        val manager = TierManager()
        assertEquals(TierState.CORE, manager.currentTier())
        assertTrue(manager.isCore())
    }

    @Test
    fun `FREE tier gateOrNull returns message for CORE operations`() {
        val manager = TierManager { TierState.FREE }
        val message = manager.gateOrNull(TierState.CORE)
        assertNotNull(message)
        assertTrue(message!!.contains("Core"))
    }

    @Test
    fun `CORE tier gateOrNull returns null for CORE operations`() {
        val manager = TierManager { TierState.CORE }
        assertNull(manager.gateOrNull(TierState.CORE))
    }

    @Test
    fun `tierGateMessage returns null when tier allows`() {
        val manager = TierManager { TierState.CORE }
        assertNull(tierGateMessage(manager, TierState.CORE, "test"))
    }

    @Test
    fun `tierGateMessage returns message when tier blocked`() {
        val manager = TierManager { TierState.FREE }
        val msg = tierGateMessage(manager, TierState.CORE, "test")
        assertNotNull(msg)
        assertTrue(msg!!.contains("TIER_GATE"))
    }
}
