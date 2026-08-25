package com.remmi.app.plugins.contacts

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.controller.RemmiController
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.plugin.PluginMetadata
import com.remmi.app.core.plugin.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugin.widgets.RemmiWidget
import com.remmi.app.plugins.contacts.ui.screens.ContactScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Contacts plugin.
 */
class ContactPlugin(override val metadata: PluginMetadata) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private var _repository: ContactRepository? = null
    private var _actions: ContactActions? = null
    private var _authRepository: AuthRepository? = null

    /** Repository for managing Contacts data */
    override val repository: ContactRepository
        get() = _repository ?: throw IllegalStateException("ContactPlugin not initialized")

    /** Action controller for contact logic. */
    override val actions: ContactActions
        get() = _actions ?: throw IllegalStateException("ContactPlugin not initialized")

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
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[ContactPlugin] - Initializing with shared context")
        
        // Initialize Repository via ServiceManager
        val repo = ContactRepository(context.databaseManager.service, context.authRepository)
        _repository = repo
        _authRepository = context.authRepository
        
        // Initialize Actions
        _actions = ContactActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    /**                                   On Command
     * Handle commands specifically targeted at the Contact plugin.
     */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.d("Remmi", "[ContactPlugin] - Received command: ${command::class.simpleName}")
        // Future: Implement contact CRUD commands if needed
    }

    /**                                   On Event
     * Handle a system-wide or plugin-specific notification (Fact).
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        // Contacts might listen for other things in future
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[ContactPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Contacts Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync contacts: ${e.message}")
            }
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
        _repository?.clear()
    }
}
