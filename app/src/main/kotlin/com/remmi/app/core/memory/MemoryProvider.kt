package com.remmi.app.core.memory

import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.KSerializer

/**
 * MEMORY PROVIDER
 *
 * Common interface for all storage backends (Database, Local, Google Drive).
 * Provides low-level CRUD operations for structured Remmi data.
 */
interface MemoryProvider {

    /**
     * Insert or update a RemmiModel item.
     */
    suspend fun <T : RemmiModel> upsert(tableName: String, item: T, serializer: KSerializer<T>)

    /**
     * Delete an item by ID.
     */
    suspend fun delete(tableName: String, id: String)

    /**
     * Retrieve all items from a table.
     */
    suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T>

    /**
     * Retrieve a specific item by ID.
     */
    suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T?

    /**
     * Retrieve items linked to a specific source.
     */
    suspend fun <T : RemmiModel> getBySource(
        tableName: String,
        sourcePlugin: String,
        sourceItemId: String,
        serializer: KSerializer<T>
    ): List<T>

    /**
     * Clear all data in a table.
     */
    suspend fun clearTable(tableName: String)
}
