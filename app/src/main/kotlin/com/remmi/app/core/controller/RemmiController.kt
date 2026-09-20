package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.engine.AutomationEngine
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.PluginManager
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.android.services.AndroidServiceManager
import kotlinx.coroutines.*

/**
 * REMMI CONTROLLER
 *
 * Central coordinator for system-level managers, plugin lifecycles, and messaging orchestration.
 * Manages the initialization, subscription, and teardown of core engines and services.
 */
class RemmiController(
    val androidContext: Context
) {

    // ----------------------------------------------------------------------------
    //                                  VARIABLES
    // ----------------------------------------------------------------------------

    /** Shared Communication Channel */
    val eventBus = EventBus()

    /** Component Container */
    val container = RemmiContainer()

    /** Core System Managers (Aliases for backward compatibility) */
    val databaseManager get() = container.get(DatabaseManager::class)
    val androidManager get() = container.get(AndroidServiceManager::class)
    val pluginManager get() = container.get(PluginManager::class)
    val automationEngine get() = container.get(AutomationEngine::class)

    private var isStarted = false

    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[RemmiController] - Constructor initialized")
        
        // Register components in dependency order
        container.register(DatabaseManager::class, DatabaseManager(eventBus))
        container.register(AndroidServiceManager::class, AndroidServiceManager(androidContext, eventBus))
        container.register(PluginManager::class, PluginManager(androidContext, eventBus))
        container.register(AutomationEngine::class, AutomationEngine(eventBus, container.get(AndroidServiceManager::class)))
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Orchestrate the startup sequence of all core systems in parallel where possible.
     */
    suspend fun start() = coroutineScope {
        if (isStarted) {
            Log.d("Remmi", "[RemmiController] - System already started, skipping")
            return@coroutineScope
        }
        Log.d("Remmi", "[RemmiController] - Starting system")
        isStarted = true

        // 1. Start Messaging Bus
        eventBus.start()

        // 2. Start Managers in parallel where possible
        // Database and Android managers can start in parallel
        val dbJob = launch { databaseManager.start() }
        val androidJob = launch { androidManager.start() }
        
        dbJob.join()
        androidJob.join()

        // 3. Discover Plugins using FileService
        pluginManager.readPlugins(androidManager.fileService)
        
        // 4. Load plugins (Parallel internally)
        pluginManager.loadPlugins()

        // 4.5 Initialize Appearance from settings
        initAppearance()

        // 5. Start Plugin Manager (Handles subscriptions and initial load)
        pluginManager.start()

        // 6. Start Engines
        automationEngine.start()
    }

    fun stop() {
        if (!isStarted) {
            Log.d("Remmi", "[RemmiController] - System not started, skipping")
            return
        }
        Log.d("Remmi", "[RemmiController] - Stopping system")
        isStarted = false

        // Stop all registered components in reverse order
        container.stopAll()
        
        // 3. Stop Core Services
        eventBus.stop()
    }

    private fun initAppearance() {
        val settings = androidManager.settingsService
        val themeStr = settings.getString("theme_pref", RemmiThemeMode.SYSTEM.name)
        GlobalUIState.themePreference = RemmiThemeMode.valueOf(themeStr ?: RemmiThemeMode.SYSTEM.name)
        val colorHex = settings.getString("primary_color_hex", "#7F3DFF")
        GlobalUIState.primaryColorHex = colorHex ?: "#7F3DFF"

    }
    }
