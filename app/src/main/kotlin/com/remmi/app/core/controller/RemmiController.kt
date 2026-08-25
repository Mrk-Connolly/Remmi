package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import com.remmi.app.core.Users.UserRepository
import com.remmi.app.core.auth.AuthRepository
import com.remmi.app.core.auth.AuthState
import com.remmi.app.core.auth.SupabaseAuthRepository
import com.remmi.app.core.automation.AutomationEngine
import com.remmi.app.core.automation.AutomationSettingsRepository
import com.remmi.app.core.events.EventBus
import com.remmi.app.core.plugin.PluginContext
import com.remmi.app.core.plugin.PluginManager
import com.remmi.app.core.service.database.DatabaseServiceManager
import com.remmi.app.core.service.file.FileServiceManager
import com.remmi.app.core.service.android.AndroidServiceManager
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
    val databaseManager = DatabaseServiceManager()
    val fileManager = FileServiceManager(androidContext)
    val androidManager = AndroidServiceManager(androidContext, eventBus)

    val pluginManager = PluginManager()
    val automationEngine = AutomationEngine(eventBus)

    /** Authentication provider (Supabase-backed). */
    val authRepository: AuthRepository = SupabaseAuthRepository()

    /** User profile repository. */
    val userRepository: UserRepository = UserRepository(databaseManager.service)

    /** Shared Plugin Context */
    private val pluginContext = PluginContext(databaseManager, fileManager, androidManager, eventBus, authRepository)

    /** UI State Tracking */
    val isEditorActive = mutableStateOf(false)
    val isMenuVisible = mutableStateOf(true)

    /** Whether plugins and services are ready for the current session */
    val isInitialized = mutableStateOf(false)

    /** Whether the auth-independent system boot has completed */
    private val isBooted = mutableStateOf(false)

    /** Guards against concurrent/duplicate startup sequences */
    private val isStarting = mutableStateOf(false)

    /** Scope for long-running runtime tasks (e.g., reacting to auth state) */
    private val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


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
     * Auth-independent system boot. Discovers and loads plugin classes and
     * starts the messaging bus, but does NOT initialize plugin data. Plugin
     * initialization (which loads user data from Supabase) must only happen
     * once a session is confirmed, via [initializePlugins].
     */
    suspend fun start() {
        bootSystem()
    }

    /**                                 Boot System
     * Auth-independent startup: event bus, plugin discovery and class loading.
     * Safe to call multiple times; runs once.
     */
    private suspend fun bootSystem() {
        if (isBooted.value) return
        Log.d("Remmi", "[RemmiController] - Booting system (auth-independent)")

        // 1. Start Messaging Bus
        eventBus.start()

        // 2. Discover Plugins using FileService
        pluginManager.readPlugins(fileManager.service)

        // 3. Subscribe Command and Event Listeners
        eventBus.subscribeCommand(pluginManager)
        eventBus.subscribeCommand(databaseManager)
        eventBus.subscribeCommand(androidManager)
        eventBus.subscribeCommand(automationEngine)
        eventBus.subscribeEvent(pluginManager)

        // 4. Load plugin classes (no user data yet)
        pluginManager.loadPlugins()

        isBooted.value = true
    }

    /**                                 Initialize Plugins
     * Auth-dependent startup: builds plugin repositories, loads the user's
     * data and starts engines. Must only run after a session is confirmed.
     * Sets [isInitialized] so the UI can leave the loading state.
     */
    suspend fun initializePlugins() {
        // Plugins depend on the system being booted first.
        bootSystem()
        if (isStarting.value || isInitialized.value) return
        isStarting.value = true
        try {
            Log.d("Remmi", "[RemmiController] - Initializing plugins (session confirmed)")

            // 1. Initialize Plugins with Shared Context
            pluginManager.initializeAll(pluginContext)

            // 2. Load Plugin Data
            pluginManager.loadAll()

            // 3. Start Engines and Services
            androidManager.start()
            automationEngine.start()

            // 4. Check and ensure Daily Briefing Schedule
            checkDailyBriefingSchedule()

            isInitialized.value = true
        } finally {
            isStarting.value = false
        }
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

        // 3. Clear plugin memory caches
        pluginManager.clearAllCaches()

        // 4. Mark system as uninitialized so the next sign-in re-initializes everything
        isInitialized.value = false

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
        eventBus.unsubscribeCommand(databaseManager)
        eventBus.unsubscribeCommand(androidManager)
        eventBus.unsubscribeCommand(automationEngine)
        eventBus.subscribeEvent(pluginManager)
        eventBus.stop()
        
        androidManager.stop()

        // 3. Unload Plugins
        pluginManager.stop()

        // 4. Cancel runtime tasks
        controllerScope.cancel()
    }
}
