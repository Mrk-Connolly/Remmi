package com.remmi.app.plugins.calendar

import android.util.Log
import androidx.compose.runtime.Composable
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginMetadata
import com.remmi.app.core.plugins.RemmiPlugin
import com.remmi.app.core.screens.RemmiScreen
import com.remmi.app.core.plugins.widgets.RemmiWidget
import com.remmi.app.plugins.calendar.ui.screens.CalendarScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The main entry point for the Calendar plugin.
 */
class CalendarPlugin(
    override val metadata: PluginMetadata
) : RemmiPlugin {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Internal storage for initialized components */
    private var _repository: CalendarRepository? = null
    private var _actions: CalendarActions? = null

    /** Repository for managing Calendar data */
    override val repository: CalendarRepository
        get() = _repository ?: throw IllegalStateException("CalendarPlugin not initialized")

    /** Action controller for calendar logic. */
    override val actions: CalendarActions
        get() = _actions ?: throw IllegalStateException("CalendarPlugin not initialized")

    /** Dashboard widget for calendar. */
    override val widget: RemmiWidget by lazy { CalendarWidget(metadata, actions) }

    /** UI screen for calendar management. */
    override val screen: RemmiScreen = object : RemmiScreen {
        @Composable
        override fun Content() {
            Log.d("Remmi", "[CalendarPlugin] - [Content] executed")
            // Screen dependencies should be handled via standard navigation.
        }
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[CalendarPlugin] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                   Initialize
     * Configure the plugin with the shared system context.
     */
    override suspend fun initialize(context: PluginContext) {
        Log.d("Remmi", "[CalendarPlugin] - Initializing with shared context")
        
        // Initialize Repository via ServiceManager
        val repo = CalendarRepository(context.serviceManager.databaseService)
        _repository = repo
        
        // Initialize Actions
        _actions = CalendarActions(repo).apply {
            this.eventBus = context.eventBus
        }
    }

    /**                                   On Load
     * Called when the plugin is loaded.
     */
    override fun onLoad() {
        Log.d("Remmi", "[CalendarPlugin] - [onLoad] executed")
        Log.d("Remmi", "Loading Calendar Plugin...")
        CoroutineScope(Dispatchers.IO).launch {
            actions.sync()
        }
        Log.d("Remmi", "Calendar Plugin Loaded")
    }

    /**                                   On Unload
     * Called when the plugin is unloaded.
     */
    override fun onUnload() {
        Log.d("Remmi", "[CalendarPlugin] - [onUnload] executed")
    }

    /**                                   Reformat
     * Reformat plugin database (clear all data).
     */
    override suspend fun reformat() {
        Log.d("Remmi", "[CalendarPlugin] - [reformat] executed")
        _repository?.clear()
    }
}
