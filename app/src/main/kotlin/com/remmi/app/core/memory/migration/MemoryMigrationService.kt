package com.remmi.app.core.memory.migration

import android.util.Log
import com.remmi.app.core.memory.MemoryProvider
import com.remmi.app.core.memory.MemoryProviderType
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.KSerializer

/**
 * MEMORY MIGRATION SERVICE
 *
 * Handles data transfer between different storage providers.
 */
class MemoryMigrationService(
    private val providers: Map<MemoryProviderType, MemoryProvider>
) {
    companion object {
        private const val TAG = "MemoryMigrationService"
    }

    /**
     * Migrates data from one provider to another for the specified tables.
     */
    suspend fun migrate(
        from: MemoryProviderType,
        to: MemoryProviderType,
        tables: List<TableMigrationSpec<*>>
    ) {
        val source = providers[from] ?: throw IllegalArgumentException("Source provider $from not found")
        val destination = providers[to] ?: throw IllegalArgumentException("Destination provider $to not found")

        Log.i(TAG, "Starting migration from $from to $to")

        tables.forEach { spec ->
            try {
                Log.d(TAG, "Migrating table: ${spec.tableName}")
                migrateTable(source, destination, spec)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to migrate table ${spec.tableName}: ${e.message}")
            }
        }

        Log.i(TAG, "Migration completed")
    }

    private suspend fun <T : RemmiModel> migrateTable(
        source: MemoryProvider,
        destination: MemoryProvider,
        spec: TableMigrationSpec<T>
    ) {
        val items = source.getAll(spec.tableName, spec.serializer)
        Log.d(TAG, "Found ${items.size} items in ${spec.tableName}")
        items.forEach { item ->
            destination.upsert(spec.tableName, item, spec.serializer)
        }
    }
}

/**
 * Specification for a table to be migrated.
 */
data class TableMigrationSpec<T : RemmiModel>(
    val tableName: String,
    val serializer: KSerializer<T>
)
