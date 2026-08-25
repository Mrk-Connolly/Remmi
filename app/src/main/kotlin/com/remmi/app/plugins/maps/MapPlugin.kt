package com.remmi.app.plugins.maps

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.PickLocationCommand
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.plugins.maps.repository.MapRepository
import com.remmi.app.plugins.maps.screens.MapScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapPlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val eventBus: EventBus
) : RemmiPlugin {

    private val _repository: MapRepository = MapRepository(databaseManager.service)
    private val _actions: MapActions = MapActions(_repository).apply {
        this.eventBus = this@MapPlugin.eventBus
    }

    override val repository: RemmiRepository<out RemmiModel> get() = _repository

    override val actions: MapActions get() = _actions

    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@MapPlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[MapPlugin] - Widget Content (Placeholder)")
        }
    }

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            MapScreen(actions, controller)
        }
    }

    override suspend fun initialize() {}

    override suspend fun onCommand(command: RemmiCommand) {}

    /**
     * Specialized handler that takes the controller to access UI state
     */
    fun handleCommandWithController(command: RemmiCommand, controller: RemmiController) {
        if (command is PickLocationCommand) {
            actions.handlePickLocation(command)
        }
    }

    override suspend fun onEvent(event: RemmiEvent) {}

    override fun onLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                refresh()
            } catch (e: Exception) {
                Log.e("Remmi", "[MapPlugin] - Initial sync failed: ${e.message}")
            }
        }
    }

    override suspend fun refresh() {
        actions.sync()
    }

    override fun onUnload() {}

    override suspend fun reformat() {
        _repository.clearAll()
    }
}
