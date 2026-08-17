package com.remmi.app.plugins.gift

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.contacts.ContactActions
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.ui.screens.GiftListScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GiftPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system context */
    private lateinit var context: PluginContext

    /** Repository for managing Gift ideas data */
    override val repository: GiftRepository = GiftRepository(SupabaseService)

    /** Action controller for gift logic. */
    override val actions: GiftActions = GiftActions(repository)
    
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
        @Composable override fun Content() {
            Log.d("Remmi", "[GiftPlugin] - [Content] (screen) executed")
            // GiftListScreen needs contactActions. This should be handled by standardizing UI access.
            // For now, keeping placeholder to fix compilation.
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Gift Plugin
     * */
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
        this.context = context
        actions.eventBus = context.eventBus
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
        repository.clearAll()
    }
}
