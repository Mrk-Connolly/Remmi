package com.remmi.app.plugins.gift

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
import com.remmi.app.plugins.gift.models.GiftIdea
import com.remmi.app.plugins.gift.ui.screens.GiftListScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Gift plugin via EventBus.
 */
class GiftPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: GiftRepository = GiftRepository()
    private val _actions: GiftActions = GiftActions(_repository).apply {
        this.eventBus = this@GiftPlugin.eventBus
    }

    /** Repository for managing Gift data */
    override val repository: GiftRepository get() = _repository

    /** Action controller for gift logic. */
    override val actions: GiftActions get() = _actions

    /** Dashboard widget for gifts. */
    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@GiftPlugin.metadata
        @Composable override fun Content() {}
    }

    /** UI screen for gift management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[GiftPlugin] - [Content] executed")
            val contactActions = controller.pluginManager.plugins["contacts"]?.actions as? com.remmi.app.plugins.contacts.ContactActions
            if (contactActions != null) {
                GiftListScreen(actions, contactActions)
            }
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[GiftPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[GiftPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Gift plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[GiftPlugin] - Received command: ${command::class.simpleName}")
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is DataFetchedEvent<*> -> {
                if (event.items.isNotEmpty() && event.items[0] is GiftIdea) {
                    _repository.clear()
                    @Suppress("UNCHECKED_CAST")
                    (event.items as List<GiftIdea>).forEach { _repository.add(it) }
                    Log.d("Remmi", "[GiftPlugin] - Updated repository with ${event.items.size} gift ideas")
                }
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[GiftPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Gift Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Gift Plugin Loaded")
    }

    /**                                   Refresh
     * Sync gifts with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[GiftPlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync gifts: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[GiftPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[GiftPlugin] - [reformat] executed")
        _repository.clear()
    }
}
