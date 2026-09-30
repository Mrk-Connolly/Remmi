package com.remmi.app.plugins.callrecorder.logic

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.remmi.app.plugins.callrecorder.models.CallRecording
import com.remmi.app.plugins.callrecorder.models.RecordingStatus
import com.remmi.app.plugins.callrecorder.models.CallDirection
import kotlinx.datetime.Instant
import java.io.File
import java.util.UUID

class CallRecordingManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var isRecording = false
    private var currentFile: File? = null
    private var startTime: Long = 0
    private var currentPhoneNumber: String? = null

    fun startRecording(phoneNumber: String?) {
        if (isRecording) return
        
        try {
            val recordingId = UUID.randomUUID().toString()
            val file = File(context.filesDir, "recordings/$recordingId.m4a")
            file.parentFile?.mkdirs()
            currentFile = file
            currentPhoneNumber = phoneNumber

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder?.apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                } catch (e: Exception) {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                }
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            startTime = System.currentTimeMillis()
            Log.i("Remmi", "[CallRecordingManager] - Recording started: $recordingId")
        } catch (e: Exception) {
            Log.e("Remmi", "[CallRecordingManager] - Failed to start recording: ${e.message}")
            isRecording = false
        }
    }

    fun stopRecording(): CallRecording? {
        if (!isRecording) return null
        
        try {
            recorder?.apply {
                stop()
                release()
            }
            val duration = System.currentTimeMillis() - startTime
            val file = currentFile
            
            if (file != null && file.exists()) {
                val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
                return CallRecording(
                    id = UUID.randomUUID().toString(),
                    created = now,
                    modified = now,
                    filePath = file.absolutePath,
                    durationMillis = duration,
                    phoneNumber = currentPhoneNumber,
                    direction = CallDirection.INCOMING, // Simplified for now
                    status = RecordingStatus.COMPLETED
                )
            }
        } catch (e: Exception) {
            Log.e("Remmi", "[CallRecordingManager] - Error stopping recorder: ${e.message}")
        } finally {
            recorder = null
            isRecording = false
            currentFile = null
        }
        return null
    }
}
