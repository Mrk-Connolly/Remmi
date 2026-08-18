package com.remmi.app.core.plugins

import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.service.file.FileService
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * PLUGIN MANAGER
 *
 * Manages plugin lifecycle and routes system Commands to the appropriate plugin instance.
 */
class PluginManager : CommandListener {

    // ----------------------------------------------------------------------------
    //                                 VARIABLES
    // ----------------------------------------------------------------------------

    /** Map of active plugin instances indexed by their metadata ID */
    val plugins = mutableMapOf<String, RemmiPlugin>()

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
            
            // Future command routing can be added here
            else -> {
                Log.w("Remmi", "[PluginManager] - Unrecognized command: ${command::class.simpleName}")
            }
        }
    }

    /**                               READ PLUGINS
     * Discover plugins from the configuration file.
     * */
    fun readPlugins(fileService: FileService) {
        Log.d("Remmi", "[PluginManager] - [readPlugins] executed")

        val fileName = "plugins.json"
        val jsonString = if (fileService.exists(fileName)) {
            fileService.readText(fileName)
        } else {
            val fromAssets = fileService.readText(fileName, useAssets = true)
            fileService.writeText(fileName, fromAssets)
            fromAssets
        }

        try {
            val metadata = jsonConfig.decodeFromString<List<PluginMetadata>>(jsonString)
            _pluginMetadata.value = metadata
        } catch (e: Exception) {
            Log.e("Remmi", "[PluginManager] - Error reading plugins: ${e.message}")
        }
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
        plugins.values.forEach { it.initialize(context) }
    }

    /**                                 Load All
     * Data loading phase for all plugins.
     * */
    fun loadAll() {
        Log.d("Remmi", "[PluginManager] - Starting data load for all plugins")
        plugins.values.forEach { it.onLoad() }
    }
}
