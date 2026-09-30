package com.remmi.app.plugins.callrecorder

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.*
import com.remmi.app.core.plugin.actions.RemmiAction
import com.remmi.app.plugins.callrecorder.models.CallRecording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CallRecorderActions(
    private val repository: CallRecorderRepository,
    private val playerManager: AudioPlayerManager
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
    
    fun updateRecordingsList() {
        _recordings.value = repository.getAll().sortedByDescending { it.created }
    }

    fun setViewMode(mode: CallRecorderViewMode) {
        _viewMode.value = mode
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

    suspend fun deleteRecording(recording: CallRecording) {
        playerManager.stop()
        repository.remove(recording.id)
        
        eventBus?.publishCommand(
            DeleteDataCommand(
                tableName = "call_recordings",
                itemId = recording.id
            )
        )

        val file = File(recording.filePath)
        if (file.exists()) {
            file.delete()
        }
        updateRecordingsList()
    }

    suspend fun addToGroup(recording: CallRecording, groupName: String?) {
        val updated = recording.copy(
            group = groupName, 
            modified = kotlinx.datetime.Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis())
        )
        repository.update(updated)
        
        eventBus?.publishCommand(
            UpsertDataCommand(
                tableName = "call_recordings",
                item = updated,
                serializer = CallRecording.serializer()
            )
        )
        updateRecordingsList()
    }

    suspend fun sync() {
        eventBus?.publishCommand(
            FetchAllDataCommand(
                tableName = "call_recordings",
                serializer = CallRecording.serializer()
            )
        )
    }

    // Settings
    suspend fun setRecordingMode(mode: String) {
        eventBus?.publishCommand(SetCallRecordingModeCommand(mode))
    }

    fun isNotificationEnabled(): Boolean = true

    fun saveRecording(recording: CallRecording) {
        repository.add(recording)
        updateRecordingsList()
        
        eventBus?.let { bus ->
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                bus.publishCommand(
                    UpsertDataCommand(
                        tableName = "call_recordings",
                        item = recording,
                        serializer = CallRecording.serializer(),
                        source = "call_recorder"
                    )
                )
            }
        }
    }
}

enum class CallRecorderViewMode {
    CONTACTS, GROUPS
}
