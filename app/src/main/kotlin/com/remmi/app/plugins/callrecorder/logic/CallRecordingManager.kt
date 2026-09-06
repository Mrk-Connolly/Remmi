package com.remmi.app.plugins.callrecorder.logic

import android.content.Context
import android.util.Log
import com.remmi.app.plugins.callrecorder.models.CallRecording
import com.remmi.app.plugins.callrecorder.models.RecordingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import java.io.File
import java.util.*

class CallRecordingManager(
    private val context: Context,
    private val backend: CallRecordingBackend
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _currentRecording = MutableStateFlow<CallRecording?>(null)
    val currentRecording = _currentRecording.asStateFlow()

    private val _isRecordingActive = MutableStateFlow(false)
    val isRecordingActive = _isRecordingActive.asStateFlow()

    suspend fun startRecording(call: CallInfo) {
        if (_isRecordingActive.value) return

        val recordingId = UUID.randomUUID().toString()
        val timestamp = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        val file = File(context.filesDir, "recordings/$recordingId.m4a").apply {
            parentFile?.mkdirs()
        }

        val recording = CallRecording(
            id = recordingId,
            created = timestamp,
            modified = timestamp,
            filePath = file.absolutePath,
            direction = call.direction,
            phoneNumber = call.phoneNumber,
            status = RecordingStatus.RECORDING,
            appPackage = call.appPackage
        )

        val success = backend.startRecording(call, file)
        if (success) {
            _currentRecording.value = recording
            _isRecordingActive.value = true
            Log.d("Remmi", "[CallRecordingManager] - Recording started: $recordingId")
        } else {
            Log.e("Remmi", "[CallRecordingManager] - Failed to start recording")
        }
    }

    suspend fun stopRecording(): CallRecording? {
        if (!_isRecordingActive.value) return null

        val result = backend.stopRecording()
        val current = _currentRecording.value ?: return null
        val now = Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())

        val finalRecording = when (result) {
            is RecordingResult.Success -> {
                current.copy(
                    modified = now,
                    durationMillis = result.durationMillis,
                    status = RecordingStatus.COMPLETED
                )
            }
            is RecordingResult.Failure -> {
                current.copy(
                    modified = now,
                    status = RecordingStatus.FAILED,
                    errorMessage = result.message
                )
            }
        }

        _currentRecording.value = null
        _isRecordingActive.value = false
        Log.d("Remmi", "[CallRecordingManager] - Recording stopped: ${finalRecording.id}")
        return finalRecording
    }
}
