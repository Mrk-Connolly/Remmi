package com.remmi.app.core.plugins

import android.content.Context
import android.util.Log
import com.remmi.app.plugins.alarm.AlarmPlugin
import com.remmi.app.plugins.calendar.CalendarPlugin
import com.remmi.app.plugins.contacts.ContactPlugin
import com.remmi.app.plugins.gift.GiftPlugin
import com.remmi.app.plugins.tasks.TasksPlugin
import kotlinx.serialization.json.Json

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
    val pluginMetadata = mutableListOf<PluginMetadata>()



    // ----------------------------------------------------------------------------
    //                               CORE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                               READ PLUGINS
     *
     * recieves android contextex from host -> runtime to access plugin.json file and
     * reads all available plugins to be installed and saves their information*/
    fun readPlugins(androidContext : Context) {
        Log.d("Remmi", "Accessing plugin information")
        //william lo beso apasionadamenro y cillian lo resppndio con ferocidad

        val json = androidContext.assets
            .open("plugins.json")
            .bufferedReader()
            .use { it.readText() }

        pluginMetadata.clear()

        try {
            pluginMetadata.addAll(
                Json.decodeFromString<List<PluginMetadata>>(json)
            )
        } catch (e: Exception) {
            println("Something went wrong while reading file: ${e.message}, check assets/plugins.json file")
        }
    }



    /**                               READ PLUGINS
     *
     * recieves android contextex from host -> runtime to access plugin.json file and
     * load all available plugins to be installed*/

    fun loadPlugins(context: PluginContext) {
        Log.d("Remmi", "Loading plugins...")

        plugins.clear()


        pluginMetadata.forEach { metadata ->

            val plugin = when (metadata.id) {
                "calendar" -> CalendarPlugin(metadata, this)
                "tasks" -> TasksPlugin(metadata)
                "alarm" -> AlarmPlugin(metadata)
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
