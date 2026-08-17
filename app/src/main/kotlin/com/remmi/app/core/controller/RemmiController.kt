package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.service.ServiceManager

/**
 * REMMI RUNTIME
 *
 * Its only use is to load and unload core information during open and close
 *
 */
class RemmiController(val androidContext: Context) {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Remmi Managers
     * Here lay the core managers from the remmi functions
     * Used to load, stop and more.
     *
     * Plugin Manager : Contains the functions to access plugin information
     * Service Manager : Contains the functions to access database and android core
     *
     * Automation Engine : Reads both manager and dynamically updates events
     * Event Bus : Communication bus of events from the managers to the automation engine
     *
     *      ------------------      -------------------     ---------------------
     *      - Plugin Manager -      - Service manager -     - Automation Engine -
     *      ------------------      -------------------     ---------------------
     *              |                        |                        |
     * Event Bus -----------------------------------------------------------
     *
     * */

    val automationEngine = AutomationEngine()
    val pluginManager = PluginManager()
    val serviceManager = ServiceManager(androidContext)
    val eventBus = EventBus()




    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------


    init {
        Log.d("Remmi", "[RemmiController] - Constructor initialized")
    }




    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**
     * START
     */
    fun start() {
        Log.d("Remmi", "[RemmiRuntime] - Starting")

        // Plugin Manager start
        pluginManager.readPlugins(androidContext)
        pluginManager.loadPlugins()

        serviceManager.start()
        automationEngine.start()
        eventBus.start()
    }

    /**
     * STOP
     */
    fun stop() {
        Log.d("Remmi", "[RemmiRuntime] - Stopping")
        pluginManager.stop()
        serviceManager.stop()
        automationEngine.stop()
        eventBus.stop()
    }
}
