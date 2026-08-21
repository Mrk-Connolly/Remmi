package com.remmi.app.core.plugin

import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.events.commands.CommandListener
import com.remmi.app.core.events.commands.CreateAlarmCommand
import com.remmi.app.core.events.commands.CreateCalendarEventCommand
import com.remmi.app.core.events.commands.CreateTaskCommand
import com.remmi.app.core.events.commands.DeleteAlarmCommand
import com.remmi.app.core.events.commands.DeleteCalendarEventCommand
import com.remmi.app.core.events.commands.DeleteTaskCommand
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.commands.SyncPluginDataCommand
import com.remmi.app.core.events.commands.UpdateAlarmCommand
import com.remmi.app.core.events.commands.UpdateCalendarEventCommand
import com.remmi.app.core.events.commands.UpdateTaskCommand
import com.remmi.app.core.events.events.EventListener
import com.remmi.app.core.events.events.RemmiEvent
import com.remmi.app.core.service.file.FileService
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.ingredients.IngredientPlugin
import com.remmi.app.plugins.recipebook.RecipePlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PLUGIN MANAGER
 *
 * Manages plugin lifecycle and routes system Commands to the appropriate plugin instance.
 */
class PluginManager : CommandListener, EventListener {

    // ----------------------------------------------------------------------------
    //                                 VARIABLES
    // ----------------------------------------------------------------------------

    /** Map of active plugin instances indexed by their metadata ID */
    val plugins = mutableMapOf<String, RemmiPlugin>()

    private var eventBus: EventBus? = null

    /** Stream of plugin metadata for all discovered plugins */
    private val _pluginMetadata = MutableStateFlow<List<PluginMetadata>>(emptyList())
    val pluginMetadata = _pluginMetadata.asStateFlow()

    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true 
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[PluginManager] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Prepare plugin management services.
     * */
    fun start() {
        Log.d("Remmi", "[PluginManager] - Starting services")
    }

