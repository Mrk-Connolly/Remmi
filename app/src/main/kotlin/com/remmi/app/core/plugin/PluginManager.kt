package com.remmi.app.core.plugin

import android.util.Log
import com.remmi.app.core.events.*
import com.remmi.app.core.events.commands.CommandListener
import com.remmi.app.core.events.commands.RemmiCommand
import com.remmi.app.core.events.commands.SyncPluginDataCommand
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
import com.remmi.app.plugins.weather.WeatherPlugin
import com.remmi.app.plugins.maps.MapPlugin
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PLUGIN MANAGER
 *
 * Manages plugin lifecycle and discovery.
 */
class PluginManager : CommandListener, EventListener {

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

    /** Registry of available plugin factory functions */
    private val pluginRegistry = mapOf<String, (PluginMetadata) -> RemmiPlugin>(
        "calendar" to { CalendarPlugin(it) },
        "tasks" to { TasksPlugin(it) },
        "alarm" to { AlarmPlugin(it) },
        "contacts" to { ContactPlugin(it) },
        "gift" to { GiftPlugin(it) },
        "recipe_book" to { RecipePlugin(it) },
        "ingredient_stock" to { IngredientPlugin(it) },
        "weather" to { WeatherPlugin(it) },
        "maps" to { MapPlugin(it) }
    )


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    init {
        Log.d("Remmi", "[PluginManager] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    fun start() {
        Log.d("Remmi", "[PluginManager] - Starting services")
    }

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

    override suspend fun onCommand(command: RemmiCommand) {
        when (command) {
            is SyncPluginDataCommand -> {
                Log.i("Remmi", "[PluginManager] - Syncing data for plugin: ${command.pluginId}")
                plugins[command.pluginId]?.onLoad()
            }
        }
    }

    override suspend fun onEvent(event: RemmiEvent) {
        // PluginManager handles generic plugin lifecycle events if any
    }

    /**                               READ PLUGINS
     * Discover plugins from the configuration file.
     * */
    fun readPlugins(fileService: FileService) {
        Log.d("Remmi", "[PluginManager] - [readPlugins] executed")
        val fileName = "plugins.json"
        
        val defaultJson = fileService.readText(fileName, useAssets = true)
        val defaultMetadata = try {
            jsonConfig.decodeFromString<List<PluginMetadata>>(defaultJson)
        } catch (e: Exception) {
            Log.e("Remmi", "[PluginManager] - Error parsing assets/plugins.json: ${e.message}")
            emptyList<PluginMetadata>()
        }

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

        val mergedMetadata = defaultMetadata.map { default ->
            userMetadata.find { it.id == default.id }?.let { user ->
                default.copy(
                    enabled = user.enabled,
                    showInNavigation = user.showInNavigation,
                    showWidget = user.showWidget
                )
            } ?: default
        }

        _pluginMetadata.value = mergedMetadata
        savePlugins(fileService, mergedMetadata)
    }

    fun updateAllPluginSettings(fileService: FileService, newList: List<PluginMetadata>) {
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
     * Instantiate discovered plugins using the registry.
     * */
    fun loadPlugins() {
        Log.d("Remmi", "[PluginManager] - Loading plugins")
        plugins.values.forEach { it.onUnload() }
        plugins.clear()

        _pluginMetadata.value.forEach { metadata ->
            val factory = pluginRegistry[metadata.id]
            if (factory != null) {
                try {
                    val plugin = factory(metadata)
                    plugins[metadata.id] = plugin
                    Log.d("Remmi", "[PluginManager] - Loaded ${metadata.name}")
                } catch (e: Exception) {
                    Log.e("Remmi", "[PluginManager] - Failed to load ${metadata.id}: ${e.message}")
                }
            }
        }
    }

    suspend fun refreshAllPlugins() {
        plugins.values.forEach { 
            try { it.refresh() } catch (e: Exception) {
                Log.e("Remmi", "[PluginManager] - Failed to refresh ${it.metadata.id}: ${e.message}")
            }
        }
    }

    fun subscribePlugins(eventBus: EventBus) {
        plugins.values.forEach {
            eventBus.subscribeCommand(it)
            eventBus.subscribeEvent(it)
        }
    }

    fun unsubscribePlugins(eventBus: EventBus) {
        plugins.values.forEach {
            eventBus.unsubscribeCommand(it)
            eventBus.unsubscribeEvent(it)
        }
    }
}
