package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.auth.AuthState
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
    val authRepository = AuthRepository()
    val serviceManager = ServiceManager(androidContext)
    val pluginManager = PluginManager()
    val automationEngine = AutomationEngine(eventBus, serviceManager)

    /** Shared Plugin Context */
    private val pluginContext = PluginContext(serviceManager, eventBus, authRepository)

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
        pluginManager.readPlugins(serviceManager.fileService)
        pluginManager.loadPlugins()

        // 3. Subscribe Command and Event Listeners
        eventBus.subscribeCommand(pluginManager)
        eventBus.subscribeCommand(serviceManager)
        eventBus.subscribeCommand(automationEngine)
        
        eventBus.subscribeEvent(pluginManager)

        // 4. Force Initialization (Bypass Login for Testing)
        Log.i("Remmi", "[RemmiController] - Bypassing authentication for testing. Initializing plugins.")
        initializePlugins()
    }

    /**                                 Initialize Plugins
     * Complete the startup sequence once a user is authenticated.
     */
    suspend fun initializePlugins() {
        Log.d("Remmi", "[RemmiController] - Initializing plugins for user")
        
        // 1. Initialize Plugins with Shared Context
        pluginManager.initializeAll(pluginContext)

        // 2. Load Plugin Data
        pluginManager.loadAll()

        // 3. Start Engines and Services
        serviceManager.start()
        automationEngine.start()

        // 4. Check and ensure Daily Briefing Schedule
        checkDailyBriefingSchedule()
    }

    /**                                 Sign Out
     * Terminate the session and clear all sensitive user data from memory.
     */
    suspend fun signOut() {
        Log.i("Remmi", "[RemmiController] - User signing out. Cleaning up environment.")
        
        // 1. Terminate Supabase session
        authRepository.signOut()

        // 2. Stop Automation and Services
        automationEngine.stop()
        serviceManager.stop()

        // 3. Clear plugin memory caches
        pluginManager.clearAllCaches()
        
        Log.i("Remmi", "[RemmiController] - Sign out complete")
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
        eventBus.unsubscribeEvent(pluginManager)
        eventBus.stop()
        
        serviceManager.stop()

        // 3. Unload Plugins
        pluginManager.stop()
    }
}
