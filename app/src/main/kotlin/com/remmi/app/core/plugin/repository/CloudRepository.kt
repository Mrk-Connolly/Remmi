package com.remmi.app.core.plugin.repository

import android.util.Log
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.database.DatabaseService
import kotlinx.serialization.KSerializer

abstract class CloudRepository<T : RemmiModel>(
    val databaseService: DatabaseService,
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
        Log.d("Remmi", "[CloudRepository] - [refresh] executed for table $tableName")
        try {
            val items = databaseService.getAll(tableName, serializer)
            clear()
            items.forEach { add(it) }
        } catch (e: Exception) {
            Log.e("Remmi", "[CloudRepository] - Error refreshing table $tableName: ${e.message}")
            // Optional: we could rethrow or handle specific Supabase codes here
        }
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
