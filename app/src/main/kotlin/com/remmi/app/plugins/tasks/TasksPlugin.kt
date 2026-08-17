package com.remmi.app.plugins.tasks

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.service.SupabaseService
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.tasks.ui.screens.TasksScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Entry point for the Tasks plugin.
 */
class TasksPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared system context */
    private lateinit var context: PluginContext

    /** Repository for managing Tasks data */
    override val repository: TasksRepository = TasksRepository(SupabaseService)

    /** Action controller for tasks logic. */
    override val actions: TasksActions = TasksActions(repository)

    /** Dashboard widget for tasks. */
    override val widget: RemmiWidget = TasksWidget(metadata, actions)

    /** UI screen for task management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable override fun Content() {
            Log.d("Remmi", "[TasksPlugin] - [Content] executed")
            TasksScreen(actions)
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Tasks Plugin
     * */
    init {
        Log.d("Remmi", "[TasksPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[TasksPlugin] - Initializing with shared context")
        this.context = context
        actions.eventBus = context.eventBus
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[TasksPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Tasks Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[TasksPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[TasksPlugin] - [reformat] executed")
        repository.clearAll()
    }
}
