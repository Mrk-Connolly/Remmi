package com.remmi.app.plugins.callrecorder

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.callrecorder.logic.AudioPlayerManager
import com.remmi.app.plugins.callrecorder.logic.CallRecordingManager
import com.remmi.app.plugins.callrecorder.logic.NativeAndroidRecorder
import com.remmi.app.plugins.callrecorder.ui.CallRecorderScreen

class CallRecorderPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {

    companion object {
        private var instance: CallRecorderPlugin? = null
        fun getActions() = instance!!.actions
        fun getManager() = instance!!.manager
    }

    private val _repository = CallRecorderRepository()
    private val _playerManager = AudioPlayerManager()
    private val _actions = CallRecorderActions(_repository, _playerManager) { manager }
    
    // We lazily initialize the manager because it needs a Context
    private val manager by lazy {
        val ctx = CallRecorderContext.context!!
        CallRecordingManager(ctx, NativeAndroidRecorder(ctx))
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
        instance = this
        Log.d("Remmi", "[CallRecorderPlugin] - Initialized")
    }

    override suspend fun initialize() {}

    override suspend fun onCommand(command: RemmiCommand) {}

    override suspend fun onEvent(event: RemmiEvent) {}

    override fun onLoad() {
        Log.d("Remmi", "[CallRecorderPlugin] - onLoad")
        CallRecorderContext.context?.let {
            repository.loadFromPrefs(it)
            actions.updateRecordingsList()
        }
    }

    override suspend fun refresh() {
        actions.updateRecordingsList()
    }

    override fun onUnload() {}

    override suspend fun reformat() {
        _repository.clear()
        CallRecorderContext.context?.let { _repository.saveToPrefs(it) }
    }
}
