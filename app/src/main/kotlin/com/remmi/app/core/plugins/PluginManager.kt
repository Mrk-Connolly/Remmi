package com.remmi.app.core.plugins

import android.content.Context
import android.util.Log
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
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

    val plugins = mutableMapOf<String, RemmiPlugin>()
    
    private val _pluginMetadata = MutableStateFlow<List<PluginMetadata>>(emptyList())
    val pluginMetadata = _pluginMetadata.asStateFlow()

    private var androidContext: Context? = null
    private val jsonConfig = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true 
    }



    // ----------------------------------------------------------------------------
    //                               CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                               READ PLUGINS
     *
     * recieves android contextex from host -> runtime to access plugin.json file and
     * reads all available plugins to be installed and saves their information*/
    fun readPlugins(context: Context) {
        Log.d("Remmi", "Accessing plugin information")
        this.androidContext = context

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
            fromAssets
        }

        try {
            val metadata = jsonConfig.decodeFromString<List<PluginMetadata>>(jsonString)
            _pluginMetadata.value = metadata
        } catch (e: Exception) {
            Log.e("Remmi", "Something went wrong while reading plugins: ${e.message}")
        }
    }

    fun updatePluginSettings(id: String, enabled: Boolean, showInNavigation: Boolean, showWidget: Boolean) {
        val currentList = _pluginMetadata.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index != -1) {
            currentList[index] = currentList[index].copy(
                enabled = enabled,
                showInNavigation = showInNavigation,
                showWidget = showWidget
            )
            _pluginMetadata.value = currentList
            savePlugins(currentList)
        }
    }

    fun updateAllPluginSettings(newList: List<PluginMetadata>) {
        _pluginMetadata.value = newList
        savePlugins(newList)
    }

    private fun savePlugins(metadata: List<PluginMetadata>) {
        androidContext?.let { context ->
            try {
                val jsonString = jsonConfig.encodeToString(metadata)
                File(context.filesDir, "plugins.json").writeText(jsonString)
            } catch (e: Exception) {
                Log.e("Remmi", "Failed to save plugin settings: ${e.message}")
            }
        }
    }



    /**                               READ PLUGINS
     *
     * recieves android contextex from host -> runtime to access plugin.json file and
     * load all available plugins to be installed*/

    fun loadPlugins(context: PluginContext) {
        Log.d("Remmi", "Loading plugins...")

        plugins.values.forEach { it.onUnload() }
        plugins.clear()
        context.widgetManager.clear()


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

                    if (metadata.showWidget) {
                        context.widgetManager.register(plugin)
                    }

                    Log.d("Remmi", "Loaded ${metadata.name}")
                }
            } catch (e: Exception) {
                println("Something went wrong loading plugin: ${e.message}, check plugin loader")
            }
        }
    }


    /**                                 CLOSE PLUGIN MANAGER
     *
     * Should run a small script to erase plugin memory data to avoid clogging up the phone, but
     * I don't know if kotlin does it automatically.
     *
     * Still calls unload function on each plugin
     * */
    fun close() {

        try {
            plugins.values.forEach { it.onUnload() }
            plugins.clear()

        }catch (e : Exception) {
            println("Something went wrong unloading plugin: ${e.message}, check plugin unloader")
        }
    }

}
