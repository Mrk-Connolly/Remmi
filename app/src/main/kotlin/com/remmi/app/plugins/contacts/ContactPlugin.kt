package com.remmi.app.plugins.contacts

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
import com.remmi.app.plugins.contacts.models.ContactItem
import com.remmi.app.plugins.contacts.ui.screens.ContactScreen
import com.remmi.app.plugins.contacts.ui.screens.ContactEditorMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Contacts plugin via EventBus.
 */
class ContactPlugin(
    override val metadata: PluginMetadata,
    private val eventBus: EventBus
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private val _repository: ContactRepository = ContactRepository()
    private val _actions: ContactActions = ContactActions(_repository).apply {
        this.eventBus = this@ContactPlugin.eventBus
    }

    /** Repository for managing Contacts data */
    override val repository: ContactRepository get() = _repository

    /** Action controller for contact logic. */
    override val actions: ContactActions get() = _actions

    /** Dashboard widget for contacts. */
    override val widget: RemmiWidget by lazy { ContactWidget(metadata, actions) }

    /** UI screen for contact management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[ContactPlugin] - [Content] executed")
            ContactScreen(actions, controller)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[ContactPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize() {
        Log.d("Remmi", "[ContactPlugin] - Initializing")
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Contact plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[ContactPlugin] - Received command: ${command::class.simpleName}")
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        when (event) {
            is DataFetchedEvent<*> -> {
                if (event.items.isNotEmpty() && event.items[0] is ContactItem) {
                    _repository.clear()
                    @Suppress("UNCHECKED_CAST")
                    (event.items as List<ContactItem>).forEach { _repository.add(it) }
                    Log.d("Remmi", "[ContactPlugin] - Updated repository with ${event.items.size} contacts")
                }
            }
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[ContactPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Contacts Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            refresh()
        }
        Log.d("Remmi", "Contacts Plugin Loaded")
    }

    /**                                   Refresh
     * Sync contacts with the database.
     */
    override suspend fun refresh() {
        Log.d("Remmi", "[ContactPlugin] - Refreshing data")
        try {
            actions.sync()
        } catch (e: Exception) {
            Log.e("Remmi", "Failed to sync contacts: ${e.message}")
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[ContactPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[ContactPlugin] - [reformat] executed")
        _repository.clear()
    }
}
