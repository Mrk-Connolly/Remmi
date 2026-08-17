package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.service.ServiceManager

/**
 * REMMI CONTROLLER
 *
 * Central coordinator for system-level managers and plugin lifecycles.
 * Handles the initialization and teardown of core engines and services.
 */
class RemmiController(val androidContext: Context) {


    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Core System Managers */
    val serviceManager = ServiceManager(androidContext)
    val eventBus = EventBus()
    val pluginManager = PluginManager()
    val automationEngine = AutomationEngine(pluginManager, eventBus)

    /** Shared Plugin Context */
    private val pluginContext = PluginContext(serviceManager, eventBus)


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[RemmiController] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Orchestrate the startup sequence of all core systems
     */
    suspend fun start() {
        Log.d("Remmi", "[RemmiController] - Starting system")

        // 1. Discover Plugins
        pluginManager.readPlugins(androidContext)
        pluginManager.loadPlugins()

        // 2. Initialize Plugins with Shared Context
        pluginManager.initializeAll(pluginContext)

        // 3. Load Plugin Data
        pluginManager.loadAll()

        // 4. Start Core Engines
        serviceManager.start()
        automationEngine.start()
        eventBus.start()
    }

    /**                                 Stop
     * Orchestrate the teardown sequence of all core systems
     */
    fun stop() {
        Log.d("Remmi", "[RemmiController] - Stopping system")

        // Stop Engines and Services
        automationEngine.stop()
        eventBus.stop()
        serviceManager.stop()

        // Unload Plugins
        pluginManager.stop()
    }
}
