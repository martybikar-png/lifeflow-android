package com.lifeflow.data.shopping

import com.lifeflow.data.store.EncryptedModuleStore
import com.lifeflow.domain.shopping.ConsumptionRate
import com.lifeflow.domain.shopping.ItemCategory
import com.lifeflow.domain.shopping.StockLevel
import com.lifeflow.domain.shopping.TrackedItem
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class LocalShoppingRepository(
    private val store: EncryptedModuleStore
) {
    fun saveItem(item: TrackedItem) {
        val items = loadAllItems().toMutableList()
        items.removeAll { it.id == item.id }
        items.add(item)
        persistItems(items)
    }

    fun loadAllItems(): List<TrackedItem> {
        return try {
            val bytes = store.get(KEY_ITEMS) ?: return emptyList()
            val array = JSONArray(String(bytes))
            (0 until array.length()).mapNotNull { i ->
                deserializeItem(array.getJSONObject(i))
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun updateStockLevel(id: String, stockLevel: StockLevel) {
        val items = loadAllItems().toMutableList()
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) {
            items[index] = items[index].copy(currentStock = stockLevel)
            persistItems(items)
        }
    }

    fun deleteItem(id: String) {
        val items = loadAllItems().filter { it.id != id }
        persistItems(items)
    }

    fun clearAll() { store.clearAll() }

    private fun persistItems(items: List<TrackedItem>) {
        val array = JSONArray()
        items.forEach { array.put(serializeItem(it)) }
        store.put(KEY_ITEMS, array.toString().toByteArray())
    }

    private fun serializeItem(item: TrackedItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("name", item.name)
            put("category", item.category.name)
            put("currentStock", item.currentStock.name)
            put("consumptionRate", item.consumptionRate.name)
        }
    }

    private fun deserializeItem(obj: JSONObject): TrackedItem? {
        return try {
            TrackedItem(
                id = obj.getString("id"),
                name = obj.getString("name"),
                category = ItemCategory.valueOf(obj.getString("category")),
                currentStock = StockLevel.valueOf(obj.getString("currentStock")),
                consumptionRate = ConsumptionRate.valueOf(obj.getString("consumptionRate"))
            )
        } catch (_: Throwable) { null }
    }

    companion object {
        private const val KEY_ITEMS = "tracked_items"

        fun newItem(
            name: String,
            category: ItemCategory,
            currentStock: StockLevel,
            consumptionRate: ConsumptionRate
        ): TrackedItem = TrackedItem(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            currentStock = currentStock,
            consumptionRate = consumptionRate
        )
    }
}
