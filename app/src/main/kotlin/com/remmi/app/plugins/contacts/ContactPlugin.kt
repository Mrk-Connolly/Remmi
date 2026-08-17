package com.remmi.app.plugins.contacts

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
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

    /** Shared system context */
    private lateinit var context: PluginContext

    /** Repository for managing Contacts data */
    override val repository: ContactRepository = ContactRepository(SupabaseService)

    /** Action controller for contact logic. */
    override val actions: ContactActions = ContactActions(repository)

    /** Dashboard widget for contacts. */
    override val widget: RemmiWidget = ContactWidget(metadata, actions)

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

    /**
     * Constructor for Contact Plugin
     * */
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
        this.context = context
        actions.eventBus = context.eventBus
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
        repository.clearAll()
    }
}
