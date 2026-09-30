package com.remmi.app.core.memory.providers

import android.util.Log
import com.remmi.app.core.android.files.FileService
import com.remmi.app.core.memory.MemoryProvider
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * LOCAL MEMORY PROVIDER
 *
 * Stores Remmi data as JSON files in the device's internal storage.
 * structure: remmi_data/<table>/<id>.json
 */
class LocalMemoryProvider(
    private val fileService: FileService
) : MemoryProvider {

    companion object {
        private const val TAG = "LocalMemoryProvider"
        private const val BASE_DIR = "remmi_data"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    private fun getTableDir(tableName: String) = "$BASE_DIR/$tableName"
    private fun getFilePath(tableName: String, id: String) = "${getTableDir(tableName)}/$id.json"

    override suspend fun <T : RemmiModel> upsert(tableName: String, item: T, serializer: KSerializer<T>) {
        try {
            val content = json.encodeToString(serializer, item)
            val path = getFilePath(tableName, item.id)
            Log.d(TAG, "[upsert] - Writing to $path")
            fileService.writeText(path, content)
        } catch (e: Exception) {
            Log.e(TAG, "[upsert] - Failed: ${e.message}")
        }
    }

    override suspend fun delete(tableName: String, id: String) {
        val path = getFilePath(tableName, id)
        Log.d(TAG, "[delete] - Deleting $path")
        fileService.delete(path)
    }

    override suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T> {
        val dir = getTableDir(tableName)
        val files = fileService.listFiles(dir)
        Log.d(TAG, "[getAll] - Found ${files.size} files in $dir")
        return files.mapNotNull { fileName ->
            try {
                val content = fileService.readText("$dir/$fileName")
                json.decodeFromString(serializer, content)
            } catch (e: Exception) {
                Log.e(TAG, "[getAll] - Failed to read $fileName: ${e.message}")
                null
            }
        }
    }

    override suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T? {
        val path = getFilePath(tableName, id)
        return if (fileService.exists(path)) {
            try {
                val content = fileService.readText(path)
                json.decodeFromString(serializer, content)
            } catch (e: Exception) {
                Log.e(TAG, "[getById] - Failed to read $path: ${e.message}")
                null
            }
        } else {
            null
        }
    }

    override suspend fun <T : RemmiModel> getBySource(
        tableName: String,
        sourcePlugin: String,
        sourceItemId: String,
        serializer: KSerializer<T>
    ): List<T> {
        return getAll(tableName, serializer).filter {
            it.sourcePlugin == sourcePlugin && it.sourceItemId == sourceItemId
        }
    }

    override suspend fun clearTable(tableName: String) {
        val dir = getTableDir(tableName)
        Log.d(TAG, "[clearTable] - Clearing $dir")
        fileService.delete(dir)
    }
}
