package com.remmi.app.core.controller

import android.content.Context
import android.util.Log
import com.remmi.app.core.automation.engine.AutomationEngine
import com.remmi.app.core.eventBus.EventBus
import com.remmi.app.core.plugin.PluginManager
import com.remmi.app.core.android.files.FileService
import com.remmi.app.core.android.files.AndroidFileService
import com.remmi.app.core.database.DatabaseManager
import com.remmi.app.core.database.DatabaseService
import com.remmi.app.core.android.services.AndroidServiceManager
import com.remmi.app.core.memory.MemoryService
import com.remmi.app.core.memory.MemoryProviderType
import com.remmi.app.core.memory.providers.DatabaseMemoryProvider
import com.remmi.app.core.memory.providers.LocalMemoryProvider
import com.remmi.app.core.memory.providers.GoogleDriveMemoryProvider
import com.remmi.app.core.memory.migration.MemoryMigrationService
import kotlinx.coroutines.*

/**
 * REMMI CONTROLLER
 *
 * Central coordinator for system-level managers, plugin lifecycles, and messaging orchestration.
 * Manages the initialization, subscription, and teardown of core engines and services.
 */
class RemmiController(
    val androidContext: Context,
    val injectedDatabaseService: DatabaseService? = null,
    val injectedFileService: FileService? = null,
    val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
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
    val memoryService get() = container.get(MemoryService::class)
    val memoryMigrationService get() = container.get(MemoryMigrationService::class)
    val androidManager get() = container.get(AndroidServiceManager::class)
    val pluginManager get() = container.get(PluginManager::class)
    val automationEngine get() = container.get(AutomationEngine::class)

    var isStarted = false
        private set

    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[RemmiController] - Constructor initialized")
        
        // Register components in dependency order
        container.register(DatabaseManager::class, DatabaseManager(eventBus, injectedDatabaseService))
        
        val androidServiceManager = AndroidServiceManager(androidContext, eventBus, injectedFileService ?: AndroidFileService(androidContext))
        container.register(AndroidServiceManager::class, androidServiceManager)

        // Memory Service Setup
        val databaseProvider = DatabaseMemoryProvider(container.get(DatabaseManager::class).service)
        val localProvider = LocalMemoryProvider(androidServiceManager.fileService)
        val googleDriveProvider = GoogleDriveMemoryProvider(androidContext, androidServiceManager.googleAuthService)
        
        val providers = mapOf(
            MemoryProviderType.DATABASE to databaseProvider,
            MemoryProviderType.LOCAL to localProvider,
            MemoryProviderType.GOOGLE_DRIVE to googleDriveProvider
        )
        container.register(MemoryService::class, MemoryService(eventBus, androidServiceManager.settingsService, providers))
        container.register(MemoryMigrationService::class, MemoryMigrationService(providers))

        container.register(PluginManager::class, PluginManager(androidContext, eventBus, defaultDispatcher))
        container.register(AutomationEngine::class, AutomationEngine(eventBus, container.get(AndroidServiceManager::class)))
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Orchestrate the startup sequence of core systems following AI Architecture Contract:
     * 1. Initialize critical path (EventBus, Database, Appearance) so Home renders immediately.
     * 2. Initialize secondary subsystems asynchronously with failure isolation.
     */
    suspend fun start() = coroutineScope {
        if (isStarted) {
            Log.d("Remmi", "[RemmiController] - System already started, skipping")
            return@coroutineScope
        }
        Log.d("Remmi", "[RemmiController] - Starting system")
        isStarted = true

        // 1. Critical Path: Start Messaging Bus
        runCatching { eventBus.start() }.onFailure { Log.e("Remmi", "[RemmiController] - EventBus start failed", it) }

        // 2. Critical Path: Start Database Manager
        runCatching { databaseManager.start() }.onFailure { Log.e("Remmi", "[RemmiController] - DatabaseManager start failed", it) }

        // 3. Critical Path: Initialize Appearance settings for Home UI
        runCatching { initAppearance() }.onFailure { Log.e("Remmi", "[RemmiController] - Appearance initialization failed", it) }

        // 4. Secondary Subsystems (Failure Isolated): Start Android Capabilities, Memory Service, Plugins, Automation Engine
        val androidJob = launch {
            runCatching { androidManager.start() }
                .onFailure { Log.e("Remmi", "[RemmiController] - AndroidServiceManager start failed", it) }
        }

        val memoryJob = launch {
            runCatching { memoryService.start() }
                .onFailure { Log.e("Remmi", "[RemmiController] - MemoryService start failed", it) }
        }

        androidJob.join()
        memoryJob.join()

        // 5. Plugins Discovery & Start (Failure Isolated)
        launch {
            runCatching {
                pluginManager.readPlugins(androidManager.fileService)
                pluginManager.loadPlugins()
                pluginManager.start()
            }.onFailure { Log.e("Remmi", "[RemmiController] - Plugin loading/starting failed", it) }
        }

        // 6. Automation Engine Start (Failure Isolated)
        launch {
            runCatching { automationEngine.start() }
                .onFailure { Log.e("Remmi", "[RemmiController] - AutomationEngine start failed", it) }
        }
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
