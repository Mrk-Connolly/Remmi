package com.remmi.app.core.service.file

import android.content.Context
import android.util.Log

/**
 * FILE MANAGER
 *
 * Specialized manager for file operations and storage lifecycle.
 */
class FileManager(private val context: Context) {

    /** File Service implementation */
    val service: FileService = AndroidFileService(context)

    init {
        Log.d("Remmi", "[FileManager] - Constructor initialized")
    }
}
