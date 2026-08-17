package com.remmi.app.core.plugins

import android.content.Context
import android.util.Log
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class PluginManager {

    /**
     *                            PLUGIN MANAGER
     *
     * Loads, unload and manages all interaction from runtime with the plugins
     *
     * */


    // ----------------------------------------------------------------------------
    //                                 VARIABLES
    // ----------------------------------------------------------------------------

    /** List of loaded plugins */
    val plugins = mutableMapOf<String, RemmiPlugin>()

    /** Metadata for all discovered plugins */
    private val _pluginMetadata = MutableStateFlow<List<PluginMetadata>>(emptyList())
    val pluginMetadata = _pluginMetadata.asStateFlow()

    /** JSON configuration for plugin metadata */
    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true 
    }


    // ----------------------------------------------------------------------------
    //                                 CONSTRUCTOR
    // ----------------------------------------------------------------------------

    /**
     * Constructor for Plugin Manager
     * */
    init {
        Log.d("Remmi", "[Plugin Manager] - Constructor initialized")
    }


    // ----------------------------------------------------------------------------
    //                                CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Start
     * Start plugin manager services (if any)
     * */
    fun start() {
        Log.d("Remmi", "[PluginManager] - Starting services")
    }

    /**                                 Stop
     * Close plugin manager and unload all plugins
     * */
    fun stop() {
        Log.d("Remmi", "[PluginManager] - Stopping services")

        try {
            plugins.values.forEach { it.onUnload() }
            plugins.clear()

        }catch (e : Exception) {
            println("Something went wrong unloading plugin: ${e.message}, check plugin unloader")
        }
    }


    // ----------------------------------------------------------------------------
    //                                ACTION FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                               READ PLUGINS
     *
     * recieves android contextex from host -> runtime to access plugin.json file and
     * reads all available plugins to be installed and saves their information*/
    fun readPlugins(context: Context) {
        Log.d("Remmi", "[PluginManager] - [readPlugins] executed")

        val localFile = File(context.filesDir, "plugins.json")
        val jsonString = if (localFile.exists()) {
            localFile.readText()
        } else {
            val fromAssets = context.assets
                .open("plugins.json")
                .bufferedReader()
                .use { it.readText() }
            
            // Copy to local files for future writing
            localFile.writeText(fromAssets)

            // return fromAssets
            fromAssets
        }

        try {
            val metadata = jsonConfig.decodeFromString<List<PluginMetadata>>(jsonString)
            _pluginMetadata.value = metadata
        } catch (e: Exception) {
            Log.e("Remmi", "Something went wrong while reading plugins: ${e.message}")
        }
    }

    /**                               UPDATE SETTINGS
     * Update plugin settings and save to disk
     * */
    fun updateAllPluginSettings(context : Context, newList: List<PluginMetadata>) {
        Log.d("Remmi", "[PluginManager] - [updateAllPluginSettings] executed")
        _pluginMetadata.value = newList
        savePlugins(context, newList)
    }

    /**                               SAVE PLUGINS
     * Internal function to save plugin metadata to local storage
     * */
    private fun savePlugins(context: Context, metadata: List<PluginMetadata>) {
        Log.d("Remmi", "[PluginManager] - [savePlugins] executed")
        context.let { context ->
            try {
                val jsonString = jsonConfig.encodeToString(metadata)
                File(context.filesDir, "plugins.json").writeText(jsonString)
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to save plugin settings: ${e.message}")
            }
        }
    }

    /**                               LOAD PLUGINS
     *
     * recieves android contextex from host -> runtime to access plugin.json file and
     * load all available plugins to be installed*/
    fun loadPlugins() {
        Log.d("Remmi", "[PluginManager] - [loadPlugins] executed")

        // Remove any existing data
        plugins.values.forEach { it.onUnload() }
        plugins.clear()

        _pluginMetadata.value.forEach { metadata ->

            val plugin = when (metadata.id) {
                "calendar" -> CalendarPlugin(metadata, this)
                "tasks" -> TasksPlugin(metadata, this)
                "alarm" -> AlarmPlugin(metadata, this)
                "contacts" -> ContactPlugin(metadata)
                "gift" -> GiftPlugin(metadata, this)
                else -> null
            }

            try {
                plugin?.let {
                    plugins[metadata.id] = plugin
                    plugin.onLoad()
                    Log.d("Remmi", "Loaded ${metadata.name}")
                }
            } catch (e: Exception) {
                println("Something went wrong loading plugin: ${e.message}, check plugin loader")
            }
        }
    }

}
