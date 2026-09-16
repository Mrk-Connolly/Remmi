package com.remmi.app.core.plugin.repository

import android.util.Log
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class MemoryRepository<T : RemmiModel> :
    RemmiRepository<T> {

    protected val items = mutableMapOf<String, T>()
    
    private val _itemsFlow = MutableStateFlow<List<T>>(emptyList())

    init {
        Log.d("Remmi", "[MemoryRepository] - [constructor] executed")
    }

    override fun add(item: T) {
        Log.d("Remmi", "[MemoryRepository] - [add] executed")
        items[item.id] = item
        notifyChanged()
    }

    override fun remove(id: String) {
        Log.d("Remmi", "[MemoryRepository] - [remove] executed")
        items.remove(id)
        notifyChanged()
    }

    override fun update(item: T) {
        Log.d("Remmi", "[MemoryRepository] - [update] executed")
        items[item.id] = item
        notifyChanged()
    }

    override fun get(id: String): T? {
        Log.d("Remmi", "[MemoryRepository] - [get] executed")
        return items[id]
    }

    override fun getAll(): List<T> {
        Log.d("Remmi", "[MemoryRepository] - [getAll] executed")
        return items.values.toList()
    }

    override fun asFlow(): Flow<List<T>> {
        return _itemsFlow.asStateFlow()
    }

    override fun clear() {
        Log.d("Remmi", "[MemoryRepository] - [clear] executed")
        items.clear()
        notifyChanged()
    }

    private fun notifyChanged() {
        _itemsFlow.value = items.values.toList()
    }
}
