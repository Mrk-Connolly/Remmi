package com.remmi.app.core.android.system

import com.remmi.app.core.eventBus.commands.CommandListener

/**
 * OCR SERVICE
 *
 * Interface for standard OCR operations.
 */
interface OCRService : CommandListener {

    /**
     * Recognize text from a given image URI.
     */
    suspend fun recognizeText(imageUri: String): String
}
