package com.remmi.app.plugins.maps

import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.model.models.PluginAction
import com.remmi.app.core.plugin.model.models.RemmiModel
import com.remmi.app.core.plugin.repository.RemmiRepository
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.plugins.maps.ui.screens.MapsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Redone Maps Plugin from scratch.
 * Provides a simple map view using MapLibre.
 */
class MapsPlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val eventBus: EventBus
) : RemmiPlugin {

    private val _repository = MapsRepository(databaseManager.service)
    private val _actions = MapsActions(_repository).apply {
        this.eventBus = this@MapsPlugin.eventBus
    }
    private val _widget = MapsWidgets(metadata)

    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content(controller: RemmiController) {
            MapsScreen(actions = _actions, controller = controller)
        }
    }

    override val widget: RemmiWidget get() = _widget
    override val actions: MapsActions get() = _actions
    override val repository: RemmiRepository<out RemmiModel> get() = _repository
    override val exposedActions: List<PluginAction> get() = emptyList()

    override suspend fun initialize() {}
    override suspend fun onCommand(command: RemmiCommand) {}
    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is com.remmi.app.core.eventBus.events.CalendarEventDeletedEvent -> {
                val locations = _actions.getAllSavedLocations().filter { it.linkedCalendarEvent == event.itemId }
                locations.forEach { loc ->
                    _actions.deleteLocation(loc.id)
                }
            }
        }
    }
    override fun onLoad() {
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
    }
    override suspend fun refresh() {
        _actions.sync()
    }
    override fun onUnload() {}
    override suspend fun reformat() {
        _repository.clear()
    }
}
