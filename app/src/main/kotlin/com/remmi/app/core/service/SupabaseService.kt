package com.remmi.app.core.service

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import com.remmi.app.core.plugins.model.models.RemmiModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object SupabaseService : DatabaseService {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Database Location */
    private const val SUPABASE_URL = "https://lmgexteedqzchmjdagxn.supabase.co"

    /** Database Public Key */
    private const val SUPABASE_ANON_KEY = "sb_publishable_NHFmOe4l9Yhz8nbfZay_pg_fi5j6boy"

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
        Log.d("Remmi", "[SupabaseService] - [insert] executed")
        val jsonElement = json.encodeToJsonElement(serializer, item)
        client.postgrest.from(tableName).insert(jsonElement)
    }

    /**                                 Delete
     * Delete an item from the specified Supabase table by ID
     * */
    override suspend fun delete(tableName: String, id: String) {
        Log.d("Remmi", "[SupabaseService] - [delete] executed")
        client.postgrest.from(tableName).delete {
            filter {
                eq("id", id)
            }
        }
    }

    /**                                 Update
     * Update a RemmiModel item in the specified Supabase table
     * */
    override suspend fun <T : RemmiModel> update(tableName: String, item: T, serializer: KSerializer<T>) {
        Log.d("Remmi", "[SupabaseService] - [update] executed")
        val jsonElement = json.encodeToJsonElement(serializer, item)
        client.postgrest.from(tableName).update(jsonElement) {
            filter {
                eq("id", item.id)
            }
        }
    }

    /**                                 Get All
     * Retrieve all items from the specified Supabase table
     * */
    override suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T> {
        Log.d("Remmi", "[SupabaseService] - [getAll] executed")
        val result = client.postgrest.from(tableName).select()
        return json.decodeFromString(ListSerializer(serializer), result.data)
    }

    /**                                 Get By ID
     * Retrieve a specific item from the specified Supabase table by ID
     * */
    override suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T? {
        Log.d("Remmi", "[SupabaseService] - [getById] executed")
        val result = client.postgrest.from(tableName).select {
            filter {
                eq("id", id)
            }
        }
        val list = json.decodeFromString(ListSerializer(serializer), result.data)
        return list.firstOrNull()
    }

    /**                                 Clear Table
     * Remove all entries from the specified Supabase table
     * */
    override suspend fun clearTable(tableName: String) {
        Log.d("Remmi", "[SupabaseService] - [clearTable] executed")
        client.postgrest.from(tableName).delete {
            filter {
                neq("id", "")
            }
        }
    }
}
