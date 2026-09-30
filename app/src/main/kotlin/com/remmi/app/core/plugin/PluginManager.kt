package com.remmi.app.core.plugin

import android.content.Context
import android.util.Log
import com.remmi.app.core.controller.RemmiComponent
import com.remmi.app.core.eventBus.*
import com.remmi.app.core.eventBus.commands.CommandListener
import com.remmi.app.core.eventBus.commands.RemmiCommand
import com.remmi.app.core.eventBus.commands.SyncPluginDataCommand
import com.remmi.app.core.eventBus.events.EventListener
import com.remmi.app.core.eventBus.events.RemmiEvent
import com.remmi.app.core.android.files.FileService
import kotlinx.serialization.json.Json
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PLUGIN MANAGER
 *
 * Manages plugin lifecycle and discovery.
 */
class PluginManager(
    private val context: Context,
    private val eventBus: EventBus,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) : CommandListener, EventListener, RemmiComponent {

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

    override suspend fun start() {
        Log.d("Remmi", "[PluginManager] - Starting services")
        eventBus.subscribeCommand(this)
        eventBus.subscribeEvent(this)
        subscribePlugins(eventBus)
        
        // Trigger initial load for all active plugins
        plugins.values.forEach { it.onLoad() }
    }

    override fun stop() {
        Log.d("Remmi", "[PluginManager] - Stopping services")
        try {
            unsubscribePlugins(eventBus)
            eventBus.unsubscribeCommand(this)
            eventBus.unsubscribeEvent(this)
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
            val user = userMetadata.find { it.id == default.id }
            var finalMetadata = if (user != null) {
                default.copy(
                    enabled = user.enabled,
                    showInNavigation = user.showInNavigation,
                    showWidget = user.showWidget
                )
            } else {
                // If it's a new plugin from assets that the user doesn't have yet, enable it!
                default.copy(enabled = true)
            }

            // FORCE FIX: Ensure Call Recorder is enabled
            if (default.id == "call_recorder") {
                finalMetadata = finalMetadata.copy(enabled = true)
            }
            
            finalMetadata
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
     * Instantiate discovered plugins in parallel using the registry.
     * */
    suspend fun loadPlugins() = coroutineScope {
        Log.d("Remmi", "[PluginManager] - Loading plugins in parallel")
        plugins.values.forEach { it.onUnload() }
        plugins.clear()

        _pluginMetadata.value.forEach { metadata ->
            val factory = PluginRegistry.getFactory(metadata.id)
            if (factory != null) {
                launch(defaultDispatcher) {
                    try {
                        val plugin = factory(metadata, eventBus, context)
                        synchronized(plugins) {
                            plugins[metadata.id] = plugin
                        }
                        plugin.initialize()
                        Log.d("Remmi", "[PluginManager] - Loaded ${metadata.name}")
                    } catch (e: Exception) {
                        Log.e("Remmi", "[PluginManager] - Failed to load ${metadata.id}: ${e.message}")
                    }
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
