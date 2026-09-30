package com.remmi.app.core.memory.providers

import com.remmi.app.core.database.DatabaseService
import com.remmi.app.core.memory.MemoryProvider
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.KSerializer

/**
 * DATABASE MEMORY PROVIDER
 *
 * Implementation of MemoryProvider that delegates to the existing DatabaseService.
 */
class DatabaseMemoryProvider(
    private val databaseService: DatabaseService
) : MemoryProvider {

    override suspend fun <T : RemmiModel> upsert(tableName: String, item: T, serializer: KSerializer<T>) {
        // DatabaseService currently has separate insert and update, 
        // but SupabaseService implementation uses upsert for UpsertDataCommand.
        // We'll use insert as a proxy for upsert if DatabaseService doesn't have a direct upsert,
        // or better, we can add upsert to DatabaseService if needed, 
        // but for now, we'll delegate to the specific methods.
        
        // Supabase implementation of DatabaseService uses upsert internally in onCommand.
        // However, the interface only has insert/update.
        // Let's check if we can just use update or insert.
        // Most providers should have a native "upsert".
        
        // For Supabase, we can use insert which is actually an upsert in its implementation sometimes,
        // but let's stick to what's in the interface.
        val existing = databaseService.getById(tableName, item.id, serializer)
        if (existing != null) {
            databaseService.update(tableName, item, serializer)
        } else {
            databaseService.insert(tableName, item, serializer)
        }
    }

    override suspend fun delete(tableName: String, id: String) {
        databaseService.delete(tableName, id)
    }

    override suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T> {
        return databaseService.getAll(tableName, serializer)
    }

    override suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T? {
        return databaseService.getById(tableName, id, serializer)
    }

    override suspend fun <T : RemmiModel> getBySource(
        tableName: String,
        sourcePlugin: String,
        sourceItemId: String,
        serializer: KSerializer<T>
    ): List<T> {
        return databaseService.getBySource(tableName, sourcePlugin, sourceItemId, serializer)
    }

    override suspend fun clearTable(tableName: String) {
        databaseService.clearTable(tableName)
    }
}
