package com.remmi.app.plugins.contacts

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugins.widgets.RemmiWidget
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
        @Composable override fun Content() {
            Log.d("Remmi", "[ContactPlugin] - [Content] executed")
            ContactScreen(actions)
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
        val repo = ContactRepository(context.serviceManager.databaseService)
        _repository = repo
        
        // Initialize Actions
        _actions = ContactActions(repo).apply {
            this.eventBus = context.eventBus
        }
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
