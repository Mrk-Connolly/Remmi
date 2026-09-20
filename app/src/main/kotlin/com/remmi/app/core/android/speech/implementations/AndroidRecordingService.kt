package com.remmi.app.core.android.speech.implementations

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.remmi.app.core.android.speech.RecordingService
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.eventBus.events.RecordingStateChangedEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ANDROID RECORDING SERVICE
 *
 * MediaRecorder-based implementation of RecordingService.
 */
class AndroidRecordingService(
    private val context: Context,
    private val eventBus: EventBus
) : RecordingService {

    private var recorder: MediaRecorder? = null
    private var startTime: Long = 0
    private var pauseTime: Long = 0
    private var totalPausedDuration: Long = 0
    private var currentFile: File? = null
    private var currentTranscriptionId: String? = null

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is StartRecordingCommand -> {
                val file = File(context.filesDir, "recordings/${command.transcriptionId}.mp4")
                file.parentFile?.mkdirs()
                if (startRecording(file)) {
                    currentTranscriptionId = command.transcriptionId
                    eventBus.publishEvent(RecordingStateChangedEvent(command.transcriptionId, "RECORDING"))
                }
            }
            is PauseRecordingCommand -> {
                if (currentTranscriptionId == command.transcriptionId) {
                    pauseRecording()
                    eventBus.publishEvent(RecordingStateChangedEvent(command.transcriptionId, "PAUSED"))
                }
            }
            is ResumeRecordingCommand -> {
                if (currentTranscriptionId == command.transcriptionId) {
                    resumeRecording()
                    eventBus.publishEvent(RecordingStateChangedEvent(command.transcriptionId, "RECORDING"))
                }
            }
            is FinishRecordingCommand -> {
                if (currentTranscriptionId == command.transcriptionId) {
                    eventBus.publishEvent(RecordingStateChangedEvent(command.transcriptionId, "FINISHING"))
                    // Stop happens in SpeechService or a coordinator? 
                    // Let's handle it here for audio persistence.
                }
            }
        }
    }

    override suspend fun startRecording(outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("Remmi", "[AndroidRecordingService] - Starting recording to ${outputFile.absolutePath}")
            currentFile = outputFile
            
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            recorder = newRecorder
            startTime = System.currentTimeMillis()
            totalPausedDuration = 0
            true
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidRecordingService] - Failed to start recording: ${e.message}")
            false
        }
    }

    override suspend fun pauseRecording() = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.pause()
                pauseTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("Remmi", "[AndroidRecordingService] - Failed to pause recording: ${e.message}")
            }
        }
    }

    override suspend fun resumeRecording() = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                recorder?.resume()
                totalPausedDuration += (System.currentTimeMillis() - pauseTime)
            } catch (e: Exception) {
                Log.e("Remmi", "[AndroidRecordingService] - Failed to resume recording: ${e.message}")
            }
        }
    }

    override suspend fun stopRecording(): Long = withContext(Dispatchers.IO) {
        val duration = (System.currentTimeMillis() - startTime) - totalPausedDuration
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            duration
        } catch (e: Exception) {
            Log.e("Remmi", "[AndroidRecordingService] - Error stopping recorder: ${e.message}")
            recorder?.release()
            recorder = null
            0L
        }
    }
}
