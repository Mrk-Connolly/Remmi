package com.remmi.app.core.plugins

import android.content.Context
import android.util.Log
import com.remmi.app.core.widgets.WidgetManager
import com.remmi.app.plugins.calendar.CalendarPlugin
import kotlinx.serialization.json.Json

class PluginManager {

    val plugins = mutableMapOf<String, RemmiPlugin>()
    val pluginMetadata = mutableListOf<PluginMetadata>()

    fun readPlugins(androidContext : Context) {
        Log.d("Remmi", "Accessing plugin information")
        //william lo beso apasionadamenro y cillian lo resppndio con ferocidad

        val json = androidContext.assets
            .open("plugins.json")
            .bufferedReader()
            .use { it.readText() }

        pluginMetadata.clear()
        pluginMetadata.addAll(
            Json.decodeFromString<List<PluginMetadata>>(json)
        )
    }

    fun loadPlugins(context: PluginContext) {
        Log.d("Remmi", "Loading plugins...")

        plugins.clear()

        pluginMetadata.forEach { metadata ->

            val plugin = when (metadata.id) {
                "calendar" -> CalendarPlugin(metadata)
                else -> null
            }

            plugin?.let {
                plugins[metadata.id] = plugin

                plugin.onLoad()

                if (metadata.showWidget) {
                    context.widgetManager.register(plugin)
                }
                
                Log.d("Remmi", "Loaded ${metadata.name}")
            }
        }
    }

    fun close() {
        plugins.values.forEach { it.onUnload() }
        plugins.clear()
    }

}
