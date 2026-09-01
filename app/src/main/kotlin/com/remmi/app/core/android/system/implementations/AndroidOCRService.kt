package com.remmi.app.core.android.system.implementations

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.remmi.app.core.android.system.OCRService
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.RequestOCRCommand
import com.remmi.app.core.eventBus.events.ReceiptTextRecognizedEvent
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * ANDROID OCR SERVICE
 *
 * Tesseract-based implementation of OCRService.
 */
class AndroidOCRService(
    private val context: Context,
    private val eventBus: EventBus
) : OCRService {

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is RequestOCRCommand -> {
                Log.i("Remmi", "[AndroidOCRService] - OCR requested for: ${command.imageUri}")
                CoroutineScope(Dispatchers.IO).launch {
                    val text = recognizeText(command.imageUri)
                    eventBus.publishEvent(
                        ReceiptTextRecognizedEvent(
                            text = text,
                            requestId = command.requestId,
                            causationId = command.commandId,
                            correlationId = command.correlationId
                        )
                    )
                }
            }
        }
    }

    private val tessDataPath = File(context.filesDir, "tessdata").absolutePath
    private val lang = "eng"

    init {
        prepareTessData()
    }

    private fun prepareTessData() {
        val dir = File(context.filesDir, "tessdata")
        if (!dir.exists()) dir.mkdirs()

        val trainedDataFile = File(dir, "$lang.traineddata")
        if (!trainedDataFile.exists()) {
            try {
                context.assets.open("tessdata/$lang.traineddata").use { input ->
                    FileOutputStream(trainedDataFile).use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("Remmi", "[AndroidOCRService] - Tessdata copied successfully")
            } catch (e: Exception) {
                Log.e("Remmi", "[AndroidOCRService] - Error copying tessdata: ${e.message}")
            }
        }
    }

    override suspend fun recognizeText(imageUri: String): String = withContext(Dispatchers.IO) {
        Log.d("Remmi", "[AndroidOCRService] - Recognizing text from $imageUri")
        val tess = TessBaseAPI()
        return@withContext try {
            if (!tess.init(context.filesDir.absolutePath, lang)) {
                Log.e("Remmi", "[AndroidOCRService] - Tesseract initialization failed")
                return@withContext ""
            }

            val inputStream = context.contentResolver.openInputStream(Uri.parse(imageUri))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            tess.setImage(bitmap)
            val result = tess.utF8Text
            tess.recycle()
            result ?: ""
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidOCRService] - OCR processing failed: ${e.message}")
            ""
        }
    }
}
