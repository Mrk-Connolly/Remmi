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
) : DatabaseService, CommandListener {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    companion object {
        private const val TAG = "SupabaseService"
        /** Database Location */
        private const val SUPABASE_URL = "https://lmgexteedqzchmjdagxn.supabase.co"

        /** Database Public Key */
        private const val SUPABASE_ANON_KEY = "sb_publishable_NHFmOe4l9Yhz8nbfZay_pg_fi5j6boy"
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

    override suspend fun onCommand(command: RemmiCommand) {
        try {
            when (command) {
                is SaveDataCommand -> {
                    Log.i(TAG, "Global save requested by ${command.source}")
                }

                is UpsertDataCommand<*> -> {
                    Log.i(TAG, "Upserting item into ${command.tableName}")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as UpsertDataCommand<RemmiModel>
                    val jsonElement = json.encodeToJsonElement(typedCommand.serializer, typedCommand.item)
                    Log.d(TAG, "[onCommand:Upsert] - Payload: $jsonElement")
                    client.postgrest.from(typedCommand.tableName).upsert(jsonElement)
                    Log.i(TAG, "[onCommand:Upsert] - SUCCESS: ${typedCommand.tableName}")
                }

                is DeleteDataCommand -> {
                    Log.i(TAG, "Deleting item ${command.itemId} from ${command.tableName}")
                    delete(command.tableName, command.itemId)
                }

                is FetchDataByIdCommand<*> -> {
                    Log.i(TAG, "Fetching item ${command.itemId} from ${command.tableName}")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as FetchDataByIdCommand<RemmiModel>
                    val result = getById(typedCommand.tableName, typedCommand.itemId, typedCommand.serializer)
                    eventBus.publishEvent(
                        DataFetchedEvent(
                            items = listOfNotNull(result),
                            requestId = typedCommand.commandId,
                            correlationId = typedCommand.correlationId ?: typedCommand.commandId,
                            causationId = typedCommand.commandId
                        )
                    )
                }

                is FetchDataBySourceCommand<*> -> {
                    Log.i(TAG, "Fetching items for source ${command.sourcePlugin}/${command.sourceItemId} in ${command.tableName}")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as FetchDataBySourceCommand<RemmiModel>
                    val results = getBySource(typedCommand.tableName, typedCommand.sourcePlugin, typedCommand.sourceItemId, typedCommand.serializer)
                    eventBus.publishEvent(
                        DataFetchedEvent(
                            items = results,
                            requestId = typedCommand.commandId,
                            correlationId = typedCommand.correlationId ?: typedCommand.commandId,
                            causationId = typedCommand.commandId
                        )
                    )
                }

                is FetchAllDataCommand<*> -> {
                    Log.i(TAG, "Fetching all items from ${command.tableName}")
                    @Suppress("UNCHECKED_CAST")
                    val typedCommand = command as FetchAllDataCommand<RemmiModel>
                    val results = getAll(typedCommand.tableName, typedCommand.serializer)
                    eventBus.publishEvent(
                        DataFetchedEvent(
                            items = results,
                            requestId = typedCommand.commandId,
                            correlationId = typedCommand.correlationId ?: typedCommand.commandId,
                            causationId = typedCommand.commandId
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Critical failure handling command ${command::class.simpleName}: ${e.message}", e)
        }
    }
}
