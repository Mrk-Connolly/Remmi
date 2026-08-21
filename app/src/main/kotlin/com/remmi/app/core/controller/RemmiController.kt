package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugins.PluginContext
import com.remmi.app.core.plugins.PluginManager
import com.remmi.app.core.database.DatabaseServiceManager
import com.remmi.app.core.file.FileServiceManager
import com.remmi.app.core.android.AndroidServiceManager
import com.remmi.app.core.android.implementations.AndroidAutomationScheduler
import kotlinx.coroutines.launch

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
    val databaseManager = DatabaseServiceManager()
    val fileManager = FileServiceManager(androidContext)
    val androidManager = AndroidServiceManager(androidContext)
    
    val pluginManager = PluginManager()
    val automationEngine = AutomationEngine(eventBus, androidManager)

    /** Shared Plugin Context */
    private val pluginContext = PluginContext(databaseManager, fileManager, androidManager, eventBus)

    /** UI State Tracking */
    val isEditorActive = mutableStateOf(false)
    val isMenuVisible = mutableStateOf(true)


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
        pluginManager.readPlugins(fileManager.service)

        // 3. Subscribe Command and Event Listeners
        eventBus.subscribeCommand(pluginManager)
        eventBus.subscribeCommand(databaseManager)
        eventBus.subscribeCommand(automationEngine)
        eventBus.subscribeEvent(pluginManager)

        // 4. Load plugins
        pluginManager.loadPlugins()

        initializePlugins()
    }

    /**                                 Initialize Plugins
     * Complete the startup sequence.
     */
    suspend fun initializePlugins() {
        Log.d("Remmi", "[RemmiController] - Initializing plugins")
        
        // 1. Initialize Plugins with Shared Context
        pluginManager.initializeAll(pluginContext)

        // 2. Load Plugin Data
        pluginManager.loadAll()

        // 3. Start Engines and Services
        androidManager.start()
        automationEngine.start()

        // 4. Check and ensure Daily Briefing Schedule
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
        eventBus.unsubscribeCommand(databaseManager)
        eventBus.unsubscribeCommand(automationEngine)
        eventBus.subscribeEvent(pluginManager)
        eventBus.stop()
        
        androidManager.stop()

        // 3. Unload Plugins
        pluginManager.stop()
    }
}
