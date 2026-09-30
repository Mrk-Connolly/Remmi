package com.remmi.app.core.database

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class SupabaseService(
    private val eventBus: EventBus
) : DatabaseService {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    companion object {
        private const val TAG = "SupabaseService"
        /** Database Location */
        private val SUPABASE_URL = com.remmi.app.BuildConfig.SUPABASE_URL

        /** Database Public Key */
        private val SUPABASE_ANON_KEY = com.remmi.app.BuildConfig.SUPABASE_ANON_KEY
    }

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Insert
     * Insert a RemmiModel item into the specified Supabase table
     * */
    override suspend fun <T : RemmiModel> insert(tableName: String, item: T, serializer: KSerializer<T>) {
        try {
            val jsonElement = json.encodeToJsonElement(serializer, item)
            Log.d(TAG, "[insert] - Table: $tableName, Item: $jsonElement")
            client.postgrest.from(tableName).insert(jsonElement)
            Log.i(TAG, "[insert] - SUCCESS: $tableName")
        } catch (e: Exception) {
            Log.e(TAG, "[insert] - FAILURE: $tableName. Error: ${e.message}", e)
            throw e
        }
    }

    /**                                 Delete
     * Delete an item from the specified Supabase table by ID
     * */
    override suspend fun delete(tableName: String, id: String) {
        try {
            Log.d(TAG, "[delete] - Table: $tableName, ID: $id")
            client.postgrest.from(tableName).delete {
                filter {
                    eq("id", id)
                }
            }
            Log.i(TAG, "[delete] - SUCCESS: $tableName")
        } catch (e: Exception) {
            Log.e(TAG, "[delete] - FAILURE: $tableName. Error: ${e.message}", e)
            throw e
        }
    }

    /**                                 Update
     * Update a RemmiModel item in the specified Supabase table
     * */
    override suspend fun <T : RemmiModel> update(tableName: String, item: T, serializer: KSerializer<T>) {
        try {
            val jsonElement = json.encodeToJsonElement(serializer, item)
            Log.d(TAG, "[update] - Table: $tableName, Item: $jsonElement")
            client.postgrest.from(tableName).update(jsonElement) {
                filter {
                    eq("id", item.id)
                }
            }
            Log.i(TAG, "[update] - SUCCESS: $tableName")
        } catch (e: Exception) {
            Log.e(TAG, "[update] - FAILURE: $tableName. Error: ${e.message}", e)
            throw e
        }
    }

    /**                                 Get All
     * Retrieve all items from the specified Supabase table
     * */
    override suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T> {
        return try {
            Log.d(TAG, "[getAll] - Table: $tableName")
            val result = client.postgrest.from(tableName).select()
            Log.d(TAG, "[getAll] - SUCCESS: $tableName. Received: ${result.data}")
            json.decodeFromString(ListSerializer(serializer), result.data)
        } catch (e: Exception) {
            Log.e(TAG, "[getAll] - FAILURE: $tableName. Error: ${e.message}", e)
            emptyList()
        }
    }

    /**                                 Get By ID
     * Retrieve a specific item from the specified Supabase table by ID
     * */
    override suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T? {
        return try {
            Log.d(TAG, "[getById] - Table: $tableName, ID: $id")
            val result = client.postgrest.from(tableName).select {
                filter {
                    eq("id", id)
                }
            }
            val list = json.decodeFromString(ListSerializer(serializer), result.data)
            Log.d(TAG, "[getById] - SUCCESS: $tableName. Found: ${list.isNotEmpty()}")
            list.firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "[getById] - FAILURE: $tableName. Error: ${e.message}", e)
            null
        }
    }

    /**                                 Get By Source
     * Retrieve all items from the specified Supabase table linked to a specific source
     * */
    override suspend fun <T : RemmiModel> getBySource(
        tableName: String,
        sourcePlugin: String,
        sourceItemId: String,
        serializer: KSerializer<T>
    ): List<T> {
        return try {
            Log.d(TAG, "[getBySource] - Table: $tableName, Source: $sourcePlugin/$sourceItemId")
            val result = client.postgrest.from(tableName).select {
                filter {
                    eq("source_plugin", sourcePlugin)
                    eq("source_item_id", sourceItemId)
                }
            }
            Log.d(TAG, "[getBySource] - SUCCESS: $tableName. Received: ${result.data}")
            json.decodeFromString(ListSerializer(serializer), result.data)
        } catch (e: Exception) {
            Log.e(TAG, "[getBySource] - FAILURE: $tableName. Error: ${e.message}", e)
            emptyList()
        }
    }

    /**                                 Clear Table
     * Remove all entries from the specified Supabase table
     * */
    override suspend fun clearTable(tableName: String) {
        try {
            Log.d(TAG, "[clearTable] - Table: $tableName")
            client.postgrest.from(tableName).delete {
                filter {
                    neq("id", "")
                }
            }
            Log.i(TAG, "[clearTable] - SUCCESS: $tableName")
        } catch (e: Exception) {
            Log.e(TAG, "[clearTable] - FAILURE: $tableName. Error: ${e.message}", e)
        }
    }
}
