package com.lifeflow.domain.shopping

/**
 * PredictiveShoppingEngine V1 — rule-based need anticipation.
 *
 * Urgent = CRITICAL or OUT stock
 * Upcoming = LOW stock + FAST/MODERATE consumption
 *
 * Fail-closed on empty inventory. No ML.
 */
class PredictiveShoppingEngine {

    fun compute(
        items: List<TrackedItem>,
        identityInitialized: Boolean
    ): ShoppingState {
        if (!identityInitialized) {
            return ShoppingState(
                urgentNeeds = emptyList(),
                upcomingNeeds = emptyList(),
                readiness = ShoppingReadiness.BLOCKED
            )
        }

        if (items.isEmpty()) {
            return ShoppingState(
                urgentNeeds = emptyList(),
                upcomingNeeds = emptyList(),
                readiness = ShoppingReadiness.EMPTY
            )
        }

        val urgent = items.filter { isUrgent(it) }
        val upcoming = items.filter { !isUrgent(it) && isUpcoming(it) }

        return ShoppingState(
            urgentNeeds = urgent.sortedBy { it.category.ordinal },
            upcomingNeeds = upcoming.sortedBy { it.category.ordinal },
            readiness = ShoppingReadiness.READY
        )
    }

    private fun isUrgent(item: TrackedItem): Boolean {
        return item.currentStock == StockLevel.CRITICAL ||
                item.currentStock == StockLevel.OUT
    }

    private fun isUpcoming(item: TrackedItem): Boolean {
        return item.currentStock == StockLevel.LOW &&
                (item.consumptionRate == ConsumptionRate.FAST ||
                item.consumptionRate == ConsumptionRate.MODERATE)
    }
}
