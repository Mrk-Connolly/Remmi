package com.remmi.app.plugins.gift

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.contacts.ContactActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GiftPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private var _repository: GiftRepository? = null
    private var _actions: GiftActions? = null

    /** Repository for managing Gift ideas data */
    override val repository: GiftRepository
        get() = _repository ?: throw IllegalStateException("GiftPlugin not initialized")

    /** Action controller for gift logic. */
    override val actions: GiftActions
        get() = _actions ?: throw IllegalStateException("GiftPlugin not initialized")
    
    /** Access to Contact actions via PluginContext.serviceManager or automation flow */
    private val contactActions: ContactActions?
        get() = null // Refactored: Should not access other plugins directly


    /** Dashboard widget for gifts. */
    override val widget: RemmiWidget = object : RemmiWidget {
        override val metadata: PluginMetadata = this@GiftPlugin.metadata
        @Composable override fun Content() {
            Log.d("Remmi", "[GiftPlugin] - [Content] (widget) executed")
            // Placeholder for gift widget if needed later
        }
    }

    /** UI screen for gift management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content(controller: RemmiController) {
            Log.d("Remmi", "[GiftPlugin] - [Content] (screen) executed")
            // GiftListScreen needs contactActions. This should be handled by standardizing UI access.
            // For now, keeping placeholder to fix compilation.
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
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[GiftPlugin] - Initializing with shared context")
        
        // Initialize Repository via ServiceManager
        val repo = GiftRepository(context.databaseManager.service)
        _repository = repo
        
        // Initialize Actions
        _actions = GiftActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Gift plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[GiftPlugin] - Received command: ${command::class.simpleName}")
        // Future: Implement gift CRUD commands if needed
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        // Gift ideas might listen for birthday events etc.
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[GiftPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Gift Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync gifts: ${e.message}")
            }
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
        _repository?.clear()
    }
}
