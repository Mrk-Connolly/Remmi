package com.remmi.app.core.service.database

import com.remmi.app.core.plugins.model.models.RemmiModel
import kotlinx.serialization.KSerializer

interface DatabaseService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    suspend fun <T : RemmiModel> insert(tableName: String, item: T, serializer: KSerializer<T>)

    suspend fun delete(tableName: String, id: String)

    suspend fun <T : RemmiModel> update(tableName: String, item: T, serializer: KSerializer<T>)

    suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T>

    suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T?

    suspend fun clearTable(tableName: String)
}
