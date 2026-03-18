package com.lifeflow.domain.shopping

/**
 * PredictiveShoppingEngine V1 — anticipation of practical needs.
 *
 * Input:  tracked items + consumption patterns
 * Output: ShoppingState with predicted needs
 *
 * V1 = rule-based threshold detection only.
 * No ML prediction. That is LF Two territory.
 */
data class TrackedItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val currentStock: StockLevel,
    val consumptionRate: ConsumptionRate
)

enum class ItemCategory {
    HEALTH_SUPPLEMENT,
    FOOD_STAPLE,
    PERSONAL_CARE,
    HOUSEHOLD,
    FITNESS,
    OTHER
}

enum class StockLevel {
    FULL,
    ADEQUATE,
    LOW,
    CRITICAL,
    OUT
}

enum class ConsumptionRate {
    SLOW,
    MODERATE,
    FAST
}

data class ShoppingState(
    val urgentNeeds: List<TrackedItem>,
    val upcomingNeeds: List<TrackedItem>,
    val readiness: ShoppingReadiness
)

enum class ShoppingReadiness {
    BLOCKED,
    EMPTY,
    READY
}
