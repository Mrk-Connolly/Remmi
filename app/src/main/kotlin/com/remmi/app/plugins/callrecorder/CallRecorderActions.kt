package com.remmi.app.plugins.callrecorder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.callrecorder.logic.AudioPlayerManager
import com.remmi.app.plugins.callrecorder.logic.CallRecordingManager
import com.remmi.app.plugins.callrecorder.models.CallRecording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class CallRecorderActions(
    private val repository: CallRecorderRepository,
    private val playerManager: AudioPlayerManager,
    private val recordingManagerProvider: () -> CallRecordingManager
) : RemmiAction {

    override var eventBus: EventBus? = null
    override val id: String = "call_recorder_actions"
    override val name: String = "Call Recorder Actions"

    private val _recordings = MutableStateFlow<List<CallRecording>>(emptyList())
    val recordings = _recordings.asStateFlow()
    
    private val _viewMode = MutableStateFlow(CallRecorderViewMode.CONTACTS)
    val viewMode = _viewMode.asStateFlow()
    
    val isPlaying = playerManager.isPlaying
    val currentPlayingPath = playerManager.currentPlayingPath
    
    val isRecordingActive get() = recordingManagerProvider().isRecordingActive
    val currentRecording get() = recordingManagerProvider().currentRecording

    fun updateRecordingsList() {
        _recordings.value = repository.getAll().sortedByDescending { it.created }
    }

    fun isNotificationEnabled(): Boolean {
        return CallRecorderContext.context?.let { repository.isNotificationEnabled(it) } ?: true
    }

    fun setNotificationEnabled(enabled: Boolean) {
        CallRecorderContext.context?.let { repository.setNotificationEnabled(it, enabled) }
    }

    fun setViewMode(mode: CallRecorderViewMode) {
        _viewMode.value = mode
    }

    fun startService(context: Context) {
        val intent = Intent(context, CallRecorderService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopService(context: Context) {
        val intent = Intent(context, CallRecorderService::class.java)
        context.stopService(intent)
    }

    suspend fun stopRecording(): CallRecording? {
        return recordingManagerProvider().stopRecording()
    }

    fun playRecording(recording: CallRecording) {
        playerManager.play(recording.filePath)
    }

    fun shareRecording(context: Context, recording: CallRecording) {
        val file = File(recording.filePath)
        if (!file.exists()) return

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/m4a"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Recording"))
    }

    fun saveRecording(recording: CallRecording) {
        repository.add(recording)
        CallRecorderContext.context?.let { repository.saveToPrefs(it) }
        updateRecordingsList()
    }

    fun deleteRecording(recording: CallRecording) {
        playerManager.stop()
        repository.remove(recording.id)
        val file = File(recording.filePath)
        if (file.exists()) {
            file.delete()
        }
        CallRecorderContext.context?.let { repository.saveToPrefs(it) }
        updateRecordingsList()
    }

    fun addToGroup(recording: CallRecording, groupName: String?) {
        val updated = recording.copy(
            group = groupName, 
            modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        )
        repository.update(updated)
        CallRecorderContext.context?.let { repository.saveToPrefs(it) }
        updateRecordingsList()
    }
}

enum class CallRecorderViewMode {
    CONTACTS, GROUPS
}
