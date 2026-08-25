package com.remmi.app.core.plugin.repository

import android.util.Log
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.service.database.DatabaseService
import com.remmi.app.core.util.ErrorToaster
import kotlinx.serialization.KSerializer

abstract class CloudRepository<T : RemmiModel>(
    protected val databaseService: DatabaseService,
    protected val tableName: String,
    protected val serializer: KSerializer<T>,
    protected val authRepository: AuthRepository? = null
) : MemoryRepository<T>() {

    init {
        Log.d("Remmi", "[CloudRepository] - [constructor] executed")
    }

    /** Id of the currently authenticated user, or null when signed out. */
    protected suspend fun currentUserId(): String? = authRepository?.getCurrentUser()?.id

    suspend fun insert(item: T) {
        Log.d("Remmi", "[CloudRepository] - [insert] executed")
        if (item.userId == null) item.userId = currentUserId()
        try {
            databaseService.insert(tableName, item, serializer)
        } catch (e: Exception) {
            ErrorToaster.show("No se pudo guardar en '$tableName': ${e.message}")
            throw e
        }
        add(item)
    }

    suspend fun delete(id: String) {
        Log.d("Remmi", "[CloudRepository] - [delete] executed")
        try {
            databaseService.delete(tableName, id)
        } catch (e: Exception) {
            ErrorToaster.show("No se pudo borrar en '$tableName': ${e.message}")
            throw e
        }
        remove(id)
    }

    suspend fun updateCloud(item: T) {
        Log.d("Remmi", "[CloudRepository] - [updateCloud] executed")
        if (item.userId == null) item.userId = currentUserId()
        try {
            databaseService.update(tableName, item, serializer)
        } catch (e: Exception) {
            ErrorToaster.show("No se pudo actualizar en '$tableName': ${e.message}")
            throw e
        }
        update(item)
    }

    suspend fun refresh() {
        Log.d("Remmi", "[CloudRepository] - [refresh] executed")
        val uid = currentUserId()
        val items = if (uid != null) {
            databaseService.getByUser(tableName, uid, serializer)
        } else {
            databaseService.getAll(tableName, serializer)
        }
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