    /**                                 Stop
     * Unload all plugins and release resources.
     * */
    fun stop() {
        Log.d("Remmi", "[PluginManager] - Stopping services")
        try {
            plugins.values.forEach { it.onUnload() }
            plugins.clear()
        } catch (e: Exception) {
            Log.e("Remmi", "[PluginManager] - Failed to stop plugins: ${e.message}")
        }
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 On Command
     * Handle incoming Intents targeted at plugins.
     * PluginManager acts as the router to the correct plugin instance.
     * */
    override suspend fun onCommand(command: RemmiCommand) {
        Log.i("Remmi", "[PluginManager] - RECEIVED COMMAND: [${command::class.simpleName}] from [${command.source}]")
        
        when (command) {
            // Alarm Commands
            is CreateAlarmCommand, is UpdateAlarmCommand, is DeleteAlarmCommand -> {
                Log.i("Remmi", "[PluginManager] - Routing command to AlarmPlugin")
                plugins["alarm"]?.onCommand(command)
            }

            // Calendar Commands
            is CreateCalendarEventCommand, is UpdateCalendarEventCommand, is DeleteCalendarEventCommand -> {
                Log.i("Remmi", "[PluginManager] - Routing command to CalendarPlugin")
                plugins["calendar"]?.onCommand(command)
            }

            // Task Commands
            is CreateTaskCommand, is UpdateTaskCommand, is DeleteTaskCommand -> {
                Log.i("Remmi", "[PluginManager] - Routing command to TasksPlugin")
                plugins["tasks"]?.onCommand(command)
            }

            is SyncPluginDataCommand -> {
                Log.i("Remmi", "[PluginManager] - Syncing data for plugin: ${command.pluginId}")
                plugins[command.pluginId]?.onLoad()
            }
            
            // Future command routing can be added here
            else -> {
                Log.w("Remmi", "[PluginManager] - Unrecognized command: ${command::class.simpleName}")
            }
        }
    }

    /**                                 On Event
     * Broadcast incoming Facts to all active plugins.
     * */
    override suspend fun onEvent(event: RemmiEvent) {
        Log.i("Remmi", "[PluginManager] - RECEIVED EVENT: [${event::class.simpleName}] from [${event.source}]")
        plugins.values.forEach { it.onEvent(event) }
    }

    /**                               READ PLUGINS
     * Discover plugins from the configuration file.
     * Implements a merge strategy to ensure new plugins from assets are discovered
     * while preserving user settings for existing plugins.
     * */
    fun readPlugins(fileService: FileService) {
        Log.d("Remmi", "[PluginManager] - [readPlugins] executed")

        val fileName = "plugins.json"
        
        // 1. Read default plugin list from Assets
        val defaultJson = fileService.readText(fileName, useAssets = true)
        val defaultMetadata = try {
            jsonConfig.decodeFromString<List<PluginMetadata>>(defaultJson)
        } catch (e: Exception) {
            Log.e("Remmi", "[PluginManager] - Error parsing assets/plugins.json: ${e.message}")
            emptyList<PluginMetadata>()
        }

        // 2. Read existing user settings from Storage
        val userMetadata = if (fileService.exists(fileName)) {
            val userJson = fileService.readText(fileName)
            try {
                jsonConfig.decodeFromString<List<PluginMetadata>>(userJson)
            } catch (e: Exception) {
                Log.e("Remmi", "[PluginManager] - Error parsing user storage plugins.json: ${e.message}")
                emptyList<PluginMetadata>()
            }
        } else {
            emptyList()
        }

        // 3. Merge: Assets are the source of truth for available plugins,
        // user settings are the source of truth for enabled/visible states.
        val mergedMetadata = defaultMetadata.map { default ->
            userMetadata.find { it.id == default.id }?.let { user ->
                // Preserving settings while updating structural metadata from assets
                default.copy(
                    enabled = user.enabled,
                    showInNavigation = user.showInNavigation,
                    showWidget = user.showWidget
                )
            } ?: default // It's a new plugin!
        }

        // 4. Update memory and persist merged list
        _pluginMetadata.value = mergedMetadata
        savePlugins(fileService, mergedMetadata)
    }

    /**                               UPDATE SETTINGS
     * Persist updated plugin configuration.
     * */
    fun updateAllPluginSettings(fileService: FileService, newList: List<PluginMetadata>) {
        Log.d("Remmi", "[PluginManager] - Updating all plugin settings")
        _pluginMetadata.value = newList
        savePlugins(fileService, newList)
    }

    private fun savePlugins(fileService: FileService, metadata: List<PluginMetadata>) {
        try {
            val jsonString = jsonConfig.encodeToString(metadata)
            fileService.writeText("plugins.json", jsonString)
        } catch (e: Exception) {
            Log.e("Remmi", "[PluginManager] - Failed to save plugin settings: ${e.message}")
        }
    }

    /**                               LOAD PLUGINS
     * Instantiate discovered plugins.
     * */
    fun loadPlugins() {
        Log.d("Remmi", "[PluginManager] - Loading plugins")
        plugins.values.forEach { it.onUnload() }
        plugins.clear()

        _pluginMetadata.value.forEach { metadata ->
            val plugin = when (metadata.id) {
                "calendar" -> CalendarPlugin(metadata)
                "tasks" -> TasksPlugin(metadata)
                "alarm" -> AlarmPlugin(metadata)
                "contacts" -> ContactPlugin(metadata)
                "gift" -> GiftPlugin(metadata)
                "recipe_book" -> RecipePlugin(metadata)
                "ingredient_stock" -> IngredientPlugin(metadata)
                else -> null
            }

            try {
                plugin?.let {
                    plugins[metadata.id] = plugin
                    Log.d("Remmi", "[PluginManager] - Loaded ${metadata.name}")
                }
            } catch (e: Exception) {
                Log.e("Remmi", "[PluginManager] - Failed to load ${metadata.id}: ${e.message}")
            }
        }
    }

    /**                                 Initialize All
     * Dependency injection phase for all plugins.
     * */
    suspend fun initializeAll(context: PluginContext) {
        Log.d("Remmi", "[PluginManager] - Initializing all plugins")
        this.eventBus = context.eventBus
        plugins.values.forEach { it.initialize(context) }
    }

    /**                                 Load All
     * Data loading phase for all plugins.
     * */
    suspend fun loadAll() {
        Log.d("Remmi", "[PluginManager] - Requesting data load for all plugins via EventBus")
        plugins.keys.forEach { id ->
            eventBus?.publishCommand(SyncPluginDataCommand(pluginId = id))
        }
    }

    /**                                 Clear All Caches
     * Clear local memory caches for all active plugins.
     * Useful during sign-out to ensure data isolation.
     * */
    fun clearAllCaches() {
        Log.d("Remmi", "[PluginManager] - Clearing all plugin memory caches")
        plugins.values.forEach { it.repository.clear() }
    }
}
