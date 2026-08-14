package com.remmi.app.core.plugins.repository

import android.util.Log
import com.remmi.app.core.plugins.model.models.RemmiModel
import com.remmi.app.core.service.DatabaseService
import kotlinx.serialization.KSerializer

abstract class CloudRepository<T : RemmiModel>(
    protected val databaseService: DatabaseService,
    protected val tableName: String,
    protected val serializer: KSerializer<T>
) : MemoryRepository<T>() {

    init {
        Log.d("Remmi", "[CloudRepository] - [constructor] executed")
    }

    suspend fun insert(item: T) {
        Log.d("Remmi", "[CloudRepository] - [insert] executed")
        databaseService.insert(tableName, item, serializer)
        add(item)
    }

    suspend fun delete(id: String) {
        Log.d("Remmi", "[CloudRepository] - [delete] executed")
        databaseService.delete(tableName, id)
        remove(id)
    }

    suspend fun updateCloud(item: T) {
        Log.d("Remmi", "[CloudRepository] - [updateCloud] executed")
        databaseService.update(tableName, item, serializer)
        update(item)
    }

    suspend fun refresh() {
        Log.d("Remmi", "[CloudRepository] - [refresh] executed")
        val items = databaseService.getAll(tableName, serializer)
        clear()
        items.forEach { add(it) }
    }

    suspend fun sync() {
        Log.d("Remmi", "[CloudRepository] - [sync] executed")
        refresh()
    }

    suspend fun clearAll() {
        Log.d("Remmi", "[CloudRepository] - [clearAll] executed")
        databaseService.clearTable(tableName)
        clear()
    }
}
