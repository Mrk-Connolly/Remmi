package com.remmi.app.core.file

import android.content.Context
import android.util.Log

/**
 * FILE SERVICE MANAGER
 *
 * Specialized manager for file operations and storage lifecycle.
 */
class FileServiceManager(private val context: Context) {

    /** File Service implementation */
    val service: FileService = AndroidFileService(context)

    init {
        Log.d("Remmi", "[FileServiceManager] - Constructor initialized")
    }
}
