package com.remmi.app.testing.core

import com.remmi.app.core.database.DatabaseService
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.KSerializer

class MockDatabaseService : DatabaseService {
    private val tables = mutableMapOf<String, MutableList<RemmiModel>>()

    override suspend fun <T : RemmiModel> insert(tableName: String, item: T, serializer: KSerializer<T>) {
        tables.getOrPut(tableName) { mutableListOf() }.add(item)
    }

    override suspend fun delete(tableName: String, id: String) {
        tables[tableName]?.removeIf { it.id == id }
    }

    override suspend fun <T : RemmiModel> update(tableName: String, item: T, serializer: KSerializer<T>) {
        val table = tables[tableName] ?: return
        val index = table.indexOfFirst { it.id == item.id }
        if (index != -1) {
            table[index] = item
        } else {
            table.add(item)
        }
    }

    override suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T> {
        @Suppress("UNCHECKED_CAST")
        return (tables[tableName] ?: emptyList<RemmiModel>()) as List<T>
    }

    override suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return tables[tableName]?.find { it.id == id } as? T
    }

    override suspend fun <T : RemmiModel> getBySource(
        tableName: String,
        sourcePlugin: String,
        sourceItemId: String,
        serializer: KSerializer<T>
    ): List<T> {
        @Suppress("UNCHECKED_CAST")
        return (tables[tableName] ?: emptyList<RemmiModel>())
            .filter { it.sourcePlugin == sourcePlugin && it.sourceItemId == sourceItemId } as List<T>
    }

    override suspend fun clearTable(tableName: String) {
        tables[tableName]?.clear()
    }
}
