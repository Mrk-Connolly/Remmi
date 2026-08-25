package com.remmi.app.plugins.maps

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.PickLocationCommand
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.plugins.maps.repository.MapRepository
import com.remmi.app.plugins.maps.ui.screens.MapScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {

    private var _repository: MapRepository? = null
    private var _actions: MapActions? = null
    private var controller: RemmiController? = null

    override val repository: RemmiRepository<out RemmiModel>
        get() = _repository ?: throw IllegalStateException("MapPlugin not initialized")

    override val actions: MapActions
        get() = _actions ?: throw IllegalStateException("MapPlugin not initialized")

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

    override suspend fun initialize(context: PluginContext) {
        val repo = MapRepository(context.databaseManager.service)
        _repository = repo
        _actions = MapActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is PickLocationCommand -> {
                // We need UIStateManager to trigger the overlay.
                // We can't easily get it here without passing it or getting it from a controller.
                // Assuming controller is available via initialization or some other means.
            }
        }
    }

    /**
     * Specialized handler that takes the controller to access UI state
     */
    fun handleCommandWithController(command: RemmiCommand, controller: RemmiController) {
        if (command is PickLocationCommand) {
            actions.handlePickLocation(command, controller.uiStateManager)
        }
    }

    override suspend fun onEvent(event: RemmiEvent) {
        // React to calendar or contact updates to refresh markers if needed
    }

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
        _repository?.clearAll()
    }
}
