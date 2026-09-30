package com.remmi.app.core.android.system.implementations

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.remmi.app.core.android.system.SystemActionService
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.OpenFileCommand
import java.io.File

/**
 * ANDROID SYSTEM ACTION SERVICE
 *
 * Intent-based implementation of SystemActionService.
 */
class AndroidSystemActionService(
    private val context: Context
) : SystemActionService {

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is OpenFileCommand -> openFile(command.filePath, command.mimeType)
        }
    }

    override fun openFile(filePath: String, mimeType: String) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e("Remmi", "[AndroidSystemActionService] - File does not exist: $filePath")
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Log.d("Remmi", "[AndroidSystemActionService] - Opened file: $filePath")
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidSystemActionService] - Failed to open file: ${e.message}")
        }
    }
}
