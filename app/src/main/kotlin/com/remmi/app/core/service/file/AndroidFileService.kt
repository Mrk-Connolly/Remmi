package com.remmi.app.core.service.file

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * ANDROID FILE SERVICE
 *
 * Android-specific implementation of FileService.
 */
class AndroidFileService(private val context: Context) : FileService {


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[AndroidFileService] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    override fun readText(fileName: String, useAssets: Boolean): String {
        Log.d("Remmi", "[AndroidFileService] - Reading file: $fileName (assets: $useAssets)")
        return if (useAssets) {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } else {
            File(context.filesDir, fileName).readText()
        }
    }

    override fun writeText(fileName: String, content: String) {
        Log.d("Remmi", "[AndroidFileService] - Writing file: $fileName")
        File(context.filesDir, fileName).writeText(content)
    }

    override fun exists(fileName: String): Boolean {
        return File(context.filesDir, fileName).exists()
    }

    override fun saveImage(bytes: ByteArray, folder: String, fileName: String): String? {
        Log.d("Remmi", "[AndroidFileService] - Saving image to $folder/$fileName")
        return try {
            // Using getExternalFilesDir to avoid permission issues while staying in DCIM structure
            // folder usually starts with "DCIM/" or "dcim/"
            val baseDir = Environment.getExternalStorageDirectory()
            val targetDir = File(baseDir, folder)
            
            if (!targetDir.exists()) {
                val created = targetDir.mkdirs()
                Log.d("Remmi", "[AndroidFileService] - Directory created: $created at ${targetDir.absolutePath}")
            }

            val file = File(targetDir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidFileService] - Failed to save image", e)
            null
        }
    }
}
