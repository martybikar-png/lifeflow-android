package com.lifeflow.domain.shopping

import org.junit.Assert.assertEquals
import org.junit.Test

class PredictiveShoppingEngineTest {

    private val engine = PredictiveShoppingEngine()

    private fun item(
        stock: StockLevel = StockLevel.ADEQUATE,
        rate: ConsumptionRate = ConsumptionRate.MODERATE,
        category: ItemCategory = ItemCategory.FOOD_STAPLE
    ) = TrackedItem(
        id = java.util.UUID.randomUUID().toString(),
        name = "Test item",
        category = category,
        currentStock = stock,
        consumptionRate = rate
    )

    @Test
    fun `identity not initialized returns BLOCKED`() {
        val result = engine.compute(emptyList(), identityInitialized = false)
        assertEquals(ShoppingReadiness.BLOCKED, result.readiness)
    }

    @Test
    fun `empty items returns EMPTY`() {
        val result = engine.compute(emptyList(), identityInitialized = true)
        assertEquals(ShoppingReadiness.EMPTY, result.readiness)
    }

    @Test
    fun `CRITICAL stock is urgent`() {
        val result = engine.compute(
            listOf(item(stock = StockLevel.CRITICAL)),
            identityInitialized = true
        )
        assertEquals(1, result.urgentNeeds.size)
        assertEquals(0, result.upcomingNeeds.size)
    }

    @Test
    fun `OUT stock is urgent`() {
        val result = engine.compute(
            listOf(item(stock = StockLevel.OUT)),
            identityInitialized = true
        )
        assertEquals(1, result.urgentNeeds.size)
    }

    @Test
    fun `LOW stock with FAST rate is upcoming`() {
        val result = engine.compute(
            listOf(item(stock = StockLevel.LOW, rate = ConsumptionRate.FAST)),
            identityInitialized = true
        )
        assertEquals(0, result.urgentNeeds.size)
        assertEquals(1, result.upcomingNeeds.size)
    }

    @Test
    fun `LOW stock with SLOW rate is not upcoming`() {
        val result = engine.compute(
            listOf(item(stock = StockLevel.LOW, rate = ConsumptionRate.SLOW)),
            identityInitialized = true
        )
        assertEquals(0, result.urgentNeeds.size)
        assertEquals(0, result.upcomingNeeds.size)
    }

    @Test
    fun `ADEQUATE stock is neither urgent nor upcoming`() {
        val result = engine.compute(
            listOf(item(stock = StockLevel.ADEQUATE, rate = ConsumptionRate.FAST)),
            identityInitialized = true
        )
        assertEquals(0, result.urgentNeeds.size)
        assertEquals(0, result.upcomingNeeds.size)
    }
}
