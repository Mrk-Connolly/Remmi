package com.remmi.app.plugins.callrecorder.logic

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class NativeAndroidRecorder(private val context: Context) : CallRecordingBackend {

    private var recorder: MediaRecorder? = null
    private var startTime: Long = 0
    private var currentFile: File? = null

    override fun isSupported(): Boolean {
        // On modern Android, we can't be 100% sure until we try, 
        // but we can check if the microphone is available.
        return true 
    }

    override suspend fun startRecording(call: CallInfo, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("Remmi", "[NativeAndroidRecorder] - Starting recording to ${outputFile.absolutePath}")
            currentFile = outputFile
            
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            newRecorder.apply {
                // VOICE_COMMUNICATION is often the best bet for modern non-root recording
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
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
            true
        } catch (e: Exception) {
            Log.e("Remmi", "[NativeAndroidRecorder] - Failed to start recording: ${e.message}")
            recorder?.release()
            recorder = null
            false
        }
    }

    override suspend fun stopRecording(): RecordingResult = withContext(Dispatchers.IO) {
        val finalFile = currentFile
        val duration = System.currentTimeMillis() - startTime
        
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            currentFile = null
            
            if (finalFile != null && finalFile.exists() && finalFile.length() > 0) {
                RecordingResult.Success(finalFile, duration)
            } else {
                RecordingResult.Failure("Empty or missing recording file")
            }
        } catch (e: Exception) {
            Log.e("Remmi", "[NativeAndroidRecorder] - Error stopping recorder: ${e.message}")
            recorder?.release()
            recorder = null
            RecordingResult.Failure(e.message ?: "Unknown error stopping recorder")
        }
    }
}
