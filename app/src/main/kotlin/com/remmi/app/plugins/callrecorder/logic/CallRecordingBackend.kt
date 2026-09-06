package com.remmi.app.plugins.callrecorder.logic

import com.remmi.app.plugins.callrecorder.models.CallDirection
import java.io.File

data class CallInfo(
    val phoneNumber: String?,
    val direction: CallDirection,
    val appPackage: String? = null // null for system calls, com.whatsapp for WhatsApp
)

sealed class RecordingResult {
    data class Success(val file: File, val durationMillis: Long) : RecordingResult()
    data class Failure(val message: String) : RecordingResult()
}

interface CallRecordingBackend {
    fun isSupported(): Boolean
    suspend fun startRecording(call: CallInfo, outputFile: File): Boolean
    suspend fun stopRecording(): RecordingResult
}
