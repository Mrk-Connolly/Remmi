package com.remmi.app.plugins.callrecorder

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.UpsertDataCommand
import com.remmi.app.core.eventBus.events.*
import com.remmi.app.core.plugin.BaseRemmiPlugin
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.callrecorder.logic.CallRecordingManager
import com.remmi.app.plugins.callrecorder.models.CallRecording
import com.remmi.app.plugins.callrecorder.models.RecordingStatus
import com.remmi.app.plugins.callrecorder.models.CallDirection
import com.remmi.app.plugins.callrecorder.ui.CallRecorderScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class CallRecorderPlugin(
    metadata: PluginMetadata,
    eventBus: EventBus,
    private val context: Context
) : BaseRemmiPlugin<CallRecording>(metadata, eventBus, CallRecording::class.java) {

    private val _repository = CallRecorderRepository()
    private val _playerManager = AudioPlayerManager()
    private val _actions = CallRecorderActions(_repository, _playerManager).apply {
        this.eventBus = this@CallRecorderPlugin.eventBus
    }

    private lateinit var _manager: CallRecordingManager

    companion object {
        private var instance: CallRecorderPlugin? = null

        fun getManager(): CallRecordingManager {
            return instance?._manager ?: throw IllegalStateException("CallRecorderPlugin not initialized")
        }

        fun getActions(): CallRecorderActions {
            return instance?.actions ?: throw IllegalStateException("CallRecorderPlugin not initialized")
        }
    }

    override val actions: CallRecorderActions get() = _actions
    override val repository: CallRecorderRepository get() = _repository

    override val widget: RemmiWidget = CallRecorderWidget(metadata, actions)

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content(controller: RemmiController) {
            CallRecorderScreen(actions)
        }
    }

    init {
        Log.d("Remmi", "[CallRecorderPlugin] - Initialized")
        instance = this
    }

    override suspend fun initialize() {
        super.initialize()
        _manager = CallRecordingManager(context)
    }

    override suspend fun onCommand(command: RemmiCommand) {
        super.onCommand(command)
    }

    override suspend fun onEvent(event: RemmiEvent) {
        super.onEvent(event)
        when (event) {
            is CallRecordingFinishedEvent -> {
                Log.d("Remmi", "[CallRecorderPlugin] - Received CallRecordingFinishedEvent")
                handleNewRecording(event)
            }
        }
    }

    private fun handleNewRecording(event: CallRecordingFinishedEvent) {
        val id = java.util.UUID.randomUUID().toString()
        val now = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        val recording = CallRecording(
            id = id,
            created = now,
            modified = now,
            filePath = event.filePath,
            durationMillis = event.durationMillis,
            phoneNumber = event.phoneNumber,
            direction = if (event.direction == "OUTGOING") CallDirection.OUTGOING else CallDirection.INCOMING,
            status = RecordingStatus.COMPLETED
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            _repository.add(recording)
            _actions.updateRecordingsList()
            
            eventBus.publishCommand(
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
