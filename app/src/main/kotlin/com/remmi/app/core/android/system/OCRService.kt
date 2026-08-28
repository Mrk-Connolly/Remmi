package com.remmi.app.core.android.system

/**
 * OCR SERVICE
 *
 * Interface for standard OCR operations.
 */
interface OCRService {

    /**
     * Recognize text from a given image URI.
     */
    suspend fun recognizeText(imageUri: String): String
}
