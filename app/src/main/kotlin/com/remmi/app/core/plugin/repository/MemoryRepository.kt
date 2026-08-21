package com.remmi.app.core.plugin.repository

import android.util.Log
import com.remmi.app.core.plugin.model.models.RemmiModel

abstract class MemoryRepository<T : RemmiModel> :
    RemmiRepository<T> {

    protected val items = mutableMapOf<String, T>()

    init {
        Log.d("Remmi", "[MemoryRepository] - [constructor] executed")
    }

    override fun add(item: T) {
        Log.d("Remmi", "[MemoryRepository] - [add] executed")
        items[item.id] = item
    }

    override fun remove(id: String) {
        Log.d("Remmi", "[MemoryRepository] - [remove] executed")
        items.remove(id)
    }

    override fun update(item: T) {
        Log.d("Remmi", "[MemoryRepository] - [update] executed")
        items[item.id] = item
    }

    override fun get(id: String): T? {
        Log.d("Remmi", "[MemoryRepository] - [get] executed")
        return items[id]
    }

    override fun getAll(): List<T> {
        Log.d("Remmi", "[MemoryRepository] - [getAll] executed")
        return items.values.toList()
    }

    override fun clear() {
        Log.d("Remmi", "[MemoryRepository] - [clear] executed")
        items.clear()
    }
}