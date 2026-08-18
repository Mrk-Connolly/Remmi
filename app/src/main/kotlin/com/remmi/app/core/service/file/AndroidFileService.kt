package com.remmi.app.core.service.file

import android.content.Context
import android.util.Log
import java.io.File

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
}
