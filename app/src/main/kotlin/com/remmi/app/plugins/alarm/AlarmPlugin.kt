package com.remmi.app.plugins.alarm

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.alarm.ui.screens.AlarmScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Alarm plugin.
 *
 * Integrates alarm scheduling and management into the Remmi platform.
 */
class AlarmPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system context */
    private lateinit var context: PluginContext

    /** Repository for persistent alarm data. */
    override val repository: AlarmRepository = AlarmRepository(SupabaseService)

    /** Action controller for alarm logic. */
    override val actions: AlarmActions = AlarmActions(repository)

    /** Dashboard widget for alarms. */
    override val widget: RemmiWidget = AlarmWidget(metadata, actions)

    /** UI screen for detailed alarm management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() {
            Log.d("Remmi", "[AlarmPlugin] - [Content] executed")
            AlarmScreen(actions)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Alarm Plugin
     * */
    init {
        Log.d("Remmi", "[AlarmPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[AlarmPlugin] - Initializing with shared context")
        this.context = context
        actions.eventBus = context.eventBus
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[AlarmPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Alarm Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                actions.sync()
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to sync alarms: ${e.message}")
            }
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[AlarmPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[AlarmPlugin] - [reformat] executed")
        repository.clearAll()
    }
}
