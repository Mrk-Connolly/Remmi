package com.remmi.app.plugins.gift

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.events.ContactDeletedEvent
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.core.database.DatabaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GiftPlugin(
    override val metadata: PluginMetadata,
    private val databaseManager: DatabaseManager,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: GiftRepository = GiftRepository(databaseManager.service)
    private val _actions: GiftActions = GiftActions(_repository).apply {
        this.eventBus = this@GiftPlugin.eventBus
    }

    /** Repository for managing Gift ideas data */
    override val repository: GiftRepository get() = _repository

    /** Action controller for gift logic. */
    override val actions: GiftActions get() = _actions


    /** Dashboard widget for gifts. */
    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@GiftPlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[GiftPlugin] - [Content] (widget) executed")
        }
    }

    /** UI screen for gift management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[GiftPlugin] - [Content] (screen) executed")
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
            is ContactDeletedEvent -> {
                Log.i("Remmi", "[GiftPlugin] - Contact ${event.itemId} deleted. Cleaning up linked gift ideas...")
                CoroutineScope(Dispatchers.IO).launch {
                    val linkedGifts = repository.databaseService.getBySource("gift_ideas", "contacts", event.itemId, GiftIdea.serializer())
                    linkedGifts.forEach { gift ->
                        repository.delete(gift.id)
                        Log.d("Remmi", "[GiftPlugin] - Deleted linked gift: ${gift.id}")
                    }
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
