package com.remmi.app.core.repository

import com.remmi.app.core.model.models.RemmiModel
import com.remmi.app.core.service.DatabaseService
import kotlinx.serialization.KSerializer

abstract class CloudRepository<T : RemmiModel>(
    protected val databaseService: DatabaseService,
    protected val tableName: String,
    protected val serializer: KSerializer<T>
) : MemoryRepository<T>() {

    suspend fun insert(item: T) {
        databaseService.insert(tableName, item, serializer)
        add(item)
    }

    suspend fun delete(id: String) {
        databaseService.delete(tableName, id)
        remove(id)
    }

    suspend fun updateCloud(item: T) {
        databaseService.update(tableName, item, serializer)
        update(item)
    }

    suspend fun refresh() {
        val items = databaseService.getAll(tableName, serializer)
        clear()
        items.forEach { add(it) }
    }

    suspend fun sync() {
        refresh()
    }
}
