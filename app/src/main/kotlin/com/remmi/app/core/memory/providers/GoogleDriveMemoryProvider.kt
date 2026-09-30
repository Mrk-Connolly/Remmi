package com.remmi.app.core.memory.providers

import android.content.Context
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.remmi.app.core.android.google.GoogleAuthService
import com.remmi.app.core.memory.MemoryProvider
import com.remmi.app.core.plugin.model.models.RemmiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * GOOGLE DRIVE MEMORY PROVIDER
 *
 * Stores Remmi data in the user's Google Drive appDataFolder.
 */
class GoogleDriveMemoryProvider(
    private val context: Context,
    private val authService: GoogleAuthService
) : MemoryProvider {

    companion object {
        private const val TAG = "GoogleDriveMemoryProvider"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun getDriveService(): Drive? {
        val account = authService.getLastSignedInAccount() ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(context, Collections.singleton(DriveScopes.DRIVE_APPDATA))
        credential.selectedAccount = account.account
        
        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Remmi")
            .build()
    }

    override suspend fun <T : RemmiModel> upsert(tableName: String, item: T, serializer: KSerializer<T>) = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext
            val content = json.encodeToString(serializer, item)
            
            // Search for existing file
            val existingFile = findFile(service, tableName, item.id)
            
            val fileMetadata = File().apply {
                name = "${item.id}.json"
                description = "Remmi Data - $tableName"
                properties = mapOf("tableName" to tableName, "itemId" to item.id)
                if (existingFile == null) {
                    parents = listOf("appDataFolder")
                }
            }

            val mediaContent = com.google.api.client.http.ByteArrayContent.fromString("application/json", content)

            if (existingFile != null) {
                service.files().update(existingFile.id, fileMetadata, mediaContent).execute()
                Log.d(TAG, "[upsert] - Updated ${item.id} in $tableName")
            } else {
                service.files().create(fileMetadata, mediaContent).execute()
                Log.d(TAG, "[upsert] - Created ${item.id} in $tableName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[upsert] - Error: ${e.message}")
        }
    }

    override suspend fun delete(tableName: String, id: String) = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext
            val file = findFile(service, tableName, id)
            if (file != null) {
                service.files().delete(file.id).execute()
                Log.d(TAG, "[delete] - Deleted $id in $tableName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "[delete] - Error: ${e.message}")
        }
    }

    override suspend fun <T : RemmiModel> getAll(tableName: String, serializer: KSerializer<T>): List<T> = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext emptyList()
            val query = "name contains '.json' and 'appDataFolder' in parents"
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setQ(query)
                .setFields("files(id, name, properties)")
                .execute()

            return@withContext result.files.filter { it.properties?.get("tableName") == tableName }.mapNotNull { file ->
                try {
                    val outputStream = ByteArrayOutputStream()
                    service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
                    json.decodeFromString(serializer, outputStream.toString())
                } catch (e: Exception) {
                    Log.e(TAG, "[getAll] - Failed to read ${file.name}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[getAll] - Error: ${e.message}")
            emptyList()
        }
    }

    override suspend fun <T : RemmiModel> getById(tableName: String, id: String, serializer: KSerializer<T>): T? = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext null
            val file = findFile(service, tableName, id) ?: return@withContext null
            
            val outputStream = ByteArrayOutputStream()
            service.files().get(file.id).executeMediaAndDownloadTo(outputStream)
            return@withContext json.decodeFromString(serializer, outputStream.toString())
        } catch (e: Exception) {
            Log.e(TAG, "[getById] - Error: ${e.message}")
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

    override suspend fun clearTable(tableName: String) = withContext(Dispatchers.IO) {
        try {
            val service = getDriveService() ?: return@withContext
            val query = "properties has { key='tableName' and value='$tableName' } and 'appDataFolder' in parents"
            // Note: properties query might be tricky, let's use a simpler filtering if needed
            val result = service.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, properties)")
                .execute()
            
            result.files.filter { it.properties?.get("tableName") == tableName }.forEach { file ->
                service.files().delete(file.id).execute()
            }
            Log.d(TAG, "[clearTable] - Cleared $tableName")
        } catch (e: Exception) {
            Log.e(TAG, "[clearTable] - Error: ${e.message}")
        }
    }

    private fun findFile(service: Drive, tableName: String, itemId: String): File? {
        val query = "name = '$itemId.json' and 'appDataFolder' in parents"
        val result = service.files().list()
            .setSpaces("appDataFolder")
            .setQ(query)
            .setFields("files(id, properties)")
            .execute()
        
        return result.files.firstOrNull { it.properties?.get("tableName") == tableName }
    }
}
