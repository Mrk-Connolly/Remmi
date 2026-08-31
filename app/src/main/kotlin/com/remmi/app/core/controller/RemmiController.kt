package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.engine.AutomationEngine
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.PluginManager
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.android.services.AndroidServiceManager

/**
 * REMMI CONTROLLER
 *
 * Central coordinator for system-level managers, plugin lifecycles, and messaging orchestration.
 * Manages the initialization, subscription, and teardown of core engines and services.
 */
class RemmiController(
    val androidContext: Context,
    databaseService: com.remmi.app.core.database.DatabaseService = com.remmi.app.core.database.SupabaseService
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared Communication Channel */
    val eventBus = EventBus()

    /** Core System Managers */
    val databaseManager = DatabaseManager(eventBus, databaseService)
    val androidManager = AndroidServiceManager(androidContext, eventBus)
    val pluginManager = PluginManager(databaseManager, androidManager, eventBus)
    val automationEngine = AutomationEngine(eventBus, androidManager)

    private var isStarted = false

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
        if (isStarted) {
            Log.d("Remmi", "[RemmiController] - System already started, skipping")
            return
        }
        Log.d("Remmi", "[RemmiController] - Starting system")
        isStarted = true

        // 1. Start Messaging Bus
        eventBus.start()

        // 2. Discover Plugins using FileService
        pluginManager.readPlugins(androidManager.fileService)
        
        // 3. Load plugins (MUST BE BEFORE SUBSCRIPTION)
        pluginManager.loadPlugins()

        // 3.5 Initialize Appearance from settings
        val settings = androidManager.settingsService
        val themeStr = settings.getString("theme_pref", RemmiThemeMode.SYSTEM.name)
        GlobalUIState.themePreference.value = RemmiThemeMode.valueOf(themeStr ?: RemmiThemeMode.SYSTEM.name)
        val colorHex = settings.getString("primary_color_hex", "#7F3DFF")
        GlobalUIState.primaryColorHex.value = colorHex ?: "#7F3DFF"

        // 4. Initialize Plugins with Context
        pluginManager.plugins.values.forEach { it.initialize() }

        // 5. Subscribe Command and Event Listeners
        subscribeAll()

        // 6. Start Engines
        automationEngine.start()
        
        // 7. Initial sync/load for plugins
        pluginManager.plugins.values.forEach { it.onLoad() }
    }

    /**                                 Stop
     * Orchestrate the teardown sequence of all core systems.
     */
    fun stop() {
        if (!isStarted) {
            Log.d("Remmi", "[RemmiController] - System not started, skipping")
            return
        }
        Log.d("Remmi", "[RemmiController] - Stopping system")
        isStarted = false

        // 1. Stop Engines
        automationEngine.stop()

        // 2. Unsubscribe all listeners
        unsubscribeAll()
        
        // 3. Stop Core Services
        eventBus.stop()
        androidManager.stop()

        // 4. Unload Plugins
        pluginManager.stop()
    }

    private fun subscribeAll() {
        // Register Managers
        eventBus.subscribeCommand(pluginManager)
        eventBus.subscribeCommand(databaseManager)
        eventBus.subscribeCommand(androidManager)
        eventBus.subscribeCommand(automationEngine)
        
        eventBus.subscribeEvent(pluginManager)
        eventBus.subscribeEvent(automationEngine)
        
        // Register Plugins directly
        pluginManager.subscribePlugins(eventBus)
    }

    private fun unsubscribeAll() {
        // Unregister Managers
        eventBus.unsubscribeCommand(pluginManager)
        eventBus.unsubscribeCommand(databaseManager)
        eventBus.unsubscribeCommand(androidManager)
        eventBus.unsubscribeCommand(automationEngine)
        
        eventBus.unsubscribeEvent(pluginManager)
        eventBus.unsubscribeEvent(automationEngine)
        
        // Unregister Plugins directly
        pluginManager.unsubscribePlugins(eventBus)
    }
}
