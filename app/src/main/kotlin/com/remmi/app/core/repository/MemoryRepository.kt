package com.remmi.app.core.repository

import com.remmi.app.core.model.models.RemmiModel

abstract class MemoryRepository<T : RemmiModel> :
    RemmiRepository<T> {

    protected val items = mutableMapOf<String, T>()

    override fun add(item: T) {
        items[item.id] = item
    }

    override fun remove(id: String) {
        items.remove(id)
    }

    override fun update(item: T) {
        items[item.id] = item
    }

    override fun get(id: String): T? {
        return items[id]
    }

    override fun getAll(): List<T> {
        return items.values.toList()
    }

    override fun clear() {
        items.clear()
    }
}