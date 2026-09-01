package com.remmi.app.plugins.maps

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.DataFetchedEvent
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.plugin.ui.RemmiScreen
import com.remmi.app.core.plugin.ui.RemmiWidget
import com.remmi.app.plugins.maps.models.SavedLocation
import com.remmi.app.plugins.maps.ui.screens.MapsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Maps plugin via EventBus.
 */
class MapsPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: MapsRepository = MapsRepository()
    private val _actions: MapsActions = MapsActions(_repository).apply {
        this.eventBus = this@MapsPlugin.eventBus
    }

    /** Repository for managing Saved Locations */
    override val repository: MapsRepository get() = _repository

    /** Action controller for maps logic. */
    override val actions: MapsActions get() = _actions

    /** Dashboard widget for maps. */
    override val widget: RemmiWidget by lazy { MapsWidgets(metadata) }

    /** UI screen for maps. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[MapsPlugin] - [Content] executed")
            MapsScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[MapsPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[MapsPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Maps plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[MapsPlugin] - Received command: ${command::class.simpleName}")
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is DataFetchedEvent<*> -> {
                if (event.items.isNotEmpty() && event.items[0] is SavedLocation) {
                    _repository.clear()
                    @Suppress("UNCHECKED_CAST")
                    (event.items as List<SavedLocation>).forEach { _repository.add(it) }
                    Log.d("Remmi", "[MapsPlugin] - Updated repository with ${event.items.size} locations")
                }
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[MapsPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Maps Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Maps Plugin Loaded")
    }

    /**                                   Refresh
     * Sync maps with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[MapsPlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync maps: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[MapsPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[MapsPlugin] - [reformat] executed")
        _repository.clear()
    }
}
