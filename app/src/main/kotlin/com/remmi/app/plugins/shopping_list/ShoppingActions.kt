package com.remmi.app.plugins.shopping_list

import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.shopping_list.models.ShoppingItem
import java.util.UUID

class ShoppingActions(
    val repository: ShoppingRepository
) : RemmiAction {
    override val id: String = "shopping_list"
    override val name: String = "Shopping List"
    override var eventBus: EventBus? = null

    fun addItem(name: String, quantity: Int = 1) {
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val item = ShoppingItem(
            id = UUID.randomUUID().toString(),
            created = now,
            modified = now,
            name = name,
            quantity = quantity,
            completed = false
        )
        repository.add(item)
    }

    fun toggleItem(id: String) {
        val existing = repository.get(id) ?: return
        val updated = existing.copy(
            completed = !existing.completed,
            modified = kotlinx.datetime.Instant.fromEpochMilliseconds(System.currentTimeMillis())
        )
        repository.update(updated)
    }

    fun deleteItem(id: String) {
        repository.remove(id)
    }
}
