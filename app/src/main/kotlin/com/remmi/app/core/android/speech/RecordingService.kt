package com.remmi.app.core.android.speech

import com.remmi.app.core.eventBus.commands.CommandListener
import java.io.File

/**
 * RECORDING SERVICE
 *
 * Interface for audio recording operations.
 */
interface RecordingService : CommandListener {
    suspend fun startRecording(outputFile: File): Boolean
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun stopRecording(): Long // Returns duration in millis
}
