package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.service.ServiceManager
import com.remmi.app.core.service.android.implementations.AndroidAutomationScheduler

/**
 * REMMI CONTROLLER
 *
 * Central coordinator for system-level managers, plugin lifecycles, and messaging orchestration.
 * Manages the initialization, subscription, and teardown of core engines and services.
 */
class RemmiController(val androidContext: Context) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared Communication Channel */
    val eventBus = EventBus()

    /** Core System Managers */
    val serviceManager = ServiceManager(androidContext)
    val pluginManager = PluginManager()
    val automationEngine = AutomationEngine(eventBus, serviceManager)

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
     * Orchestrate the startup sequence of all core systems.
     */
    suspend fun start() {
        Log.d("Remmi", "[RemmiController] - Starting system")

        // 1. Start Messaging Bus
        eventBus.start()

        // 2. Discover Plugins using FileService
        pluginManager.readPlugins(serviceManager.fileService)
        pluginManager.loadPlugins()

        // 3. Subscribe Command Listeners
        eventBus.subscribeCommand(pluginManager)
        eventBus.subscribeCommand(serviceManager)
        eventBus.subscribeCommand(automationEngine)

        // 4. Initialize Plugins with Shared Context
        pluginManager.initializeAll(pluginContext)

        // 5. Load Plugin Data
        pluginManager.loadAll()

        // 6. Start Engines and Services
        serviceManager.start()
        automationEngine.start()

        // 7. Check and ensure Daily Briefing Schedule
        checkDailyBriefingSchedule()
    }

    private fun checkDailyBriefingSchedule() {
        val repository = AutomationSettingsRepository(androidContext)
        val settings = repository.getBriefingSettings()
        if (settings.enabled) {
            val scheduler = AndroidAutomationScheduler(androidContext)
            scheduler.scheduleDailyBriefing(settings)
        }
    }

    /**                                 Stop
     * Orchestrate the teardown sequence of all core systems.
     */
    fun stop() {
        Log.d("Remmi", "[RemmiController] - Stopping system")

        // 1. Stop Automation (Unsubscribe from facts)
        automationEngine.stop()

        // 2. Stop Core Services and Command Channel
        eventBus.unsubscribeCommand(pluginManager)
        eventBus.unsubscribeCommand(serviceManager)
        eventBus.unsubscribeCommand(automationEngine)
        eventBus.stop()
        
        serviceManager.stop()

        // 3. Unload Plugins
        pluginManager.stop()
    }
}
